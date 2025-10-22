#!/bin/bash

# Weekly Sky Report for Porvoo, Finland
# Lat: 60.3932, Lon: 25.6653

# Configuration
BASE_URL="${SKY_REPORT_URL:-http://localhost:8080}"
LATITUDE=60.3932
LONGITUDE=25.6653

# Colors
RESET='\033[0m'
BOLD='\033[1m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'

# Function to get cloud coverage icon
get_cloud_icon() {
    local condition=$1
    case $condition in
        "clear") echo "☀️  Clear" ;;
        "partly_cloudy") echo "⛅ Partly Cloudy" ;;
        "cloudy") echo "☁️  Cloudy" ;;
        "overcast") echo "☁️  Overcast" ;;
        "unknown") echo "❓ Unknown" ;;
        *) echo "  $condition" ;;
    esac
}

# Function to get temperature icon
get_temp_icon() {
    local condition=$1
    case $condition in
        "very_cold") echo "🥶" ;;
        "cold") echo "❄️ " ;;
        "comfortable") echo "🌡️ " ;;
        "warm") echo "🌤️ " ;;
        "hot") echo "🔥" ;;
        *) echo "  " ;;
    esac
}

# Function to get light pollution icon
get_light_icon() {
    local bortle=$1
    if [ "$bortle" -le 3 ]; then
        echo "⭐⭐⭐ Excellent"
    elif [ "$bortle" -le 4 ]; then
        echo "⭐⭐  Good"
    elif [ "$bortle" -le 6 ]; then
        echo "⭐   Moderate"
    else
        echo "💡   Poor"
    fi
}

# Function to get quality rating
get_quality_rating() {
    local cloud_pct=$1
    local bortle=$2

    if [ "$cloud_pct" -le 20 ] && [ "$bortle" -le 3 ]; then
        echo -e "${GREEN}${BOLD}★★★★★ EXCELLENT${RESET}"
    elif [ "$cloud_pct" -le 30 ] && [ "$bortle" -le 4 ]; then
        echo -e "${GREEN}${BOLD}★★★★  VERY GOOD${RESET}"
    elif [ "$cloud_pct" -le 40 ] && [ "$bortle" -le 5 ]; then
        echo -e "${YELLOW}${BOLD}★★★   GOOD${RESET}"
    elif [ "$cloud_pct" -le 60 ]; then
        echo -e "${YELLOW}${BOLD}★★    MODERATE${RESET}"
    else
        echo -e "${RED}${BOLD}★     POOR${RESET}"
    fi
}

# Header
echo -e "\n${BOLD}${BLUE}╔════════════════════════════════════════════════════════════════════════╗${RESET}"
echo -e "${BOLD}${BLUE}║       SKY REPORT - 7-Day Astrophotography Forecast for Porvoo         ║${RESET}"
echo -e "${BOLD}${BLUE}║              Latitude: ${LATITUDE}°N, Longitude: ${LONGITUDE}°E              ║${RESET}"
echo -e "${BOLD}${BLUE}╚════════════════════════════════════════════════════════════════════════╝${RESET}\n"

# Check if service is running
if ! curl -s "${BASE_URL}/health" > /dev/null 2>&1; then
    echo -e "${RED}Error: Sky Report service is not running at ${BASE_URL}${RESET}"
    echo "Please start the service first: cd sky-report && ./sky-report"
    exit 1
fi

# Loop through next 7 days
for day in {0..6}; do
    # Calculate date (evening time, 22:00 local time which is good for astrophotography)
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
        echo -e "${RED}Error fetching data for day ${day}${RESET}"
        continue
    fi

    # Parse JSON response
    cloud_pct=$(echo "$response" | jq -r '.cloud_coverage.percentage')
    cloud_cond=$(echo "$response" | jq -r '.cloud_coverage.condition')
    temp_c=$(echo "$response" | jq -r '.temperature.celsius')
    temp_f=$(echo "$response" | jq -r '.temperature.fahrenheit')
    temp_cond=$(echo "$response" | jq -r '.temperature.condition')
    bortle=$(echo "$response" | jq -r '.light_pollution.bortle_scale')
    light_level=$(echo "$response" | jq -r '.light_pollution.level')
    sqm=$(echo "$response" | jq -r '.light_pollution.sqm')

    # Get icons
    cloud_icon=$(get_cloud_icon "$cloud_cond")
    temp_icon=$(get_temp_icon "$temp_cond")
    light_icon=$(get_light_icon "$bortle")
    quality=$(get_quality_rating "$cloud_pct" "$bortle")

    # Print day header
    echo -e "${BOLD}${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
    echo -e "${BOLD}📅 ${display_date} (22:00 Evening)${RESET}"
    echo -e "${BOLD}${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"

    # Print conditions
    echo -e "  ${cloud_icon} - ${cloud_pct}% cloud coverage"
    echo -e "  ${temp_icon}  ${temp_c}°C (${temp_f}°F)"
    echo -e "  ${light_icon} - Bortle ${bortle}, SQM ${sqm}"
    echo -e "  Overall: ${quality}"
    echo ""
done

# Footer
echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}${BLUE}Legend:${RESET}"
echo -e "  ⭐⭐⭐ = Excellent dark skies (Bortle 1-3)"
echo -e "  ⭐⭐  = Good skies (Bortle 4)"
echo -e "  ⭐   = Moderate light pollution (Bortle 5-6)"
echo -e "  💡   = High light pollution (Bortle 7+)"
echo ""
echo -e "${BOLD}${BLUE}Rating:${RESET} ★★★★★ Best  |  ★★★★ Very Good  |  ★★★ Good  |  ★★ Moderate  |  ★ Poor"
echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}\n"

echo -e "${YELLOW}Note: Forecasts are estimates. Always check actual conditions on-site!${RESET}\n"
