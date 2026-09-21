const BASE_URL = 'http://localhost:8080/api';

// ============================================================
// Utilidades de sesión (JWT en sessionStorage)
// ============================================================
function getToken() {
    return sessionStorage.getItem('jwt_token');
}

function limpiarSesion() {
    sessionStorage.clear();
}

function irAlLogin() {
    limpiarSesion();
    window.location.href = 'index.html';
}

function decodificarToken(token) {
    try {
        const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(atob(payload));
    } catch (error) {
        return null;
    }
}

function obtenerRoles() {
    const datos = decodificarToken(getToken());
    if (!datos || !datos.roles) {
        return [];
    }
    return datos.roles.split(',');
}

function tieneRol(rol) {
    return obtenerRoles().includes(rol);
}

function escapeHtml(texto) {
    return String(texto ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// ============================================================
// Petición a la API con el token y manejo de errores HTTP
// ============================================================
async function llamarApi(ruta, opciones = {}) {
    const token = getToken();

    const respuesta = await fetch(BASE_URL + ruta, {
        ...opciones,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    });

    if (respuesta.status === 401 || respuesta.status === 403) {
        irAlLogin();
        throw new Error('Sesión no válida');
    }

    if (!respuesta.ok) {
        let problema = {};
        try {
            problema = await respuesta.json();
        } catch (error) {
        }
        const errores = extraerErrores(respuesta.status, problema);
        mostrarErrores(errores);
        throw new Error('Error HTTP ' + respuesta.status);
    }

    if (respuesta.status === 204) {
        return null;
    }
    return respuesta.json();
}

function extraerErrores(status, problema) {
    const errores = [];

    if (problema.invalidFields) {
        for (const campo in problema.invalidFields) {
            errores.push(campo + ': ' + problema.invalidFields[campo]);
        }
    }

    if (errores.length === 0) {
        if (status === 404) {
            errores.push(problema.detail || 'El recurso solicitado no existe.');
        } else {
            errores.push(problema.detail || 'Ocurrió un error inesperado (HTTP ' + status + ').');
        }
    }
    return errores;
}

function mostrarErrores(errores) {
    const caja = document.getElementById('alertaBox');
    const lista = document.getElementById('alertaLista');
    if (!caja || !lista) {
        return;
    }
    lista.innerHTML = errores.map(e => `<li>${escapeHtml(e)}</li>`).join('');
    caja.classList.remove('hidden');
}

function ocultarErrores() {
    const caja = document.getElementById('alertaBox');
    if (caja) {
        caja.classList.add('hidden');
    }
}

// ============================================================
// Login (index.html)
// ============================================================
const loginForm = document.getElementById('loginForm');

if (loginForm) {
    if (getToken()) {
        window.location.href = 'dashboard.html';
    }

    loginForm.addEventListener('submit', async function (evento) {
        evento.preventDefault();

        const cajaError = document.getElementById('loginError');
        cajaError.classList.add('hidden');

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        try {
            const respuesta = await fetch(BASE_URL + '/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (respuesta.status === 401) {
                throw new Error('Usuario o contraseña incorrectos.');
            }
            if (respuesta.status === 400) {
                throw new Error('Debe ingresar el usuario y la contraseña.');
            }
            if (!respuesta.ok) {
                throw new Error('No se pudo iniciar sesión (HTTP ' + respuesta.status + ').');
            }

            const datos = await respuesta.json();
            sessionStorage.setItem('jwt_token', datos.token);
            window.location.href = 'dashboard.html';
        } catch (error) {
            if (error instanceof TypeError) {
                cajaError.textContent = 'No se pudo conectar con el servidor.';
            } else {
                cajaError.textContent = error.message;
            }
            cajaError.classList.remove('hidden');
        }
    });
}

// ============================================================
// Dashboard (dashboard.html)
// ============================================================
const enviosGrid = document.getElementById('enviosGrid');

if (enviosGrid) {
    let envios = [];
    let filtroActual = 'TODOS';

    iniciarDashboard();

    function iniciarDashboard() {
        if (!getToken() || !decodificarToken(getToken())) {
            irAlLogin();
            return;
        }

        const datos = decodificarToken(getToken());
        document.getElementById('nombreUsuario').textContent = datos.sub;
        document.getElementById('rolUsuario').textContent = obtenerRoles()
            .map(r => r.replace('ROLE_', '')).join(', ');

        aplicarVistaPorRol();
        configurarEventos();
        cargarEnvios();
    }

    // ---------- RBAC en el cliente ----------
    function aplicarVistaPorRol() {
        const esAdmin = tieneRol('ROLE_ADMIN');
        const esOperador = tieneRol('ROLE_OPERADOR');

        document.getElementById('bitacoraPanel').classList.toggle('hidden', !esAdmin);
        document.getElementById('btnNuevoVehiculo').classList.toggle('hidden', !esAdmin);

        document.getElementById('btnNuevoEnvio').classList.toggle('hidden', !(esAdmin || esOperador));
    }

    function configurarEventos() {
        document.getElementById('btnLogout').addEventListener('click', irAlLogin);

        document.querySelectorAll('.filter-btn').forEach(boton => {
            boton.addEventListener('click', () => {
                filtroActual = boton.dataset.estado;
                document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
                boton.classList.add('active');
                mostrarEnvios();
            });
        });

        document.getElementById('btnNuevoEnvio').addEventListener('click', () => {
            document.getElementById('dialogEnvio').showModal();
        });
        document.getElementById('btnNuevoVehiculo').addEventListener('click', () => {
            document.getElementById('dialogVehiculo').showModal();
        });
        document.querySelectorAll('[data-cerrar]').forEach(boton => {
            boton.addEventListener('click', () => {
                document.getElementById(boton.dataset.cerrar).close();
            });
        });

        document.getElementById('formEnvio').addEventListener('submit', crearEnvio);
        document.getElementById('formVehiculo').addEventListener('submit', crearVehiculo);

        enviosGrid.addEventListener('click', evento => {
            const boton = evento.target.closest('button[data-accion]');
            if (!boton) {
                return;
            }
            const id = boton.dataset.id;
            if (boton.dataset.accion === 'bitacora') {
                verBitacora(id, boton.dataset.codigo);
            } else {
                cambiarEstado(id, boton.dataset.accion);
            }
        });
    }

    // ---------- Envíos ----------
    async function cargarEnvios() {
        try {
            envios = await llamarApi('/envios/optimizados');
            mostrarEnvios();
        } catch (error) {
            console.error(error);
        }
    }

    function mostrarEnvios() {
        const filtrados = filtroActual === 'TODOS'
            ? envios
            : envios.filter(e => e.estadoEnvio === filtroActual);

        actualizarKpis();
        document.getElementById('contadorEnvios').textContent = filtrados.length + ' envíos';

        if (filtrados.length === 0) {
            enviosGrid.innerHTML = '<p>No hay envíos para mostrar.</p>';
            return;
        }

        enviosGrid.innerHTML = filtrados.map(crearTarjeta).join('');
    }

    function crearTarjeta(envio) {
        const esAdmin = tieneRol('ROLE_ADMIN');
        const esOperador = tieneRol('ROLE_OPERADOR');
        const esConductor = tieneRol('ROLE_CONDUCTOR');
        const botones = [];

        if ((esAdmin || esOperador) && envio.estadoEnvio === 'PENDIENTE') {
            botones.push(`<button type="button" class="btn btn-warning" data-accion="EN_TRANSITO" data-id="${envio.id}">Marcar en tránsito</button>`);
        }
        if ((esAdmin || esConductor) && envio.estadoEnvio === 'EN_TRANSITO') {
            botones.push(`<button type="button" class="btn btn-success" data-accion="ENTREGADO" data-id="${envio.id}">Marcar entregado</button>`);
        }
        if (esAdmin) {
            botones.push(`<button type="button" class="btn btn-secondary" data-accion="bitacora" data-id="${envio.id}" data-codigo="${escapeHtml(envio.codigoRastreo)}">Ver bitácora</button>`);
        }

        return `
            <article class="card-envio estado-${escapeHtml(envio.estadoEnvio)}">
                <header class="card-header">
                    <h3>${escapeHtml(envio.codigoRastreo)}</h3>
                    <span class="estado-pill ${escapeHtml(envio.estadoEnvio)}">${escapeHtml(envio.estadoEnvio)}</span>
                </header>
                <section class="card-body">
                    <p><span>Destino:</span> ${escapeHtml(envio.direccionDestino)}</p>
                    <p><span>Peso:</span> ${escapeHtml(envio.pesoKg)} kg</p>
                    <p><span>Costo:</span> ₡${escapeHtml(envio.costo)}</p>
                    <p><span>Vehículo:</span> ${escapeHtml(envio.placaVehiculo ?? 'N/A')}</p>
                    <p><span>Conductor:</span> ${escapeHtml(envio.nombreConductor ?? 'N/A')}</p>
                </section>
                <footer class="card-actions">${botones.join('')}</footer>
            </article>
        `;
    }

    function actualizarKpis() {
        const entregados = envios.filter(e => e.estadoEnvio === 'ENTREGADO').length;
        const placasActivas = new Set(
            envios.filter(e => e.estadoEnvio === 'EN_TRANSITO' && e.placaVehiculo)
                .map(e => e.placaVehiculo)
        );

        document.getElementById('kpiTotal').textContent = envios.length;
        document.getElementById('kpiVehiculos').textContent = placasActivas.size;
        document.getElementById('kpiEntregados').textContent = entregados;
    }

    async function cambiarEstado(id, nuevoEstado) {
        ocultarErrores();
        try {
            await llamarApi('/envios/' + id + '/estado', {
                method: 'PATCH',
                body: JSON.stringify({ nuevoEstado })
            });
            cargarEnvios();
        } catch (error) {
            console.error(error);
        }
    }

    async function crearEnvio(evento) {
        evento.preventDefault();
        ocultarErrores();

        const nuevoEnvio = {
            codigoRastreo: document.getElementById('codigoRastreo').value,
            direccionDestino: document.getElementById('direccionDestino').value,
            pesoKg: parseFloat(document.getElementById('pesoKg').value),
            costo: parseFloat(document.getElementById('costo').value),
            vehiculoId: parseInt(document.getElementById('vehiculoId').value),
            conductorId: parseInt(document.getElementById('conductorId').value)
        };

        try {
            await llamarApi('/envios', {
                method: 'POST',
                body: JSON.stringify(nuevoEnvio)
            });
            document.getElementById('formEnvio').reset();
            document.getElementById('dialogEnvio').close();
            cargarEnvios();
        } catch (error) {
            document.getElementById('dialogEnvio').close();
            console.error(error);
        }
    }

    // ---------- Vehículos (solo ADMIN) ----------
    async function crearVehiculo(evento) {
        evento.preventDefault();
        ocultarErrores();

        const nuevoVehiculo = {
            placa: document.getElementById('vPlaca').value,
            capacidadKg: parseFloat(document.getElementById('vCapacidad').value),
            estado: document.getElementById('vEstado').value,
            empresa: { id: parseInt(document.getElementById('vEmpresaId').value) }
        };

        try {
            await llamarApi('/vehiculos', {
                method: 'POST',
                body: JSON.stringify(nuevoVehiculo)
            });
            document.getElementById('formVehiculo').reset();
            document.getElementById('dialogVehiculo').close();
        } catch (error) {
            document.getElementById('dialogVehiculo').close();
            console.error(error);
        }
    }

    // ---------- Bitácora (solo ADMIN) ----------
    async function verBitacora(id, codigo) {
        ocultarErrores();
        try {
            const registros = await llamarApi('/envios/' + id + '/bitacora');
            document.getElementById('bitacoraTitulo').textContent = 'Envío ' + codigo;

            const lista = document.getElementById('bitacoraLista');
            if (registros.length === 0) {
                lista.innerHTML = '<li>Sin cambios de estado registrados.</li>';
                return;
            }
            lista.innerHTML = registros.map(r => `
                <li>
                    <strong>${escapeHtml(r.estadoAnterior)} &rarr; ${escapeHtml(r.estadoNuevo)}</strong>
                    <small>${new Date(r.fechaCambio).toLocaleString('es-CR')} · ${escapeHtml(r.usuario)}</small>
                    ${r.observaciones ? `<small>${escapeHtml(r.observaciones)}</small>` : ''}
                </li>
            `).join('');
        } catch (error) {
            console.error(error);
        }
    }
}
