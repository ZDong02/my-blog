#!/bin/bash

# Blog Backend Startup Script
# Optimized for 2C2G Alibaba Cloud Ubuntu Server

APP_NAME="blog-backend"
JAR_FILE="blog-backend-1.0.0.jar"
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_FILE="${APP_DIR}/logs/app.log"

# JVM settings for 2C2G server
JAVA_OPTS="-Xmx768m -Xms256m"
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=200"
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=prod"

# Create logs directory
mkdir -p "${APP_DIR}/logs"

# PID file
PID_FILE="${APP_DIR}/app.pid"

# Get PID from file
get_pid() {
    if [ -f "$PID_FILE" ]; then
        cat "$PID_FILE"
    fi
}

# Check if process is running
is_running() {
    local pid=$(get_pid)
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
        return 0
    fi
    return 1
}

# Start the application
start() {
    if is_running; then
        echo "${APP_NAME} is already running (PID: $(get_pid))"
        return 1
    fi

    echo "Starting ${APP_NAME}..."
    cd "${APP_DIR}"

    nohup java $JAVA_OPTS -jar "$JAR_FILE" >> "$LOG_FILE" 2>&1 &
    local pid=$!
    echo $pid > "$PID_FILE"
    echo "${APP_NAME} started (PID: $pid)"

    # Wait a bit and check if it's running
    sleep 3
    if is_running; then
        echo "Application is running successfully"
        return 0
    else
        echo "Failed to start application. Check logs: $LOG_FILE"
        rm -f "$PID_FILE"
        return 1
    fi
}

# Stop the application
stop() {
    if ! is_running; then
        echo "${APP_NAME} is not running"
        return 1
    fi

    echo "Stopping ${APP_NAME} (PID: $(get_pid))..."
    local pid=$(get_pid)
    kill "$pid" 2>/dev/null

    # Wait for graceful shutdown (max 30 seconds)
    local count=0
    while is_running && [ $count -lt 30 ]; do
        sleep 1
        count=$((count + 1))
        echo -n "."
    done

    # Force kill if still running
    if is_running; then
        echo ""
        echo "Forcing shutdown..."
        kill -9 "$pid" 2>/dev/null
    fi

    rm -f "$PID_FILE"
    echo "${APP_NAME} stopped"
}

# Restart the application
restart() {
    stop
    sleep 2
    start
}

# Status of application
status() {
    if is_running; then
        echo "${APP_NAME} is running (PID: $(get_pid))"
    else
        echo "${APP_NAME} is not running"
    fi
}

# Show logs
logs() {
    tail -f "$LOG_FILE"
}

# Execute command
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        logs
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|logs}"
        exit 1
        ;;
esac

exit 0