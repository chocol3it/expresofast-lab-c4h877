const BASE_URL = 'http://localhost:8080/api';
const URL_ENVIOS = BASE_URL + '/envios';
const URL_VEHICULOS = BASE_URL + '/vehiculos';

let listaEnvios = [];
let bitacoraActual = [];

// ============================================================
// Utilidades de sesión
// ============================================================
function getToken() {
    return localStorage.getItem('jwt_token');
}

function getUsername() {
    return localStorage.getItem('username') || '';
}

function getRoles() {
    try {
        return JSON.parse(localStorage.getItem('roles') || '[]');
    } catch (e) {
        return [];
    }
}

function hasRole(role) {
    return getRoles().includes(role);
}

function logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    localStorage.removeItem('roles');
    window.location.href = 'login.html';
}

function fetchWithAuth(url, options = {}) {
    const headers = Object.assign(
        {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`
        },
        options.headers || {}
    );

    return fetch(url, Object.assign({}, options, { headers })).then(response => {
        if (response.status === 401 || response.status === 403) {
            logout();
            throw new Error('Sesión expirada o sin permisos suficientes.');
        }
        return response;
    });
}

// ============================================================
// Página de Login (login.html)
// ============================================================
const formLogin = document.getElementById('form-login');
if (formLogin) {
    formLogin.addEventListener('submit', function (e) {
        e.preventDefault();
        const errorBox = document.getElementById('login-error');
        errorBox.classList.add('hidden');

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        fetch(BASE_URL + '/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        })
            .then(async response => {
                if (!response.ok) {
                    throw new Error('Usuario o contraseña incorrectos.');
                }
                return response.json();
            })
            .then(data => {
                localStorage.setItem('jwt_token', data.token);
                localStorage.setItem('username', data.username);
                localStorage.setItem('roles', JSON.stringify(data.roles));
                window.location.href = 'index.html';
            })
            .catch(error => {
                errorBox.textContent = error.message;
                errorBox.classList.remove('hidden');
            });
    });
}

// ============================================================
// Tablero principal (index.html)
// ============================================================
const enviosGrid = document.getElementById('envios-grid');
if (enviosGrid) {

    window.onload = function () {
        if (!getToken()) {
            window.location.href = 'login.html';
            return;
        }
        aplicarVisibilidadPorRol();
        cargarEnvios();
        if (hasRole('ROLE_ADMIN')) {
            cargarFlota();
        }
    };

    function aplicarVisibilidadPorRol() {
        document.getElementById('session-username').textContent = getUsername();
        document.getElementById('session-role').textContent = getRoles().join(', ');

        const esSoloConductor = hasRole('ROLE_CONDUCTOR')
            && !hasRole('ROLE_ADMIN') && !hasRole('ROLE_OPERADOR');

        document.getElementById('panel-crear-envio').classList.toggle('hidden', esSoloConductor);
        document.getElementById('panel-flota').classList.toggle('hidden', !hasRole('ROLE_ADMIN'));
    }

    function cargarEnvios() {
        fetchWithAuth(URL_ENVIOS + '/optimizados')
            .then(response => response.json())
            .then(data => {
                listaEnvios = data;
                mostrarEnvios(listaEnvios);
            })
            .catch(error => {
                console.error('Error al cargar:', error);
            });
    }

    function mostrarEnvios(envios) {
        const contenedor = document.getElementById('envios-grid');
        contenedor.innerHTML = '';
        document.getElementById('contador-envios').textContent = envios.length + ' envíos';

        if (envios.length === 0) {
            contenedor.innerHTML = '<p>No hay envíos registrados.</p>';
            return;
        }

        const puedeCambiarEstado = hasRole('ROLE_ADMIN') || hasRole('ROLE_CONDUCTOR');
        const puedeVerBitacora = hasRole('ROLE_ADMIN') || hasRole('ROLE_OPERADOR');

        envios.forEach(envio => {
            const acciones = [];
            if (puedeCambiarEstado) {
                acciones.push(`<button class="btn btn-action btn-transito" onclick="cambiarEstado(${envio.id}, 'EN_TRANSITO')">En Tránsito</button>`);
                acciones.push(`<button class="btn btn-action btn-entregado" onclick="cambiarEstado(${envio.id}, 'ENTREGADO')">Entregado</button>`);
            }
            if (puedeVerBitacora) {
                acciones.push(`<button class="btn btn-action btn-bitacora" onclick="verBitacora(${envio.id})">Ver Bitácora</button>`);
            }

            contenedor.innerHTML += `
                <article class="card-envio">
                    <div class="card-header">
                        <strong>${envio.codigoRastreo}</strong>
                        <span class="pill-status pill-${envio.estadoEnvio}">${envio.estadoEnvio}</span>
                    </div>
                    <div class="card-body">
                        <p><strong>Destino:</strong> ${envio.direccionDestino}</p>
                        <p><strong>Peso:</strong> ${envio.pesoKg} kg | <strong>Costo:</strong> ₡${envio.costo}</p>
                        <p><strong>Vehículo:</strong> ${envio.placaVehiculo ?? 'N/A'}</p>
                        <p><strong>Conductor:</strong> ${envio.nombreConductor ?? 'N/A'}</p>
                    </div>
                    <div class="card-actions">
                        ${acciones.join('')}
                    </div>
                </article>
            `;
        });
    }

    const formEnvio = document.getElementById('form-envio');
    if (formEnvio) {
        formEnvio.addEventListener('submit', function (e) {
            e.preventDefault();

            const botonSubmit = e.target.querySelector('button[type="submit"]');
            if (botonSubmit.disabled) return;

            const nuevoEnvio = {
                codigoRastreo: document.getElementById('codigoRastreo').value,
                direccionDestino: document.getElementById('direccionDestino').value,
                pesoKg: parseFloat(document.getElementById('pesoKg').value),
                costo: parseFloat(document.getElementById('costo').value),
                vehiculoId: parseInt(document.getElementById('vehiculoId').value),
                conductorId: parseInt(document.getElementById('conductorId').value)
            };

            botonSubmit.disabled = true;

            fetchWithAuth(URL_ENVIOS, {
                method: 'POST',
                body: JSON.stringify(nuevoEnvio)
            })
                .then(async response => {
                    if (response.ok) {
                        alert('Envío registrado con éxito');
                        document.getElementById('form-envio').reset();
                        cargarEnvios();
                    } else {
                        const error = await response.json();
                        alert('Error al registrar: ' + (error.detail || 'Solicitud inválida.'));
                    }
                })
                .catch(error => console.error('Error en POST:', error))
                .finally(() => {
                    botonSubmit.disabled = false;
                });
        });
    }

    window.cambiarEstado = function (id, nuevoEstado) {
        fetchWithAuth(URL_ENVIOS + '/' + id + '/estado', {
            method: 'PATCH',
            body: JSON.stringify({ nuevoEstado })
        })
            .then(async response => {
                if (response.ok) {
                    cargarEnvios();
                } else {
                    const error = await response.json();
                    alert(error.detail || 'No se pudo actualizar el estado.');
                }
            })
            .catch(error => console.error('Error en PATCH:', error));
    };

    window.filtrar = function (estado) {
        if (estado === 'TODOS') {
            mostrarEnvios(listaEnvios);
        } else {
            mostrarEnvios(listaEnvios.filter(e => e.estadoEnvio === estado));
        }
    };

    // ---------- Bitácora ----------
    window.verBitacora = function (envioId) {
        fetchWithAuth(URL_ENVIOS + '/' + envioId + '/bitacora')
            .then(response => response.json())
            .then(data => {
                bitacoraActual = data;
                document.getElementById('bitacora-desde').value = '';
                document.getElementById('bitacora-hasta').value = '';
                renderBitacora();
                document.getElementById('modal-bitacora').classList.remove('hidden');
            })
            .catch(error => console.error('Error al cargar bitácora:', error));
    };

    window.cerrarBitacora = function () {
        document.getElementById('modal-bitacora').classList.add('hidden');
    };

    window.renderBitacora = function () {
        const desde = document.getElementById('bitacora-desde').value;
        const hasta = document.getElementById('bitacora-hasta').value;

        const filtradas = bitacoraActual.filter(item => {
            const fecha = item.fechaCambio.substring(0, 10);
            if (desde && fecha < desde) return false;
            if (hasta && fecha > hasta) return false;
            return true;
        });

        const contenedor = document.getElementById('bitacora-lista');
        contenedor.innerHTML = '';

        if (filtradas.length === 0) {
            contenedor.innerHTML = '<p>No hay registros de bitácora para el rango seleccionado.</p>';
            return;
        }

        filtradas.forEach(item => {
            contenedor.innerHTML += `
                <div class="bitacora-item">
                    <div class="transicion">${item.estadoAnterior} &rarr; ${item.estadoNuevo}</div>
                    <div class="meta">
                        ${new Date(item.fechaCambio).toLocaleString('es-CR')} · Usuario: ${item.usuario}
                    </div>
                    ${item.observaciones ? `<div class="meta">Observaciones: ${item.observaciones}</div>` : ''}
                </div>
            `;
        });
    };

    // ---------- Flota (solo ADMIN) ----------
    function cargarFlota() {
        fetchWithAuth(URL_VEHICULOS)
            .then(response => response.json())
            .then(data => mostrarFlota(data))
            .catch(error => console.error('Error al cargar flota:', error));
    }

    function mostrarFlota(vehiculos) {
        const contenedor = document.getElementById('flota-lista');
        if (!contenedor) return;
        contenedor.innerHTML = '';
        vehiculos.forEach(v => {
            contenedor.innerHTML += `
                <div class="flota-item">
                    <span>${v.placa} · ${v.capacidadKg} kg</span>
                    <span>${v.estado}</span>
                </div>
            `;
        });
    }

    const formVehiculo = document.getElementById('form-vehiculo');
    if (formVehiculo) {
        formVehiculo.addEventListener('submit', function (e) {
            e.preventDefault();

            const nuevoVehiculo = {
                placa: document.getElementById('v-placa').value,
                capacidadKg: parseFloat(document.getElementById('v-capacidad').value),
                estado: document.getElementById('v-estado').value,
                empresa: { id: parseInt(document.getElementById('v-empresaId').value) }
            };

            fetchWithAuth(URL_VEHICULOS, {
                method: 'POST',
                body: JSON.stringify(nuevoVehiculo)
            })
                .then(async response => {
                    if (response.ok) {
                        document.getElementById('form-vehiculo').reset();
                        cargarFlota();
                    } else {
                        const error = await response.json();
                        alert(error.detail || 'No se pudo crear el vehículo.');
                    }
                })
                .catch(error => console.error('Error al crear vehículo:', error));
        });
    }
}
