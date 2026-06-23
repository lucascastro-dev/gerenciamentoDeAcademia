# Sobe Docker + tunel publico e exibe URLs (local e internet).
param(
    [switch]$SemTunel,
    [switch]$Demo,
    [switch]$Build
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$servico = if ($SemTunel) { 'todos' } else { 'todos' }
$args = @('-Servico', $servico, '-Pull')
if ($Build) { $args += '-Build' }

if ($SemTunel) {
    foreach ($s in @('postgres', 'backend', 'frontend')) {
        & "$PSScriptRoot\subir-servico.ps1" -Servico $s -Pull:$(if ($s -eq 'postgres') { $true } else { $false }) -Build:$Build -Quiet
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }
} else {
    & "$PSScriptRoot\subir-servico.ps1" @args -Quiet
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

$port = "5173"
if (Test-Path ".env") {
    $line = Get-Content ".env" | Where-Object { $_ -match '^\s*APP_PORT\s*=' } | Select-Object -First 1
    if ($line -match '=\s*(\d+)') { $port = $Matches[1] }
}

$ipv4 = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
    Where-Object { $_.IPAddress -notlike '127.*' -and $_.PrefixOrigin -ne 'WellKnown' } |
    Select-Object -First 1 -ExpandProperty IPAddress

Write-Host ""
Write-Host "=== URLs ===" -ForegroundColor Green
Write-Host "  Local:          http://localhost:$port"
if ($ipv4) { Write-Host "  Rede (opcional): http://${ipv4}:$port" }
Write-Host "  Credenciais:    docs/USUARIOS_TESTE.md"
Write-Host "  Servicos:       subir-postgres.bat | subir-backend.bat | subir-frontend.bat | subir-tunel.bat"
