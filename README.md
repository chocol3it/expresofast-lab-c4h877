# ExpresoFast - Laboratorio 11

**Curso:** IF0009 - Desarrollo de Software IV
**Ciclo:** II-2026
**Laboratorio:** 11 - Formularios Reactivos Avanzados y Consolidación Full-Stack
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
6. `database/06_schema_lab11_paquetes.sql` — crea `PAQUETES`, relacionada 1:N con `Envio`
   (`FK_Paquetes_Envios ... ON DELETE CASCADE`).

```powershell
sqlcmd -S localhost,1433 -U sa -P <password> -d ExpresoFast_C4H877_II2026 -i database\02_schema_lab6_extension.sql
sqlcmd -S localhost,1433 -U sa -P <password> -d ExpresoFast_C4H877_II2026 -i database\03_data_seeds.sql
sqlcmd -S localhost,1433 -U sa -P <password> -d ExpresoFast_C4H877_II2026 -i database\04_schema_lab9_procedures.sql
sqlcmd -S localhost,1433 -U sa -P <password> -d ExpresoFast_C4H877_II2026 -i database\05_data_lab9_seeds.sql
sqlcmd -S localhost,1433 -U sa -P <password> -d ExpresoFast_C4H877_II2026 -i database\06_schema_lab11_paquetes.sql
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

| Ruta           | Componente                    | Descripción                                                  |
|----------------|---------------------------------|---------------------------------------------------------------|
| `/envios`      | `EnvioListComponent`            | Tabla de envíos con insignia de estado, paquetes y cambio de estado |
| `/nuevo-envio` | `EnvioAvanzadoFormComponent`    | Formulario reactivo con paquetes dinámicos (Laboratorio 11)  |
| `/rastreo`     | `EnvioTrackingComponent`        | Búsqueda de un envío por código de rastreo (incluye paquetes) |

## Laboratorio 11 - Formularios Reactivos Avanzados

### Relación 1:N Envío-Paquetes

Un envío ahora puede tener varios paquetes. `PAQUETES` se relaciona con `Envio` mediante
`envio_id` (`FK_Paquetes_Envios ... ON DELETE CASCADE`). En JPA, `Envio` tiene
`@OneToMany(mappedBy = "envio", cascade = CascadeType.ALL, orphanRemoval = true)`, así que
`EnvioService.crearEnvioSimple(...)` arma el `Envio` con su `List<Paquete>` ya asociada y hace
**un solo** `envioRepository.save(envio)`: Hibernate inserta el envío y cada paquete dentro de
la misma transacción (`@Transactional`) gracias al cascade, sin necesidad de un
`PaqueteRepository` aparte.

### Endpoint de validación asíncrona

`GET /api/v1/envios/check-tracking/{trackingNumber}` retorna `true`/`false` según exista o no
ese `codigoRastreo`. Vive bajo `/api/v1/envios/**`, así que ya queda cubierto por el mismo
`permitAll()` que `SecurityConfig` dejó abierto en el Laboratorio 10 para toda la SPA — no hubo
que tocar seguridad otra vez.

### Formulario avanzado (`EnvioAvanzadoFormComponent`)

Reemplaza al antiguo `EnvioFormComponent` (basado en `[(ngModel)]`, eliminado) en la ruta
`/nuevo-envio`. Construido 100% con `ReactiveFormsModule` y `NonNullableFormBuilder`:

- **Typed Forms:** `form.controls.montoFlete` es `FormControl<number>`; asignarle un string no
  compila.
- **`FormArray` de paquetes:** cada paquete es un `FormGroup` (`descripcion`, `pesoKg`) dentro
  de `paquetes: FormArray`. "+ Añadir Paquete" hace `paquetesArray.push(...)`; el botón "X"
  llama `removeAt(i)` solo si `paquetesArray.length > 1`.
- **Validación cruzada síncrona:** `fechasValidator` se registra como validador del `FormGroup`
  raíz (no de un control individual) y compara `fechaEntregaEstimada > fechaDespacho`. El
  template muestra el error con `form.hasError('fechasInvalidas')` y el botón de submit se
  deshabilita con `[disabled]="form.invalid"`.
- **Validador asíncrono:** `trackingAsyncValidator()` en el control `numeroTracking` llama
  `EnvioService.checkTracking()` (con `debounceTime(400)` para no disparar una petición por
  cada tecla) y marca `{ trackingTomado: true }` si el backend confirma que el código ya existe.

> Nota de implementación: estas dos fechas son solo del formulario (demuestran el validador
> cruzado); el backend no las persiste porque ni el script SQL oficial del laboratorio ni la
> sección 2 del enunciado piden una columna para ellas — el `codigoRastreo` sí lo escribe el
> operador y sí viaja al backend, porque sin eso el validador asíncrono no tendría nada real
> contra qué comparar.

### Preguntas teóricas

**1. UX y escalabilidad: `FormArray` vs. 10 campos estáticos ocultos**

Con campos fijos (`paquete1`, `paquete2`, ... `paquete10` ocultos con `hidden`/`display:none`)
el formulario queda atado a un límite arbitrario: el día que un envío necesite 11 paquetes, hay
que tocar el HTML y recompilar. Además, esos 10 bloques existen en el DOM y en el árbol de
`FormGroup` desde el primer render aunque el usuario solo use 2: Angular sigue evaluando
`valueChanges`/`statusChanges` de los 8 controles "ocultos" en cada ciclo de detección de
cambios, y si el usuario llenó el campo 5 y luego lo "quita" visualmente, es fácil olvidar
limpiar su valor antes de armar el payload y terminar enviando basura al backend. Con
`FormArray`, la cantidad de controles en memoria es exactamente la cantidad de paquetes reales:
`paquetesArray.push()`/`removeAt()` crecen y encogen el arreglo en tiempo real, y
`paquetesArray.value` ya es un array limpio `{descripcion, pesoKg}[]` que calza uno a uno con
el `List<PaqueteDTO>` que espera `CrearEnvioDTO` — no hace falta un paso intermedio para filtrar
slots vacíos. La validación también se define **una sola vez** en `crearPaqueteGroup()` en vez
de repetir `Validators.required` a mano en 20 campos (10 descripciones + 10 pesos), así que
corregir una regla de negocio es un cambio en un solo lugar, no veinte. Para el usuario, la UX
es la de un formulario que crece con su flujo de trabajo real (un paquete hoy, siete mañana) en
vez de una pared fija de inputs vacíos que hay que escanear visualmente para saber cuáles aplican.

**2. Event Loop: validador síncrono vs. validador asíncrono**

JavaScript corre en un único hilo con una pila de llamadas y un *event loop* que solo saca la
siguiente tarea de la cola cuando la pila queda vacía. `fechasValidator` es una función
síncrona: se ejecuta íntegra dentro del mismo *tick* en que Angular recalcula la validez del
`FormGroup` (por ejemplo, al teclear en `fechaDespacho`), compara dos `Date` y retorna
`{fechasInvalidas: true} | null` antes de que el control vuelva a quedar libre — no hay
*hand-off* a la cola de tareas, por eso `form.errors` refleja el resultado en el mismo instante.
`trackingAsyncValidator`, en cambio, necesita hacer una petición HTTP real a
`/check-tracking/{codigo}`, y una llamada de red no puede resolverse dentro del mismo *frame* de
ejecución sin congelar el hilo (y con él, toda la UI) mientras se espera la respuesta. Por eso
Angular exige que un validador asíncrono retorne un `Observable` o una `Promise`: son las dos
abstracciones que tiene JavaScript para representar "este valor va a estar listo en un turno
futuro del *event loop*", de forma que Angular pueda hacer `subscribe()`/`.then()` y quedar a la
espera sin bloquear nada. En el momento en que se dispara la validación, Angular pone el control
en estado `PENDING` de inmediato y le devuelve el control al hilo principal; la petición HTTP la
gestiona el navegador fuera del motor de JS, y cuando la respuesta llega, su *callback* se
encola (vía la cola de microtareas para Promesas o la cola de tareas para el evento de red) y el
*event loop* lo ejecuta recién cuando la pila está libre — ahí es cuando finalmente corre
`map`/`catchError` y `setErrors({trackingTomado: true})`, pasando el control de `PENDING` a
`VALID`/`INVALID`. El mensaje "Verificando disponibilidad..." que se muestra mientras
`numeroTracking.pending` es `true` es, literalmente, la ventana de tiempo entre esos dos turnos
del *event loop*.

## Estructura del Repositorio

```
expresofast-lab-c4h877/
├── expresofast-backend/       <- API RESTful Spring Boot (Java 21)
├── expresofast-frontend/      <- SPA Angular Standalone (Laboratorios 10-11)
├── legacy/
│   └── vanilla-js-console/    <- Cliente HTML/CSS/JS de los Laboratorios 8-9 (archivado)
├── database/
├── docs/
└── README.md
```
