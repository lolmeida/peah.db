#!/bin/bash

# Script to validate Grafana dashboard JSON files in the ConfigMap
# Also checks if dashboards are loaded in Grafana

echo "=== Grafana Dashboard Validator ==="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Part 1: Validate dashboard JSON structure locally
echo -e "${YELLOW}📋 Part 1: Validating dashboard JSON structure...${NC}"
echo ""

# Extract and validate each dashboard JSON from the YAML file
YAML_FILE="templates/grafana-dashboards.yaml"

if [ ! -f "$YAML_FILE" ]; then
    echo -e "${RED}ERROR: $YAML_FILE not found!${NC}"
    exit 1
fi

# List of expected dashboards
DASHBOARDS=("n8n-overview" "postgresql-dashboard" "mysql-dashboard" "redis-dashboard" "kubernetes-overview")

# Counter for errors
ERROR_COUNT=0

for DASHBOARD in "${DASHBOARDS[@]}"; do
    echo "Checking $DASHBOARD.json..."
    
    # Extract the JSON content between the dashboard markers
    START_LINE=$(grep -n "^  ${DASHBOARD}.json: |" "$YAML_FILE" | cut -d: -f1)
    
    if [ -z "$START_LINE" ]; then
        echo -e "  ${RED}❌ Dashboard $DASHBOARD not found in YAML${NC}"
        ((ERROR_COUNT++))
        continue
    fi
    
    # Find the next dashboard or end of file
    NEXT_START=$(tail -n +$((START_LINE + 1)) "$YAML_FILE" | grep -n "^  [a-z-]*.json: |" | head -1 | cut -d: -f1)
    
    if [ -z "$NEXT_START" ]; then
        # This is the last dashboard, extract until end of file or end marker
        END_LINE=$(tail -n +$((START_LINE + 1)) "$YAML_FILE" | grep -n "^{{- end" | head -1 | cut -d: -f1)
        if [ -z "$END_LINE" ]; then
            END_LINE=$(wc -l < "$YAML_FILE")
        else
            END_LINE=$((START_LINE + END_LINE - 1))
        fi
    else
        END_LINE=$((START_LINE + NEXT_START - 1))
    fi
    
    # Extract the JSON content and remove the leading spaces (4 spaces indent)
    JSON_CONTENT=$(sed -n "$((START_LINE + 1)),$((END_LINE))p" "$YAML_FILE" | sed 's/^    //')
    
    # Save to temp file for validation
    TEMP_FILE="/tmp/${DASHBOARD}.json"
    echo "$JSON_CONTENT" > "$TEMP_FILE"
    
    # Validate JSON structure
    if ! jq empty "$TEMP_FILE" 2>/dev/null; then
        echo -e "  ${RED}❌ Invalid JSON structure${NC}"
        jq . "$TEMP_FILE" 2>&1 | head -5
        ((ERROR_COUNT++))
        continue
    fi
    
    # Check for wrapped dashboard structure
    if jq -e '.dashboard' "$TEMP_FILE" >/dev/null 2>&1; then
        echo -e "  ${YELLOW}⚠️  Dashboard is wrapped in 'dashboard' object (incorrect structure)${NC}"
        
        # Check if title exists in wrapped structure
        TITLE=$(jq -r '.dashboard.title // empty' "$TEMP_FILE")
        if [ -z "$TITLE" ]; then
            echo -e "  ${RED}❌ Missing title in dashboard${NC}"
            ((ERROR_COUNT++))
        else
            echo -e "  ${GREEN}✓ Title found: '$TITLE'${NC}"
        fi
        
        # Check other required fields
        if ! jq -e '.dashboard.panels' "$TEMP_FILE" >/dev/null 2>&1; then
            echo -e "  ${RED}❌ Missing panels array${NC}"
            ((ERROR_COUNT++))
        else
            PANEL_COUNT=$(jq '.dashboard.panels | length' "$TEMP_FILE")
            echo -e "  ${GREEN}✓ Panels found: $PANEL_COUNT${NC}"
        fi
        
        echo -e "  ${YELLOW}⚠️  NEEDS FIX: Remove the 'dashboard' wrapper${NC}"
        ((ERROR_COUNT++))
    else
        # Check for correct structure (title at root level)
        TITLE=$(jq -r '.title // empty' "$TEMP_FILE")
        if [ -z "$TITLE" ]; then
            echo -e "  ${RED}❌ Missing title at root level${NC}"
            ((ERROR_COUNT++))
        else
            echo -e "  ${GREEN}✓ Title found at root: '$TITLE'${NC}"
        fi
        
        # Check panels at root
        if ! jq -e '.panels' "$TEMP_FILE" >/dev/null 2>&1; then
            echo -e "  ${RED}❌ Missing panels array at root${NC}"
            ((ERROR_COUNT++))
        else
            PANEL_COUNT=$(jq '.panels | length' "$TEMP_FILE")
            echo -e "  ${GREEN}✓ Panels found at root: $PANEL_COUNT${NC}"
        fi
    fi
    
    # Clean up
    rm -f "$TEMP_FILE"
    echo ""
