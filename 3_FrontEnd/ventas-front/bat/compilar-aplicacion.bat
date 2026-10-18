@echo off
setlocal EnableExtensions

for %%I in ("%~dp0..") do set "PROJECT_DIR=%%~fI"

if not exist "%PROJECT_DIR%\package.json" goto :invalid_project
if not exist "%PROJECT_DIR%\package-lock.json" goto :missing_lock

pushd "%PROJECT_DIR%" || goto :directory_error

echo.
echo Verificando Node.js y npm...
node --version || goto :command_error
call npm --version || goto :command_error

echo.
echo Instalando dependencias exactas con npm ci...
call npm ci || goto :build_error

echo.
echo Compilando la aplicacion Angular...
call npm run build || goto :build_error

if not exist "%PROJECT_DIR%\dist\ventas-front\browser\index.html" goto :output_error

echo.
echo Compilacion completada correctamente.
echo Salida: "%PROJECT_DIR%\dist\ventas-front\browser"
popd
exit /b 0

:command_error
echo ERROR: Node.js o npm no estan disponibles en PATH.
popd
exit /b 1

:build_error
echo ERROR: No fue posible instalar o compilar la aplicacion.
popd
exit /b 1

:output_error
echo ERROR: La compilacion termino, pero no se encontro index.html en la salida esperada.
popd
exit /b 1

:directory_error
echo ERROR: No fue posible acceder a "%PROJECT_DIR%".
exit /b 1

:invalid_project
echo ERROR: No se encontro package.json en "%PROJECT_DIR%".
exit /b 1

:missing_lock
echo ERROR: No se encontro package-lock.json. Ejecuta npm install para generarlo.
exit /b 1
