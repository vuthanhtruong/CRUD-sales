$ErrorActionPreference = "Stop"

Write-Host "=== CRUD Sales - Docker runner ===" -ForegroundColor Cyan
Write-Host "This script creates .env for Gmail SMTP, then starts MySQL, Redis, RabbitMQ, Backend, and Frontend." -ForegroundColor Cyan
Write-Host ""

$defaultEmail = "your-email@gmail.com"
$emailInput = Read-Host "Gmail username [$defaultEmail]"
if ([string]::IsNullOrWhiteSpace($emailInput)) {
    $emailInput = $defaultEmail
}

$appPassword = Read-Host "Gmail App Password (16 chars; spaces will be removed)"
$appPassword = ($appPassword -replace "\s", "")

if ([string]::IsNullOrWhiteSpace($appPassword)) {
    throw "GMAIL_APP_PASSWORD is required. Create a Google App Password, then run this script again."
}

$envContent = @"
GMAIL_USERNAME=$emailInput
GMAIL_APP_PASSWORD=$appPassword
APP_FRONTEND_RESET_PASSWORD_URL=http://localhost:4200/reset-password
APP_FRONTEND_ORDERS_URL=http://localhost:4200/orders
BACKEND_PORT=8080
FRONTEND_PORT=4200
MYSQL_PORT=3307
REDIS_PORT=6379
RABBITMQ_PORT=5672
RABBITMQ_MANAGEMENT_PORT=15672
MYSQL_ROOT_PASSWORD=123456
RABBITMQ_DEFAULT_USER=guest
RABBITMQ_DEFAULT_PASS=guest
APP_CORS_ALLOWED_ORIGINS=http://localhost:4200
"@

Set-Content -Path ".env" -Value $envContent -Encoding UTF8
Write-Host "Created .env" -ForegroundColor Green

Write-Host "Starting containers..." -ForegroundColor Cyan
docker compose down
docker compose up -d --build

Write-Host ""
Write-Host "Containers started." -ForegroundColor Green
Write-Host "Frontend: http://localhost:4200" -ForegroundColor Yellow
Write-Host "Backend:  http://localhost:8080" -ForegroundColor Yellow
Write-Host "RabbitMQ: http://localhost:15672  (guest / guest)" -ForegroundColor Yellow
Write-Host ""
Write-Host "To verify Gmail variables inside backend:" -ForegroundColor Cyan
Write-Host "docker exec sale_backend printenv | Select-String GMAIL" -ForegroundColor White
Write-Host ""
Write-Host "To watch backend logs while testing forgot password:" -ForegroundColor Cyan
Write-Host "docker logs -f sale_backend" -ForegroundColor White
