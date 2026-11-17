# test-jwt.ps1 - quick API test for direct-jwt-generation
# 1) Start the Spring Boot app first (DirectJwtApplication).
# 2) In PowerShell run:  .\test-jwt.ps1
# If scripts are blocked, run once:  Set-ExecutionPolicy -Scope Process Bypass

$base = "http://localhost:8080"
$json = @{ "Content-Type" = "application/json" }

function Decode-Base64Url([string]$s) {
    $s = $s.Replace('-', '+').Replace('_', '/')
    switch ($s.Length % 4) { 2 { $s += '==' } 3 { $s += '=' } }
    [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($s))
}

# Sends a request and returns the HTTP status code (works for 2xx and error codes)
function Post-Status([string]$url, [string]$body) {
    try {
        $r = Invoke-WebRequest -Uri $url -Method Post -Headers $json -Body $body -UseBasicParsing
        return [int]$r.StatusCode
    } catch {
        return [int]$_.Exception.Response.StatusCode
    }
}

Write-Host "`n=== 1. Generate token (expect 200) ===" -ForegroundColor Cyan
$body = '{"username":"Afreen","role":"ADMIN"}'
$resp = Invoke-RestMethod -Uri "$base/jwt/generate" -Method Post -Headers $json -Body $body
$token = $resp.token
Write-Host "Token: $token"

Write-Host "`n=== 2. Three-part check ===" -ForegroundColor Cyan
$parts = $token.Split('.')
Write-Host "Number of parts: $($parts.Length) (expected 3)"

Write-Host "`n=== 3. Decoded header and payload ===" -ForegroundColor Cyan
Write-Host "Header : $(Decode-Base64Url $parts[0])"
Write-Host "Payload: $(Decode-Base64Url $parts[1])"

Write-Host "`n=== 4. Verify valid token (expect 200) ===" -ForegroundColor Cyan
$vbody = (@{ token = $token } | ConvertTo-Json -Compress)
Invoke-RestMethod -Uri "$base/jwt/verify" -Method Post -Headers $json -Body $vbody | ConvertTo-Json

Write-Host "`n=== 5. Verify TAMPERED token (expect 401) ===" -ForegroundColor Cyan
$sig = $parts[2]
$newFirst = if ($sig[0] -eq 'A') { 'B' } else { 'A' }
$tampered = $parts[0] + "." + $parts[1] + "." + $newFirst + $sig.Substring(1)
$tbody = (@{ token = $tampered } | ConvertTo-Json -Compress)
Write-Host "Status: $(Post-Status "$base/jwt/verify" $tbody)"

Write-Host "`n=== 6. Missing role (expect 400) ===" -ForegroundColor Cyan
Write-Host "Status: $(Post-Status "$base/jwt/generate" '{"username":"Afreen"}')"

Write-Host "`n=== 7. Invalid JSON (expect 400) ===" -ForegroundColor Cyan
Write-Host "Status: $(Post-Status "$base/jwt/generate" '{ not json')"

Write-Host "`nDone." -ForegroundColor Green
