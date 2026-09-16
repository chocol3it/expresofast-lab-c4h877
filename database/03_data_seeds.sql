
IF NOT EXISTS (SELECT 1 FROM Rol WHERE nombre_rol = 'ROLE_ADMIN')
    INSERT INTO Rol (nombre_rol) VALUES ('ROLE_ADMIN');
IF NOT EXISTS (SELECT 1 FROM Rol WHERE nombre_rol = 'ROLE_OPERADOR')
    INSERT INTO Rol (nombre_rol) VALUES ('ROLE_OPERADOR');
IF NOT EXISTS (SELECT 1 FROM Rol WHERE nombre_rol = 'ROLE_CONDUCTOR')
    INSERT INTO Rol (nombre_rol) VALUES ('ROLE_CONDUCTOR');

IF NOT EXISTS (SELECT 1 FROM Usuario WHERE username = 'admin')
    INSERT INTO Usuario (username, password_hash, nombre_completo, email, activo)
    VALUES ('admin', '$2a$10$53D6nlmGRklhb0oJSjvaxeDZceuMIoaCyIu7kZakkWpTbpOSW9wkG',
            'Administrador General', 'admin@expresofast.cr', 1);

IF NOT EXISTS (SELECT 1 FROM Usuario WHERE username = 'operador1')
    INSERT INTO Usuario (username, password_hash, nombre_completo, email, activo)
    VALUES ('operador1', '$2a$10$GbwgOfM1E778H/xQ/v0.Q.xtYKdbno8Ak9vM6F45TthuWsBRyrm/u',
            'Operador de Logistica', 'operador1@expresofast.cr', 1);

IF NOT EXISTS (SELECT 1 FROM Usuario WHERE username = 'conductor1')
    INSERT INTO Usuario (username, password_hash, nombre_completo, email, activo)
    VALUES ('conductor1', '$2a$10$uyMM6TNbLL9vE/fo61eSEeiotQeG2rpYuGL.ZQh2flQ2LmHOe4gX2',
            'Conductor de Ruta', 'conductor1@expresofast.cr', 1);

INSERT INTO UsuarioRol (usuario_id, rol_id)
SELECT u.usuario_id, r.rol_id
FROM Usuario u, Rol r
WHERE u.username = 'admin' AND r.nombre_rol = 'ROLE_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM UsuarioRol ur WHERE ur.usuario_id = u.usuario_id AND ur.rol_id = r.rol_id
  );

INSERT INTO UsuarioRol (usuario_id, rol_id)
SELECT u.usuario_id, r.rol_id
FROM Usuario u, Rol r
WHERE u.username = 'operador1' AND r.nombre_rol = 'ROLE_OPERADOR'
  AND NOT EXISTS (
      SELECT 1 FROM UsuarioRol ur WHERE ur.usuario_id = u.usuario_id AND ur.rol_id = r.rol_id
  );

INSERT INTO UsuarioRol (usuario_id, rol_id)
SELECT u.usuario_id, r.rol_id
FROM Usuario u, Rol r
WHERE u.username = 'conductor1' AND r.nombre_rol = 'ROLE_CONDUCTOR'
  AND NOT EXISTS (
      SELECT 1 FROM UsuarioRol ur WHERE ur.usuario_id = u.usuario_id AND ur.rol_id = r.rol_id
  );
