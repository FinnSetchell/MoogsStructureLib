#Requires -Version 5.1
<#
.SYNOPSIS
    Smoke-tests EquipArmorStandProcessor across every supported MSL branch.

.DESCRIPTION
    For each branch listed in $BRANCHES the script:
      1. Creates a temporary git worktree
      2. Checks whether the test class has been propagated to that branch
      3. Runs NeoForge (or Forge for 1.20.x) game-test server headlessly
      4. Captures pass / fail
      5. Tears down the worktree

    Emits a markdown results matrix to stdout when all branches are done.
    Exits non-zero if any branch fails.

.NOTES
    Run from the repo root:  .\scripts\test-armor-stand.ps1
    Run a subset:            .\scripts\test-armor-stand.ps1 -Only "26.1.0-26.1.2","1.21.4"
#>

param(
    # Optional list of branch names to test; defaults to all branches.
    [string[]] $Only = @()
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# ---------------------------------------------------------------------------
# Branch table
# ---------------------------------------------------------------------------
# Loader: "neoforge" -> `:neoforge:runGameTestServer`
#         "forge"    -> `:forge:runGameTestServer`  (1.20.x has no NeoForge)
# TestClass: relative path from worktree root to the expected test file
# ---------------------------------------------------------------------------
$BRANCHES = @(
    [ordered]@{ Branch = "26.1.0-26.1.2";   MC = "26.1.2"; Java = 25; Loader = "neoforge" }
    [ordered]@{ Branch = "1.21.11";          MC = "1.21.11"; Java = 21; Loader = "neoforge" }
    [ordered]@{ Branch = "1.21.5-1.21.10";  MC = "1.21.5";  Java = 21; Loader = "neoforge" }
    [ordered]@{ Branch = "1.21.4";          MC = "1.21.4";  Java = 21; Loader = "neoforge" }
    [ordered]@{ Branch = "1.21.2-1.21.3";   MC = "1.21.3";  Java = 21; Loader = "neoforge" }
    [ordered]@{ Branch = "1.21-1.21.1";     MC = "1.21.1";  Java = 21; Loader = "neoforge" }
    [ordered]@{ Branch = "1.20.5-1.20.6";   MC = "1.20.6";  Java = 21; Loader = "neoforge" }
    [ordered]@{ Branch = "1.20-1.20.4";     MC = "1.20.1";  Java = 17; Loader = "forge"    }
)

$TEST_CLASS_NEOFORGE = "neoforge\src\main\java\com\finndog\moogs_structures\gametest\EquipArmorStandProcessorTest.java"
$TEST_CLASS_FORGE    = "forge\src\main\java\com\finndog\moogs_structures\gametest\EquipArmorStandProcessorTest.java"

# Gradle task timeout in seconds
$GRADLE_TIMEOUT_SECONDS = 600

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
function Write-Header { param([string]$msg) Write-Host "`n=== $msg ===" -ForegroundColor Cyan }
function Write-Pass   { param([string]$msg) Write-Host "  PASS  $msg" -ForegroundColor Green }
function Write-Fail   { param([string]$msg) Write-Host "  FAIL  $msg" -ForegroundColor Red }
function Write-Skip   { param([string]$msg) Write-Host "  SKIP  $msg" -ForegroundColor Yellow }

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
$ROOT = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$WORKTREE_BASE = Join-Path $ROOT ".worktrees\smoketest"

Write-Host "MSL armor-stand smoke test harness" -ForegroundColor Magenta
Write-Host "Repo root : $ROOT"
Write-Host "Worktrees : $WORKTREE_BASE"

# Filter branches if -Only was supplied
$targets = if ($Only.Count -gt 0) {
    $BRANCHES | Where-Object { $Only -contains $_.Branch }
} else {
    $BRANCHES
}

if ($targets.Count -eq 0) {
    Write-Warning "No matching branches found (check -Only values)"
    exit 1
}

$results = [System.Collections.Generic.List[object]]::new()

foreach ($entry in $targets) {
    $branch  = $entry.Branch
    $mc      = $entry.MC
    $loader  = $entry.Loader
    $worktree = Join-Path $WORKTREE_BASE $branch.Replace("/", "-")

    Write-Header "Branch: $branch  (MC $mc, loader: $loader)"

    # -- worktree setup --
    if (Test-Path $worktree) {
        Write-Host "  Removing stale worktree at $worktree"
        git -C $ROOT worktree remove --force $worktree 2>&1 | Out-Null
        Remove-Item -Recurse -Force $worktree -ErrorAction SilentlyContinue
    }

    $null = git -C $ROOT worktree add $worktree $branch 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Fail "Could not create worktree for branch $branch"
        $results.Add([pscustomobject]@{ Branch = $branch; MC = $mc; Status = "ERROR"; Detail = "worktree failed" })
        continue
    }

    try {
        # -- check test class is present --
        $testClassPath = if ($loader -eq "forge") { $TEST_CLASS_FORGE } else { $TEST_CLASS_NEOFORGE }
        $testClassFull = Join-Path $worktree $testClassPath
        if (-not (Test-Path $testClassFull)) {
            Write-Skip "test class not yet propagated to $branch -- cherry-pick feat/armor-stand-smoketest"
            $results.Add([pscustomobject]@{ Branch = $branch; MC = $mc; Status = "SKIP"; Detail = "test not propagated" })
            continue
        }

        # -- run game test server in background --
        $logFile = Join-Path $worktree "runs\gameTestServer\gametest.log"
        New-Item -ItemType Directory -Force (Split-Path $logFile) | Out-Null

        $gradleCmd = if ($IsWindows -or $PSVersionTable.Platform -eq "Win32NT" -or -not $PSVersionTable.Platform) {
            ".\gradlew.bat"
        } else {
            "./gradlew"
        }
        $task = ":${loader}:runGameTestServer"

        Write-Host "  Running: $gradleCmd $task"
        Write-Host "  Log    : $logFile"

        $proc = Start-Process `
            -FilePath $gradleCmd `
            -ArgumentList @($task, "--stacktrace") `
            -WorkingDirectory $worktree `
            -RedirectStandardOutput $logFile `
            -RedirectStandardError  ($logFile + ".err") `
            -NoNewWindow `
            -PassThru

        # Poll until done or timeout
        $deadline = [DateTime]::Now.AddSeconds($GRADLE_TIMEOUT_SECONDS)
        while (-not $proc.HasExited -and [DateTime]::Now -lt $deadline) {
            Start-Sleep -Seconds 5
            if (Test-Path $logFile) {
                $last = Get-Content $logFile -Tail 1 -ErrorAction SilentlyContinue
                if ($last) { Write-Host "  ..." $last }
            }
        }

        if (-not $proc.HasExited) {
            $proc.Kill()
            Write-Fail "Timed out after $GRADLE_TIMEOUT_SECONDS seconds"
            $results.Add([pscustomobject]@{ Branch = $branch; MC = $mc; Status = "FAIL"; Detail = "timeout" })
            continue
        }

        $exit = $proc.ExitCode
        $log  = if (Test-Path $logFile) { Get-Content $logFile -Raw } else { "" }

        # Parse outcome: look for NeoForge/Forge game test result markers
        $passCount = ([regex]::Matches($log, "(?i)Game ?tests? passed|Required tests? passed")).Count
        $failCount = ([regex]::Matches($log, "(?i)Game ?tests? failed|Required tests? failed")).Count
        $testMention = $log -match "equipment_key_is_written"

        if ($exit -eq 0 -and $passCount -gt 0 -and $failCount -eq 0) {
            Write-Pass "exit=$exit, patterns matched pass"
            $results.Add([pscustomobject]@{ Branch = $branch; MC = $mc; Status = "PASS"; Detail = "" })
        } elseif ($failCount -gt 0 -or $exit -ne 0) {
            # Extract first failure line mentioning our test
            $failLine = ($log -split "`n" | Where-Object { $_ -match "equipment_key_is_written|FAIL|fail" } | Select-Object -First 1)?.Trim()
            Write-Fail "exit=$exit -- $failLine"
            $results.Add([pscustomobject]@{ Branch = $branch; MC = $mc; Status = "FAIL"; Detail = $failLine })
        } else {
            # Gradle succeeded but no clear test markers found -- treat as inconclusive
            Write-Skip "exit=$exit but no test result markers found in log -- check $logFile"
            $results.Add([pscustomobject]@{ Branch = $branch; MC = $mc; Status = "?"; Detail = "no test markers" })
        }
    }
    finally {
        # -- tear down worktree --
        git -C $ROOT worktree remove --force $worktree 2>&1 | Out-Null
        Remove-Item -Recurse -Force $worktree -ErrorAction SilentlyContinue
    }
}

# ---------------------------------------------------------------------------
# Markdown matrix
# ---------------------------------------------------------------------------
Write-Host "`n"
Write-Host "## EquipArmorStandProcessor smoke-test results" -ForegroundColor Magenta
Write-Host ""
Write-Host "| Branch | MC version | Result | Notes |"
Write-Host "|---|---|---|---|"
foreach ($r in $results) {
    $icon = switch ($r.Status) {
        "PASS"  { "OK" }
        "FAIL"  { "FAIL" }
        "SKIP"  { "SKIP" }
        "ERROR" { "ERROR" }
        default { "?" }
    }
    Write-Host "| $($r.Branch) | $($r.MC) | $icon | $($r.Detail) |"
}
Write-Host ""

$anyFail = $results | Where-Object { $_.Status -in @("FAIL", "ERROR") }
if ($anyFail) {
    Write-Host "One or more branches FAILED." -ForegroundColor Red
    exit 1
}

$anySkip = $results | Where-Object { $_.Status -eq "SKIP" }
if ($anySkip) {
    Write-Host "Some branches were skipped (test not yet propagated)." -ForegroundColor Yellow
}

Write-Host "All tested branches passed." -ForegroundColor Green
exit 0
