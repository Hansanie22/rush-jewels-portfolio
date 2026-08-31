#!/bin/bash
# =====================================================
# RUSH JEWELS - Bluehost VPS Startup Script
# =====================================================
# Usage: ./start.sh
# To run in background: nohup ./start.sh &
# =====================================================

# Java 17 path - update if different on your server
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
JAVA=$JAVA_HOME/bin/java

# If Java is in PATH, just use:
# JAVA=java

APP_JAR="RUSH_JEWELS-0.0.1-SNAPSHOT.jar"
LOG_FILE="rush_jewels.log"

echo "============================================="
echo "  Starting RUSH JEWELS Application"
echo "  $(date)"
echo "============================================="

# Create log directory if not exists
mkdir -p /var/log/rush_jewels

# Start the application
nohup $JAVA \
  -Xms256m \
  -Xmx512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar $APP_JAR \
  --spring.profiles.active=production \
  >> $LOG_FILE 2>&1 &

echo "Application started with PID: $!"
echo "Logs: tail -f $LOG_FILE"
