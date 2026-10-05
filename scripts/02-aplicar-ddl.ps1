# Aula 15: Invoke-Sqlcmd contra Azure SQL PaaS, com credenciais solicitadas em memoria.
# Execute no PowerShell do Azure Cloud Shell ou no PC com o modulo SqlServer.
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-z0-9][a-z0-9-]*[a-z0-9]$')]
    [string]$ServerName,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-z0-9][a-z0-9-]*[a-z0-9]$')]
    [string]$DatabaseName
)

$ErrorActionPreference = 'Stop'
$ddlFile = Join-Path $PSScriptRoot 'ddl.sql'
if (-not (Test-Path -LiteralPath $ddlFile -PathType Leaf)) {
    throw 'scripts/ddl.sql nao encontrado. Execute a partir de uma copia completa do repositorio.'
}
if (-not (Get-Command Invoke-Sqlcmd -ErrorAction SilentlyContinue)) {
    throw 'Invoke-Sqlcmd ausente. Use o PowerShell do Azure Cloud Shell com o modulo SqlServer.'
}

# Solicita usuario e senha sem eco; nao abre caixa que mostre o nome de login.
$secureSqlUser = Read-Host 'Usuario administrador SQL (entrada oculta)' -AsSecureString
$sqlUser = [System.Net.NetworkCredential]::new('', $secureSqlUser).Password
if ([string]::IsNullOrWhiteSpace($sqlUser)) {
    throw 'Informe o usuario administrador SQL.'
}
$secureSqlPassword = Read-Host 'Senha administrador SQL (entrada oculta)' -AsSecureString
$sqlCredential = [System.Management.Automation.PSCredential]::new($sqlUser, $secureSqlPassword)

try {
    $sqlParameters = @{
        ServerInstance = "$ServerName.database.windows.net"
        Database       = $DatabaseName
        Credential     = $sqlCredential
        InputFile      = $ddlFile
        AbortOnError   = $true
        OutputSqlErrors = $false
        ErrorAction    = 'Stop'
    }
    # O modulo v22 usa Encrypt Mandatory por padrao. Versoes anteriores usam
    # EncryptConnection; mantenha certificado validado, sem TrustServerCertificate.
    if ((Get-Command Invoke-Sqlcmd).Parameters.ContainsKey('Encrypt')) {
        $sqlParameters.Encrypt = 'Mandatory'
    }
    else {
        $sqlParameters.EncryptConnection = $true
    }
    Invoke-Sqlcmd @sqlParameters | Out-Null
    Write-Host 'DDL aplicado ao Azure SQL. As tabelas existentes e seus dados foram preservados.'
}
catch {
    # O erro do driver pode incluir o login. Nao imprimir a excecao original.
    throw 'Falha ao aplicar DDL. Verifique credenciais, firewall para seu IP, banco e scripts/ddl.sql.'
}
finally {
    $sqlParameters = $null
    $sqlCredential = $null
    $sqlUser = $null
    $secureSqlUser.Dispose()
    $secureSqlPassword.Dispose()
    $secureSqlUser = $null
    $secureSqlPassword = $null
}
