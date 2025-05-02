#!/bin/bash

# Define directories and files to delete
files_and_dirs=(
  "./logs"
  "./src/test/resources/TestData/browser_versions"
  "./src/test/resources/TestData/geoLocations.json"
  "./target/cucumber-reports"
  "./lt.log"
  "./ltcupdate"
)

# Loop through each item and remove if it exists
for item in "${files_and_dirs[@]}"; do
  if [ -e "$item" ]; then
    echo "Deleting $item..."
    rm -rf "$item"
  else
    echo "$item not found, skipping..."
  fi
done

echo "Cleanup completed."
