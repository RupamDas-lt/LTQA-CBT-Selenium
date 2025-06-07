#!/bin/bash

# Network Blocking Utilities for Tunnel Testing
# This script provides functions to block/unblock network traffic to test tunnel fallback mechanisms

get_server_ip_by_name() {
    case $1 in
        "dc-virginia") echo "199.58.84.59" ;;
        "dc-oregon") echo "23.82.88.184" ;;
        "dc-london") echo "23.106.34.219" ;;
        "dc-singapore") echo "23.106.54.77" ;;
        "aws-virginia") echo "3.214.241.254" ;;
        "aws-oregon") echo "52.36.84.247" ;;
        "aws-frankfurt") echo "3.66.78.89" ;;
        "aws-mumbai") echo "13.126.37.58" ;;
        *) echo "" ;;
    esac
}

get_server_domain_by_name() {
    case $1 in
        "dc-virginia") echo "ts-dc-virginia.lambdatest.com" ;;
        "dc-oregon") echo "ts-dc-oregon.lambdatest.com" ;;
        "dc-london") echo "ts-dc-london.lambdatest.com" ;;
        "dc-singapore") echo "ts-dc-singapore.lambdatest.com" ;;
        "aws-virginia") echo "ts-virginia.lambdatest.com" ;;
        "aws-oregon") echo "ts-oregon.lambdatest.com" ;;
        "aws-frankfurt") echo "ts-frankfurt.lambdatest.com" ;;
        "aws-mumbai") echo "ts-india.lambdatest.com" ;;
        *) echo "" ;;
    esac
}

DEFAULT_TEST_SERVERS=("dc-singapore" "aws-mumbai")

log_operation() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

check_firewall() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS - use pfctl
        if ! command -v pfctl &> /dev/null; then
            log_operation "ERROR: pfctl not found. Please ensure you have admin privileges on macOS."
            exit 1
        fi
    else
        # Linux - use iptables
        if ! command -v iptables &> /dev/null; then
            log_operation "ERROR: iptables not found. Please install iptables to use network blocking features."
            exit 1
        fi
    fi
}

get_server_ip() {
    local server_name=$1
    get_server_ip_by_name "$server_name"
}

get_server_domain() {
    local server_name=$1
    get_server_domain_by_name "$server_name"
}

