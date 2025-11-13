# git-commit.ps1 - PowerShell-friendly git add & commit wrapper
param(
  [string]$Message = "chore: update",
  [switch]$All
)

try {
  if ($All) {
    git add -A
  } else {
    git add src/main/resources/application.yml, docker-compose.yml, .env
  }
} catch {
  Write-Warning "git add failed, falling back to 'git add -A'"
  git add -A
}

# Commit
try {
  git commit -m $Message
} catch {
  Write-Warning "git commit failed or nothing to commit"
}

# Show status
git status --porcelain
