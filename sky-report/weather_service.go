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
	now := time.Now()
	isHistorical := t.Before(now.Add(-24 * time.Hour)) // More than 24 hours ago

	var url string
	if isHistorical {
		// Use archive API for historical data (more accurate than forecast)
		dateStr := t.Format("2006-01-02")
		url = fmt.Sprintf(
			"https://archive-api.open-meteo.com/v1/archive?latitude=%.6f&longitude=%.6f&start_date=%s&end_date=%s&hourly=temperature_2m,cloud_cover&timezone=auto",
			lat, lon, dateStr, dateStr,
		)
	} else {
		// Use forecast API for current and future data
		url = fmt.Sprintf(
			"https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&current=temperature_2m,relative_humidity_2m,cloud_cover,wind_speed_10m&hourly=temperature_2m,cloud_cover&timezone=auto",
			lat, lon,
		)
	}

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

	var percentage int

	// Determine if we should use current data or hourly forecast
	now := time.Now()
	useCurrent := data.Current.Time != "" && t.Sub(now).Abs() < 30*time.Minute

	if useCurrent {
		// Use current data only if requested time is within 30 minutes of now
		percentage = data.Current.CloudCover
	} else if len(data.Hourly.CloudCover) > 0 {
		// Use hourly data for specific time (works for both forecast and archive)
		percentage = ws.getClosestHourlyValue(data.Hourly.Time, data.Hourly.CloudCover, t)
	} else {
		return CloudInfo{
			Percentage: 50,
			Condition:  "unknown",
		}, fmt.Errorf("no cloud coverage data available")
	}

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

// getClosestHourlyValue finds the value for the hour closest to the target time
func (ws *WeatherService) getClosestHourlyValue(times []string, values []int, target time.Time) int {
	if len(times) == 0 || len(values) == 0 {
		return 50 // Default fallback
	}

	// Round target to nearest hour
	targetHour := target.Truncate(time.Hour)

	// Find the closest matching hour
	var closestIdx int
	minDiff := time.Duration(1<<63 - 1) // Max duration

	for i, timeStr := range times {
		// Parse the time string
		t, err := time.Parse(time.RFC3339, timeStr)
		if err != nil {
			// Try alternate format without timezone
			t, err = time.Parse("2006-01-02T15:04", timeStr)
			if err != nil {
				continue
			}
		}

		// Calculate time difference
		diff := t.Sub(targetHour).Abs()
		if diff < minDiff {
			minDiff = diff
			closestIdx = i
		}

		// If we found exact match or passed target, use this one
		if !t.Before(targetHour) && i > 0 {
			break
		}
	}

	if closestIdx < len(values) {
		return values[closestIdx]
	}

	// Fallback to last value
	return values[len(values)-1]
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

	var celsius float64

	// Determine if we should use current data or hourly forecast
	now := time.Now()
	useCurrent := data.Current.Time != "" && t.Sub(now).Abs() < 30*time.Minute

	if useCurrent {
		// Use current data only if requested time is within 30 minutes of now
		celsius = data.Current.Temperature2m
	} else if len(data.Hourly.Temperature2m) > 0 {
		// Use hourly data for specific time (works for both forecast and archive)
		celsius = ws.getClosestHourlyValueFloat(data.Hourly.Time, data.Hourly.Temperature2m, t)
	} else {
		return Temperature{
			Celsius:    15.0,
			Fahrenheit: 59.0,
			Condition:  "unknown",
		}, fmt.Errorf("no temperature data available")
	}

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

// getClosestHourlyValueFloat finds the float value for the hour closest to the target time
func (ws *WeatherService) getClosestHourlyValueFloat(times []string, values []float64, target time.Time) float64 {
	if len(times) == 0 || len(values) == 0 {
		return 15.0 // Default fallback
	}

	// Round target to nearest hour
	targetHour := target.Truncate(time.Hour)

	// Find the closest matching hour
	var closestIdx int
	minDiff := time.Duration(1<<63 - 1) // Max duration

	for i, timeStr := range times {
		// Parse the time string
		t, err := time.Parse(time.RFC3339, timeStr)
		if err != nil {
			// Try alternate format without timezone
			t, err = time.Parse("2006-01-02T15:04", timeStr)
			if err != nil {
				continue
			}
		}

		// Calculate time difference
		diff := t.Sub(targetHour).Abs()
		if diff < minDiff {
			minDiff = diff
			closestIdx = i
		}

		// If we found exact match or passed target, use this one
		if !t.Before(targetHour) && i > 0 {
			break
		}
	}

	if closestIdx < len(values) {
		return values[closestIdx]
	}

	// Fallback to last value
	return values[len(values)-1]
}
