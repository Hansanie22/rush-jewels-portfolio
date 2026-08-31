#!/bin/bash
# ============================================================
# RUSH JEWELS - Deploy Script (Run on Bluehost VPS)
# Run after uploading JAR: bash deploy.sh
# ============================================================

APP_DIR="/opt/rush_jewels"
JAR_NAME="RUSH_JEWELS-0.0.1-SNAPSHOT.jar"
SERVICE_NAME="rush-jewels"

echo ""
echo "================================================"
echo "  Deploying RUSH JEWELS..."
echo "================================================"

# Stop existing service if running
if sudo systemctl is-active --quiet $SERVICE_NAME; then
    echo "Stopping existing service..."
    sudo systemctl stop $SERVICE_NAME
fi

# Create systemd service
echo "Creating systemd service..."
sudo tee /etc/systemd/system/rush-jewels.service > /dev/null <<EOF
[Unit]
Description=Rush Jewels Spring Boot Application
After=network.target mysql.service
Requires=mysql.service

[Service]
User=$USER
WorkingDirectory=$APP_DIR
ExecStart=/usr/bin/java -Xms256m -Xmx512m -XX:+UseG1GC -jar $APP_DIR/$JAR_NAME --spring.profiles.active=production --server.port=8081
SuccessExitStatus=143
Restart=on-failure
RestartSec=15
StandardOutput=append:/var/log/rush_jewels/app.log
StandardError=append:/var/log/rush_jewels/error.log

[Install]
WantedBy=multi-user.target
EOF

# Reload systemd and start
sudo systemctl daemon-reload
sudo systemctl enable $SERVICE_NAME
sudo systemctl start $SERVICE_NAME

sleep 3

# Check status
if sudo systemctl is-active --quiet $SERVICE_NAME; then
    echo "✅ Rush Jewels is RUNNING!"
    echo ""
    echo "Check logs: sudo journalctl -u rush-jewels -f"
    echo "App URL: http://129.121.78.119:9090"
else
    echo "❌ Service failed to start. Check logs:"
    sudo journalctl -u $SERVICE_NAME -n 30 --no-pager
fi
