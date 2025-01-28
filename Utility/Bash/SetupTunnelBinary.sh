#!/bin/bash

# Default environment is prod
ENV="prod"

# Check if LT_Tunnel_Binary directory exists, if not, create it
if [[ ! -d "LT_Tunnel_Binary" ]]; then
    mkdir -p "LT_Tunnel_Binary"
    echo "Directory 'LT_Tunnel_Binary' created."
else
    echo "Directory 'LT_Tunnel_Binary' already exists."
fi

# Parse arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --env) ENV="$2"; shift ;; # Set the environment (stage or prod)
        *) echo "Unknown parameter: $1"; exit 1 ;;
    esac
    shift
done

# Set the download URL prefix based on the environment
if [[ "$ENV" == "stage" ]]; then
    URL_PREFIX="stage-downloads.lambdatestinternal.com"
elif [[ "$ENV" == "prod" ]]; then
    URL_PREFIX="downloads.lambdatest.com"
else
    echo "Invalid environment: $ENV. Use 'stage' or 'prod'."
    exit 1
fi

# Check the OS type
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="Linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="Mac"
elif [[ "$OSTYPE" == "msys"* || "$OSTYPE" == "cygwin"* || "$OSTYPE" == "win32" ]]; then
    OS="Windows"
else
    echo "Unsupported OS: $OSTYPE"
    exit 1
fi

# Create the directory based on OS
FOLDER="LT_Tunnel_Binary/$OS"
mkdir -p "$FOLDER"

# Define download URL and extraction folder based on OS
if [[ "$OS" == "Linux" ]]; then
    DOWNLOAD_URL="https://$URL_PREFIX/tunnel/v3/linux/64bit/LT_Linux.zip"
elif [[ "$OS" == "Mac" ]]; then
    DOWNLOAD_URL="https://$URL_PREFIX/tunnel/v3/mac/64bit/LT_Mac.zip"
elif [[ "$OS" == "Windows" ]]; then
    DOWNLOAD_URL="https://$URL_PREFIX/tunnel/v3/windows/64bit/LT_Windows.zip"
fi

# Download the file
wget "$DOWNLOAD_URL" -P "$FOLDER/"

# Extract the file
unzip -o "$FOLDER/$(basename "$DOWNLOAD_URL")" -d "$FOLDER"

# Remove the ZIP file
rm -f "$FOLDER/$(basename "$DOWNLOAD_URL")"

echo "Tunnel Binary Setup completed for $OS in $ENV environment."
