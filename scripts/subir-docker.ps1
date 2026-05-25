# Sobe a aplicacao com Docker Compose e exibe URLs para testadores na rede.
param(
    [switch]$Demo
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host "Arquivo .env criado a partir de .env.example. Revise senhas antes de expor na rede." -ForegroundColor Yellow
}

$port = "5173"
if (Test-Path ".env") {
    $line = Get-Content ".env" | Where-Object { $_ -match '^\s*APP_PORT\s*=' } | Select-Object -First 1
    if ($line -match '=\s*(\d+)') { $port = $Matches[1] }
}

if ($Demo) {
    Write-Host "Modo demo: apenas porta $port (API via proxy nginx)." -ForegroundColor Cyan
    docker compose -f docker-compose.yml -f docker-compose.demo.yml up -d --build
} else {
    docker compose up -d --build
}

Write-Host ""
Write-Host "Aguardando containers..." -ForegroundColor Gray
Start-Sleep -Seconds 5
docker compose ps

$ipv4 = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
    Where-Object { $_.IPAddress -notlike '127.*' -and $_.PrefixOrigin -ne 'WellKnown' } |
    Select-Object -First 1 -ExpandProperty IPAddress

Write-Host ""
Write-Host "=== URLs ===" -ForegroundColor Green
Write-Host "  Nesta maquina:  http://localhost:$port"
if ($ipv4) {
    Write-Host "  Rede local:     http://${ipv4}:$port  (compartilhe com testadores)"
} else {
    Write-Host "  Rede local:     use 'ipconfig' para ver o IPv4" -ForegroundColor Yellow
}
Write-Host "  Credenciais:    docs/USUARIOS_TESTE.md"
Write-Host ""
Write-Host "Logs: docker compose logs -f backend"
