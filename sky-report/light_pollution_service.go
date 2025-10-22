package main

import (
	"math"
)

// LightPollutionService handles light pollution calculations
type LightPollutionService struct {
	// Major cities data: lat, lon, population
	majorCities []CityData
}

type CityData struct {
	Name       string
	Latitude   float64
	Longitude  float64
	Population int // in millions
}

// NewLightPollutionService creates a new light pollution service
func NewLightPollutionService() *LightPollutionService {
	return &LightPollutionService{
		majorCities: []CityData{
			// Major cities worldwide (sample data)
			{"New York", 40.7128, -74.0060, 8},
			{"Los Angeles", 34.0522, -118.2437, 4},
			{"Chicago", 41.8781, -87.6298, 3},
			{"London", 51.5074, -0.1278, 9},
			{"Paris", 48.8566, 2.3522, 2},
			{"Tokyo", 35.6762, 139.6503, 14},
			{"Beijing", 39.9042, 116.4074, 21},
			{"Shanghai", 31.2304, 121.4737, 27},
			{"Mumbai", 19.0760, 72.8777, 20},
			{"Delhi", 28.7041, 77.1025, 30},
			{"São Paulo", -23.5505, -46.6333, 12},
			{"Mexico City", 19.4326, -99.1332, 9},
			{"Moscow", 55.7558, 37.6173, 12},
			{"Istanbul", 41.0082, 28.9784, 15},
			{"Sydney", -33.8688, 151.2093, 5},
			{"Toronto", 43.6532, -79.3832, 3},
			{"Berlin", 52.5200, 13.4050, 4},
			{"Madrid", 40.4168, -3.7038, 3},
			{"Rome", 41.9028, 12.4964, 3},
			{"Dubai", 25.2048, 55.2708, 3},
		},
	}
}

// CalculateDistance calculates the great circle distance between two points
func (lps *LightPollutionService) CalculateDistance(lat1, lon1, lat2, lon2 float64) float64 {
	const earthRadius = 6371 // kilometers

	lat1Rad := lat1 * math.Pi / 180
	lat2Rad := lat2 * math.Pi / 180
	deltaLat := (lat2 - lat1) * math.Pi / 180
	deltaLon := (lon2 - lon1) * math.Pi / 180

	a := math.Sin(deltaLat/2)*math.Sin(deltaLat/2) +
		math.Cos(lat1Rad)*math.Cos(lat2Rad)*
			math.Sin(deltaLon/2)*math.Sin(deltaLon/2)

	c := 2 * math.Atan2(math.Sqrt(a), math.Sqrt(1-a))

	return earthRadius * c
}

// GetLightPollution calculates light pollution for a given location
func (lps *LightPollutionService) GetLightPollution(lat, lon float64) LightPollution {
	// Calculate light pollution based on distance from major cities
	var minPollutionScore float64 = 1.0 // Best case
	var closestCityDistance float64 = 1000000

	for _, city := range lps.majorCities {
		distance := lps.CalculateDistance(lat, lon, city.Latitude, city.Longitude)

		if distance < closestCityDistance {
			closestCityDistance = distance
		}

		// Calculate pollution contribution from this city
		// Formula: pollution decreases with distance
		// Higher population = more pollution
		if distance < 500 { // Only consider cities within 500km
			cityPollution := float64(city.Population) / math.Max(distance, 1)
			if cityPollution > minPollutionScore {
				minPollutionScore = cityPollution
			}
		}
	}

	// Convert pollution score to Bortle scale (1-9)
	var bortleScale int
	switch {
	case minPollutionScore < 0.05:
		bortleScale = 1 // Excellent dark sky
	case minPollutionScore < 0.1:
		bortleScale = 2 // Typical dark sky
	case minPollutionScore < 0.5:
		bortleScale = 3 // Rural sky
	case minPollutionScore < 1.5:
		bortleScale = 4 // Rural/suburban transition
	case minPollutionScore < 3.0:
		bortleScale = 5 // Suburban sky
	case minPollutionScore < 6.0:
		bortleScale = 6 // Bright suburban
	case minPollutionScore < 12.0:
		bortleScale = 7 // Suburban/urban transition
	case minPollutionScore < 25.0:
		bortleScale = 8 // City sky
	default:
		bortleScale = 9 // Inner city
	}

	// Additional adjustment based on distance from nearest city
	if closestCityDistance > 200 {
		// Very remote location - improve by 1 level
		bortleScale = int(math.Max(1, float64(bortleScale-1)))
	} else if closestCityDistance < 20 {
		// Very close to city - worsen by 1 level
		bortleScale = int(math.Min(9, float64(bortleScale+1)))
	}

	var level string
	var sqm float64

	switch bortleScale {
	case 1:
		level = "excellent"
		sqm = 21.9
	case 2:
		level = "excellent"
		sqm = 21.7
	case 3:
		level = "good"
		sqm = 21.4
	case 4:
		level = "good"
		sqm = 20.8
	case 5:
		level = "moderate"
		sqm = 19.8
	case 6:
		level = "moderate"
		sqm = 19.1
	case 7:
		level = "poor"
		sqm = 18.5
	case 8:
		level = "very_poor"
		sqm = 18.0
	default:
		level = "very_poor"
		sqm = 17.5
	}

	return LightPollution{
		BortleScale: bortleScale,
		Level:       level,
		SQM:         sqm,
	}
}
