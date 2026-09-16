

USE ExpresoFast_C4H877_II2026;
GO

-- 1. Tabla EmpresaLogistica
CREATE TABLE EmpresaLogistica (
    empresa_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    cedula_juridica VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- 2. Tabla Vehiculo
CREATE TABLE Vehiculo (
    vehiculo_id INT IDENTITY(1,1) PRIMARY KEY,
    placa VARCHAR(15) NOT NULL UNIQUE,
    capacidad_kg DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('DISPONIBLE', 'EN_RUTA', 'MANTENIMIENTO')),
    empresa_id INT NOT NULL,
    CONSTRAINT FK_Vehiculo_Empresa FOREIGN KEY (empresa_id) REFERENCES EmpresaLogistica(empresa_id)
);
GO

-- 3. Tabla Conductor
CREATE TABLE Conductor (
    conductor_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    licencia VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL
);
GO

-- 4. Tabla Envio
CREATE TABLE Envio (
    envio_id INT IDENTITY(1,1) PRIMARY KEY,
    codigo_rastreo VARCHAR(30) NOT NULL UNIQUE,
    direccion_destino VARCHAR(200) NOT NULL,
    peso_kg DECIMAL(10,2) NOT NULL,
    costo DECIMAL(10,2) NOT NULL,
    estado_envio VARCHAR(20) NOT NULL CHECK (estado_envio IN ('PENDIENTE', 'EN_TRANSITO', 'ENTREGADO', 'CANCELADO')),
    vehiculo_id INT NOT NULL,
    conductor_id INT NOT NULL,
    fecha_creacion DATETIME NULL,
    fecha_modificacion DATETIME NULL,
    CONSTRAINT FK_Envio_Vehiculo FOREIGN KEY (vehiculo_id) REFERENCES Vehiculo(vehiculo_id),
    CONSTRAINT FK_Envio_Conductor FOREIGN KEY (conductor_id) REFERENCES Conductor(conductor_id)
);
GO

-- Datos iniciales para pruebas
INSERT INTO EmpresaLogistica (nombre, cedula_juridica, telefono, fecha_registro)
VALUES ('ExpresoFast Central', '3-101-998877', '2550-0000', GETDATE());

INSERT INTO Vehiculo (placa, capacidad_kg, estado, empresa_id)
VALUES ('102938', 500.00, 'DISPONIBLE', 1),
       ('887766', 1200.00, 'DISPONIBLE', 1);

INSERT INTO Conductor (nombre, apellidos, licencia, telefono)
VALUES ('Carlos', 'Mora V.', 'B1-99882', '8888-1122'),
       ('Laura', 'Rojas S.', 'B2-33441', '8777-3344');
GO

USE ExpresoFast_C4H877_II2026;
SELECT envio_id, codigo_rastreo, estado_envio, peso_kg FROM Envio;