drop database punto_de_venta;
create database punto_de_venta;
use punto_de_venta;
-- 1. Tabla para estados
CREATE TABLE cState (
    id INT AUTO_INCREMENT NOT NULL,
    nombre_estado NVARCHAR(50) NOT NULL, -- Por ejemplo: 'Pendiente', 'Completado'
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_cState PRIMARY KEY (id)
);
INSERT INTO cState (id,nombre_estado) VALUES
    (1,'Disponible'),
    (2,'Cancelado'),
    (3,'Pendiente'),
    (4,'Vendido'),
    (5,'En espera'),
    (6,'Pagado'),
    (7,'No disponible'),
    (8,'Caducado'),
    (9,'Reservado'),
    (10,'En proceso');

-- 11. usuarios
CREATE TABLE usuario (
    id INT AUTO_INCREMENT NOT NULL,  -- ID único del usuario
    nombre NVARCHAR(100) NOT NULL,  -- Nombre del usuario
    correo NVARCHAR(100) NOT NULL UNIQUE,  -- Correo electrónico (único)
    telefono NVARCHAR(20) NOT NULL UNIQUE,
    contrasena NVARCHAR(255) NOT NULL,  -- Contraseña encriptada
    estado BIT NOT NULL DEFAULT 1,  -- Estado: 1 = activo, 0 = inactivo
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Fecha de creación
    fecha_ultima_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- Fecha de última actualización
    CONSTRAINT PK_usuario PRIMARY KEY (id)
);

-- 2. Tabla para productos
CREATE TABLE producto (
    id INT AUTO_INCREMENT NOT NULL,
    nombre NVARCHAR(100) NOT NULL,
    codigo NVARCHAR(255) NOT NULL,
    descripcion NVARCHAR(255) NULL,
    categoria NVARCHAR(50) NULL,
    precio DECIMAL(10, 2) NOT NULL,
    costo DECIMAL(10, 2) NOT NULL,
    id_usuario INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_producto_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT PK_producto PRIMARY KEY (id)
);

-- 3. Tabla para imagenes del producto
CREATE TABLE producto_imagen (
    id INT AUTO_INCREMENT NOT NULL,
    producto_id INT NOT NULL,
    imagen LONGBLOB NOT NULL, -- Almacena la imagen como un archivo binario
    descripcion VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE
);

-- 4. Tabla para lotes
CREATE TABLE lote (
    id INT AUTO_INCREMENT NOT NULL,
    id_producto INT NOT NULL,           -- Relación con producto
    cantidad INT NOT NULL,              -- Cantidad disponible en este lote
    fecha_caducidad DATE NULL,      -- Fecha de caducidad del lote
    fecha_entrada DATETIME NOT NULL,    -- Fecha en que el lote fue ingresado al inventario
    id_state INT NOT NULL DEFAULT 1,    -- Estado del lote (Activo, Caducado)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_lote PRIMARY KEY (id),
    CONSTRAINT FK_lote_producto FOREIGN KEY (id_producto) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT FK_lote_state FOREIGN KEY (id_state) REFERENCES cState(id) ON DELETE CASCADE
);

-- 5. Tabla para entradas
CREATE TABLE entrada (
    id INT AUTO_INCREMENT NOT NULL,
    fecha DATETIME NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    id_state INT NOT NULL, -- Relación con cState
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_entrada PRIMARY KEY (id),
    CONSTRAINT FK_entrada_cState FOREIGN KEY (id_state) REFERENCES cState(id) ON DELETE CASCADE
);

-- 6. Detalle de entradas
CREATE TABLE detalle_entrada (
    id INT AUTO_INCREMENT NOT NULL,
    id_entrada INT NOT NULL,            -- Relación con la tabla de entradas
    id_producto INT NOT NULL,           -- Relación con producto
    id_lote INT NOT NULL,               -- Relación con lote
    cantidad INT NOT NULL,              -- Cantidad ingresada
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_detalle_entrada PRIMARY KEY (id),
    CONSTRAINT FK_detalle_entrada_entrada FOREIGN KEY (id_entrada) REFERENCES entrada(id) ON DELETE CASCADE,
    CONSTRAINT FK_detalle_entrada_producto FOREIGN KEY (id_producto) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT FK_detalle_entrada_lote FOREIGN KEY (id_lote) REFERENCES lote(id) ON DELETE CASCADE
);

