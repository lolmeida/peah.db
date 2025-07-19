#!/bin/bash

# Test script for K8s Config API endpoints
# Make sure the application is running on localhost:8080

BASE_URL="http://localhost:8080/api/config"

echo "🧪 Testing K8s Config API Endpoints"
echo "====================================="

# Test 1: Get environments
echo "📋 1. GET /environments"
curl -s "$BASE_URL/environments" | jq . || echo "❌ Failed to get environments"
echo -e "\n"

# Test 2: Get stacks for environment 1
echo "📋 2. GET /environments/1/stacks"
curl -s "$BASE_URL/environments/1/stacks" | jq . || echo "❌ Failed to get stacks"
echo -e "\n"

# Test 3: Get apps for database stack
echo "📋 3. GET /environments/1/stacks/database/apps"
curl -s "$BASE_URL/environments/1/stacks/database/apps" | jq . || echo "❌ Failed to get apps"
echo -e "\n"

# Test 4: Get manifests for postgresql app
echo "📋 4. GET /environments/1/stacks/database/apps/postgresql/manifests"
curl -s "$BASE_URL/environments/1/stacks/database/apps/postgresql/manifests" | jq . || echo "❌ Failed to get manifests"
echo -e "\n"

# Test 5: Generate values for database stack
echo "📋 5. GET /environments/1/stacks/database/values"
curl -s "$BASE_URL/environments/1/stacks/database/values" | jq . || echo "❌ Failed to generate values"
echo -e "\n"

# Test 6: Test deploy endpoint
echo "📋 6. POST /environments/1/stacks/database/deploy"
curl -s -X POST "$BASE_URL/environments/1/stacks/database/deploy" | jq . || echo "❌ Failed to deploy"
echo -e "\n"

# Test 7: Update stack enabled status
echo "📋 7. PUT /environments/1/stacks/database (enable/disable)"
curl -s -X PUT "$BASE_URL/environments/1/stacks/database" \
  -H "Content-Type: application/json" \
  -d '{"enabled": true, "environmentId": 1}' | jq . || echo "❌ Failed to update stack"
echo -e "\n"

# Test 8: Update app enabled status
echo "📋 8. PUT /environments/1/stacks/database/apps/postgresql (enable/disable)"
curl -s -X PUT "$BASE_URL/environments/1/stacks/database/apps/postgresql" \
  -H "Content-Type: application/json" \
  -d '{"enabled": true, "stackId": 1}' | jq . || echo "❌ Failed to update app"
echo -e "\n"

echo "✅ Testing completed!"