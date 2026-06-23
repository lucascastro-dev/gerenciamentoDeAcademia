# Backup do PostgreSQL (container academia-postgres) via pg_dump.
param(
    [ValidateSet('manual', 'pre-restart', 'semanal', 'automatico')]
    [string]$Motivo = 'manual',

    [switch]$Forcar,
    [switch]$Quiet
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$Container = 'academia-postgres'
$DbName = 'gerenciamento_academia'
$DbUser = 'academia_app'
$BackupDir = Join-Path $Root 'backups\postgres'
$Retention = 14
$WeeklyDays = 7

function Read-EnvValue([string]$Key, [string]$Default) {
    if (-not (Test-Path '.env')) { return $Default }
    $line = Get-Content '.env' | Where-Object { $_ -match "^\s*$Key\s*=" } | Select-Object -First 1
    if ($line -match '=\s*(.+)$') {
        return $Matches[1].Trim().Trim('"').Trim("'")
    }
    return $Default
}

function Test-DockerRunning {
    docker info 2>$null | Out-Null
    return $LASTEXITCODE -eq 0
}

function Test-ContainerRunning([string]$Name) {
    $state = docker inspect --format '{{.State.Running}}' $Name 2>$null
    return $state -eq 'true'
}

function Test-ContainerExists([string]$Name) {
    docker inspect $Name 2>$null | Out-Null
    return $LASTEXITCODE -eq 0
}

function Get-LatestBackupTime {
    if (-not (Test-Path $BackupDir)) { return $null }
    $latest = Get-ChildItem $BackupDir -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match '\.(sql|sql\.gz)$' -and $_.Name -notmatch '^\.+' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $latest) { return $null }
    return $latest.LastWriteTime
}

function Test-WeeklyBackupDue {
    param([int]$Days)
    if ($Forcar) { return $true }
    $latest = Get-LatestBackupTime
    if ($null -eq $latest) { return $true }
    return ((Get-Date) - $latest).TotalDays -ge $Days
}

function Remove-OldBackups {
    param([int]$Keep)
    if (-not (Test-Path $BackupDir)) { return }
    $files = Get-ChildItem $BackupDir -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match '\.(sql|sql\.gz)$' } |
        Sort-Object LastWriteTime -Descending
    if ($files.Count -le $Keep) { return }
    $files | Select-Object -Skip $Keep | ForEach-Object {
        Remove-Item $_.FullName -Force
        if (-not $Quiet) {
            Write-Host "  Removido backup antigo: $($_.Name)" -ForegroundColor DarkGray
        }
    }
}

function Invoke-PostgresBackup {
    if (-not (Test-DockerRunning)) {
        Write-Host '[ERRO] Docker nao esta rodando.' -ForegroundColor Red
        exit 1
    }
    if (-not (Test-ContainerRunning $Container)) {
        Write-Host '[AVISO] Postgres nao esta em execucao; backup ignorado.' -ForegroundColor Yellow
        exit 0
    }

    $health = docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{end}}' $Container 2>$null
    if ($health -and $health -ne 'healthy') {
        Write-Host '[AVISO] Postgres ainda nao esta healthy; aguardando...' -ForegroundColor Yellow
        $deadline = (Get-Date).AddSeconds(30)
        while ((Get-Date) -lt $deadline) {
            Start-Sleep -Seconds 2
            $health = docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{end}}' $Container 2>$null
            if ($health -eq 'healthy') { break }
        }
    }

    New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null

    $stamp = Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'
    $fileName = "${DbName}_${stamp}_${Motivo}.sql"
    $tmpInContainer = "/tmp/$fileName"
    $destPath = Join-Path $BackupDir $fileName

    if (-not $Quiet) {
        Write-Host "Gerando backup ($Motivo)..." -ForegroundColor Cyan
    }

    docker exec $Container pg_dump -U $DbUser -d $DbName --no-owner --no-acl -F p -f $tmpInContainer
    if ($LASTEXITCODE -ne 0) {
        Write-Host '[ERRO] pg_dump falhou.' -ForegroundColor Red
        exit 1
    }

    docker cp "${Container}:${tmpInContainer}" $destPath
    if ($LASTEXITCODE -ne 0) {
        Write-Host '[ERRO] Falha ao copiar dump do container.' -ForegroundColor Red
        exit 1
    }
    docker exec $Container rm -f $tmpInContainer 2>$null | Out-Null

    $gzPath = "$destPath.gz"
    try {
        $inputStream = [System.IO.File]::OpenRead($destPath)
        $outputStream = [System.IO.File]::Create($gzPath)
        $gzip = New-Object System.IO.Compression.GZipStream($outputStream, [System.IO.Compression.CompressionLevel]::Optimal)
        $inputStream.CopyTo($gzip)
        $gzip.Dispose()
        $outputStream.Dispose()
        $inputStream.Dispose()
        Remove-Item $destPath -Force
        $destPath = $gzPath
    } catch {
        Write-Host '[AVISO] Nao foi possivel comprimir; mantendo .sql' -ForegroundColor Yellow
    }

    $meta = @{
        data = (Get-Date).ToString('o')
        motivo = $Motivo
        arquivo = Split-Path $destPath -Leaf
        tamanhoBytes = (Get-Item $destPath).Length
    } | ConvertTo-Json -Compress
    Set-Content -Path (Join-Path $BackupDir '.ultimo-backup.json') -Value $meta -Encoding UTF8

    Remove-OldBackups -Keep $Retention

    if (-not $Quiet) {
        $sizeMb = [math]::Round((Get-Item $destPath).Length / 1MB, 2)
        Write-Host "  [OK] Backup salvo: $destPath ($sizeMb MB)" -ForegroundColor Green
    }
}

$Retention = [int](Read-EnvValue 'BACKUP_RETENTION' '14')
$WeeklyDays = [int](Read-EnvValue 'BACKUP_WEEKLY_DAYS' '7')

Invoke-PostgresBackup
