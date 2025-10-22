#!/bin/bash

# Simple Weekly Sky Report for Porvoo, Finland
# Plain text output (no colors or emojis)

# Configuration
BASE_URL="${SKY_REPORT_URL:-http://localhost:8080}"
LATITUDE=60.3932
LONGITUDE=25.6653

# Check if service is running
if ! curl -s "${BASE_URL}/health" > /dev/null 2>&1; then
    echo "Error: Sky Report service is not running at ${BASE_URL}"
    echo "Please start the service first: cd sky-report && ./sky-report"
    exit 1
fi

echo ""
echo "========================================================================"
echo "  7-Day Astrophotography Forecast for Porvoo, Finland"
echo "  Location: ${LATITUDE}N, ${LONGITUDE}E"
echo "  Generated: $(date)"
echo "========================================================================"
echo ""

# Loop through next 7 days
for day in {0..6}; do
    # Calculate date (evening time, 22:00 local time)
    if date --version >/dev/null 2>&1; then
        # GNU date
        target_date=$(date -u -d "+${day} days 20:00" +"%Y-%m-%dT%H:%M:%SZ")
        display_date=$(date -d "+${day} days" +"%A, %B %d, %Y")
    else
        # BSD date (macOS)
        target_date=$(date -u -v+${day}d -v20H -v0M -v0S +"%Y-%m-%dT%H:%M:%SZ")
        display_date=$(date -v+${day}d +"%A, %B %d, %Y")
    fi

    # Fetch data from API
    response=$(curl -s "${BASE_URL}/api/v1/sky-condition?lat=${LATITUDE}&lon=${LONGITUDE}&time=${target_date}")

    if [ $? -ne 0 ]; then
        echo "Error fetching data for day ${day}"
        continue
    fi

    # Parse JSON response
    cloud_pct=$(echo "$response" | jq -r '.cloud_coverage.percentage')
    cloud_cond=$(echo "$response" | jq -r '.cloud_coverage.condition')
    temp_c=$(echo "$response" | jq -r '.temperature.celsius')
    temp_f=$(echo "$response" | jq -r '.temperature.fahrenheit')
    bortle=$(echo "$response" | jq -r '.light_pollution.bortle_scale')
    light_level=$(echo "$response" | jq -r '.light_pollution.level')
    sqm=$(echo "$response" | jq -r '.light_pollution.sqm')
    recommendation=$(echo "$response" | jq -r '.recommendation')

    # Determine quality
    quality="POOR"
    if [ "$cloud_pct" -le 20 ] && [ "$bortle" -le 3 ]; then
        quality="EXCELLENT"
    elif [ "$cloud_pct" -le 30 ] && [ "$bortle" -le 4 ]; then
        quality="VERY GOOD"
    elif [ "$cloud_pct" -le 40 ] && [ "$bortle" -le 5 ]; then
        quality="GOOD"
    elif [ "$cloud_pct" -le 60 ]; then
        quality="MODERATE"
    fi

    # Print day information
    echo "------------------------------------------------------------------------"
    echo "Date: ${display_date} at 22:00 (evening)"
    echo "------------------------------------------------------------------------"
    echo "  Clouds:          ${cloud_pct}% (${cloud_cond})"
    echo "  Temperature:     ${temp_c}°C (${temp_f}°F)"
    echo "  Light Pollution: Bortle ${bortle} (${light_level}) - SQM ${sqm}"
    echo "  Overall Quality: ${quality}"
    echo ""
    echo "  Recommendation:"
    echo "    ${recommendation}"
    echo ""
done

echo "========================================================================"
echo "Bortle Scale Guide:"
echo "  1-3: Excellent dark sky sites"
echo "  4:   Good rural sky"
echo "  5-6: Moderate suburban sky"
echo "  7-9: High light pollution (urban/city)"
echo ""
echo "SQM (Sky Quality Meter): Higher values = darker skies (better)"
echo "========================================================================"
echo ""
echo "Note: Weather forecasts are estimates. Always verify conditions on-site"
echo "      before your astrophotography session!"
echo ""
