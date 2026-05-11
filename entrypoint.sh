#!/bin/sh

echo "Waiting for Database to be ready..."

apt-get update
apt-get install -y --no-install-recommends netcat-openbsd
rm -rf /var/lib/apt/lists/*

# Try Docker Compose service name first, then Kubernetes service name
MONGO_HOST="${MONGO_HOST:-db}"
MONGO_PORT="${MONGO_PORT:-27017}"

while ! nc -z "$MONGO_HOST" "$MONGO_PORT"; do
    echo "Database not ready yet, retrying..."
    sleep 1
done
echo "Database Ready...."
exec java -jar /app/app.jar "$@"
