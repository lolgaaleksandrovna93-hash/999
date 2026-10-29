\$ErrorActionPreference = "Stop"

Write-Host "=== 1. Сборка проекта (без тестов) ===" -ForegroundColor Cyan
.\gradlew.bat build -x test
if (\$LASTEXITCODE -ne 0) { throw "Сборка не удалась" }

Write-Host "=== 2. Запуск SUT-сервера ===" -ForegroundColor Cyan
# ВАЖНО: Убрали -NoNewWindow. Теперь Start-Process запустит java и сразу вернет управление скрипту.
# Если у тебя jar-файл, используй строку ниже:
# Start-Process -FilePath "java" -ArgumentList "-jar", "artifacts\app-order.jar"

# Если у тебя класс (как в предыдущих примерах), используй эту строку:
Start-Process -FilePath "java" -ArgumentList "-cp", "build\classes\java\main", "SutServer"
# Если есть пакет: Start-Process -FilePath "java" -ArgumentList "-cp", "build\classes\java\main", "ru.netology.qa.SutServer"

# Небольшая пауза на старт JVM
Start-Sleep -Seconds 5

Write-Host "=== 3. Ожидание готовности SUT ===" -ForegroundColor Cyan
\$maxTries = 60
\$try = 0
\$uri = 'http://localhost:9999'

while ($try -lt $maxTries) {
    try {
        # Используем GET и SilentlyContinue, чтобы ошибка соединения не прерывала цикл
        $resp = Invoke-WebRequest -Uri $uri -Method Get -TimeoutSec 5 -UseBasicParsing -ErrorAction SilentlyContinue
        
        if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500) {
            Write-Host "SUT is ready (Status: $($resp.StatusCode))" -ForegroundColor Green
            break
        } else {
            Write-Host "SUT returned status $($resp.StatusCode). Waiting..." -ForegroundColor Yellow
        }
    } catch {
        Write-Host "Waiting for SUT... try $try. Error: $(\$_.Exception.Message)" -ForegroundColor Red
    }
    
    Start-Sleep -Seconds 2
    \$try++
}

if ($try -eq $maxTries) {
    Write-Host "SUT did not start in time." -ForegroundColor Red
    exit 1
}

Write-Host "=== 4. Запуск тестов ===" -ForegroundColor Cyan
.\gradlew.bat test
