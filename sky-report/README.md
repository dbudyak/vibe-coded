# Sky Report Service

A Go-based backend service that provides real-time sky condition information for astrophotography planning. Get detailed insights about cloud coverage, light pollution, and temperature for any location and time.

## Features

- **Real-time Weather Data**: Cloud coverage and temperature from Open-Meteo API
- **Light Pollution Calculation**: Bortle scale and Sky Quality Meter (SQM) values
- **Astrophotography Recommendations**: Smart recommendations based on conditions
- **RESTful API**: Simple HTTP endpoints with JSON responses
- **Docker Support**: Easy deployment with Docker and Docker Compose

## API Endpoints

### Health Check

```
GET /health
```

Check if the service is running.

**Response:**
```json
{
  "status": "healthy",
  "service": "sky-report"
}
```

### Get Sky Condition

```
GET /api/v1/sky-condition?lat={latitude}&lon={longitude}&time={iso8601_time}
```

or

```
POST /api/v1/sky-condition
Content-Type: application/json

{
  "latitude": 40.7128,
  "longitude": -74.0060,
  "time": "2025-10-22T22:00:00Z"
}
```

Get detailed sky conditions for astrophotography at a specific location and time.

**Query Parameters (GET):**
- `lat` (required): Latitude (-90 to 90)
- `lon` (required): Longitude (-180 to 180)
- `time` (optional): ISO 8601 timestamp (defaults to current time)

**Request Body (POST):**
```json
{
  "latitude": 40.7128,
  "longitude": -74.0060,
  "time": "2025-10-22T22:00:00Z"
}
```

**Response:**
```json
{
  "location": {
    "latitude": 40.7128,
    "longitude": -74.006
  },
  "observation_time": "2025-10-22T22:00:00Z",
  "cloud_coverage": {
    "percentage": 15,
    "condition": "clear"
  },
  "light_pollution": {
    "bortle_scale": 8,
    "level": "very_poor",
    "sqm": 18.0
  },
  "temperature": {
    "celsius": 12.5,
    "fahrenheit": 54.5,
    "condition": "cold"
  },
  "recommendation": "Sky conditions: clear with 15% clouds. However, significant light pollution (Bortle 8) will limit deep sky photography. Consider targeting brighter objects like the Moon or planets.",
  "data_source": {
    "weather": "Open-Meteo Forecast API",
    "light_pollution": "Calculated based on proximity to major cities",
    "disclaimer": "Weather data may not reflect actual local conditions. Cloud coverage can vary significantly over short distances. Always verify conditions on-site before astrophotography."
  }
}
```

## Response Fields

### Cloud Coverage
- `percentage`: Cloud coverage percentage (0-100)
- `condition`: Descriptive condition
  - `clear`: 0-10% clouds
  - `partly_cloudy`: 11-40% clouds
  - `cloudy`: 41-75% clouds
  - `overcast`: 76-100% clouds

### Light Pollution
- `bortle_scale`: Bortle Dark-Sky Scale (1-9)
  - 1-2: Excellent dark sky sites
  - 3-4: Good rural skies
  - 5-6: Moderate suburban areas
  - 7-9: Poor urban/city areas
- `level`: Descriptive level (excellent, good, moderate, poor, very_poor)
- `sqm`: Sky Quality Meter reading in mag/arcsec² (higher is better)

### Temperature
- `celsius`: Temperature in Celsius
- `fahrenheit`: Temperature in Fahrenheit
- `condition`: Descriptive condition
  - `very_cold`: Below -10°C
  - `cold`: -10°C to 5°C
  - `comfortable`: 5°C to 20°C
  - `warm`: 20°C to 30°C
  - `hot`: Above 30°C

### Data Source
- `weather`: The weather data source used
  - `Open-Meteo Forecast API`: For current and future times
  - `Open-Meteo Archive API (historical data)`: For dates more than 24 hours in the past
- `light_pollution`: How light pollution was calculated
- `disclaimer`: Important reminder about data accuracy and limitations

## Running the Service

### Local Development

```bash
# Run directly
go run .

# Or build and run
go build -o sky-report
./sky-report
```

The service will start on port 8080 (or the port specified in the `PORT` environment variable).

### Using Docker

```bash
# Build the image
docker build -t sky-report .

# Run the container
docker run -p 8080:8080 sky-report
```

### Using Docker Compose

```bash
# Start the service
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the service
docker-compose down
```

## Weekly Forecast Scripts

Two convenient scripts are provided to get a 7-day forecast for a specific location:

### Colorful Interactive Forecast

```bash
# For Porvoo, Finland (default location)
./weekly_forecast.sh

# For custom location, set environment variable
SKY_REPORT_URL=http://localhost:8080 ./weekly_forecast.sh
```

