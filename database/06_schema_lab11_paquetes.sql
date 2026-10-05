USE ExpresoFast_C4H877_II2026;
GO

-- Laboratorio 11: un envio ahora puede tener multiples paquetes (1:N).
-- Adaptado al esquema real del proyecto: la tabla se llama "Envio" (no "ENVIOS")
-- y su PK es "envio_id" INT (no "id" BIGINT como en el script generico del enunciado).
IF OBJECT_ID('dbo.PAQUETES', 'U') IS NULL
BEGIN
    CREATE TABLE PAQUETES (
        id INT IDENTITY(1,1) PRIMARY KEY,
        envio_id INT NOT NULL,
        descripcion VARCHAR(255) NOT NULL,
        peso_kg DECIMAL(5,2) NOT NULL,
        CONSTRAINT FK_Paquetes_Envios FOREIGN KEY (envio_id) REFERENCES Envio(envio_id) ON DELETE CASCADE
    );
END;
GO
