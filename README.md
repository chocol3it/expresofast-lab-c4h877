# ExpresoFast - Laboratorio 9

**Curso:** IF0009 - Desarrollo de Software IV
**Ciclo:** II-2026
**Laboratorio:** 9 - Procedimientos Almacenados, Paginación Relacional y Vistas HTML5 Paginadas
**Estudiante:** Andreé Murillo Sojo
**Carné:** C4H877

## Requisitos de Entorno

- Java 21+ (proyecto configurado con Java 21 local) y Maven.
- Spring Boot 4.x con Spring Data JPA, Spring Security y Hibernate.
- jjwt 0.12.5.
- Microsoft SQL Server (Developer Edition) y SSMS.
- Navegador web moderno con DevTools (para el frontend estático).
- JUnit 5 Jupiter, Mockito 5.x y MockMvc (incluidos vía `spring-boot-starter-test`).
- Plugin `jacoco-maven-plugin` 0.8.11 para análisis de cobertura de código.

## Guía de Configuración de Base de Datos

La base de datos `ExpresoFast_C4H877_II2026` ya contiene el esquema del Laboratorio 5
(`EmpresaLogistica`, `Vehiculo`, `Conductor`, `Envio`). Para dejar el esquema completo del
Laboratorio 6, ejecute en orden contra esa base de datos (con SSMS o `sqlcmd`):

1. `database/01_schema_lab5.sql` — documentación del esquema base (no reejecutar si ya existe).
2. `database/02_schema_lab6_extension.sql` — crea `Usuario`, `Rol`, `UsuarioRol` y `BitacoraEnvio`.
3. `database/03_data_seeds.sql` — inserta los 3 roles RBAC y los usuarios de prueba con su
   contraseña ya encriptada con BCrypt.
4. `database/04_schema_lab9_procedures.sql` — agrega la columna `destinatario` a `Envio` (si
   el backend ya corrió con `ddl-auto=update` esta columna puede existir; el script es
   idempotente) y crea los procedimientos `SP_OBTENER_ENVIOS_POR_ESTADO` y
   `SP_RESUMEN_METRICAS_ENVIOS`.
5. `database/05_data_lab9_seeds.sql` — inserta 15 envíos de prueba con estados variados
   (`PENDIENTE`, `EN_TRANSITO`, `ENTREGADO`, `CANCELADO`) para probar la paginación.

```powershell
sqlcmd -S localhost,1433 -U sa -P <password> -d ExpresoFast_C4H877_II2026 -i database\02_schema_lab6_extension.sql
sqlcmd -S localhost,1433 -U sa -P <password> -d ExpresoFast_C4H877_II2026 -i database\03_data_seeds.sql
sqlcmd -S localhost,1433 -U sa -P <password> -d ExpresoFast_C4H877_II2026 -i database\04_schema_lab9_procedures.sql
sqlcmd -S localhost,1433 -U sa -P <password> -d ExpresoFast_C4H877_II2026 -i database\05_data_lab9_seeds.sql
```

> **Nota:** `application.properties.template` trae `spring.jpa.hibernate.ddl-auto=update`
> para que Hibernate agregue automáticamente la columna `destinatario` al iniciar el backend
> sin correr el `ALTER TABLE` a mano. Los procedimientos almacenados **no** los crea
> Hibernate: siempre hay que ejecutar `04_schema_lab9_procedures.sql`. Una vez verificado el
> esquema en SSMS, regrese `ddl-auto` a `validate` para volver al modo estricto de Lab 8.

## Usuarios de Prueba

Contraseña para los tres usuarios: **`Password123!`**

| Username     | Contraseña     | Rol             |
|--------------|----------------|-----------------|
| `admin`      | `Password123!` | `ROLE_ADMIN`    |
| `operador1`  | `Password123!` | `ROLE_OPERADOR` |
| `conductor1` | `Password123!` | `ROLE_CONDUCTOR`|

## Instrucciones de Ejecución

### Backend

1. Copie `expresofast-backend/application.properties.template` a
   `expresofast-backend/src/main/resources/application.properties` y complete las credenciales de su
   SQL Server local y una clave `app.jwt.secret` propia (mínimo 32 caracteres).
2. Desde `expresofast-backend/`, ejecute:

   ```powershell
   mvn spring-boot:run
   ```

   El API queda disponible en `http://localhost:8080`.

