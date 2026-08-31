#!/bin/bash
# ============================================================
# RUSH JEWELS - VPS Setup Script (Run on Bluehost VPS)
# Run this ONCE on your server: bash vps_setup.sh
# ============================================================

set -e  # Exit on any error

echo ""
echo "================================================"
echo "  RUSH JEWELS VPS Setup - Ubuntu 24.04"
echo "================================================"
echo ""

# ---- Step 1: Update system ----
echo "[1/7] Updating system packages..."
sudo apt update -y && sudo apt upgrade -y

# ---- Step 2: Install Java 17 ----
echo "[2/7] Installing Java 17..."
sudo apt install -y openjdk-17-jdk
java -version

# ---- Step 3: Install MySQL ----
echo "[3/7] Installing MySQL..."
sudo apt install -y mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql

# ---- Step 4: Create database & user ----
echo "[4/7] Setting up MySQL database..."
echo "Creating database 'rush_jewels' and user 'rushuser'..."
sudo mysql -e "CREATE DATABASE IF NOT EXISTS rush_jewels CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql -e "CREATE USER IF NOT EXISTS 'rushuser'@'localhost' IDENTIFIED BY 'YOUR_DB_PASSWORD';"
sudo mysql -e "GRANT ALL PRIVILEGES ON rush_jewels.* TO 'rushuser'@'localhost';"
sudo mysql -e "FLUSH PRIVILEGES;"
echo "✅ Database created: rush_jewels"
echo "✅ User: rushuser | Password: YOUR_DB_PASSWORD"

# ---- Step 5: Create app directory ----
echo "[5/7] Creating application directory..."
sudo mkdir -p /opt/rush_jewels
sudo mkdir -p /var/log/rush_jewels
sudo chown -R $USER:$USER /opt/rush_jewels
sudo chown -R $USER:$USER /var/log/rush_jewels
echo "✅ App directory: /opt/rush_jewels"

# ---- Step 6: Install Nginx ----
echo "[6/7] Installing Nginx..."
sudo apt install -y nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# ---- Step 7: Configure firewall ----
echo "[7/7] Configuring firewall..."
sudo ufw allow 22    # SSH
sudo ufw allow 80    # HTTP
sudo ufw allow 443   # HTTPS
sudo ufw allow 8080  # Spring Boot (temporary)
sudo ufw --force enable

echo ""
echo "================================================"
echo "  ✅ VPS Setup Complete!"
echo "================================================"
echo ""
echo "Next steps:"
echo "  1. Upload JAR:  scp RUSH_JEWELS.jar user@129.121.78.119:/opt/rush_jewels/"
echo "  2. Upload config: scp application.properties user@129.121.78.119:/opt/rush_jewels/"
echo "  3. Run: bash deploy.sh"
echo ""
