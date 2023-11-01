$ScriptPath = $MyInvocation.MyCommand.Path
$ScriptDirectory = Split-Path $ScriptPath -Parent
function Get-Latest-Archive {
    $response = Invoke-RestMethod -Uri 'https://api.github.com/repos/all3n/jobdeploy/releases/latest'
    $response.assets.browser_download_url
}

$LocalConfPath = Join-Path $ScriptDirectory 'bin\deploy.local.ps1'
if (Test-Path $LocalConfPath) {
    . $LocalConfPath
}

if (-not $env:JAVA) {
    $env:JAVA = "java"
}
$Java = $env:JAVA
$WgetOptions = $env:WGET_OPTIONS
$DeployOptions = ""

$LogDirectory = Join-Path $ScriptDirectory 'logs'
$JarFile = Get-ChildItem $ScriptDirectory | Where-Object { $_.Name -like 'jobdeploy-*.jar' } | Select-Object -First 1
$Command = $args[0]

if (-not (Test-Path $LogDirectory)) {
    New-Item -ItemType Directory -Path $LogDirectory | Out-Null
    Set-ItemProperty -Path $LogDirectory -Name Attributes -Value 'Hidden'
}
if (-not $env:USERNAME) {
    $env:USERNAME=$env:USER
}
$LogFile = Join-Path $LogDirectory ("$env:USERNAME-deploy.log")

$LoaderPath = Join-Path $ScriptDirectory 'conf;libs'
if ($Command -eq '--update') {
    $AppTmpDir = New-TemporaryFile
    $ArchiveTgzUrl = Get-Latest-Archive
    Invoke-WebRequest -Uri $ArchiveTgzUrl -OutFile $AppTmpDir
    Remove-Item -Force -Recurse $ScriptDirectory\libs
    Remove-Item -Force $ScriptDirectory\*.jar
    Expand-Archive -Path $AppTmpDir -DestinationPath $ScriptDirectory
    Write-Host "update job success"
}
else {
    $AppExt = Join-Path $ScriptDirectory 'exts'
    $ExtPaths = $AppExt
    if ($env:DEPLOY_PLUGIN) {
        $ExtPaths = "$ExtPaths,$env:DEPLOY_PLUGIN"
    }
    $DeployOptions = "-Dloader.path=$ExtPaths"
    Start-Process -FilePath $Java -ArgumentList "$DeployOptions -Dlog.file=$LogFile -jar $JarFile $args" -Wait
}
