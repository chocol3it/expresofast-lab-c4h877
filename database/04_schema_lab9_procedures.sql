USE ExpresoFast_C4H877_II2026;
GO

-- Laboratorio 9: columna nueva para el resumen del envio (nombre del destinatario)
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Envio' AND COLUMN_NAME = 'destinatario'
)
BEGIN
    ALTER TABLE Envio ADD destinatario VARCHAR(100) NOT NULL DEFAULT 'Sin asignar';
END;
GO

-- 1. SP_OBTENER_ENVIOS_POR_ESTADO
-- Devuelve los envios de un estado dado, mas recientes primero.
IF OBJECT_ID('dbo.SP_OBTENER_ENVIOS_POR_ESTADO', 'P') IS NOT NULL
    DROP PROCEDURE SP_OBTENER_ENVIOS_POR_ESTADO;
GO

CREATE PROCEDURE SP_OBTENER_ENVIOS_POR_ESTADO
    @pEstado VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT envio_id, codigo_rastreo, destinatario, direccion_destino, peso_kg, costo,
           estado_envio, vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion
    FROM Envio
    WHERE estado_envio = @pEstado
    ORDER BY fecha_creacion DESC;
END;
GO

-- 2. SP_RESUMEN_METRICAS_ENVIOS (reto opcional)
-- Conteo de envios y suma de flete agrupados por estado.
IF OBJECT_ID('dbo.SP_RESUMEN_METRICAS_ENVIOS', 'P') IS NOT NULL
    DROP PROCEDURE SP_RESUMEN_METRICAS_ENVIOS;
GO

CREATE PROCEDURE SP_RESUMEN_METRICAS_ENVIOS
AS
BEGIN
    SET NOCOUNT ON;

    SELECT estado_envio,
           COUNT(*) AS total_envios,
           SUM(costo) AS suma_flete
    FROM Envio
    GROUP BY estado_envio;
END;
GO