-- 7. Tabla perdidas (simplificada, sin detalle_perdida)
CREATE TABLE perdida (
    id INT AUTO_INCREMENT NOT NULL,
    id_producto INT NOT NULL,           -- Relación con producto
    id_lote INT NOT NULL,               -- Relación con lote
    cantidad INT NOT NULL CHECK (cantidad >= 0), -- Cantidad perdida
    costo_unitario DECIMAL(10, 2) NOT NULL, -- Costo unitario del producto perdido
    total DECIMAL(10, 2) NOT NULL,      -- Total de la pérdida (cantidad * costo_unitario)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_perdida PRIMARY KEY (id),
    CONSTRAINT FK_perdida_producto FOREIGN KEY (id_producto) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT FK_perdida_lote FOREIGN KEY (id_lote) REFERENCES lote(id) ON DELETE CASCADE
);

-- 8. Tabla para ventas
CREATE TABLE venta (
    id INT AUTO_INCREMENT NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    fecha DATETIME NOT NULL,
    id_state INT NOT NULL, -- Relación con cState
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_venta PRIMARY KEY (id),
    CONSTRAINT FK_venta_cState FOREIGN KEY (id_state) REFERENCES cState(id) ON DELETE CASCADE
);

-- 9. Detalle de ventas
CREATE TABLE detalle_venta (
    id INT AUTO_INCREMENT NOT NULL,
    id_venta INT NOT NULL, -- Relación con venta
    id_producto INT NOT NULL, -- Relación con producto
    id_lote INT NULL, -- Relación con lote
    id_state INT DEFAULT 6,  -- estado pagado
    costo_unitario DECIMAL(10, 2) NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    cantidad INT NOT NULL,
    id_promocion INT NULL, -- Relación con promocion
    descuento_aplicado DECIMAL(10, 2) NULL, -- Descuento aplicado
    subtotal DECIMAL(10, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_detalle_venta PRIMARY KEY (id),
    CONSTRAINT FK_detalle_venta_venta FOREIGN KEY (id_venta) REFERENCES venta(id) ON DELETE CASCADE,
    CONSTRAINT FK_detalle_venta_producto FOREIGN KEY (id_producto) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT FK_detalle_venta_lote FOREIGN KEY (id_lote) REFERENCES lote(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_venta_state FOREIGN KEY (id_state) REFERENCES cState(id)
);

-- 10. Tabla para promociones
CREATE TABLE promocion (
    id INT AUTO_INCREMENT NOT NULL,
    id_producto INT NOT NULL, -- Producto al que aplica la promoción
    nombre NVARCHAR(100) NOT NULL,
    tipo NVARCHAR(50) NOT NULL, -- Tipo de promoción: porcentaje o monto fijo
    valor_descuento DECIMAL(10, 2) NULL, -- Porcentaje o monto fijo
    precio_final DECIMAL(10, 2) NULL, -- Precio final calculado, si aplica
    cantidad_necesaria INT NOT NULL DEFAULT 1, -- Cantidad mínima del producto
    activo BIT NOT NULL DEFAULT 1, -- Si la promoción está activa
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_promocion PRIMARY KEY (id),
    CONSTRAINT FK_promocion_producto FOREIGN KEY (id_producto) REFERENCES producto(id) ON DELETE CASCADE
);

-- 12. roles
CREATE TABLE rol (
    id INT AUTO_INCREMENT NOT NULL,  -- ID único del rol
    nombre NVARCHAR(50) NOT NULL,  -- Nombre del rol (ej. 'Administrador', 'Vendedor')
    descripcion NVARCHAR(255) NULL,  -- Descripción del rol
    CONSTRAINT PK_rol PRIMARY KEY (id)
);

INSERT INTO rol (id, nombre, descripcion) VALUES
    (1, 'Administrador', 'Acceso completo al sistema'),
    (2, 'Moderador', 'Puede gestionar contenido y usuarios'),
    (3, 'Vendedor', 'Usuario estándar con permisos básicos'),
    (4, 'Invitado', 'Acceso limitado solo para ver información'),
    (5, 'Soporte Técnico', 'Resuelve problemas técnicos del sistema'),
    (6, 'Supervisor', 'Supervisa y monitorea actividades'),
    (7, 'Desarrollador', 'Desarrolla y mantiene el sistema'),
    (8, 'Auditor', 'Acceso para revisar actividades y registros'),
    (9, 'Operador', 'Gestiona tareas operativas específicas'),
    (10, 'Gerente', 'Toma decisiones basadas en reportes');

-- 13. detalle roles usuario
CREATE TABLE usuario_rol (
    id_usuario INT NOT NULL,  -- ID del usuario
    id_rol INT NOT NULL,  -- ID del rol
    CONSTRAINT PK_usuario_rol PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT FK_usuario_rol_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id),
    CONSTRAINT FK_usuario_rol_rol FOREIGN KEY (id_rol) REFERENCES rol(id)
);


-- 14. permisos
CREATE TABLE permiso (
    id INT AUTO_INCREMENT NOT NULL,  -- ID único del permiso
    nombre NVARCHAR(50) NOT NULL,  -- Nombre del permiso (ej. 'Crear Producto', 'Eliminar Venta')
    descripcion NVARCHAR(255) NULL,  -- Descripción del permiso
    CONSTRAINT PK_permiso PRIMARY KEY (id)
);

INSERT INTO permiso (id, nombre, descripcion) VALUES
    (1, 'Crear','Permite crear nuevos registros o elementos'),
    (2, 'Leer','Permite leer información del sistema'),
    (3, 'Actualizar','Permite modificar registros existentes'),
    (4, 'Eliminar','Permite eliminar registros'),
    (5, 'Configurar','Permite acceder a configuraciones del sistema'),
    (6, 'Gestionar Usuarios','Permite agregar, modificar o eliminar usuarios'),
    (7, 'Acceder a Reportes','Permite ver reportes generados por el sistema'),
    (8, 'Exportar Datos','Permite exportar información a archivos externos'),
    (9, 'Importar Datos','Permite importar información desde archivos externos'),
    (10, 'Ver Estadísticas','Permite ver estadísticas y métricas del sistema'),
    (11, 'Aprobar Solicitudes','Permite aprobar solicitudes o procesos pendientes'),
    (12, 'Revertir Cambios','Permite deshacer cambios realizados'),
    (13, 'Realizar Backups','Permite crear respaldos de información'),
    (14, 'Restaurar Backups','Permite restaurar información desde respaldos'),
    (15, 'Acceso API','Permite interactuar con la API del sistema'),
    (16, 'Gestionar Roles','Permite crear, modificar o eliminar roles'),
    (17, 'Auditar Actividades','Permite revisar y auditar actividades'),
    (18, 'Gestionar Permisos','Permite asignar o revocar permisos'),
    (19, 'Gestionar Inventario','Permite gestionar productos o recursos'),
    (20, 'Ver Log de Actividades', 'Permite ver registros de actividades realizadas');


-- 15. 
CREATE TABLE rol_permiso (
    id_rol INT NOT NULL,  -- ID del rol
    id_permiso INT NOT NULL,  -- ID del permiso
    CONSTRAINT PK_rol_permiso PRIMARY KEY (id_rol, id_permiso),
    CONSTRAINT FK_rol_permiso_rol FOREIGN KEY (id_rol) REFERENCES rol(id),
    CONSTRAINT FK_rol_permiso_permiso FOREIGN KEY (id_permiso) REFERENCES permiso(id)
);

INSERT INTO rol_permiso (id_rol, id_permiso) VALUES
    -- Permisos de Administrador (Acceso completo)
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
    (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 20),

    -- Permisos de Moderador
    (2, 2), (2, 3), (2, 4), (2, 6), (2, 7), (2, 10),

    -- Permisos de Usuario
    (3, 2), (3, 7), (3, 10),

    -- Permisos de Invitado
    (4, 2), (4, 10),

    -- Permisos de Soporte Técnico
    (5, 2), (5, 5), (5, 13), (5, 14), (5, 20),

    -- Permisos de Supervisor
    (6, 2), (6, 7), (6, 10), (6, 17), (6, 20),

    -- Permisos de Desarrollador
    (7, 1), (7, 2), (7, 3), (7, 4), (7, 5), (7, 15), (7, 19), (7, 20),

    -- Permisos de Auditor
    (8, 2), (8, 7), (8, 10), (8, 17), (8, 20),

    -- Permisos de Operador
    (9, 2), (9, 3), (9, 4), (9, 19),

    -- Permisos de Gerente
    (10, 2), (10, 7), (10, 8), (10, 10), (10, 11), (10, 20);

DELIMITER //

CREATE TRIGGER asignar_rol_por_defecto
AFTER INSERT ON usuario
FOR EACH ROW
BEGIN
    -- Asignamos el rol con id=3 (vendedor) al usuario recién creado
    INSERT INTO usuario_rol (id_usuario, id_rol)
    VALUES (NEW.id, 3);
END //

DELIMITER ;


DELIMITER $$

CREATE PROCEDURE insertar_perdidas_caducadas()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_id_lote INT;
    DECLARE v_id_producto INT;
    DECLARE v_cantidad INT;
    DECLARE v_costo_unitario DECIMAL(10, 2);
    DECLARE v_total DECIMAL(10, 2);
    DECLARE v_fecha_caducidad DATE;

    -- Cursor para obtener los lotes caducados
    DECLARE cur CURSOR FOR 
        SELECT l.id, l.id_producto, l.cantidad, l.fecha_caducidad, p.costo
        FROM lote l
        JOIN producto p ON l.id_producto = p.id
        WHERE l.fecha_caducidad < CURDATE()
            AND l.id_state = 1 -- Lotes caducados y activos
            AND l.cantidad > 0; -- Lotes caducados, activos y con cantidad mayor a cero

    -- Handler para cerrar el cursor cuando se termine
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    -- Abrir el cursor
    OPEN cur;

    -- Loop a través de los lotes caducados
    read_loop: LOOP
        FETCH cur INTO v_id_lote, v_id_producto, v_cantidad, v_fecha_caducidad, v_costo_unitario;
        
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- Calcular el total de la pérdida (cantidad * costo_unitario)
        SET v_total = v_cantidad * v_costo_unitario;

        -- Insertar la pérdida en la tabla "perdida"
        INSERT INTO perdida (id_producto, id_lote, cantidad, costo_unitario, total)
        VALUES (v_id_producto, v_id_lote, v_cantidad, v_costo_unitario, v_total);

        -- Actualizar el estado del lote a "Caducado" (id_state = 2)
        UPDATE lote
        SET id_state = 2
        WHERE id = v_id_lote;

    END LOOP;

    -- Cerrar el cursor
    CLOSE cur;

END$$

DELIMITER ;

SET GLOBAL event_scheduler = ON;
CREATE EVENT insertar_perdidas_diarias
ON SCHEDULE EVERY 1 DAY
STARTS '2025-01-20 00:00:00'  -- Fecha y hora de inicio
DO
  CALL insertar_perdidas_caducadas();

INSERT INTO `usuario` VALUES (1,'Administrador','administrador@mail.com','1234567890','$2a$10$XGDDF17aaBDqQPo7rdtdjO90Us67BlwVEfSUjEGNjY63WEPZKdyGW',1, '2025-01-14 04:25:03','2025-01-15 02:33:18');