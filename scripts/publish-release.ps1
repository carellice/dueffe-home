param([ValidateSet('patch', 'minor', 'major')][string]$Bump = 'patch')
# Compila l'APK firmato, incrementa la versione, crea il tag e pubblica la release su GitHub.
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$project = Split-Path $PSScriptRoot -Parent
$repository = 'carellice/dueffe-home'
$versionPath = Join-Path $project 'version.properties'
$signingPath = Join-Path $project '.release\signing.properties'
$outDir = Join-Path $project '.release\out'
$utf8 = New-Object System.Text.UTF8Encoding($false)
$versionBefore = $null
$committed = $false

function Step($text) { Write-Host "`n==> $text" -ForegroundColor Yellow }
function Run($exe, [string[]]$arguments) {
    & $exe @arguments
    if ($LASTEXITCODE -ne 0) { throw "$exe $($arguments -join ' ') non riuscito (codice $LASTEXITCODE)." }
}

Push-Location $project
try {
    foreach ($command in @('git', 'gh', 'java', 'node')) {
        if (-not (Get-Command $command -ErrorAction SilentlyContinue)) { throw "Comando mancante: $command" }
    }
    if (-not (Test-Path $signingPath)) {
        throw 'Firma assente (.release\signing.properties). Ripristina il backup della cartella .release: senza la stessa chiave gli aggiornamenti in-app non si installano.'
    }
    Run 'gh' @('auth', 'status', '--hostname', 'github.com')
    $branch = (& git branch --show-current).Trim()
    if ($branch -ne 'main') { throw "Sei sul branch '$branch': passa a main prima di pubblicare." }

    Step 'Sincronizzo con GitHub'
    Run 'git' @('pull', '--rebase', '--autostash', 'origin', 'main')
    Run 'git' @('fetch', '--tags', 'origin')

    # Versione: patch di default, oppure minor/major.
    $versionBefore = [IO.File]::ReadAllText($versionPath)
    $props = ConvertFrom-StringData $versionBefore
    $parts = @($props.versionName.Split('.') | ForEach-Object { [int]$_ })
    while ($parts.Count -lt 3) { $parts += 0 }
    switch ($Bump) {
        'major' { $parts = @(($parts[0] + 1), 0, 0) }
        'minor' { $parts = @($parts[0], ($parts[1] + 1), 0) }
        default { $parts = @($parts[0], $parts[1], ($parts[2] + 1)) }
    }
    $name = $parts -join '.'
    $code = [int]$props.versionCode + 1
    $tag = "v$name"
    & git rev-parse -q --verify "refs/tags/$tag" | Out-Null
    if ($LASTEXITCODE -eq 0) { throw "Il tag $tag esiste gia." }
    [IO.File]::WriteAllText($versionPath, "versionCode=$code`nversionName=$name`n", $utf8)
    Write-Host "Nuova versione: $name (versionCode $code)" -ForegroundColor Green

    Step 'Aggiorno il catalogo incluso nell''APK'
    & node tools/build-catalog.mjs
    if ($LASTEXITCODE -ne 0) { Write-Warning 'Catalogo non aggiornato: uso quello gia presente.' }

    Step 'Compilo l''APK firmato'
    $env:ANDROID_HOME = $null
    Run (Join-Path $project 'gradlew.bat') @('clean', 'assembleRelease', '--console=plain')
    $apk = Join-Path $project 'app\build\outputs\apk\release\app-release.apk'
    if (-not (Test-Path $apk)) { throw 'APK firmato non trovato.' }
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
    $apkOut = Join-Path $outDir "DueffeHome-$tag.apk"
    Copy-Item $apk $apkOut -Force

    Step 'Commit e tag'
    $previousTag = (cmd /c 'git describe --tags --abbrev=0 2>nul')
    Run 'git' @('add', '-A')
    Run 'git' @('commit', '-m', "Release $tag")
    $committed = $true
    Run 'git' @('tag', '-a', $tag, '-m', "Dueffe Home $name")
    Run 'git' @('push', '--atomic', 'origin', 'main', $tag)

    Step 'Pubblico la release'
    $changes = if ($previousTag) { & git log "$previousTag..HEAD~1" --pretty=format:'- %s' --no-merges } else { @() }
    $changes = @($changes | Where-Object { $_ -and $_ -notmatch '^- Release v' })
    $notes = "## Dueffe Home $name`n`n"
    if ($changes.Count) { $notes += "### Novita`n" + ($changes -join "`n") + "`n`n" }
    $notes += "Scarica **DueffeHome-$tag.apk** e installalo, oppure aggiorna da **Info > Controlla aggiornamenti** dentro l'app.`n`nAndroid 8.0 o successivo. Versione web: https://carellice.github.io/dueffe-home/"
    $notesPath = Join-Path $outDir "notes-$tag.md"
    [IO.File]::WriteAllText($notesPath, $notes, $utf8)
    Run 'gh' @('release', 'create', $tag, $apkOut, '--repo', $repository, '--title', "Dueffe Home $name", '--notes-file', $notesPath, '--verify-tag', '--latest')
    Write-Host "`nPubblicato: https://github.com/$repository/releases/tag/$tag" -ForegroundColor Green
} catch {
    if (-not $committed -and $null -ne $versionBefore) {
        [IO.File]::WriteAllText($versionPath, $versionBefore, $utf8)
        Write-Host 'Versione ripristinata.' -ForegroundColor DarkYellow
    }
    Write-Host "ERRORE: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    Pop-Location
}