This script displays a beautifully formatted 7-day forecast with:
- Color-coded quality ratings (★★★★★ excellent to ★ poor)
- Emoji weather icons (☀️ clear, ☁️ cloudy, etc.)
- Temperature indicators
- Light pollution ratings with visual stars
- Evening time forecasts (22:00 local time, ideal for astrophotography)

### Simple Text-Only Forecast

```bash
# Plain text output (good for logging or scripts)
./weekly_forecast_simple.sh

# Redirect to file
./weekly_forecast_simple.sh > forecast.txt
```

This version provides the same information in plain text format without colors or emojis, suitable for:
- Logging to files
- Email notifications
- Integration with other scripts
- Text-based terminals

Both scripts can be customized by editing the `LATITUDE` and `LONGITUDE` variables at the top of the file, or by setting the `SKY_REPORT_URL` environment variable if the service runs on a different host/port.

## Usage Examples

### Using curl

```bash
# Get current conditions for New York City
curl "http://localhost:8080/api/v1/sky-condition?lat=40.7128&lon=-74.0060"

# Get conditions for a specific time
curl "http://localhost:8080/api/v1/sky-condition?lat=40.7128&lon=-74.0060&time=2025-10-22T22:00:00Z"

# Using POST
curl -X POST http://localhost:8080/api/v1/sky-condition \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 40.7128,
    "longitude": -74.0060,
    "time": "2025-10-22T22:00:00Z"
  }'
```

### Example Locations

**Good dark sky locations:**
- Cherry Springs State Park, PA: `lat=41.6628&lon=-77.8209`
- Death Valley, CA: `lat=36.5054&lon=-117.0794`
- Mauna Kea, HI: `lat=19.8207&lon=-155.4681`

**Urban locations (for comparison):**
- New York City: `lat=40.7128&lon=-74.0060`
- Los Angeles: `lat=34.0522&lon=-118.2437`
- London: `lat=51.5074&lon=-0.1278`

## Data Sources

- **Weather Data**: [Open-Meteo](https://open-meteo.com/) - Free weather API with no authentication required
- **Light Pollution**: Calculated based on proximity to major cities and population density

## Environment Variables

- `PORT`: Server port (default: 8080)

## Error Handling

The API returns appropriate HTTP status codes:
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters
- `405 Method Not Allowed`: Invalid HTTP method
- `500 Internal Server Error`: Server error

## Recommendations Guide

The service provides intelligent recommendations based on:
- Cloud coverage percentage
- Bortle scale rating
- Temperature conditions

Example recommendations:
- **Excellent**: Clear skies + Bortle 1-3 + Low cloud coverage
- **Good**: Partly cloudy + Bortle 4-6
- **Poor**: High cloud coverage or Bortle 7-9 (suggests brighter targets)

## Data Accuracy and Limitations

### Important Notes

**Weather Data Accuracy**: Weather forecasts and even historical weather data can be inaccurate, especially for cloud coverage. Cloud conditions can vary significantly over short distances (even a few kilometers) due to:
- Localized weather patterns
- Microclimate effects
- Rapid weather changes
- Differences between forecast models and actual conditions

**Historical Data**: For dates more than 24 hours in the past, the service automatically switches to the Open-Meteo Archive API, which provides historical observations. However, even this data:
- May come from weather stations far from your exact location
- Represents regional averages rather than point measurements
- May not capture very localized clear patches or cloud formations

**Light Pollution**: The light pollution calculation is based on distance from major cities and is an estimation. Actual light pollution can vary based on:
- Local lighting ordinances
- Topography (hills/mountains blocking light)
- Weather conditions (humidity affects light scatter)
- Time of year and special events

### Best Practices

1. **Always verify on-site**: Use this service for planning, but always check actual conditions when you arrive at your location
2. **Use multiple sources**: Cross-reference with other services like:
   - Clear Outside (cleardarksky.com)
   - Weather Underground local station data
   - Satellite imagery
   - Local weather apps
3. **Local knowledge**: Develop familiarity with your astrophotography sites and their typical weather patterns
4. **Real-time updates**: Weather can change rapidly - check conditions multiple times leading up to your session

### Known Issues

- Weather API may return forecast data that differs from actual conditions
- Cloud coverage percentages are regional estimates, not point measurements
- Temperature readings are from the nearest weather station, which may be kilometers away
- The service shows a disclaimer in every response to remind users of these limitations

## License

This service uses free and open APIs and is designed for educational and hobbyist use.

## Future Enhancements

Potential improvements:
- Moon phase calculation
- Astronomical twilight times
- Humidity and dew point
- Wind speed considerations
- Historical data analysis
- Multi-day forecasts
- Integration with additional light pollution databases
