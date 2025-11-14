# Kraft Application Deployment Script for Production (PowerShell)

param(
    [string]$AppDir = "C:\app\kraft",
    [string]$DockerComposeFile = "docker-compose.prod.yml"
)

$ErrorActionPreference = "Stop"

# Colors
$Green = "Green"
$Yellow = "Yellow"
$Red = "Red"

function Log {
    param([string]$Message)
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')]" -ForegroundColor $Green -NoNewline
    Write-Host " $Message"
}

function Error {
    param([string]$Message)
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] ERROR:" -ForegroundColor $Red -NoNewline
    Write-Host " $Message"
}

function Warning {
    param([string]$Message)
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] WARNING:" -ForegroundColor $Yellow -NoNewline
    Write-Host " $Message"
}

# Change to application directory
Set-Location $AppDir

# Create backup directory
$BackupDir = Join-Path $AppDir "backups"
if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

function Backup-Database {
    Log "📦 Creating database backup..."

    $BackupFile = Join-Path $BackupDir "kraft_db_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql.gz"

    try {
        docker exec kraft-mariadb-prod mariadb-dump `
            -u"$env:MARIADB_USER" `
            -p"$env:MARIADB_PASSWORD" `
            "$env:MARIADB_DATABASE" | gzip > $BackupFile

        Log "✅ Database backup created: $BackupFile"

        # Keep only last 7 backups
        Get-ChildItem $BackupDir -Filter "kraft_db_*.sql.gz" |
            Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-7) } |
            Remove-Item
    }
    catch {
        Warning "Database backup failed: $_"
    }
}

function Test-Health {
    Log "🏥 Performing health check..."

    $maxAttempts = 30
    $attempt = 1

    while ($attempt -le $maxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                Log "✅ Application is healthy!"
                return $true
            }
        }
        catch {
            Warning "Waiting for application to be healthy... (attempt $attempt/$maxAttempts)"
            Start-Sleep -Seconds 10
            $attempt++
        }
    }

    Error "❌ Health check failed after $maxAttempts attempts!"
    return $false
}

# Main deployment
try {
    Log "=========================================="
    Log "Kraft Application Deployment"
    Log "=========================================="

    # Check if .env file exists
    if (-not (Test-Path ".env")) {
        throw ".env file not found!"
    }

    # Load .env file
    Get-Content ".env" | ForEach-Object {
        if ($_ -match '^([^#].+?)=(.+)$') {
            [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
        }
    }

    # Backup database
    $containers = docker ps --format "{{.Names}}"
    if ($containers -match "kraft-mariadb-prod") {
        Backup-Database
    }
    else {
        Warning "Database container not running, skipping backup"
    }

    # Pull latest images
    Log "📥 Pulling latest Docker images..."
    docker-compose -f $DockerComposeFile pull
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to pull Docker images!"
    }

    # Stop old containers
    Log "🛑 Stopping old containers..."
    docker-compose -f $DockerComposeFile down

    # Start new containers
    Log "🚀 Starting new containers..."
    docker-compose -f $DockerComposeFile up -d
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to start containers!"
    }

    # Wait for startup
    Log "⏳ Waiting for application to start..."
    Start-Sleep -Seconds 20

    # Health check
    if (Test-Health) {
        Log "✅ Deployment successful!"
    }
    else {
        Error "❌ Deployment failed - application is not healthy!"

        Log "📋 Recent application logs:"
        docker-compose -f $DockerComposeFile logs --tail=50 app

        exit 1
    }

    # Clean up
    Log "🧹 Cleaning up old Docker images..."
    docker system prune -f

    # Show running containers
    Log "📊 Running containers:"
    docker-compose -f $DockerComposeFile ps

    Log "=========================================="
    Log "✅ Deployment completed successfully!"
    Log "=========================================="
}
catch {
    Error "Deployment failed: $_"
    exit 1
}

