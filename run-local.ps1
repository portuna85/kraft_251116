# run-local.ps1 - loads .env and runs the Spring Boot app using Gradle
# Usage: .\run-local.ps1 [-EnvFile ".env"]
param(
  [string]$EnvFile = ".env"
)

if (Test-Path $EnvFile) {
  Get-Content $EnvFile | ForEach-Object {
    if ($_ -and -not $_.StartsWith('#')) {
      $parts = $_ -split '=', 2
      if ($parts.Length -eq 2) {
        $name = $parts[0].Trim()
        $value = $parts[1].Trim()
        if ($value -ne '') {
          Write-Host "Setting env $name"
          [System.Environment]::SetEnvironmentVariable($name, $value)
        }
      }
    }
  }
} else {
  Write-Host "Env file $EnvFile not found"
}

# Ensure gradlew is executable and run bootRun
Write-Host "Starting application with './gradlew bootRun'"
./gradlew bootRun

