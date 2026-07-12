#!/bin/bash
# Load test script for Hotel API using Apache Bench (ab)

echo "Starting load test for Hotel API (Without Cache vs With Cache)"
echo "--------------------------------------------------------------"

# Note: Before running this script, ensure the application is running on localhost:8082
# and a Bearer token is provided if testing secured endpoints.
# For this baseline, we test a public or mocked endpoint if available.
# Wait for the service to be up
sleep 2

echo "Running 1000 requests (100 concurrent) to /hotel/getAll"
ab -n 1000 -c 100 http://localhost:8082/hotel/getAll

echo "Load test complete. Compare latencies in the output above."
