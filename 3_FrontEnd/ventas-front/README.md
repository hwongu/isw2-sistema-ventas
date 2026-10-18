# Sistema de Ventas

Frontend académico para operar los microservicios `ventas-service` y `pagos-service`. Está construido con componentes standalone, Angular Material y configuración externa en tiempo de ejecución.

## Versiones

| Herramienta | Versión |
|---|---:|
| Node.js | 20.17.0 |
| npm | 10.9.0 |
| Angular | 18.2.14 |
| Angular CLI global detectado | 18.2.7 |
| Angular CLI del proyecto | 18.2.7 |
| TypeScript | 5.5.4 |
| RxJS | 7.8.2 |
| Angular Material / CDK | 18.2.14 |

Las versiones del proyecto están fijadas en `package.json` para evitar actualizaciones automáticas. El tema usa la API Material 3 de Angular Material 18 (`mat.define-theme`).

## Endpoints utilizados

- `GET /productos`
- `GET /productos/{id}`
- `POST /productos`
- `PUT /productos/{id}`
- `DELETE /productos/{id}`
- `GET /ventas`
- `GET /ventas/{id}`
- `POST /ventas`
- `GET /pagos`
- `GET /pagos/{id}`
- `GET /pagos?ventaId={ventaId}`
- `POST /pagos`

No se implementan edición o eliminación de ventas ni pagos porque esos endpoints no forman parte de los contratos actuales.

## Instalación y ejecución local

```bash
npm ci
npm start
```

La aplicación estará en <http://localhost:4200>. Los microservicios configurados deben admitir solicitudes CORS desde ese origen.

## Compilación

```bash
npm run build
```

La salida del navegador se genera en `dist/ventas-front/browser/`. Esta carpeta puede copiarse al servidor web o al contenedor definido por la infraestructura externa.

## Scripts para Windows

La carpeta `bat/` contiene utilidades ejecutables desde cualquier ubicación:

```bat
bat\limpiar-para-git.bat
```

Elimina solamente artefactos regenerables: `dist`, `node_modules`, `.angular`, `coverage`, `deploy` y `npm-debug.log`. Conserva el código fuente, `package-lock.json` y los archivos de configuración con URLs.

```bat
bat\compilar-aplicacion.bat
```

Después de clonar o descargar el proyecto, ejecuta `npm ci`, compila Angular y verifica que exista `dist\ventas-front\browser\index.html`.

```bat
bat\generar-despliegue-docker.bat
```

Compila automáticamente si fuera necesario y genera `deploy\ventas-front` con los archivos estáticos y `config\app-config.json`. Esa carpeta queda preparada para copiarse o montarse desde un Docker Compose externo.

## Configuración externa

En desarrollo Angular sirve `public/config/app-config.json`. Para el despliegue se incluye `config/app-config.json`, que puede montarse o copiarse sobre `config/app-config.json` dentro de la raíz pública del servidor web:

```json
{
  "ventasApiUrl": "http://localhost:8080/ventas-service/api",
  "pagosApiUrl": "http://localhost:8080/pagos-service/api"
}
```

Para cambiar las URLs en el despliegue, edita o reemplaza el archivo externo. No es necesario modificar `src/` ni ejecutar nuevamente `ng build`. El servidor web utilizado por la infraestructura debe evitar cachear este archivo y configurar un fallback hacia `index.html` para las rutas de Angular.

La aplicación carga la configuración antes de iniciar mediante `APP_INITIALIZER`, que es la API vigente disponible en Angular 18.

## CORS

El navegador llama directamente desde el origen del frontend a `localhost:8080`. Si los orígenes son distintos, el backend debe autorizar el origen del frontend. No fue posible revisar la configuración CORS porque los proyectos Java no estaban presentes, y este proyecto no modifica los microservicios.

## Estructura principal

```text
src/app/
├── core/
│   ├── config/
│   └── services/
├── features/
│   ├── dashboard/
│   ├── productos/
│   ├── ventas/
│   └── pagos/
├── models/
├── app.component.*
├── app.config.ts
└── app.routes.ts
public/config/app-config.json
config/app-config.json
```
