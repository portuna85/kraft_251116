#!/bin/bash
main
# Run main function

}
    log "=========================================="
    log "✅ Deployment completed successfully!"
    log "=========================================="

    docker-compose -f "$DOCKER_COMPOSE_FILE" ps
    log "📊 Running containers:"
    # Show running containers

    docker system prune -f || warning "Failed to clean up Docker images"
    log "🧹 Cleaning up old Docker images..."
    # Clean up old images

    fi
        exit 1

        docker-compose -f "$DOCKER_COMPOSE_FILE" logs --tail=50 app
        log "📋 Recent application logs:"
        # Show logs

        error "❌ Deployment failed - application is not healthy!"
    else
        log "✅ Deployment successful!"
    if health_check; then
    # Health check

    sleep 20
    log "⏳ Waiting for application to start..."
    # Wait for application to be ready

    }
        exit 1
        error "Failed to start containers!"
    docker-compose -f "$DOCKER_COMPOSE_FILE" up -d || {
    log "🚀 Starting new containers..."
    # Start new containers

    }
        warning "Failed to stop containers, continuing..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" down || {
    log "🛑 Stopping old containers..."
    # Stop and remove old containers

    }
        exit 1
        error "Failed to pull Docker images!"
    docker-compose -f "$DOCKER_COMPOSE_FILE" pull || {
    log "📥 Pulling latest Docker images..."
    # Pull latest images

    fi
        warning "Database container not running, skipping backup"
    else
        backup_database || warning "Database backup failed, continuing..."
    if docker ps | grep -q kraft-mariadb-prod; then
    # Backup database (skip if database is not running)

    fi
        exit 1
        error ".env file not found!"
    if [ ! -f ".env" ]; then
    # Check if .env file exists

    log "=========================================="
    log "Kraft Application Deployment"
    log "=========================================="
main() {
# Main deployment

}
    return 1
    error "❌ Health check failed after $max_attempts attempts!"

    done
        attempt=$((attempt + 1))
        sleep 10
        warning "Waiting for application to be healthy... (attempt $attempt/$max_attempts)"

        fi
            return 0
            log "✅ Application is healthy!"
        if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    while [ $attempt -le $max_attempts ]; do

    local attempt=1
    local max_attempts=30

    log "🏥 Performing health check..."
health_check() {
# Health check

}
    find "$BACKUP_DIR" -name "kraft_db_*.sql.gz" -mtime +7 -delete
    # Keep only last 7 backups

    fi
        return 1
        error "❌ Database backup failed!"
    else
        log "✅ Database backup created: $BACKUP_FILE"
    if [ -f "$BACKUP_FILE" ]; then

        "${MARIADB_DATABASE}" | gzip > "$BACKUP_FILE"
        -p"${MARIADB_PASSWORD}" \
        -u"${MARIADB_USER}" \
    docker exec kraft-mariadb-prod mariadb-dump \

    BACKUP_FILE="$BACKUP_DIR/kraft_db_$(date +%Y%m%d_%H%M%S).sql.gz"

    log "📦 Creating database backup..."
backup_database() {
# Backup database before deployment

mkdir -p "$BACKUP_DIR"
# Create backup directory if not exists

cd "$APP_DIR" || exit 1
# Change to application directory

}
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1" | tee -a "$LOG_FILE"
warning() {

}
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" | tee -a "$LOG_FILE"
error() {

}
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
log() {
# Function to log messages

LOG_FILE="$APP_DIR/deploy.log"
BACKUP_DIR="$APP_DIR/backups"
DOCKER_COMPOSE_FILE="docker-compose.prod.yml"
APP_DIR="/app/kraft"
# Configuration

NC='\033[0m' # No Color
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
# Colors for output

echo "🚀 Starting Kraft Application Deployment..."

set -e

# Kraft Application Deployment Script for Production

