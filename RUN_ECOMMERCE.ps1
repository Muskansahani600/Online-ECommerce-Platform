$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$ProgressPreference = 'SilentlyContinue'
function Find-Jdk {
    $candidates = New-Object 'System.Collections.Generic.List[string]'
    if ($env:JAVA_HOME) { $candidates.Add($env:JAVA_HOME) }
    $commands = @(Get-Command javac.exe -All -ErrorAction SilentlyContinue)
    foreach ($command in $commands) {
        try {
            $resolved = (Resolve-Path $command.Source -ErrorAction Stop).Path
            $candidate = Split-Path (Split-Path $resolved -Parent) -Parent
            $candidates.Add($candidate)
        } catch {}
    }
    foreach ($base in @('C:\Program Files\Java','C:\Program Files\Eclipse Adoptium','C:\Program Files\Microsoft','C:\Program Files\Amazon Corretto','C:\Program Files\Zulu','C:\Program Files\BellSoft','C:\Program Files\Semeru','C:\Program Files\RedHat','C:\Program Files (x86)\Java')) {
        if (Test-Path $base) {
            Get-ChildItem $base -Directory -ErrorAction SilentlyContinue | ForEach-Object { $candidates.Add($_.FullName) }
        }
    }
    foreach ($path in ($candidates | Select-Object -Unique)) {
        $java = Join-Path $path 'bin\java.exe'
        $javac = Join-Path $path 'bin\javac.exe'
        if (!(Test-Path $java) -or !(Test-Path $javac)) { continue }
        $v = (& cmd.exe /d /c ('"' + $java + '" -version 2>&1') | Out-String)
        if ($v -match '(?:version\s+|openjdk\s+)(?:")?(\d+)') {
            $major = [int]$Matches[1]
            if ($major -eq 1 -and $v -match '1\.(\d+)') { $major = [int]$Matches[1] }
            if ($major -ge 11) { return $path }
        }
    }
    throw 'No supported JDK found. Tomcat 10.1 requires JDK 11 or newer (17 or 21 recommended). Install a JDK, not just a JRE.'
}
function Valid-Tomcat([string]$path) {
    return ($path -and (Test-Path (Join-Path $path 'bin\catalina.bat')) -and (Test-Path (Join-Path $path 'lib\servlet-api.jar')) -and (Test-Path (Join-Path $path 'webapps')))
}
function Find-Tomcat {
    $saved = Join-Path $project '.tomcat-path.txt'
    $candidates = New-Object 'System.Collections.Generic.List[string]'
    if (Test-Path $saved) { $candidates.Add((Get-Content $saved -Raw).Trim()) }
    if ($env:CATALINA_HOME) { $candidates.Add($env:CATALINA_HOME) }
    $roots = @($env:USERPROFILE, (Join-Path $env:USERPROFILE 'Downloads'), (Join-Path $env:USERPROFILE 'Desktop'), (Join-Path $env:USERPROFILE 'Documents'), 'C:\', 'D:\')
    foreach ($root in $roots) {
        if (!(Test-Path $root)) { continue }
        # Inspect likely top-level and nested extracted archive directories, not an unbounded disk crawl.
        $first = @(Get-ChildItem $root -Directory -ErrorAction SilentlyContinue)
        foreach ($d in $first) {
            if ($d.Name -like '*tomcat*') { $candidates.Add($d.FullName) }
            if ($d.Name -like '*tomcat*' -or $d.Name -in @('Downloads','Desktop','Documents','Dnt','Servers','Tools','Programs')) {
                Get-ChildItem $d.FullName -Directory -ErrorAction SilentlyContinue | ForEach-Object {
                    if ($_.Name -like '*tomcat*') { $candidates.Add($_.FullName) }
                    Get-ChildItem $_.FullName -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -like '*tomcat*' } | ForEach-Object { $candidates.Add($_.FullName) }
                }
            }
        }
    }
    foreach ($path in ($candidates | Select-Object -Unique)) {
        if (Valid-Tomcat $path) {
            Set-Content -Path $saved -Value $path
            return $path
        }
    }
    throw 'Tomcat was not found automatically. Extract Apache Tomcat 10.1 under Downloads or Desktop and rerun. Alternatively set CATALINA_HOME to the Tomcat root folder.'
}
function Wait-ForWebsite([string]$url, [int]$seconds) {
    for ($i = 0; $i -lt $seconds; $i += 2) {
        try {
            $request = [System.Net.HttpWebRequest]::Create($url)
            $request.Timeout = 1500
            $request.AllowAutoRedirect = $true
            $response = $request.GetResponse()
            $response.Close()
            return $true
        } catch [System.Net.WebException] {
            if ($_.Exception.Response) {
                # HTTP 500 or 404 means Tomcat is reachable, but app isn't healthy yet.
                $_.Exception.Response.Close()
            }
        } catch {}
        Start-Sleep -Seconds 2
    }
    return $false
}
try {
    Write-Host 'E-COMMERCE AUTO SETUP' -ForegroundColor Cyan
    $jdk = Find-Jdk
    $tomcat = Find-Tomcat
    $env:JAVA_HOME = $jdk
    $env:JRE_HOME = $jdk
    $env:PATH = (Join-Path $jdk 'bin') + ';' + $env:PATH
    $env:CATALINA_HOME = $tomcat
    $env:CATALINA_BASE = $tomcat
    Write-Host "JDK: $jdk" -ForegroundColor Green
    Write-Host "Tomcat: $tomcat" -ForegroundColor Green
    $jar = Join-Path $project 'lib\mysql-connector-j.jar'
    $src = Join-Path $project 'src\main\java\com\ecommerce\OnlineECommercePlatform.java'
    $setup = Join-Path $project 'src\main\java\com\ecommerce\DatabaseSetup.java'
    foreach ($file in @($jar,$src,$setup)) { if (!(Test-Path $file)) { throw "Missing project file: $file" } }
    $username = Read-Host 'MySQL username [root]'
    if ([string]::IsNullOrWhiteSpace($username)) { $username = 'root' }
    $secure = Read-Host 'MySQL password (hidden)' -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { $env:ECOMMERCE_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr); $secure.Dispose() }
    $env:ECOMMERCE_DB_USER = $username
    $classes = Join-Path $project 'web\WEB-INF\classes'
    New-Item -ItemType Directory -Force -Path $classes | Out-Null
    Write-Host 'Compiling Java sources...' -ForegroundColor Cyan
    & (Join-Path $jdk 'bin\javac.exe') -encoding UTF-8 -cp "$tomcat\lib\servlet-api.jar;$jar" -d $classes $src $setup
    if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
    Write-Host 'Creating missing MySQL database/tables without deleting data...' -ForegroundColor Cyan
    & (Join-Path $jdk 'bin\java.exe') -cp "$classes;$jar" com.ecommerce.DatabaseSetup
    if ($LASTEXITCODE -ne 0) { throw 'MySQL setup failed. Check that MySQL Server is running, the credentials are correct, and the account has CREATE privileges.' }
    $webLib = Join-Path $project 'web\WEB-INF\lib'
    New-Item -ItemType Directory -Force -Path $webLib | Out-Null
    Copy-Item $jar (Join-Path $webLib 'mysql-connector-j.jar') -Force
    $dest = Join-Path $tomcat 'webapps\ecommerce'
    # Replace only this application's deployment, never other Tomcat apps.
    if (Test-Path $dest) { Remove-Item $dest -Recurse -Force }
    Copy-Item (Join-Path $project 'web') $dest -Recurse -Force
    Write-Host 'Application deployed. Starting Tomcat...' -ForegroundColor Green
    $url = 'http://localhost:8080/ecommerce/'
    # Use catalina.bat start so the script can wait for readiness and launch the browser.
    & (Join-Path $tomcat 'bin\catalina.bat') start
    if ($LASTEXITCODE -ne 0) { throw 'Tomcat could not start. Check its logs directory.' }
    if (Wait-ForWebsite $url 60) {
        Write-Host "Opening $url" -ForegroundColor Green
        Start-Process $url
        Write-Host 'Setup complete. Tomcat remains running in the background.' -ForegroundColor Green
    } else {
        Write-Host "Tomcat did not serve the website within 60 seconds. Try $url and check Tomcat logs." -ForegroundColor Yellow
        throw 'Website did not become ready. See Tomcat logs for details.'
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    Remove-Item Env:ECOMMERCE_DB_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:ECOMMERCE_DB_USER -ErrorAction SilentlyContinue
}
