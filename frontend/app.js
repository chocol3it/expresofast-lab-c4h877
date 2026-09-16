const URL_API = 'http://localhost:8080/api/envios';
let listaEnvios = [];

window.onload = function () {
    cargarEnvios();
};

function cargarEnvios() {
    fetch(URL_API + '/optimizados')
        .then(response => response.json())
        .then(data => {
            listaEnvios = data;
            mostrarEnvios(listaEnvios);
        })
        .catch(error => {
            console.error('Error al cargar:', error);
            alert('No se pudo conectar con el servidor.');
        });
}

function mostrarEnvios(envios) {
    const contenedor = document.getElementById('envios-grid');
    contenedor.innerHTML = '';

    if (envios.length === 0) {
        contenedor.innerHTML = '<p>No hay envíos registrados.</p>';
        return;
    }

    envios.forEach(envio => {
        let placa = envio.vehiculo ? envio.vehiculo.placa : 'N/A';
        let conductor = envio.conductor ? (envio.conductor.nombre + ' ' + envio.conductor.apellidos) : 'N/A';

        contenedor.innerHTML += `
            <article class="card-envio">
                <div class="card-header">
                    <strong>${envio.codigoRastreo}</strong>
                    <span class="pill-status pill-${envio.estadoEnvio}">${envio.estadoEnvio}</span>
                </div>
                <div class="card-body">
                    <p><strong>Destino:</strong> ${envio.direccionDestino}</p>
                    <p><strong>Peso:</strong> ${envio.pesoKg} kg | <strong>Costo:</strong> ₡${envio.costo}</p>
                    <p><strong>Vehículo:</strong> ${placa}</p>
                    <p><strong>Conductor:</strong> ${conductor}</p>
                </div>
                <div class="card-actions">
                    <button class="btn btn-transito" onclick="cambiarEstado(${envio.id}, 'EN_TRANSITO')">En Tránsito</button>
                    <button class="btn btn-entregado" onclick="cambiarEstado(${envio.id}, 'ENTREGADO')">Entregado</button>
                </div>
            </article>
        `;
    });
}

document.getElementById('form-envio').addEventListener('submit', function (e) {
    e.preventDefault();

    const botonSubmit = e.target.querySelector('button[type="submit"]');
    if (botonSubmit.disabled) return;

    const nuevoEnvio = {
        codigoRastreo: document.getElementById('codigoRastreo').value,
        direccionDestino: document.getElementById('direccionDestino').value,
        pesoKg: parseFloat(document.getElementById('pesoKg').value),
        costo: parseFloat(document.getElementById('costo').value),
        vehiculo: { id: parseInt(document.getElementById('vehiculoId').value) },
        conductor: { id: parseInt(document.getElementById('conductorId').value) }
    };

    botonSubmit.disabled = true;

    fetch(URL_API, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(nuevoEnvio)
    })
        .then(async response => {
            if (response.ok) {
                alert('Envío registrado con éxito');
                document.getElementById('form-envio').reset();
                cargarEnvios();
            } else {
                const error = await response.text();
                alert('Error al registrar: ' + error);
            }
        })
        .catch(error => {
            console.error('Error en POST:', error);
            alert('Error de conexión.');
        })
        .finally(() => {
            botonSubmit.disabled = false;
        });
});

function cambiarEstado(id, nuevoEstado) {
    fetch(URL_API + '/' + id + '/estado', {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ estadoEnvio: nuevoEstado })
    })
        .then(response => {
            if (response.ok) {
                cargarEnvios();
            } else {
                alert('No se pudo actualizar el estado.');
            }
        })
        .catch(error => console.error('Error en PATCH:', error));
}

function filtrar(estado) {
    if (estado === 'TODOS') {
        mostrarEnvios(listaEnvios);
    } else {
        const filtrados = listaEnvios.filter(e => e.estadoEnvio === estado);
        mostrarEnvios(filtrados);
    }
}