done

echo "=== Structure Validation Summary ==="
echo -e "Total errors found: ${ERROR_COUNT}"

if [ $ERROR_COUNT -gt 0 ]; then
    echo ""
    echo -e "${RED}❌ Dashboard validation FAILED${NC}"
    echo ""
    echo -e "${YELLOW}The dashboards have incorrect structure. They should NOT be wrapped in a 'dashboard' object.${NC}"
    echo -e "${YELLOW}Grafana expects the dashboard JSON to have 'title', 'panels', etc. at the root level.${NC}"
    echo ""
fi

# Part 2: Check if dashboards are loaded in Grafana (optional)
if [ "$1" == "--check-grafana" ]; then
    echo ""
    echo -e "${YELLOW}📡 Part 2: Checking dashboards in Grafana...${NC}"
    echo ""
    
    GRAFANA_URL="https://grafana.lolmeida.com"
    ADMIN_USER="admin"
    ADMIN_PASS="grafana-changeme123"
    
    # Test Grafana health
    echo -e "${YELLOW}Testing Grafana connectivity...${NC}"
    if curl -s -o /dev/null -w "%{http_code}" "$GRAFANA_URL/api/health" | grep -q "200"; then
        echo -e "${GREEN}✅ Grafana is accessible!${NC}"
    else
        echo -e "${RED}❌ Grafana is not accessible${NC}"
        exit 1
    fi
    
    # List dashboards
    echo -e "${YELLOW}Listing dashboards...${NC}"
    LOADED_DASHBOARDS=$(curl -s -u "$ADMIN_USER:$ADMIN_PASS" \
        "$GRAFANA_URL/api/search?type=dash-db" \
        | jq -r '.[] | "- \(.title) (ID: \(.id))"' 2>/dev/null)
    
    if [ -n "$LOADED_DASHBOARDS" ]; then
        echo -e "${GREEN}✅ Dashboards found:${NC}"
        echo "$LOADED_DASHBOARDS"
    else
        echo -e "${YELLOW}⚠️  No dashboards found yet${NC}"
    fi
    
    # Check specific dashboards
    echo ""
    echo -e "${YELLOW}Checking expected dashboards...${NC}"
    
    EXPECTED_TITLES=(
        "N8N Overview"
        "PostgreSQL Monitoring"
        "MySQL Monitoring"
        "Redis Monitoring"
        "Kubernetes Cluster"
    )
    
    for title in "${EXPECTED_TITLES[@]}"; do
        if echo "$LOADED_DASHBOARDS" | grep -q "$title"; then
            echo -e "${GREEN}✅ $title - Loaded${NC}"
        else
            echo -e "${YELLOW}⏳ $title - Not loaded yet${NC}"
        fi
    done
fi

echo ""
if [ $ERROR_COUNT -gt 0 ]; then
    echo -e "${RED}🚨 Action Required: Fix the dashboard JSON structure before deploying!${NC}"
    exit 1
else
    echo -e "${GREEN}✅ All dashboard structures are valid!${NC}"
    echo ""
    echo "To check if dashboards are loaded in Grafana, run:"
    echo "  ./check-dashboards.sh --check-grafana"
fi 