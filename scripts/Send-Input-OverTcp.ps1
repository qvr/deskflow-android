param(
    [string]$Hostname = "localhost",
    [int]$Port = 12345,
    [string]$Message
)

try {
		Write-Output "Sending message to $Hostname on port $Port > $Message"
    $client = New-Object System.Net.Sockets.TcpClient
    $client.Connect($Hostname, $Port)
    $stream = $client.GetStream()
    $writer = New-Object System.IO.StreamWriter($stream, [System.Text.Encoding]::ASCII)
    $writer.AutoFlush = $true
    $writer.Write($Message)
    $writer.Close()
    $client.Close()
}
catch {
    Write-Error "An error occurred: $($_.Exception.Message)"
}
