$ErrorActionPreference = "Stop"
. "$PSScriptRoot\scripts-env.ps1"
.\mvnw.cmd test
