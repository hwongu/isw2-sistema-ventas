@echo off
setlocal EnableExtensions

for %%I in ("%~dp0..") do set "PROJECT_DIR=%%~fI"
set "BUILD_DIR=%PROJECT_DIR%\dist\ventas-front\browser"
set "DEPLOY_DIR=%PROJECT_DIR%\deploy\ventas-front"

if not exist "%PROJECT_DIR%\package.json" goto :invalid_project

if not exist "%BUILD_DIR%\index.html" (
  echo No existe una compilacion lista. Ejecutando compilar-aplicacion.bat...
  call "%~dp0compilar-aplicacion.bat" || goto :build_error
)

echo.
echo Preparando carpeta para el despliegue externo...

if exist "%DEPLOY_DIR%" rmdir /s /q "%DEPLOY_DIR%"
mkdir "%DEPLOY_DIR%" || goto :copy_error

robocopy "%BUILD_DIR%" "%DEPLOY_DIR%" /E /NFL /NDL /NJH /NJS /NP
if errorlevel 8 goto :copy_error

if not exist "%DEPLOY_DIR%\config" mkdir "%DEPLOY_DIR%\config"
if exist "%PROJECT_DIR%\config\app-config.json" (
  copy /Y "%PROJECT_DIR%\config\app-config.json" "%DEPLOY_DIR%\config\app-config.json" >nul
) else (
  echo ADVERTENCIA: No se encontro config\app-config.json.
)

echo.
echo Carpeta de despliegue generada correctamente:
echo "%DEPLOY_DIR%"
echo.
echo Monta o copia el contenido de esta carpeta en la raiz publica de tu contenedor web.
exit /b 0

:copy_error
echo ERROR: No fue posible generar "%DEPLOY_DIR%".
exit /b 1

:build_error
echo ERROR: No fue posible compilar la aplicacion.
exit /b 1

:invalid_project
echo ERROR: No se encontro un proyecto Angular valido en "%PROJECT_DIR%".
exit /b 1
