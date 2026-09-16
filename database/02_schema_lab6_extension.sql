

IF OBJECT_ID('dbo.Usuario', 'U') IS NULL
BEGIN
    CREATE TABLE Usuario (
        usuario_id       INT IDENTITY(1,1) PRIMARY KEY,
        username         VARCHAR(50)  NOT NULL UNIQUE,
        password_hash    VARCHAR(255) NOT NULL,
        nombre_completo  VARCHAR(100) NOT NULL,
        email            VARCHAR(100) NOT NULL UNIQUE,
        activo           BIT          NOT NULL DEFAULT 1
    );
END;

IF OBJECT_ID('dbo.Rol', 'U') IS NULL
BEGIN
    CREATE TABLE Rol (
        rol_id      INT IDENTITY(1,1) PRIMARY KEY,
        nombre_rol  VARCHAR(30) NOT NULL UNIQUE
    );
END;

IF OBJECT_ID('dbo.UsuarioRol', 'U') IS NULL
BEGIN
    CREATE TABLE UsuarioRol (
        usuario_id INT NOT NULL REFERENCES Usuario(usuario_id),
        rol_id     INT NOT NULL REFERENCES Rol(rol_id),
        CONSTRAINT PK_UsuarioRol PRIMARY KEY (usuario_id, rol_id)
    );
END;

IF OBJECT_ID('dbo.BitacoraEnvio', 'U') IS NULL
BEGIN
    CREATE TABLE BitacoraEnvio (
        bitacora_id      INT IDENTITY(1,1) PRIMARY KEY,
        envio_id         INT NOT NULL REFERENCES Envio(envio_id),
        estado_anterior  VARCHAR(20) NOT NULL,
        estado_nuevo     VARCHAR(20) NOT NULL,
        fecha_cambio     DATETIME    NOT NULL,
        usuario_id       INT NOT NULL REFERENCES Usuario(usuario_id),
        observaciones    VARCHAR(250) NULL
    );
END;
