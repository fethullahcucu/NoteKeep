#!/bin/sh

echo "Waiting for Database to be ready..."

apt-get update
apt-get install -y --no-install-recommends netcat-openbsd
rm -rf /var/lib/apt/lists/*

while ! nc -z db 27017; do
    sleep 1
done
echo "Database Ready...."
exec java -jar /app/app.jar "$@"
