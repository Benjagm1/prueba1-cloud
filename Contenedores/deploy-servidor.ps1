# deploy-servidor.ps1 - Despliegue remoto via VPN WireGuard + SSH
# Uso:
#   cd "D:\fULLSTSACK 3\Nuevo 13 de Mayo\H\Contenedores"
#   .\deploy-servidor.ps1
#   .\deploy-servidor.ps1 -DeployMethod local
#
# La contrasena SSH se pide de forma interactiva (no se guarda en este script).

[CmdletBinding()]
param(
    [string] $ServerHost = "192.168.1.111",
    [string] $RemoteUser = "simetria",
    [string] $RemoteBase = "~/Duoc-fullStack",
    [string] $RemoteRepoPath = "H",
    [string] $GitRepoUrl = "https://github.com/ctorresp/H.git",
    [ValidateSet("git", "local")]
    [string] $DeployMethod = "git",
    [switch] $SkipDockerBuild,
    [int] $ConnectTimeoutSec = 8
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$LocalProjectRoot = Resolve-Path (Join-Path $ScriptDir "..")

function Write-Step([string] $Message) {
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Test-ServerConnectivity {
    param([string] $HostName)
    $pingOk = $false
    try { $pingOk = Test-Connection -ComputerName $HostName -Count 1 -Quiet -ErrorAction SilentlyContinue } catch { }
    $sshOk = $false
    try {
        $tn = Test-NetConnection -ComputerName $HostName -Port 22 -WarningAction SilentlyContinue
        $sshOk = [bool]$tn.TcpTestSucceeded
    } catch { }
    [PSCustomObject]@{ Ping = $pingOk; SshPort22 = $sshOk }
}

function Invoke-Remote([string] $RemoteCommand) {
    $target = "{0}@{1}" -f $RemoteUser, $ServerHost
    $oneLine = ($RemoteCommand -replace "[\r\n]+", " " -replace "\s+", " ").Trim()
    $escaped = $oneLine -replace "'", "'\''"
    Write-Host "ssh $target" -ForegroundColor DarkGray
    Write-Host "  $oneLine" -ForegroundColor DarkGray
    & ssh -o ConnectTimeout=$ConnectTimeoutSec -o StrictHostKeyChecking=accept-new $target "bash -lc '$escaped'"
    if ($LASTEXITCODE -ne 0) {
        throw "Comando SSH fallo (codigo $LASTEXITCODE)."
    }
}

Write-Step "Comprobando conectividad con $ServerHost (VPN WireGuard)"
$conn = Test-ServerConnectivity -HostName $ServerHost
Write-Host ("  Ping: {0}  |  Puerto SSH 22: {1}" -f $conn.Ping, $conn.SshPort22)

if (-not $conn.SshPort22) {
    Write-Host @"

No hay acceso SSH al servidor. Activa el tunel WireGuard antes de continuar:

  1. Instala WireGuard para Windows: https://www.wireguard.com/install/
  2. WireGuard -> Import tunnel from file
  3. Archivo: C:\Users\Mao\wireguard-simetria.conf
  4. Activate / Activar
  5. Verifica: Test-NetConnection 192.168.1.111 -Port 22

"@ -ForegroundColor Yellow
    exit 2
}

$remoteFull = "$RemoteBase/$RemoteRepoPath"
$remoteContenedores = "$remoteFull/Contenedores"

Write-Step "Preparando directorio remoto $remoteFull"
Invoke-Remote "mkdir -p $RemoteBase"

if ($DeployMethod -eq "git") {
    Write-Step "Sincronizando codigo con git en el servidor ($GitRepoUrl)"
    $gitCmd = "if [ -d '$remoteFull/.git' ]; then cd '$remoteFull' && git fetch origin && git checkout main && git pull --ff-only origin main; else rm -rf '$remoteFull' 2>/dev/null || true; git clone '$GitRepoUrl' '$remoteFull'; fi"
    Invoke-Remote $gitCmd
} else {
    Write-Step "Subiendo proyecto local (sin .git) con scp"
    $stagingRoot = Join-Path $env:TEMP ("H-deploy-{0}" -f [guid]::NewGuid().ToString("N"))
    $staging = Join-Path $stagingRoot $RemoteRepoPath
    New-Item -ItemType Directory -Path $staging -Force | Out-Null
    try {
        & robocopy $LocalProjectRoot $staging /MIR /XD .git node_modules dist target .angular /NFL /NDL /NJH /NJS /nc /ns /np | Out-Null
        if ($LASTEXITCODE -ge 8) { throw "robocopy fallo con codigo $LASTEXITCODE" }
        $scpTarget = "{0}@{1}:{2}" -f $RemoteUser, $ServerHost, $RemoteBase
        & scp -o ConnectTimeout=$ConnectTimeoutSec -o StrictHostKeyChecking=accept-new -r $stagingRoot "${scpTarget}/"
        if ($LASTEXITCODE -ne 0) { throw "scp fallo (codigo $LASTEXITCODE)." }
    } finally {
        Remove-Item -Recurse -Force $stagingRoot -ErrorAction SilentlyContinue
    }
}

Write-Step "Archivo .env en Contenedores (si falta)"
Invoke-Remote "cd '$remoteContenedores' && if [ ! -f .env ] && [ -f .env.example ]; then cp .env.example .env; fi"

Write-Step "Docker Compose en el servidor"
$composeFlags = if ($SkipDockerBuild) { "up -d" } else { "up -d --build" }
Invoke-Remote "cd '$remoteContenedores' && docker compose $composeFlags"

Write-Step "Nota: acceso al frontend"
Write-Host @"
El compose publica el frontend en 127.0.0.1:8050 (solo localhost en el servidor).
Para acceso remoto en la LAN/VPN, en el servidor cambia en docker-compose.yml:

  - "127.0.0.1:${FRONTEND_PORT:-8050}:80"
  por
  - "0.0.0.0:${FRONTEND_PORT:-8050}:80"

Luego: cd ~/Duoc-fullStack/H/Contenedores && docker compose up -d
URL desde tu PC (con VPN): http://192.168.1.111:8050
"@ -ForegroundColor Green

Write-Host ""
Write-Host "Despliegue remoto finalizado." -ForegroundColor Green
