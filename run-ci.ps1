\$ErrorActionPreference = "Stop"

Write-Host "=== 1. Сборка проекта (без тестов) ===" -ForegroundColor Cyan
.\gradlew.bat build -x test
if (\$LASTEXITCODE -ne 0) { throw "Сборка не удалась" }

Write-Host "=== 2. Запуск SUT-сервера ===" -ForegroundColor Cyan
# Запускаем сервер в фоне
Start-Process -FilePath "java" -ArgumentList "-jar", "artifacts\app-order.jar" -NoNewWindow

# Небольшая пауза на старт JVM
Start-Sleep -Seconds 5

Write-Host "=== 3. Ожидание готовности SUT ===" -ForegroundColor Cyan
\$maxTries = 60
\$try = 0
# ВАЖНО: Используем GET вместо HEAD и правильный эндпоинт. 
# Если у тебя Spring Boot без кастомных контроллеров, скорее всего, нужен /actuator/health.
# Если это учебный проект Netology, попробуй сначала /, если не сработает - раскомментируй строку ниже.
\$uri = "http://localhost:9999/" 
# \$uri = "http://localhost:9999/actuator/health" 

while ($try -lt $maxTries) {
    try {
        # Используем GET, так как многие сервера блокируют HEAD на корне
        $resp = Invoke-WebRequest -Uri $uri -Method Get -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        if (\$resp.StatusCode -eq 200) {
            Write-Host "SUT is ready (Status: $($resp.StatusCode))." -ForegroundColor Green
            break
        }
        Write-Host "SUT returned status $($resp.StatusCode). Waiting... try \$try" -ForegroundColor Yellow
    } catch {
        Write-Host "Waiting for SUT... try $try. Error: $(\$_.Exception.Message)" -ForegroundColor Red
    }
    Start-Sleep -Seconds 2
    \$try++
}

if ($try -eq $maxTries) {
    Write-Host "SUT did not start in time." -ForegroundColor Red
    taskkill /F /IM java.exe 2>\$null
    exit 1
}

Write-Host "=== 4. Запуск тестов ===" -ForegroundColor Cyan
.\gradlew.bat test
$testExitCode = $LASTEXITCODE

Write-Host "=== 5. Остановка SUT ===" -ForegroundColor Cyan
taskkill /F /IM java.exe 2>\$null

exit \$testExitCode