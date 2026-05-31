# parse_log.ps1
$logPath = "C:\Users\ASUS I5\capstone_project2\CapstoneProjectB.2\baseline-system\task-90.log"
if (-not (Test-Path $logPath)) {
    $logPath = "C:\Users\ASUS I5\.gemini\antigravity\brain\aaa8921e-16bd-4f54-b668-cbaece1bc452\.system_generated\tasks\task-90.log"
}
$content = Get-Content $logPath -Raw

$scenarios = @("none", "l1", "redis", "full")

for ($i = 0; $i -lt $scenarios.Count; $i++) {
    $mode = $scenarios[$i]
    $headerPattern = "=========================================`r?`nSCENARIO: CACHE_MODE = $mode`r?`n========================================="
    
    $startIdx = [regex]::Match($content, $headerPattern).Index
    if ($startIdx -lt 0) {
        Write-Host "Could not find start for scenario $mode"
        continue
    }
    
    $endIdx = $content.Length
    if ($i -lt ($scenarios.Count - 1)) {
        $nextMode = $scenarios[$i+1]
        $nextHeaderPattern = "=========================================`r?`nSCENARIO: CACHE_MODE = $nextMode`r?`n========================================="
        $nextMatch = [regex]::Match($content, $nextHeaderPattern)
        if ($nextMatch.Success) {
            $endIdx = $nextMatch.Index
        }
    }
    
    $section = $content.Substring($startIdx, $endIdx - $startIdx)
    
    Write-Host "`n=========================================" -ForegroundColor Yellow
    Write-Host "RESULTS FOR CACHE_MODE = $($mode.ToUpper())" -ForegroundColor Yellow
    Write-Host "=========================================" -ForegroundColor Yellow
    
    # Find the last "Response time percentiles (approximated)" table
    $percentileHeader = "Response time percentiles (approximated)"
    $percentileMatches = [regex]::Matches($section, [regex]::Escape($percentileHeader))
    if ($percentileMatches.Count -gt 0) {
        $lastMatch = $percentileMatches[$percentileMatches.Count - 1]
        $pStart = $lastMatch.Index
        
        $lines = $section.Substring($pStart).Split("`n")
        Write-Host "`nFinal Response Percentiles Table:" -ForegroundColor Green
        for ($j = 0; $j -lt 15; $j++) {
            if ($j -lt $lines.Count) {
                Write-Host $lines[$j].TrimEnd()
            }
        }
    } else {
        Write-Host "No percentile table found." -ForegroundColor Red
    }
    
    # Find the last throughput table
    $throughputHeader = "Type\s+Name\s+# reqs"
    $throughputMatches = [regex]::Matches($section, $throughputHeader)
    if ($throughputMatches.Count -gt 0) {
        $lastTMatch = $throughputMatches[$throughputMatches.Count - 1]
        $tStart = $lastTMatch.Index
        
        $tLength = $section.Length - $tStart
        if ($percentileMatches.Count -gt 0 -and $percentileMatches[$percentileMatches.Count - 1].Index -gt $tStart) {
            $tLength = $percentileMatches[$percentileMatches.Count - 1].Index - $tStart
        }
        
        $tLines = $section.Substring($tStart, $tLength).Split("`n")
        Write-Host "`nFinal Throughput Table:" -ForegroundColor Green
        for ($j = 0; $j -lt 15; $j++) {
            if ($j -lt $tLines.Count) {
                Write-Host $tLines[$j].TrimEnd()
            }
        }
    } else {
        Write-Host "No throughput table found." -ForegroundColor Red
    }
}
