# Aguarda servico ficar pronto (substitui sleep fixo de 15s).
param(
    [Parameter(Mandatory)]
    [ValidateSet('postgres', 'backend', 'frontend', 'tunnel')]
    [string]$Servico,

    [int]$TimeoutSec = 120
)

$ErrorActionPreference = 'SilentlyContinue'

function Get-AppPort {
    $port = 5173
    if (Test-Path '.env') {
        $line = Get-Content '.env' | Where-Object { $_ -match '^\s*APP_PORT\s*=' } | Select-Object -First 1
        if ($line -match '=\s*(\d+)') { $port = [int]$Matches[1] }
    }
    return $port
}

function Wait-ContainerHealthy {
    param([string]$Container, [int]$Timeout)
    $deadline = (Get-Date).AddSeconds($Timeout)
    while ((Get-Date) -lt $deadline) {
        $running = docker inspect --format '{{.State.Running}}' $Container 2>$null
        if ($running -ne 'true') {
            Start-Sleep -Seconds 2
            continue
        }
        $health = docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{end}}' $Container 2>$null
        if ([string]::IsNullOrWhiteSpace($health) -or $health -eq 'healthy') {
            return $true
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

function Wait-HttpOk {
    param([string]$Url, [int]$Timeout)
    $deadline = (Get-Date).AddSeconds($Timeout)
    while ((Get-Date) -lt $deadline) {
        try {
            $r = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3
            if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { return $true }
        } catch {
            # ainda subindo
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

Write-Host "Aguardando $Servico..." -ForegroundColor DarkGray

switch ($Servico) {
    'postgres' {
        if (Wait-ContainerHealthy 'academia-postgres' $TimeoutSec) {
            Write-Host "  [OK] Postgres pronto." -ForegroundColor Green
            exit 0
        }
    }
    'backend' {
        if (-not (Wait-ContainerHealthy 'academia-postgres' 30)) {
            Write-Host "  [AVISO] Postgres ainda nao saudavel; tentando backend mesmo assim." -ForegroundColor Yellow
        }
        if (Wait-HttpOk 'http://127.0.0.1:8000/srv-gerenciaracademia/actuator/health' $TimeoutSec) {
            Write-Host "  [OK] Backend pronto." -ForegroundColor Green
            exit 0
        }
    }
    'frontend' {
        $port = Get-AppPort
        if (Wait-HttpOk "http://127.0.0.1:$port/" ([Math]::Min($TimeoutSec, 60))) {
            Write-Host "  [OK] Frontend pronto (porta $port)." -ForegroundColor Green
            exit 0
        }
    }
    'tunnel' {
        $running = docker inspect --format '{{.State.Running}}' 'academia-tunnel' 2>$null
        if ($running -eq 'true') {
            Write-Host "  [OK] Container do tunel em execucao." -ForegroundColor Green
            exit 0
        }
    }
}

Write-Host "  [AVISO] Timeout aguardando $Servico (${TimeoutSec}s)." -ForegroundColor Yellow
exit 1
