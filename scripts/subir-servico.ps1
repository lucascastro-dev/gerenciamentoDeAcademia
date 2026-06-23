# Sobe um servico do compose (postgres | backend | frontend | tunnel | todos).
param(
    [Parameter(Mandatory)]
    [ValidateSet('postgres', 'backend', 'frontend', 'tunnel', 'todos')]
    [string]$Servico,

    [switch]$Build,
    [switch]$Pull,
    [switch]$Quiet
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$env:DOCKER_BUILDKIT = '1'
$env:COMPOSE_DOCKER_CLI_BUILD = '1'

function Write-Step([string]$Msg) {
    Write-Host $Msg -ForegroundColor Cyan
}

function Test-DockerRunning {
    docker info 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host '[ERRO] Docker nao esta rodando. Abra o Docker Desktop.' -ForegroundColor Red
        exit 1
    }
}

function Ensure-EnvFile {
    if (-not (Test-Path '.env')) {
        Copy-Item '.env.example' '.env'
        Write-Host '[OK] Arquivo .env criado a partir de .env.example.' -ForegroundColor Yellow
    }
}

function Ensure-Ports {
    if (-not (Get-Command python -ErrorAction SilentlyContinue)) { return }
    python "$PSScriptRoot\configurar-portas.py"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

function Pull-Images {
    param([string[]]$ServicosLista)
    if (-not (Get-Command powershell -ErrorAction SilentlyContinue)) { return }
    & powershell -NoProfile -ExecutionPolicy Bypass -File "$PSScriptRoot\baixar-imagens-docker.ps1" -Servicos $ServicosLista
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

function Test-ContainerRunning([string]$Name) {
    $state = docker inspect --format '{{.State.Running}}' $Name 2>$null
    return $state -eq 'true'
}

function Ensure-PostgresReady {
    if ((Test-ContainerRunning 'academia-postgres')) {
        $health = docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{end}}' 'academia-postgres' 2>$null
        if ($health -eq 'healthy') { return }
    }
    Write-Host 'Postgres nao esta pronto; subindo postgres primeiro...' -ForegroundColor Yellow
    Start-ComposeService 'postgres' -DoBuild:$false
    & "$PSScriptRoot\aguardar-servico.ps1" -Servico postgres -TimeoutSec 90
}

function Start-ComposeService {
    param(
        [string]$Name,
        [bool]$DoBuild
    )

    if ($DoBuild) {
        Write-Step "Build $Name..."
        & docker compose build $Name
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[ERRO] Falha no build de $Name" -ForegroundColor Red
            exit 1
        }
    }

    Write-Step "Subindo $Name..."
    & docker compose up -d $Name
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERRO] Falha ao subir $Name" -ForegroundColor Red
        docker compose logs --tail 40 $Name
        exit 1
    }

    # Workaround Windows: containers as vezes ficam em Created
    switch ($Name) {
        'backend' { docker start academia-backend 2>$null | Out-Null }
        'frontend' { docker start academia-frontend 2>$null | Out-Null }
        'tunnel' { docker start academia-tunnel 2>$null | Out-Null }
    }
}

function Show-ServiceInfo([string]$Name) {
    switch ($Name) {
        'postgres' {
            $port = '5435'
            if (Test-Path '.env') {
                $line = Get-Content '.env' | Where-Object { $_ -match '^\s*POSTGRES_HOST_PORT\s*=' } | Select-Object -First 1
                if ($line -match '=\s*(\d+)') { $port = $Matches[1] }
            }
            Write-Host "  Postgres: localhost:$port" -ForegroundColor Green
        }
        'backend' {
            Write-Host '  Backend:  http://127.0.0.1:8000/srv-gerenciaracademia' -ForegroundColor Green
            Write-Host '  Logs:     docker compose logs -f backend' -ForegroundColor DarkGray
        }
        'frontend' {
            $port = 5173
            if (Test-Path '.env') {
                $line = Get-Content '.env' | Where-Object { $_ -match '^\s*APP_PORT\s*=' } | Select-Object -First 1
                if ($line -match '=\s*(\d+)') { $port = $Matches[1] }
            }
            Write-Host "  Frontend: http://localhost:$port" -ForegroundColor Green
        }
        'tunnel' {
            Write-Host '  URL publica: URL_PUBLICA.txt ou docker compose logs tunnel --tail 20' -ForegroundColor Green
        }
    }
}

function Invoke-OneService {
    param(
        [string]$Name,
        [bool]$DoBuild,
        [bool]$DoPull,
        [bool]$DoBackupCheck = $false
    )

    if ($DoPull) {
        Pull-Images @($Name)
    }

    if ($Name -eq 'backend') { Ensure-PostgresReady }
    if ($Name -eq 'frontend') {
        if (-not (Test-ContainerRunning 'academia-backend')) {
            Write-Host '[AVISO] Backend nao esta rodando; API pode falhar ate subir o backend.' -ForegroundColor Yellow
        }
    }
    if ($Name -eq 'tunnel') {
        if (-not (Test-ContainerRunning 'academia-frontend')) {
            Write-Host '[AVISO] Frontend nao esta rodando; suba o frontend antes do tunel.' -ForegroundColor Yellow
        }
    }

    if ($Name -eq 'postgres' -and $DoBackupCheck) {
        & "$PSScriptRoot\backup-postgres-hooks.ps1" -Acao 'pre-restart' -Quiet:$Quiet
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }

    Start-ComposeService $Name -DoBuild:$DoBuild
    & "$PSScriptRoot\aguardar-servico.ps1" -Servico $Name -TimeoutSec $(if ($Name -eq 'backend') { 180 } else { 120 })

    if ($Name -eq 'tunnel' -and (Get-Command python -ErrorAction SilentlyContinue)) {
        python "$PSScriptRoot\aguardar-url-publica.py"
    }

    Show-ServiceInfo $Name
}

Test-DockerRunning
Ensure-EnvFile

$verificarBackup = ($Servico -eq 'todos')
if ($verificarBackup) {
    & "$PSScriptRoot\backup-postgres-hooks.ps1" -Acao 'semanal-check' -Quiet:$Quiet
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

if ($Servico -eq 'todos') {
    Ensure-Ports
    if ($Pull) { Pull-Images @('postgres', 'backend', 'frontend', 'tunnel') }
    foreach ($s in @('postgres', 'backend', 'frontend', 'tunnel')) {
        Write-Host ''
        Invoke-OneService -Name $s -DoBuild:$Build -DoPull:$false -DoBackupCheck:$verificarBackup
    }
    Write-Host ''
    Write-Host 'Stack completa no ar.' -ForegroundColor Green
} else {
    if ($Servico -eq 'postgres') { Ensure-Ports }
    Invoke-OneService -Name $Servico -DoBuild:$Build -DoPull:$Pull
}

if (-not $Quiet) {
    Write-Host ''
    pause
}
