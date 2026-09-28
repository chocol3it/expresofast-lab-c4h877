// Consola Paginada (dashboard_paginado.html): reutiliza las utilidades de app.js

if (!getToken() || !decodificarToken(getToken())) {
    irAlLogin();
}

let paginaActual = 0;
let modoStoredProcedure = false;

document.addEventListener('DOMContentLoaded', iniciarConsolaPaginada);

function iniciarConsolaPaginada() {
    const datos = decodificarToken(getToken());
    document.getElementById('nombreUsuario').textContent = datos.sub;
    document.getElementById('rolUsuario').textContent = obtenerRoles()
        .map(r => r.replace('ROLE_', '')).join(', ');

    document.getElementById('btnLogout').addEventListener('click', irAlLogin);

    document.getElementById('formFiltros').addEventListener('submit', evento => {
        evento.preventDefault();
        modoStoredProcedure = false;
        cargarPagina(0);
    });

    document.getElementById('btnStoredProcedure').addEventListener('click', ejecutarStoredProcedure);

    document.getElementById('btnPrimera').addEventListener('click', () => cargarPagina(0));
    document.getElementById('btnAnterior').addEventListener('click', () => cargarPagina(paginaActual - 1));
    document.getElementById('btnSiguiente').addEventListener('click', () => cargarPagina(paginaActual + 1));
    document.getElementById('btnUltima').addEventListener('click', () => {
        const totalPaginas = Number(document.getElementById('indicadorPagina').dataset.totalPaginas || 1);
        cargarPagina(totalPaginas - 1);
    });

    cargarPagina(0);
}

// ---------- Modo paginado (Pageable) ----------
async function cargarPagina(page) {
    ocultarErrores();
    document.getElementById('modoActivo').textContent = 'Modo: consulta paginada (Pageable).';

    const busqueda = document.getElementById('busqueda').value.trim();
    const estado = document.getElementById('estado').value;
    const sortBy = document.getElementById('sortBy').value;
    const direction = document.getElementById('direction').value;
    const size = document.getElementById('tamanoPagina').value;

    const parametros = new URLSearchParams();
    parametros.set('page', page);
    parametros.set('size', size);
    parametros.set('sortBy', sortBy);
    parametros.set('direction', direction);
    if (busqueda) {
        parametros.set('busqueda', busqueda);
    }
    if (estado) {
        parametros.set('estado', estado);
    }

    try {
        const respuesta = await llamarApi('/v1/envios?' + parametros.toString());
        modoStoredProcedure = false;
        paginaActual = respuesta.number;
        renderizarTabla(respuesta.content);
        renderizarPaginador(respuesta);
    } catch (error) {
        console.error(error);
    }
}

// ---------- Modo Stored Procedure ----------
async function ejecutarStoredProcedure() {
    ocultarErrores();

    const estado = document.getElementById('estado').value;
    if (!estado) {
        mostrarErrores(['Seleccione un estado específico para ejecutar el Stored Procedure.']);
        return;
    }

    try {
        const envios = await llamarApi('/v1/envios/procedimiento/' + estado);
        modoStoredProcedure = true;
        document.getElementById('modoActivo').textContent =
            'Modo: SP_OBTENER_ENVIOS_POR_ESTADO (' + estado + ') - sin paginación.';
        renderizarTabla(envios);
        renderizarPaginadorDeshabilitado(envios.length);
    } catch (error) {
        console.error(error);
    }
}

// ---------- Render ----------
function renderizarTabla(envios) {
    const cuerpo = document.getElementById('tablaEnviosBody');

    if (!envios || envios.length === 0) {
        cuerpo.innerHTML = '<tr><td colspan="5">No hay envíos para mostrar.</td></tr>';
        return;
    }

    cuerpo.innerHTML = envios.map(envio => `
        <tr>
            <td>${escapeHtml(envio.codigoRastreo)}</td>
            <td>${escapeHtml(envio.destinatario)}</td>
            <td>${escapeHtml(envio.direccionDestino)}</td>
            <td>₡${escapeHtml(envio.montoFlete)}</td>
            <td><span class="estado-pill ${escapeHtml(envio.estado)}">${escapeHtml(envio.estado)}</span></td>
        </tr>
    `).join('');
}

function renderizarPaginador(pagina) {
    const numeroMostrado = pagina.number + 1;
    const totalPaginas = Math.max(pagina.totalPages, 1);

    const indicador = document.getElementById('indicadorPagina');
    indicador.textContent = `Página ${numeroMostrado} de ${totalPaginas} (Total: ${pagina.totalElements} envíos)`;
    indicador.dataset.totalPaginas = totalPaginas;

    document.getElementById('btnPrimera').disabled = pagina.first;
    document.getElementById('btnAnterior').disabled = pagina.first;
    document.getElementById('btnSiguiente').disabled = pagina.last;
    document.getElementById('btnUltima').disabled = pagina.last;
}

function renderizarPaginadorDeshabilitado(totalRegistros) {
    const indicador = document.getElementById('indicadorPagina');
    indicador.textContent = `Resultado del Stored Procedure (Total: ${totalRegistros} envíos)`;
    indicador.dataset.totalPaginas = 1;

    document.getElementById('btnPrimera').disabled = true;
    document.getElementById('btnAnterior').disabled = true;
    document.getElementById('btnSiguiente').disabled = true;
    document.getElementById('btnUltima').disabled = true;
}
