param(
    [string]$BackendUrl = $env:SENTINEL_API_BASE_URL,
    [string]$Token = $env:SENTINEL_SMOKE_TEST_TOKEN,
    [switch]$IncludeAiCheck
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($BackendUrl)) {
    throw "Set SENTINEL_API_BASE_URL or pass -BackendUrl."
}
if ([string]::IsNullOrWhiteSpace($Token)) {
    throw "Set SENTINEL_SMOKE_TEST_TOKEN to a valid JWT from /login or pass -Token."
}

$baseUrl = $BackendUrl.TrimEnd("/")
$headers = @{ Authorization = "Bearer $Token" }

function Invoke-SentinelGet {
    param([Parameter(Mandatory)][string]$Path)

    $request = @{
        Method = "Get"
        Uri = "$baseUrl$Path"
        Headers = $headers
        TimeoutSec = 25
    }
    $result = Invoke-RestMethod @request
    Write-Host "PASS GET $Path"
    return ,$result
}

Write-Host "Checking Sentinel API at $baseUrl"

$healthRequest = @{
    Method = "Get"
    Uri = "$baseUrl/actuator/health/readiness"
    TimeoutSec = 15
}
$health = Invoke-RestMethod @healthRequest
if ($health.status -ne "UP") {
    throw "Readiness check did not report UP."
}
Write-Host "PASS GET /actuator/health/readiness"

$null = Invoke-SentinelGet "/dashboard"
$projects = Invoke-SentinelGet "/projects"
$null = Invoke-SentinelGet "/services"
$null = Invoke-SentinelGet "/endpoints"
$null = Invoke-SentinelGet "/metrics/latest"
$null = Invoke-SentinelGet "/alerts/active"
$incidents = Invoke-SentinelGet "/incidents"
$null = Invoke-SentinelGet "/actuator/prometheus"

if ($projects -and $projects.Count -gt 0 -and $projects[0].id) {
    $projectId = $projects[0].id
    $null = Invoke-SentinelGet "/service-dependencies/project/$projectId/graph"
} else {
    Write-Host "SKIP dependency graph: no project exists for this account yet."
}

# Verify the authenticated event stream responds with the expected content type.
$httpClient = [System.Net.Http.HttpClient]::new()
$streamResponse = $null
try {
    $httpClient.DefaultRequestHeaders.Authorization =
        [System.Net.Http.Headers.AuthenticationHeaderValue]::new("Bearer", $Token)

    $streamResponse = $httpClient.GetAsync(
        "$baseUrl/events/stream",
        [System.Net.Http.HttpCompletionOption]::ResponseHeadersRead
    ).GetAwaiter().GetResult()

    if ($streamResponse.StatusCode -ne [System.Net.HttpStatusCode]::OK) {
        throw "SSE stream returned HTTP $([int]$streamResponse.StatusCode)."
    }

    $mediaType = $streamResponse.Content.Headers.ContentType.MediaType
    if ($mediaType -ne "text/event-stream") {
        throw "SSE stream returned unexpected content type '$mediaType'."
    }

    Write-Host "PASS GET /events/stream (text/event-stream)"
}
finally {
    if ($streamResponse) { $streamResponse.Dispose() }
    $httpClient.Dispose()
}

if ($IncludeAiCheck) {
    if ($incidents -and $incidents.Count -gt 0 -and $incidents[0].id) {
        $incidentId = $incidents[0].id
        $aiRequest = @{
            Method = "Post"
            Uri = "$baseUrl/ai/incidents/$incidentId/analysis"
            Headers = $headers
            ContentType = "application/json"
            Body = "{}"
            TimeoutSec = 90
        }
        $null = Invoke-RestMethod @aiRequest
        Write-Host "PASS POST /ai/incidents/$incidentId/analysis"
    } else {
        Write-Host "SKIP AI analysis: create an incident before running -IncludeAiCheck."
    }
}

Write-Host ""
Write-Host "Sentinel API smoke checks completed successfully."