### Frontend (SPA Angular - Laboratorio 10)

El cliente vigente es la SPA en `expresofast-frontend/` (Angular Standalone), que consume la
API en `http://localhost:8080/api/v1`. Ver la sección **Laboratorio 10** más abajo para
instrucciones de ejecución.

### Frontend legado (Laboratorios 8-9, archivado)

El cliente estático (HTML/CSS/JS puro con Fetch API) de los Laboratorios 8 y 9 se conserva
como referencia en `legacy/vanilla-js-console/` y consume la API en
`http://localhost:8080/api`. Ya no es el entregable activo, pero puede seguir ejecutándose:

1. Deje el backend corriendo (ver sección anterior).
2. Abra la carpeta `legacy/vanilla-js-console/` con VS Code y ejecute **Live Server** sobre `index.html`.
   El servidor debe quedar en el puerto **5500** (`http://localhost:5500` o
   `http://127.0.0.1:5500`), que es el origen permitido por CORS en el backend
   (`WebConfig` y `SecurityConfig`).
3. Inicie sesión con alguno de los usuarios de prueba. El token JWT se guarda en
   `sessionStorage` y la aplicación redirige a `dashboard.html`.

Archivos del cliente:

| Archivo          | Descripción                                                       |
|------------------|-------------------------------------------------------------------|
| `index.html`     | Vista de autenticación (`#loginForm`)                             |
| `dashboard.html` | Consola de operaciones (header, nav, main, aside y footer)        |
| `dashboard_paginado.html` | Consola paginada (Lab 9): filtros, tabla y navegador de páginas |
| `styles.css`     | Estilos con variables CSS, Grid, Flexbox y diseño Mobile-First    |
| `app.js`         | Login, consumo de la API con `fetch`, manejo de errores y roles   |
| `paginacion.js`  | Fetch async/await hacia `/api/v1/envios`, render de tabla y paginador |

Vistas por rol en el cliente:

| Rol              | Qué ve                                                                      |
|------------------|-----------------------------------------------------------------------------|
| `ROLE_ADMIN`     | Todo: bitácora de auditoría (`<aside>`), registrar vehículos, crear envíos, cambiar estados |
| `ROLE_OPERADOR`  | Crear envíos (asignar vehículo) y marcar envíos como `EN_TRANSITO`          |
| `ROLE_CONDUCTOR` | Tarjetas de envíos y botón para marcar `ENTREGADO`                          |

Manejo de errores: 401/403 limpian la sesión y redirigen a `index.html`; 400 y 404 se
muestran en una caja de alerta con los detalles del JSON RFC 7807.

## Pruebas y Cobertura de Código

El proyecto cuenta con una suite de pruebas unitarias (JUnit 5 + Mockito) para la capa de
lógica de negocio y pruebas de corte de controladores (`@WebMvcTest` + MockMvc) para la capa
web, con verificación automática de cobertura mediante JaCoCo.

### Ejecutar las pruebas

Desde `expresofast-backend/`:

```powershell
mvn clean test
```

Esto ejecuta toda la suite de pruebas y genera el reporte de cobertura. Para además construir
el artefacto final y validar el umbral mínimo de cobertura (falla el build si no se cumple):

```powershell
mvn clean verify
```

Un `BUILD SUCCESS` indica que todas las pruebas pasaron y que la cobertura de instrucciones en
el paquete `cr.ac.ucr.paraiso.ie.c4h877.expresofast.business` es de al menos **85%**.

### Ver el reporte de cobertura

Tras ejecutar `mvn clean test` o `mvn clean verify`, abra en el navegador:

```
expresofast-backend/target/site/jacoco/index.html
```

### Suite de pruebas incluida

- **Pruebas unitarias de servicios** (`business/`): `EnvioServiceTest`, `VehiculoServiceTest`,
  `EmpresaLogisticaServiceTest` y `AuthServiceTest`, aislando las dependencias con `@Mock` /
  `@InjectMocks` y cubriendo casos de éxito y de excepción (transiciones de estado inválidas,
  capacidad excedida, placas/recursos duplicados, credenciales incorrectas, etc.).
- **Pruebas de controladores REST** (`controller/`): `EnvioControllerTest` y
  `AuthControllerTest`, validando códigos de estado HTTP (200, 400, 401, 404) y la estructura
  de las respuestas JSON mediante `jsonPath`.
