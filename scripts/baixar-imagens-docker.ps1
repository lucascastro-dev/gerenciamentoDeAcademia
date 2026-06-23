# Baixa imagens base (ECR publico primeiro; Docker Hub como fallback)
param(
    [ValidateSet('postgres', 'backend', 'frontend', 'tunnel', 'all')]
    [string[]]$Servicos = @('all')
)

$ErrorActionPreference = "Continue"

$mapaServicos = @{
    postgres = @('postgres:16-alpine')
    backend  = @('eclipse-temurin:17-jdk-alpine', 'eclipse-temurin:17-jre-alpine')
    frontend = @('node:20-alpine', 'nginx:alpine')
    tunnel   = @('cloudflare/cloudflared:latest')
}

# Nome local esperado pelo compose/build -> fontes em ordem de tentativa
$imagens = @(
    @{
        Nome = "node:20-alpine"
        Fontes = @(
            "public.ecr.aws/docker/library/node:20-alpine",
            "node:20-alpine"
        )
    },
    @{
        Nome = "nginx:alpine"
        Fontes = @(
            "public.ecr.aws/docker/library/nginx:alpine",
            "nginx:alpine"
        )
    },
    @{
        Nome = "postgres:16-alpine"
        Fontes = @(
            "public.ecr.aws/docker/library/postgres:16-alpine",
            "postgres:16-alpine"
        )
    },
    @{
        Nome = "eclipse-temurin:17-jdk-alpine"
        Fontes = @(
            "public.ecr.aws/docker/library/eclipse-temurin:17-jdk-alpine",
            "eclipse-temurin:17-jdk-alpine"
        )
    },
    @{
        Nome = "eclipse-temurin:17-jre-alpine"
        Fontes = @(
            "public.ecr.aws/docker/library/eclipse-temurin:17-jre-alpine",
            "eclipse-temurin:17-jre-alpine"
        )
    },
    @{
        Nome = "cloudflare/cloudflared:latest"
        Fontes = @(
            "cloudflare/cloudflared:latest"
        )
    }
)

$alvo = New-Object 'System.Collections.Generic.HashSet[string]'
if ($Servicos -contains 'all') {
    foreach ($k in $mapaServicos.Keys) {
        foreach ($n in $mapaServicos[$k]) { [void]$alvo.Add($n) }
    }
} else {
    foreach ($s in $Servicos) {
        foreach ($n in $mapaServicos[$s]) { [void]$alvo.Add($n) }
    }
}

$imagens = $imagens | Where-Object { $alvo.Contains($_.Nome) }

$tentativasMax = 2
$pausaSegundos = 5
$falhas = @()

function Test-ImagemLocal {
    param([string]$Ref)
    docker image inspect $Ref 2>$null | Out-Null
    return $LASTEXITCODE -eq 0
}

function Pull-UmaFonte {
    param([string]$Fonte)
    $null = docker pull $Fonte 2>&1
    return $LASTEXITCODE -eq 0
}

Write-Host ""
Write-Host "Baixando imagens base (mirror AWS ECR + cache local)..." -ForegroundColor Cyan
Write-Host "Docker Hub costuma falhar com timeout; usamos ECR publico quando possivel." -ForegroundColor DarkGray
Write-Host ""

foreach ($item in $imagens) {
    $nome = $item.Nome
    if (Test-ImagemLocal $nome) {
        Write-Host "  [OK cache] $nome" -ForegroundColor Green
        continue
    }

    $ok = $false
    foreach ($fonte in $item.Fontes) {
        for ($t = 1; $t -le $tentativasMax; $t++) {
            Write-Host "  [$t/$tentativasMax] $fonte" -ForegroundColor Gray
            if (Pull-UmaFonte $fonte) {
                if ($fonte -ne $nome) {
                    docker tag $fonte $nome 2>$null | Out-Null
                }
                Write-Host "  [OK] $nome" -ForegroundColor Green
                $ok = $true
                break
            }
            if ($t -lt $tentativasMax) {
                Start-Sleep -Seconds $pausaSegundos
            }
        }
        if ($ok) { break }
    }

    if (-not $ok) {
        if (Test-ImagemLocal $nome) {
            Write-Host "  [OK cache apos tentativas] $nome" -ForegroundColor Yellow
            continue
        }
        $falhas += $nome
        Write-Host "  [FALHA] $nome" -ForegroundColor Red
    }
}

Write-Host ""
if ($falhas.Count -gt 0) {
    $criticas = $falhas | Where-Object { $_ -notmatch "cloudflared" }
    if ($criticas.Count -eq 0) {
        Write-Host "[AVISO] Tunel cloudflared nao baixado; app sobe sem URL publica na internet." -ForegroundColor Yellow
        exit 0
    }
    Write-Host "Nao foi possivel obter:" -ForegroundColor Red
    $criticas | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    Write-Host ""
    Write-Host "Teste manual (mirror AWS, costuma funcionar no Brasil):" -ForegroundColor Yellow
    Write-Host "  docker pull public.ecr.aws/docker/library/node:20-alpine"
    Write-Host "  docker tag public.ecr.aws/docker/library/node:20-alpine node:20-alpine"
    Write-Host ""
    Write-Host "Ou use rede movel/VPN e execute subir.bat novamente."
    exit 1
}

Write-Host "Imagens base OK." -ForegroundColor Green
exit 0
