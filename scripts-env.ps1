$ErrorActionPreference = "Stop"
$localJdk = Join-Path $PSScriptRoot ".jdk\jdk-26.0.2"
if (Test-Path $localJdk) {
  $env:JAVA_HOME = $localJdk
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}
