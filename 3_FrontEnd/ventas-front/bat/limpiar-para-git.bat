@echo off
setlocal EnableExtensions

for %%I in ("%~dp0..") do set "PROJECT_DIR=%%~fI"

if not exist "%PROJECT_DIR%\package.json" goto :invalid_project
if not exist "%PROJECT_DIR%\angular.json" goto :invalid_project

echo.
echo Limpiando artefactos locales de ventas-front...

if exist "%PROJECT_DIR%\dist" rmdir /s /q "%PROJECT_DIR%\dist"
if exist "%PROJECT_DIR%\node_modules" rmdir /s /q "%PROJECT_DIR%\node_modules"
if exist "%PROJECT_DIR%\.angular" rmdir /s /q "%PROJECT_DIR%\.angular"
if exist "%PROJECT_DIR%\coverage" rmdir /s /q "%PROJECT_DIR%\coverage"
if exist "%PROJECT_DIR%\deploy" rmdir /s /q "%PROJECT_DIR%\deploy"
if exist "%PROJECT_DIR%\npm-debug.log" del /f /q "%PROJECT_DIR%\npm-debug.log"

echo.
echo Limpieza completada.
echo Se conservaron src, public, config, package.json y package-lock.json.
exit /b 0

:invalid_project
echo ERROR: No se encontro un proyecto Angular valido en "%PROJECT_DIR%".
exit /b 1
