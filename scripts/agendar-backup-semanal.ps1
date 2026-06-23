# Registra tarefa no Agendador de Tarefas do Windows (backup semanal do Postgres).
param(
    [ValidateSet('Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')]
    [string]$DiaSemana = 'Sunday',

    [string]$Hora = '03:00'
)

$ErrorActionPreference = 'Stop'
$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$TaskName = 'EduGestao-BackupPostgresSemanal'
$ScriptPath = Join-Path $Root 'scripts\backup-postgres.ps1'

if (-not (Test-Path $ScriptPath)) {
    Write-Host "[ERRO] Script nao encontrado: $ScriptPath" -ForegroundColor Red
    exit 1
}

$action = New-ScheduledTaskAction `
    -Execute 'powershell.exe' `
    -Argument "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File `"$ScriptPath`" -Motivo semanal -Quiet"

$trigger = New-ScheduledTaskTrigger -Weekly -DaysOfWeek $DiaSemana -At $Hora
$settings = New-ScheduledTaskSettingsSet -StartWhenAvailable -DontStopOnIdleEnd -AllowStartIfOnBatteries

$existing = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if ($existing) {
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
}

Register-ScheduledTask `
    -TaskName $TaskName `
    -Action $action `
    -Trigger $trigger `
    -Settings $settings `
    -Description 'Backup semanal do PostgreSQL (EduGestao Inteligente)' `
    -RunLevel Highest | Out-Null

Write-Host ''
Write-Host '[OK] Tarefa agendada registrada:' -ForegroundColor Green
Write-Host "  Nome:   $TaskName"
Write-Host "  Quando: todo $($DiaSemana) as $Hora"
Write-Host "  Script: $ScriptPath"
Write-Host ''
Write-Host 'Requisitos: Docker Desktop rodando no horario do backup.'
Write-Host 'Teste manual: backup-banco.bat'
Write-Host ''
Write-Host 'Remover agendamento:'
Write-Host "  Unregister-ScheduledTask -TaskName '$TaskName' -Confirm:`$false"