- **Pruebas parametrizadas** con `@ParameterizedTest` y `@CsvSource` para la matriz de cálculo
  de tarifas de envío según peso y distancia.

## Matriz de Permisos (RBAC)

| Endpoint                          | Método | Roles Permitidos               |
|------------------------------------|--------|---------------------------------|
| `/api/auth/login`                  | POST   | Público                        |
| `/api/envios/optimizados`          | GET    | ADMIN, OPERADOR, CONDUCTOR     |
| `/api/envios`                      | POST   | ADMIN, OPERADOR                |
| `/api/envios/{id}/estado`          | PATCH  | ADMIN, OPERADOR, CONDUCTOR     |
| `/api/envios/{id}/bitacora`        | GET    | ADMIN, OPERADOR                |
| `/api/vehiculos/**`                | ALL    | ADMIN                          |
| `/api/v1/envios`                   | GET    | ADMIN, OPERADOR, CONDUCTOR     |
| `/api/v1/envios/procedimiento/{estado}` | GET | ADMIN, OPERADOR, CONDUCTOR |

## Laboratorio 9 - Stored Procedures y Paginación Relacional

### Procedimientos almacenados

| Procedimiento                  | Parámetro     | Descripción                                                  |
|---------------------------------|---------------|---------------------------------------------------------------|
| `SP_OBTENER_ENVIOS_POR_ESTADO` | `@pEstado`    | Envíos de un estado, ordenados por `fecha_creacion` DESC.     |
| `SP_RESUMEN_METRICAS_ENVIOS`   | (ninguno)     | Conteo y suma de flete agrupados por `estado_envio` (reto).   |

En `EnvioRepository` se mapea con `@Procedure(procedureName = "SP_OBTENER_ENVIOS_POR_ESTADO")`
y se expone en `GET /api/v1/envios/procedimiento/{estado}`.

### Endpoint paginado

`GET /api/v1/envios` acepta `page` (default 0), `size` (default 5), `sortBy` (default
`fechaCreacion`), `direction` (`asc`/`desc`, default `desc`), `busqueda` (código, destinatario
o dirección) y `estado`. Internamente `EnvioService.listarPaginado` arma un
`PageRequest.of(page, size, Sort.by(...))` y `EnvioRepository.buscarPaginado` retorna un
`Page<Envio>` que se mapea a `Page<EnvioDTO>`. La respuesta JSON trae `content`, `number`,
`totalPages`, `totalElements`, `first` y `last`, que es lo que consume
`frontend/paginacion.js` para pintar la tabla y habilitar/deshabilitar los botones de
navegación (`legacy/vanilla-js-console/paginacion.js`, archivado).

### Notas de depuración

- El cliente siempre envía `page` en base 0 a la API, pero muestra `número de página + 1` en
  el indicador ("Página X de Y").
- Las consultas paginadas de `Envio` no usan `JOIN FETCH` sobre colecciones (el modelo solo
  tiene relaciones `@ManyToOne`), por lo que no aplica la advertencia `HHH000104`.

## Laboratorio 10 - SPA Angular Standalone

El cliente web se migró a una Single Page Application con **Angular Standalone**, ubicada en
`expresofast-frontend/`, que consume la API RESTful versionada en `/api/v1/envios`.

### Ejecutar el frontend

```powershell
cd expresofast-frontend
npm install
npm start
```

La aplicación queda disponible en `http://localhost:4200` y espera que el backend esté
corriendo en `http://localhost:8080` (ver `src/environments/environment.ts` para el
`API_URL` configurado).

### Vistas

| Ruta           | Componente             | Descripción                                              |
|----------------|-------------------------|-----------------------------------------------------------|
| `/envios`      | `EnvioListComponent`    | Tabla de envíos con insignia de estado y cambio de estado |
| `/nuevo-envio` | `EnvioFormComponent`    | Formulario de registro de un nuevo envío                  |
| `/rastreo`     | `EnvioTrackingComponent`| Búsqueda de un envío por código de rastreo                |

## Estructura del Repositorio

```
expresofast-lab-c4h877/
├── expresofast-backend/       <- API RESTful Spring Boot (Java 21)
├── expresofast-frontend/      <- SPA Angular Standalone (Laboratorio 10)
├── legacy/
│   └── vanilla-js-console/    <- Cliente HTML/CSS/JS de los Laboratorios 8-9 (archivado)
├── database/
├── docs/
└── README.md
```