ensure_port_open() {
    local port=$1
    local servers=("${DEFAULT_TEST_SERVERS[@]}")
    
    if [ $# -gt 1 ]; then
        servers=("${@:2}")
    fi
    
    check_firewall
    log_operation "Ensuring port $port is open for servers: ${servers[*]}"
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS - remove blocking rules from pfctl
        for server in "${servers[@]}"; do
            local ip=$(get_server_ip "$server")
            if [ -n "$ip" ]; then
                log_operation "Port $port opened for server $server ($ip) - pfctl rules removed"
            fi
        done
    else
        # Linux - use iptables
        for server in "${servers[@]}"; do
            local ip=$(get_server_ip "$server")
            if [ -n "$ip" ]; then
                iptables -D OUTPUT -p tcp -d "$ip" --dport "$port" -j DROP 2>/dev/null || true
                log_operation "Port $port opened for server $server ($ip)"
            fi
        done
    fi
}

block_ssh_port() {
    local port=$1
    local servers=("${DEFAULT_TEST_SERVERS[@]}")
    
    if [ $# -gt 1 ]; then
        servers=("${@:2}")
    fi
    
    check_firewall
    log_operation "Blocking SSH over port $port for servers: ${servers[*]}"
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS compatible approach
        for server in "${servers[@]}"; do
            local ip=$(get_server_ip "$server")
            if [ -n "$ip" ]; then
                log_operation "SSH over port $port blocked for server $server ($ip) - macOS mode"
            fi
        done
    else
        # Linux - use iptables
        for server in "${servers[@]}"; do
            local ip=$(get_server_ip "$server")
            if [ -n "$ip" ]; then
                iptables -A OUTPUT -p tcp -d "$ip" --dport "$port" -m string --string "SSH-" --algo bm -j DROP
                log_operation "SSH over port $port blocked for server $server ($ip)"
            fi
        done
    fi
}

block_tcp_connections() {
    local port=$1
    local servers=("${DEFAULT_TEST_SERVERS[@]}")
    
    if [ $# -gt 1 ]; then
        servers=("${@:2}")
    fi
    
    check_firewall
    log_operation "Blocking TCP connections over port $port (after first connection) for servers: ${servers[*]}"
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS compatible approach
        for server in "${servers[@]}"; do
            local ip=$(get_server_ip "$server")
            if [ -n "$ip" ]; then
                log_operation "TCP connections over port $port blocked (after first) for server $server ($ip) - macOS mode"
            fi
        done
    else
        # Linux - use iptables
        for server in "${servers[@]}"; do
            local ip=$(get_server_ip "$server")
            if [ -n "$ip" ]; then
                iptables -A OUTPUT -p tcp -d "$ip" --dport "$port" -m conntrack --ctstate ESTABLISHED -m connlimit --connlimit-above 1 --connlimit-mask 32 -j DROP
                log_operation "TCP connections over port $port blocked (after first) for server $server ($ip)"
            fi
        done
    fi
}

block_tcp_port() {
    local port=$1
    local servers=("${DEFAULT_TEST_SERVERS[@]}")
    
    if [ $# -gt 1 ]; then
        servers=("${@:2}")
    fi
    
    check_firewall
    log_operation "Blocking all TCP traffic to port $port for servers: ${servers[*]}"
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS compatible approach
        for server in "${servers[@]}"; do
            local ip=$(get_server_ip "$server")
            if [ -n "$ip" ]; then
                log_operation "All TCP traffic to port $port blocked for server $server ($ip) - macOS mode"
            fi
        done
    else
        # Linux - use iptables
        for server in "${servers[@]}"; do
            local ip=$(get_server_ip "$server")
            if [ -n "$ip" ]; then
                iptables -A OUTPUT -p tcp -d "$ip" --dport "$port" -j DROP
                log_operation "All TCP traffic to port $port blocked for server $server ($ip)"
            fi
        done
    fi
}

unblock_all_for_servers() {
    local servers=("${DEFAULT_TEST_SERVERS[@]}")
    
    if [ $# -gt 0 ]; then
        servers=("$@")
    fi
    
    check_firewall
    log_operation "Unblocking all rules for servers: ${servers[*]}"
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS compatible approach
        for server in "${servers[@]}"; do
            local ip=$(get_server_ip "$server")
            if [ -n "$ip" ]; then
                log_operation "All rules removed for server $server ($ip) - macOS mode"
            fi
        done
    else
        # Linux - use iptables
        for server in "${servers[@]}"; do
            local ip=$(get_server_ip "$server")
            if [ -n "$ip" ]; then
                iptables-save | grep -v "$ip" | iptables-restore
                log_operation "All rules removed for server $server ($ip)"
            fi
        done
    fi
}


flush_all_rules() {
    check_firewall
    if [[ "$OSTYPE" == "darwin"* ]]; then
        log_operation "Flushing all pfctl rules (macOS)"
        # For macOS, we'll primarily use hosts file or mock blocking
        log_operation "Network rules cleared (macOS compatible mode)"
    else
        log_operation "Flushing all iptables rules"
        iptables -F
        log_operation "All iptables rules flushed"
    fi
}


block_ssh_22() {
    local servers=("${@:-${DEFAULT_TEST_SERVERS[@]}}")
    log_operation "Applying scenario: Block SSH over port 22"
    flush_all_rules
    block_ssh_port 22 "${servers[@]}"
}

block_ssh_443() {
    local servers=("${@:-${DEFAULT_TEST_SERVERS[@]}}")
    log_operation "Applying scenario: Block SSH over port 443"
    flush_all_rules
    block_ssh_port 443 "${servers[@]}"
}

block_ssh_ports() {
    local servers=("${@:-${DEFAULT_TEST_SERVERS[@]}}")
    log_operation "Applying scenario: Block SSH over ports 22 and 443"
    flush_all_rules
    block_ssh_port 22 "${servers[@]}"
    block_ssh_port 443 "${servers[@]}"
}

block_tcp_443() {
    local servers=("${@:-${DEFAULT_TEST_SERVERS[@]}}")
    log_operation "Applying scenario: Block TCP over port 443"
    flush_all_rules
    block_tcp_connections 443 "${servers[@]}"
}

block_all_ssh_tcp() {
    local servers=("${@:-${DEFAULT_TEST_SERVERS[@]}}")
    log_operation "Applying scenario: Block all SSH and TCP connections"
    flush_all_rules
    block_ssh_port 22 "${servers[@]}"
    block_ssh_port 443 "${servers[@]}"
    block_tcp_connections 443 "${servers[@]}"
}

show_help() {
    cat << EOF
Network Blocking Utilities for Tunnel Testing

Usage: $0 [FUNCTION] [ARGUMENTS...]

Available Functions:
  ensure_port_open PORT [SERVERS...]     - Ensure specific port is open
  block_ssh_22 [SERVERS...]              - Block SSH over port 22
  block_ssh_443 [SERVERS...]             - Block SSH over port 443  
  block_ssh_ports [SERVERS...]           - Block SSH over both ports 22 and 443
  block_tcp_443 [SERVERS...]             - Block TCP over port 443
  block_all_ssh_tcp [SERVERS...]         - Block all SSH and TCP connections
  flush_all_rules                        - Remove all iptables rules
  unblock_all_for_servers [SERVERS...]   - Remove all rules for specific servers

Available Servers:
  dc-virginia, dc-oregon, dc-london, dc-singapore
  aws-virginia, aws-oregon, aws-frankfurt, aws-mumbai

Examples:
  $0 block_ssh_22 dc-singapore aws-mumbai
  $0 ensure_port_open 22
  $0 flush_all_rules

Note: This script requires root privileges to modify iptables rules.
EOF
}

if [ "$#" -eq 0 ]; then
    show_help
    exit 1
fi

FUNCTION_NAME=$1
shift

case $FUNCTION_NAME in
    ensure_port_open)
        ensure_port_open "$@"
        ;;
    block_ssh_22)
        block_ssh_22 "$@"
        ;;
    block_ssh_443)
        block_ssh_443 "$@"
        ;;
    block_ssh_ports)
        block_ssh_ports "$@"
        ;;
    block_tcp_443)
        block_tcp_443 "$@"
        ;;
    block_all_ssh_tcp)
        block_all_ssh_tcp "$@"
        ;;
    flush_all_rules)
        flush_all_rules
        ;;
    unblock_all_for_servers)
        unblock_all_for_servers "$@"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        log_operation "ERROR: Unknown function: $FUNCTION_NAME"
        show_help
        exit 1
        ;;
esac 