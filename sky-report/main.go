package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"strconv"
	"time"
)

type SkyConditionRequest struct {
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
	Time      string  `json:"time"` // ISO 8601 format
}

type SkyConditionResponse struct {
	Location        Location         `json:"location"`
	ObservationTime string          `json:"observation_time"`
	CloudCoverage   CloudInfo       `json:"cloud_coverage"`
	LightPollution  LightPollution  `json:"light_pollution"`
	Temperature     Temperature     `json:"temperature"`
	Recommendation  string          `json:"recommendation"`
	DataSource      DataSourceInfo  `json:"data_source"`
}

type DataSourceInfo struct {
	Weather        string `json:"weather"`
	LightPollution string `json:"light_pollution"`
	Disclaimer     string `json:"disclaimer"`
}

type Location struct {
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
}

type CloudInfo struct {
	Percentage int    `json:"percentage"` // 0-100
	Condition  string `json:"condition"`  // clear, partly_cloudy, cloudy, overcast
}

type LightPollution struct {
	BortleScale int     `json:"bortle_scale"` // 1-9, 1 is best for astrophotography
	Level       string  `json:"level"`        // excellent, good, moderate, poor, very_poor
	SQM         float64 `json:"sqm"`          // Sky Quality Meter value (mag/arcsec²)
}

type Temperature struct {
	Celsius    float64 `json:"celsius"`
	Fahrenheit float64 `json:"fahrenheit"`
	Condition  string  `json:"condition"` // comfortable, cold, very_cold, warm
}

var (
	weatherService        *WeatherService
	lightPollutionService *LightPollutionService
)

func main() {
	// Initialize services
	weatherService = NewWeatherService()
	lightPollutionService = NewLightPollutionService()

	http.HandleFunc("/health", healthHandler)
	http.HandleFunc("/api/v1/sky-condition", skyConditionHandler)

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	log.Printf("Sky Report Service starting on port %s", port)
	if err := http.ListenAndServe(":"+port, nil); err != nil {
		log.Fatal(err)
	}
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{
		"status": "healthy",
		"service": "sky-report",
	})
}

func skyConditionHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")

	if r.Method != http.MethodGet && r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var lat, lon float64
	var timeStr string
	var err error

	if r.Method == http.MethodGet {
		// Parse query parameters
		latStr := r.URL.Query().Get("lat")
		lonStr := r.URL.Query().Get("lon")
		timeStr = r.URL.Query().Get("time")

		if latStr == "" || lonStr == "" {
			http.Error(w, "Missing required parameters: lat and lon", http.StatusBadRequest)
			return
		}

		lat, err = strconv.ParseFloat(latStr, 64)
		if err != nil {
			http.Error(w, "Invalid latitude", http.StatusBadRequest)
			return
		}

		lon, err = strconv.ParseFloat(lonStr, 64)
		if err != nil {
			http.Error(w, "Invalid longitude", http.StatusBadRequest)
			return
		}
	} else {
		// Parse JSON body
		var req SkyConditionRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "Invalid request body", http.StatusBadRequest)
			return
		}
		lat = req.Latitude
		lon = req.Longitude
		timeStr = req.Time
	}

	// Validate coordinates
	if lat < -90 || lat > 90 {
		http.Error(w, "Latitude must be between -90 and 90", http.StatusBadRequest)
		return
	}
	if lon < -180 || lon > 180 {
		http.Error(w, "Longitude must be between -180 and 180", http.StatusBadRequest)
		return
	}

	// Parse time or use current time
	var observationTime time.Time
	if timeStr == "" {
		observationTime = time.Now()
	} else {
		observationTime, err = time.Parse(time.RFC3339, timeStr)
		if err != nil {
			http.Error(w, "Invalid time format. Use ISO 8601 (RFC3339) format", http.StatusBadRequest)
			return
		}
	}

	// Get sky conditions
	response := getSkyCondition(lat, lon, observationTime)

	json.NewEncoder(w).Encode(response)
}

func getSkyCondition(lat, lon float64, t time.Time) SkyConditionResponse {
	// Get weather data
	cloudInfo := getCloudCoverage(lat, lon, t)
	temp := getTemperature(lat, lon, t)

	// Get light pollution
	lightPollution := getLightPollution(lat, lon)

	// Generate recommendation
	recommendation := generateRecommendation(cloudInfo, lightPollution, temp)

	// Determine weather data source
	now := time.Now()
	isHistorical := t.Before(now.Add(-24 * time.Hour))
	weatherSource := "Open-Meteo Forecast API"
	if isHistorical {
		weatherSource = "Open-Meteo Archive API (historical data)"
	}

	return SkyConditionResponse{
		Location: Location{
			Latitude:  lat,
			Longitude: lon,
		},
		ObservationTime: t.Format(time.RFC3339),
		CloudCoverage:   cloudInfo,
		LightPollution:  lightPollution,
		Temperature:     temp,
		Recommendation:  recommendation,
		DataSource: DataSourceInfo{
			Weather:        weatherSource,
			LightPollution: "Calculated based on proximity to major cities",
			Disclaimer:     "Weather data may not reflect actual local conditions. Cloud coverage can vary significantly over short distances. Always verify conditions on-site before astrophotography.",
		},
	}
}

func getCloudCoverage(lat, lon float64, t time.Time) CloudInfo {
	cloudInfo, err := weatherService.GetCloudCoverage(lat, lon, t)
	if err != nil {
		log.Printf("Error fetching cloud coverage: %v", err)
		// Return fallback data
		return CloudInfo{
			Percentage: 50,
			Condition:  "unknown",
		}
	}
	return cloudInfo
}

func getTemperature(lat, lon float64, t time.Time) Temperature {
	temp, err := weatherService.GetTemperature(lat, lon, t)
	if err != nil {
		log.Printf("Error fetching temperature: %v", err)
		// Return fallback data
		return Temperature{
			Celsius:    15.0,
			Fahrenheit: 59.0,
			Condition:  "unknown",
		}
	}
	return temp
}

func getLightPollution(lat, lon float64) LightPollution {
	return lightPollutionService.GetLightPollution(lat, lon)
}

func generateRecommendation(cloud CloudInfo, light LightPollution, temp Temperature) string {
	if cloud.Percentage > 50 {
		return "Poor conditions for astrophotography due to high cloud coverage. Consider waiting for clearer skies."
	}

	if light.BortleScale >= 7 {
		return fmt.Sprintf("Sky conditions: %s with %d%% clouds. However, significant light pollution (Bortle %d) will limit deep sky photography. Consider targeting brighter objects like the Moon or planets.",
			cloud.Condition, cloud.Percentage, light.BortleScale)
	}

	if light.BortleScale <= 3 && cloud.Percentage <= 20 {
		return fmt.Sprintf("Excellent conditions! %s skies with minimal light pollution (Bortle %d). Perfect for deep sky astrophotography. Temperature: %.1f°C - dress warmly!",
			cloud.Condition, light.BortleScale, temp.Celsius)
	}

	if cloud.Percentage <= 30 {
		return fmt.Sprintf("Good conditions for astrophotography. %s skies with %s light pollution (Bortle %d). Temperature: %.1f°C.",
			cloud.Condition, light.Level, light.BortleScale, temp.Celsius)
	}

	return fmt.Sprintf("Moderate conditions. %d%% cloud coverage with %s light pollution. Some astrophotography possible but conditions could be better.",
		cloud.Percentage, light.Level)
}
