param(
    [Parameter(Mandatory = $true)][string]$Jdk,
    [Parameter(Mandatory = $true)][string]$BuildTools,
    [Parameter(Mandatory = $true)][string]$Platform,
    [string]$BuildDirectory = "$PSScriptRoot/build",
    [string]$Apk = "$PSScriptRoot/build/Sweet-0.2.2.apk"
)
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = (Resolve-Path -LiteralPath $Jdk).Path
$env:PATH = "$env:JAVA_HOME/bin;$env:PATH"

function Run([string]$Program, [string[]]$Arguments) {
    & $Program @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Program exited with code $LASTEXITCODE"
    }
}

$java = Join-Path $env:JAVA_HOME 'bin'
$android = Join-Path (Resolve-Path -LiteralPath $Platform).Path 'android.jar'
$tools = (Resolve-Path -LiteralPath $BuildTools).Path
$main = "$PSScriptRoot/src/main"
$javaSource = "$main/java"
$apkPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Apk)
$buildPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($BuildDirectory)
New-Item -ItemType Directory -Force -Path $buildPath, (Split-Path -Parent $apkPath), "$PSScriptRoot/.signing" | Out-Null
$stage = (New-Item -ItemType Directory -Path (Join-Path $buildPath ([Guid]::NewGuid().ToString('N')))).FullName
New-Item -ItemType Directory -Path "$stage/classes", "$stage/dex" | Out-Null

$sass = "$PSScriptRoot/node_modules/.bin/sass.cmd"
if (!(Test-Path -LiteralPath $sass)) {
    throw "Install the SCSS compiler first: npm ci (from $PSScriptRoot)"
}
Copy-Item -LiteralPath "$main/assets" -Destination "$stage/assets" -Recurse
Run $sass @('--no-source-map', "${stage}/assets:${stage}/assets")
Get-ChildItem -LiteralPath "$stage/assets" -Filter '*.scss' -File -Recurse |
    Remove-Item

Run "$tools/aapt2.exe" @('compile', '--dir', "$main/res", '-o', "$stage/resources.zip")
Run "$tools/aapt2.exe" @('link', '-o', "$stage/unsigned.apk", '--manifest',
    "$main/AndroidManifest.xml", '-I', $android,
    "$stage/resources.zip")
$sources = @(Get-ChildItem -LiteralPath $javaSource -Filter '*.java' -File -Recurse |
    ForEach-Object { $_.FullName })
$compilerArgs = @('-J-Duser.language=en', '-J-Duser.country=US', '-encoding', 'UTF-8',
    '--release', '8', '-classpath', $android, '-d', "$stage/classes") + $sources
$compilerOutput = & "$java/javac.exe" @compilerArgs 2>&1
$compilerExit = $LASTEXITCODE
$compilerLog = $compilerOutput | Out-String
if ($compilerExit -ne 0) {
    # Some managed Windows environments deny ZIP filesystem cleanup after class generation.
    $errors = @($compilerLog -split '\r?\n' | Where-Object { $_ -match '\berror:' })
    $cleanupOnly = $compilerExit -eq 3 -and $errors.Count -eq 1 -and
        $errors[0] -match 'error: cannot close compiler resources' -and
        $compilerLog -match 'AccessDeniedException' -and
        $compilerLog -match 'jdk.zipfs' -and
        $compilerLog -match 'ArchiveContainer.close'
    if (!$cleanupOnly) {
        Write-Output $compilerLog
        throw "javac exited with code $compilerExit"
    }
    foreach ($source in $sources) {
        $classPath = [IO.Path]::ChangeExtension($source.Substring($javaSource.Length + 1), '.class')
        if (!(Test-Path -LiteralPath "$stage/classes/$classPath")) {
            throw "javac did not produce $classPath"
        }
    }
    Write-Output 'Compiled successfully. The SDK archive could not be closed by javac.'
}
Run "$java/jar.exe" @('cf', "$stage/classes.jar", '-C', "$stage/classes", '.')
Run "$tools/d8.bat" @('--min-api', '26', '--lib', $android, '--output', "$stage/dex",
    "$stage/classes.jar")
# jar writes forward slashes for nested assets even on Windows.
Run "$java/jar.exe" @('uf', "$stage/unsigned.apk", '-C', "$stage/dex", 'classes.dex',
    '-C', $stage, 'assets')
Run "$tools/zipalign.exe" @('-f', '-P', '16', '4', "$stage/unsigned.apk", "$stage/aligned.apk")

$key = "$PSScriptRoot/.signing/debug.p12"
if (!(Test-Path -LiteralPath $key)) {
    Run "$java/keytool.exe" @('-genkeypair', '-keystore', $key, '-storetype', 'PKCS12',
        '-storepass', 'android', '-keypass', 'android', '-alias', 'sweet-debug', '-keyalg',
        'RSA', '-keysize', '2048', '-validity', '10000', '-dname', 'CN=Sweet Local Debug')
}
Run "$tools/apksigner.bat" @('sign', '--ks', $key, '--ks-pass', 'pass:android',
    '--key-pass', 'pass:android', '--v4-signing-enabled', 'false', '--out', $apkPath,
    "$stage/aligned.apk")
Run "$tools/apksigner.bat" @('verify', '--verbose', $apkPath)
Run "$tools/aapt2.exe" @('dump', 'badging', $apkPath)
Get-FileHash -Algorithm SHA256 -LiteralPath $apkPath
