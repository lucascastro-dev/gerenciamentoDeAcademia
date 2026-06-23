# Hooks de backup: pre-restart do Postgres e verificacao semanal.
param(
    [ValidateSet('pre-restart', 'semanal-check')]
    [Parameter(Mandatory)]
    [string]$Acao,

    [switch]$Quiet
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
$Container = 'academia-postgres'

function Test-ContainerExists([string]$Name) {
    docker inspect $Name 2>$null | Out-Null
    return $LASTEXITCODE -eq 0
}

function Test-ContainerRunning([string]$Name) {
    $state = docker inspect --format '{{.State.Running}}' $Name 2>$null
    return $state -eq 'true'
}

$backupScript = Join-Path $PSScriptRoot 'backup-postgres.ps1'
$commonArgs = @()
if ($Quiet) { $commonArgs += '-Quiet' }

switch ($Acao) {
    'pre-restart' {
        if (-not (Test-ContainerExists $Container)) { exit 0 }
        if (-not (Test-ContainerRunning $Container)) {
            if (-not $Quiet) {
                Write-Host 'Postgres parado; iniciando temporariamente para backup...' -ForegroundColor DarkGray
            }
            docker start $Container 2>$null | Out-Null
            Start-Sleep -Seconds 4
        }
        if (-not $Quiet) {
            Write-Host ''
            Write-Host 'Backup de seguranca antes de reiniciar o Postgres...' -ForegroundColor Cyan
        }
        & $backupScript -Motivo 'pre-restart' @commonArgs
        exit $LASTEXITCODE
    }
    'semanal-check' {
        if (-not (Test-ContainerRunning $Container)) { exit 0 }
        $weeklyDays = 7
        if (Test-Path (Join-Path $Root '.env')) {
            $line = Get-Content (Join-Path $Root '.env') | Where-Object { $_ -match '^\s*BACKUP_WEEKLY_DAYS\s*=' } | Select-Object -First 1
            if ($line -match '=\s*(\d+)') { $weeklyDays = [int]$Matches[1] }
        }
        $backupDir = Join-Path $Root 'backups\postgres'
        $due = $true
        if (Test-Path $backupDir) {
            $latest = Get-ChildItem $backupDir -File -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -match '\.(sql|sql\.gz)$' } |
                Sort-Object LastWriteTime -Descending |
                Select-Object -First 1
            if ($null -ne $latest) {
                $due = ((Get-Date) - $latest.LastWriteTime).TotalDays -ge $weeklyDays
            }
        }
        if (-not $due) { exit 0 }
        if (-not $Quiet) {
            Write-Host ''
            Write-Host "Backup semanal (ultimo backup ha mais de $weeklyDays dias)..." -ForegroundColor Cyan
        }
        & $backupScript -Motivo 'semanal' @commonArgs
        exit $LASTEXITCODE
    }
}
