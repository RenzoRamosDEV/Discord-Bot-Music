@echo off
REM ============================================================
REM    Discord Music Bot - Ejecutar
REM ============================================================

:MENU
cls
echo.
echo ============================================================
echo          Discord Music Bot
echo ============================================================
echo.
echo  [1] Ejecutar Bot
echo  [2] Recompilar Proyecto
echo  [3] Ver Logs
echo  [4] Salir
echo.
echo ============================================================
echo.

set /p opcion="Selecciona una opcion (1-4): "

if "%opcion%"=="1" goto EJECUTAR
if "%opcion%"=="2" goto COMPILAR
if "%opcion%"=="3" goto LOGS
if "%opcion%"=="4" exit /b 0
goto MENU

REM ============================================================
:EJECUTAR
cls
echo.
echo ============================================================
echo          VERIFICANDO CONFIGURACION...
echo ============================================================
echo.

REM Verificar Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] Java no esta instalado
    echo     Descarga Java 17: https://adoptium.net/
    pause
    goto MENU
)
echo [OK] Java instalado

REM Verificar .env
if not exist .env (
    copy .env.example .env >nul 2>&1
    echo.
    echo [X] Archivo .env no existe
    echo     Creando .env desde .env.example...
    echo.
    echo     CONFIGURA TU TOKEN AHORA:
    echo.
    notepad .env
    pause
    goto MENU
)

findstr /C:"your_bot_token_here" .env >nul 2>&1
if %errorlevel% equ 0 (
    echo.
    echo [X] Token no configurado en .env
    echo.
    notepad .env
    pause
    goto MENU
)
echo [OK] Token configurado

REM Verificar JAR
if not exist "target\discord-music-bot-1.0.0.jar" (
    echo.
    echo [X] Proyecto no compilado
    echo     Selecciona opcion [2] para compilar
    echo.
    pause
    goto MENU
)
echo [OK] Proyecto compilado

echo.
echo ============================================================
echo          INICIANDO BOT...
echo ============================================================
echo.
echo Comandos Discord:
echo    /play ^<cancion^>  - Reproducir musica
echo    /stop            - Detener bot
echo    /queue           - Ver cola
echo.
echo Presiona Ctrl+C para detener
echo Logs: logs\discord-bot.log
echo.
echo ============================================================
echo.

java -Xmx512m -Xms256m -jar target\discord-music-bot-1.0.0.jar

if %errorlevel% neq 0 (
    echo.
    echo ============================================================
    echo [ERROR] El bot se detuvo inesperadamente
    echo ============================================================
    echo.
    echo Revisa los logs: logs\discord-bot.log
    echo.
    pause
)
goto MENU

REM ============================================================
:COMPILAR
cls
echo.
echo ============================================================
echo          COMPILANDO PROYECTO...
echo ============================================================
echo.

REM Detener procesos Java
echo [*] Deteniendo bot si esta corriendo...
taskkill /F /IM java.exe >nul 2>&1
if %errorlevel% == 0 (
    echo     [OK] Bot detenido
    timeout /t 2 /nobreak >nul
) else (
    echo     [i] Bot no estaba corriendo
)

echo.
echo [*] Compilando con Maven...
echo.

call ".maven\apache-maven-3.9.6\bin\mvn.cmd" clean package -DskipTests

if %errorlevel% == 0 (
    echo.
    echo ============================================================
    echo [OK] COMPILACION EXITOSA
    echo ============================================================
    echo.
    echo JAR: target\discord-music-bot-1.0.0.jar
    echo.
) else (
    echo.
    echo ============================================================
    echo [ERROR] ERROR EN LA COMPILACION
    echo ============================================================
    echo.
)

pause
goto MENU

REM ============================================================
:LOGS
cls
echo.
echo ============================================================
echo          LOGS DEL BOT
echo ============================================================
echo.

if not exist "logs\discord-bot.log" (
    echo [X] No hay logs disponibles
    echo     El bot aun no se ha ejecutado
    echo.
    pause
    goto MENU
)

echo Abriendo logs\discord-bot.log...
echo.
notepad logs\discord-bot.log

goto MENU
