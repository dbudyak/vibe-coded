package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

// OpenMeteoResponse represents the response from Open-Meteo API
type OpenMeteoResponse struct {
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
	Timezone  string  `json:"timezone"`
	Current   struct {
		Time          string  `json:"time"`
		Temperature2m float64 `json:"temperature_2m"`
		CloudCover    int     `json:"cloud_cover"`
		WindSpeed10m  float64 `json:"wind_speed_10m"`
		Humidity      int     `json:"relative_humidity_2m"`
	} `json:"current"`
	Hourly struct {
		Time          []string  `json:"time"`
		Temperature2m []float64 `json:"temperature_2m"`
		CloudCover    []int     `json:"cloud_cover"`
	} `json:"hourly"`
}

// WeatherService handles weather data retrieval
type WeatherService struct {
	client *http.Client
}

// NewWeatherService creates a new weather service
func NewWeatherService() *WeatherService {
	return &WeatherService{
		client: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

// GetWeatherData fetches weather data from Open-Meteo API
func (ws *WeatherService) GetWeatherData(lat, lon float64, t time.Time) (*OpenMeteoResponse, error) {
	// Open-Meteo API endpoint (free, no API key required)
	url := fmt.Sprintf(
		"https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&current=temperature_2m,relative_humidity_2m,cloud_cover,wind_speed_10m&hourly=temperature_2m,cloud_cover&timezone=auto",
		lat, lon,
	)

	resp, err := ws.client.Get(url)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch weather data: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("weather API returned status %d", resp.StatusCode)
	}

	var weatherResp OpenMeteoResponse
	if err := json.NewDecoder(resp.Body).Decode(&weatherResp); err != nil {
		return nil, fmt.Errorf("failed to decode weather response: %w", err)
	}

	return &weatherResp, nil
}

// GetCloudCoverage retrieves cloud coverage information
func (ws *WeatherService) GetCloudCoverage(lat, lon float64, t time.Time) (CloudInfo, error) {
	data, err := ws.GetWeatherData(lat, lon, t)
	if err != nil {
		// Fallback to default values on error
		return CloudInfo{
			Percentage: 50,
			Condition:  "unknown",
		}, err
	}

	percentage := data.Current.CloudCover

	var condition string
	switch {
	case percentage <= 10:
		condition = "clear"
	case percentage <= 40:
		condition = "partly_cloudy"
	case percentage <= 75:
		condition = "cloudy"
	default:
		condition = "overcast"
	}

	return CloudInfo{
		Percentage: percentage,
		Condition:  condition,
	}, nil
}

// GetTemperature retrieves temperature information
func (ws *WeatherService) GetTemperature(lat, lon float64, t time.Time) (Temperature, error) {
	data, err := ws.GetWeatherData(lat, lon, t)
	if err != nil {
		// Fallback to default values on error
		return Temperature{
			Celsius:    15.0,
			Fahrenheit: 59.0,
			Condition:  "unknown",
		}, err
	}

	celsius := data.Current.Temperature2m
	fahrenheit := celsius*9/5 + 32

	var condition string
	switch {
	case celsius < -10:
		condition = "very_cold"
	case celsius < 5:
		condition = "cold"
	case celsius < 20:
		condition = "comfortable"
	case celsius < 30:
		condition = "warm"
	default:
		condition = "hot"
	}

	return Temperature{
		Celsius:    celsius,
		Fahrenheit: fahrenheit,
		Condition:  condition,
	}, nil
}
