$ErrorActionPreference = "Stop"
. "$PSScriptRoot\scripts-env.ps1"
.\mvnw.cmd javafx:run -Ddiscscout.demo=true
