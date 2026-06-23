# Restaura backup .sql ou .sql.gz no Postgres (CUIDADO: sobrescreve dados atuais).
param(
    [string]$Arquivo,
    [switch]$Confirmar
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$Container = 'academia-postgres'
$DbName = 'gerenciamento_academia'
$DbUser = 'academia_app'
$BackupDir = Join-Path $Root 'backups\postgres'

if (-not $Arquivo) {
    if (-not (Test-Path $BackupDir)) {
        Write-Host '[ERRO] Nenhum backup encontrado em backups/postgres' -ForegroundColor Red
        exit 1
    }
    $latest = Get-ChildItem $BackupDir -File |
        Where-Object { $_.Name -match '\.(sql|sql\.gz)$' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $latest) {
        Write-Host '[ERRO] Nenhum arquivo .sql/.sql.gz em backups/postgres' -ForegroundColor Red
        exit 1
    }
    $Arquivo = $latest.FullName
}

if (-not (Test-Path $Arquivo)) {
    Write-Host "[ERRO] Arquivo nao encontrado: $Arquivo" -ForegroundColor Red
    exit 1
}

docker inspect $Container 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host '[ERRO] Container academia-postgres nao existe. Suba o Postgres primeiro.' -ForegroundColor Red
    exit 1
}

$running = docker inspect --format '{{.State.Running}}' $Container 2>$null
if ($running -ne 'true') {
    Write-Host 'Iniciando Postgres para restauracao...' -ForegroundColor Yellow
    docker start $Container | Out-Null
    Start-Sleep -Seconds 5
}

Write-Host ''
Write-Host 'ATENCAO: isso substitui TODOS os dados atuais do banco!' -ForegroundColor Red
Write-Host "Arquivo: $Arquivo"
Write-Host ''

if (-not $Confirmar) {
    $resp = Read-Host 'Digite SIM para confirmar a restauracao'
    if ($resp -ne 'SIM') {
        Write-Host 'Restauracao cancelada.'
        exit 0
    }
}

$sqlPath = $Arquivo
$tempSql = $null
if ($Arquivo -match '\.gz$') {
    $tempSql = Join-Path $env:TEMP "restore_$([guid]::NewGuid().ToString('N')).sql"
    $inputStream = [System.IO.File]::OpenRead($Arquivo)
    $gzip = New-Object System.IO.Compression.GZipStream($inputStream, [System.IO.Compression.CompressionMode]::Decompress)
    $outputStream = [System.IO.File]::Create($tempSql)
    $gzip.CopyTo($outputStream)
    $gzip.Dispose()
    $outputStream.Dispose()
    $inputStream.Dispose()
    $sqlPath = $tempSql
}

Write-Host 'Encerrando conexoes e recriando banco...' -ForegroundColor Cyan
docker exec $Container psql -U $DbUser -d postgres -v ON_ERROR_STOP=1 -c `
    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$DbName' AND pid <> pg_backend_pid();" 2>$null | Out-Null
docker exec $Container psql -U $DbUser -d postgres -v ON_ERROR_STOP=1 -c "DROP DATABASE IF EXISTS $DbName;"
docker exec $Container psql -U $DbUser -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $DbName OWNER $DbUser;"

Write-Host 'Importando dump...' -ForegroundColor Cyan
Get-Content -Path $sqlPath -Raw -Encoding UTF8 | docker exec -i $Container psql -U $DbUser -d $DbName -v ON_ERROR_STOP=1 -q
if ($LASTEXITCODE -ne 0) {
    if ($tempSql) { Remove-Item $tempSql -Force -ErrorAction SilentlyContinue }
    Write-Host '[ERRO] Restauracao falhou.' -ForegroundColor Red
    exit 1
}

if ($tempSql) { Remove-Item $tempSql -Force -ErrorAction SilentlyContinue }

Write-Host '[OK] Banco restaurado com sucesso.' -ForegroundColor Green
Write-Host 'Reinicie o backend: subir-backend.bat' -ForegroundColor Yellow
