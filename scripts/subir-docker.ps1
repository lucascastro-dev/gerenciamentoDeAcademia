# Sobe Docker + tunel publico e exibe URLs (local e internet).
param(
    [switch]$SemTunel,
    [switch]$Demo
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host "Arquivo .env criado. Revise senhas antes de expor na internet." -ForegroundColor Yellow
}

if (Get-Command python -ErrorAction SilentlyContinue) {
    python "$PSScriptRoot\configurar-portas.py"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

$port = "5173"
if (Test-Path ".env") {
    $line = Get-Content ".env" | Where-Object { $_ -match '^\s*APP_PORT\s*=' } | Select-Object -First 1
    if ($line -match '=\s*(\d+)') { $port = $Matches[1] }
}

$composeArgs = @("compose")
if ($Demo) {
    $composeArgs += "-f", "docker-compose.yml", "-f", "docker-compose.demo.yml"
}
$composeArgs += @("up", "-d", "--build")

Write-Host "Subindo containers..." -ForegroundColor Cyan
if ($SemTunel) {
    docker compose up -d --build postgres backend frontend
} else {
    & docker @composeArgs
}

Start-Sleep -Seconds 15
docker compose ps

if (-not $SemTunel) {
    if (Get-Command python -ErrorAction SilentlyContinue) {
        python "$PSScriptRoot\aguardar-url-publica.py"
    } else {
        Write-Host "Obtenha URL publica: docker compose logs tunnel" -ForegroundColor Yellow
    }
}

$ipv4 = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
    Where-Object { $_.IPAddress -notlike '127.*' -and $_.PrefixOrigin -ne 'WellKnown' } |
    Select-Object -First 1 -ExpandProperty IPAddress

Write-Host ""
Write-Host "=== URLs ===" -ForegroundColor Green
Write-Host "  Local:          http://localhost:$port"
if ($ipv4) { Write-Host "  Rede (opcional): http://${ipv4}:$port" }
Write-Host "  Credenciais:    docs/USUARIOS_TESTE.md"
Write-Host "  Guia completo:  PASSO_A_PASSO_DEPLOY.txt"
