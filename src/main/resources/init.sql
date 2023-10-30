CREATE TABLE funkos (
    ID                INTEGER AUTO_INCREMENT PRIMARY KEY,
    cod               VARCHAR(36) NOT NULL,
    MyId              NUMBER,
    nombre            VARCHAR,
    modelo            VARCHAR,
    precio            NUMBER,
    fecha_lanzamiento DATE,
    created_at        DATE,
    updated_at        DATE,
    CONSTRAINT modelo_funko CHECK (modelo IN ('MARVEL', 'DISNEY', 'ANIME', 'OTROS'))
);