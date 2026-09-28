USE ExpresoFast_C4H877_II2026;
GO

-- Laboratorio 9: 15 envios de prueba con estados variados para probar paginacion.
-- Usa los vehiculos (1, 2) y conductores (1, 2) creados en 01_schema_lab5.sql.

IF NOT EXISTS (SELECT 1 FROM Envio WHERE codigo_rastreo = 'EXP-9001')
INSERT INTO Envio (codigo_rastreo, destinatario, direccion_destino, peso_kg, costo, estado_envio, vehiculo_id, conductor_id, fecha_creacion)
VALUES
('EXP-9001', 'Maria Jimenez',   'Paraiso, Cartago',       12.50, 4500.00, 'PENDIENTE',   1, 1, DATEADD(DAY, -1, GETDATE())),
('EXP-9002', 'Jose Fernandez',  'Orosi, Cartago',         30.00, 8200.00, 'PENDIENTE',   2, 2, DATEADD(DAY, -2, GETDATE())),
('EXP-9003', 'Ana Solano',      'Cartago Centro',          5.20, 2100.00, 'PENDIENTE',   1, 1, DATEADD(DAY, -3, GETDATE())),
('EXP-9004', 'Luis Alvarado',   'Turrialba, Cartago',     45.00, 9800.00, 'EN_TRANSITO', 2, 2, DATEADD(DAY, -4, GETDATE())),
('EXP-9005', 'Karla Vargas',    'Tres Rios, Cartago',     18.75, 5300.00, 'EN_TRANSITO', 1, 1, DATEADD(DAY, -5, GETDATE())),
('EXP-9006', 'Diego Castro',    'La Suiza, Turrialba',     8.00, 3100.00, 'EN_TRANSITO', 2, 2, DATEADD(DAY, -6, GETDATE())),
('EXP-9007', 'Sofia Mendez',    'Paraiso, Cartago',       22.30, 6200.00, 'EN_TRANSITO', 1, 1, DATEADD(DAY, -7, GETDATE())),
('EXP-9008', 'Andres Chacon',   'Cachi, Paraiso',         14.00, 4700.00, 'ENTREGADO',   2, 2, DATEADD(DAY, -8, GETDATE())),
('EXP-9009', 'Paula Rojas',     'Juan Vinas, Cartago',     9.50, 3400.00, 'ENTREGADO',   1, 1, DATEADD(DAY, -9, GETDATE())),
('EXP-9010', 'Esteban Ureña',   'Tucurrique, Jimenez',    27.00, 7100.00, 'ENTREGADO',   2, 2, DATEADD(DAY, -10, GETDATE())),
('EXP-9011', 'Gabriela Nuñez',  'Pejibaye, Jimenez',      33.40, 8600.00, 'ENTREGADO',   1, 1, DATEADD(DAY, -11, GETDATE())),
('EXP-9012', 'Ricardo Salas',   'Cervantes, Alvarado',    16.80, 5000.00, 'ENTREGADO',   2, 2, DATEADD(DAY, -12, GETDATE())),
('EXP-9013', 'Natalia Quiros',  'Capellades, Alvarado',   40.00, 9200.00, 'CANCELADO',   1, 1, DATEADD(DAY, -13, GETDATE())),
('EXP-9014', 'Manuel Brenes',   'Paraiso, Cartago',        6.30, 2600.00, 'CANCELADO',   2, 2, DATEADD(DAY, -14, GETDATE())),
('EXP-9015', 'Valeria Rodriguez','Orosi, Cartago',        20.00, 5900.00, 'CANCELADO',   1, 1, DATEADD(DAY, -15, GETDATE()));
GO
