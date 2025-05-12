#!/bin/bash

# Function to add entry to /etc/hosts
add_entry() {
  # Check if the entry already exists
  if grep -q "127.0.0.1       locallambda.com" /etc/hosts; then
    echo "Entry already exists in /etc/hosts. No changes made."
  else
    echo "127.0.0.1       locallambda.com" | sudo tee -a /etc/hosts > /dev/null
    echo "Entry added to /etc/hosts"
  fi
}

# Function to remove entry from /etc/hosts
remove_entry() {
  # Check if the entry exists before trying to remove it
  if grep -q "127.0.0.1       locallambda.com" /etc/hosts; then
    sudo sed -i '' '/127.0.0.1       locallambda.com/d' /etc/hosts
    echo "Entry removed from /etc/hosts"
  else
    echo "Entry not found in /etc/hosts. No changes made."
  fi
}

# Check if the user has passed an argument
if [ $# -eq 0 ]; then
  echo "Error: No arguments provided. Usage: $0 --addEntry | --removeEntry"
  exit 1
fi

# Check for the command passed as an argument
if [ $# -gt 1 ]; then
  echo "Error: Only one argument is allowed. Usage: $0 --addEntry | --removeEntry"
  exit 1
fi

case $1 in
  --addEntry)
    add_entry
    ;;
  --removeEntry)
    remove_entry
    ;;
  *)
    echo "Error: Invalid option. Usage: $0 --addEntry | --removeEntry"
    exit 1
    ;;
esac
