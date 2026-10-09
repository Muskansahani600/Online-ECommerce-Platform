$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$defaultTomcat = 'C:\Users\crazy\Desktop\Dnt\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60'
function Get-Tomcat {
    if (Test-Path (Join-Path $defaultTomcat 'lib\servlet-api.jar')) { return $defaultTomcat }
    Write-Host 'Tomcat was not found at the saved location.' -ForegroundColor Yellow
    $path = Read-Host 'Paste the Tomcat root folder (not bin)'
    $path = $path.Trim('"')
    if (!(Test-Path (Join-Path $path 'lib\servlet-api.jar'))) { throw 'Invalid Tomcat folder: servlet-api.jar missing.' }
    return $path
}
try {
    $tomcat = Get-Tomcat
    if (!(Get-Command javac -ErrorAction SilentlyContinue)) { throw 'JDK javac not found in PATH.' }
    if (!(Get-Command java -ErrorAction SilentlyContinue)) { throw 'Java not found in PATH.' }
    $jar = Join-Path $project 'lib\mysql-connector-j.jar'
    if (!(Test-Path $jar)) { throw 'MySQL Connector/J is missing from lib.' }
    $src = Join-Path $project 'src\main\java\com\ecommerce\OnlineECommercePlatform.java'
    if (!(Test-Path $src)) { throw 'Java source file missing.' }
    $username = Read-Host 'MySQL username [root]'
    if ([string]::IsNullOrWhiteSpace($username)) { $username = 'root' }
    $secure = Read-Host 'MySQL password (hidden)' -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { $env:ECOMMERCE_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr); $secure.Dispose() }
    $env:ECOMMERCE_DB_USER = $username
    Write-Host 'Compiling Java...' -ForegroundColor Cyan
    $classes = Join-Path $project 'web\WEB-INF\classes'
    if (Test-Path $classes) { Remove-Item $classes -Recurse -Force }
    New-Item -ItemType Directory -Force $classes | Out-Null
    & javac -encoding UTF-8 -cp "$tomcat\lib\servlet-api.jar;$jar" -d $classes $src
    if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed. See the errors above.' }
    $webLib = Join-Path $project 'web\WEB-INF\lib'
    New-Item -ItemType Directory -Force $webLib | Out-Null
    Copy-Item $jar (Join-Path $webLib 'mysql-connector-j.jar') -Force
    $dest = Join-Path $tomcat 'webapps\ecommerce'
    if (Test-Path $dest) { Remove-Item $dest -Recurse -Force }
    Copy-Item (Join-Path $project 'web') $dest -Recurse -Force
    Write-Host 'Deployed to Tomcat.' -ForegroundColor Green
    Write-Host 'Starting Tomcat in this console. Keep it open.' -ForegroundColor Cyan
    $env:CATALINA_HOME = $tomcat
    $env:CATALINA_BASE = $tomcat
    Start-Process 'http://localhost:8080/ecommerce/'
    & (Join-Path $tomcat 'bin\catalina.bat') run
    if ($LASTEXITCODE -ne 0) { throw "Tomcat stopped with exit code $LASTEXITCODE" }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    Remove-Item Env:ECOMMERCE_DB_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:ECOMMERCE_DB_USER -ErrorAction SilentlyContinue
}
