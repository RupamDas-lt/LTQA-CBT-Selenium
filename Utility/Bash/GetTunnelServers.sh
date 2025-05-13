#!/bin/bash

# Define server names and their corresponding domains
stage="stage-ts.lambdatestinternal.com"
prod_1="ts-virginia.lambdatest.com"
prod_2="ts-oregon.lambdatest.com"
prod_3="ts-india.lambdatest.com"
prod_4="ts-frankfurt.lambdatest.com"
prod_5="ts-dc-virginia.lambdatest.com"
prod_6="ts-dc-oregon.lambdatest.com"
prod_7="ts-dc-singapore.lambdatest.com"
prod_8="ts-dc-london.lambdatest.com"

# Path to the JSON file
file_path="src/test/resources/TestData/tunnelServers.json"

# Initialize JSON output array
json_output="{"

# List of server names
servers=($stage $prod_1 $prod_2 $prod_3 $prod_4 $prod_5 $prod_6 $prod_7 $prod_8)

# Loop through the servers and get their IPs
for server in "${servers[@]}"; do
    # Get the IP address for each server using dig
    ip=$(dig +short "$server" | grep -E '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$')

    # Debugging output to check if dig command works as expected
    if [ -n "$ip" ]; then
        echo "IP for $server: $ip"
        # If IP is found, create a JSON entry
        json_output+="\"$server\": \"$ip\","
    else
        echo "No IP found for $server."
    fi
done

# Check if there were any IPs found, if not, exit with an error
if [[ "$json_output" == "{" ]]; then
    echo "No IPs found. Exiting script."
    exit 1
fi

# Remove the trailing comma from the last entry
json_output=${json_output%,}

# Close the JSON array
json_output+="}"

# Check if the file exists
if [ -f "$file_path" ]; then
    echo "File exists, overriding the data."
else
    echo "File does not exist, creating the file."
fi

# Write the JSON output to the file, completely overriding the file
echo "$json_output" > "$file_path"

echo "Data has been written to $file_path."
