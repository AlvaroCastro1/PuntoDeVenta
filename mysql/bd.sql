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

-- 2. Tabla para productos
CREATE TABLE producto (
    id INT AUTO_INCREMENT NOT NULL,
    nombre NVARCHAR(100) NOT NULL,
    descripcion NVARCHAR(255) NULL,
    categoria NVARCHAR(50) NULL,
    precio DECIMAL(10, 2) NOT NULL,
    costo DECIMAL(10, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_producto PRIMARY KEY (id)
);

-- 2.1. Tabla para imagenes del producto
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

-- 3. Tabla para lotes
CREATE TABLE lote (
    id INT AUTO_INCREMENT NOT NULL,
    id_producto INT NOT NULL,           -- Relación con producto
    cantidad INT NOT NULL,              -- Cantidad disponible en este lote
    fecha_caducidad DATE NOT NULL,      -- Fecha de caducidad del lote
    fecha_entrada DATETIME NOT NULL,    -- Fecha en que el lote fue ingresado al inventario
    id_state INT NOT NULL DEFAULT 1,    -- Estado del lote (Activo, Caducado)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_lote PRIMARY KEY (id),
    CONSTRAINT FK_lote_producto FOREIGN KEY (id_producto) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT FK_lote_state FOREIGN KEY (id_state) REFERENCES cState(id) ON DELETE CASCADE
);

-- 4. Tabla para entradas
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

-- 5. Detalle de entradas
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

-- 6. Tabla para pérdidas
CREATE TABLE perdida (
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

-- 7. Detalle de pérdidas
CREATE TABLE detalle_perdida (
    id INT AUTO_INCREMENT NOT NULL,
    id_perdida INT NOT NULL, -- Relación con perdida
    id_producto INT NOT NULL, -- Relación con producto
    costo_unitario DECIMAL(10, 2) NOT NULL,
    cantidad INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_detalle_perdida PRIMARY KEY (id),
    CONSTRAINT FK_detalle_perdida_perdida FOREIGN KEY (id_perdida) REFERENCES perdida(id) ON DELETE CASCADE,
    CONSTRAINT FK_detalle_perdida_producto FOREIGN KEY (id_producto) REFERENCES producto(id) ON DELETE CASCADE
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
    costo_unitario DECIMAL(10, 2) NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    cantidad INT NOT NULL,
    id_promocion INT NULL, -- Relación con promocion
    descuento_aplicado DECIMAL(10, 2) NULL, -- Descuento aplicado
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_detalle_venta PRIMARY KEY (id),
    CONSTRAINT FK_detalle_venta_venta FOREIGN KEY (id_venta) REFERENCES venta(id) ON DELETE CASCADE,
    CONSTRAINT FK_detalle_venta_producto FOREIGN KEY (id_producto) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT FK_detalle_venta_lote FOREIGN KEY (id_lote) REFERENCES lote(id) ON DELETE CASCADE
);

-- 10. Tabla para promociones
CREATE TABLE promocion (
    id INT AUTO_INCREMENT NOT NULL,
    nombre NVARCHAR(100) NOT NULL,
    tipo NVARCHAR(50) NOT NULL, -- Tipo de promoción
    valor_descuento DECIMAL(10, 2) NULL, -- Porcentaje o monto fijo
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activo BIT NOT NULL DEFAULT 1, -- Si la promoción está activa
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_promocion PRIMARY KEY (id)
);

-- 11. Tabla para productos en promociones
CREATE TABLE producto_promocion (
    id INT AUTO_INCREMENT NOT NULL,
    id_producto INT NOT NULL, -- Relación con producto
    id_promocion INT NOT NULL, -- Relación con promocion
    cantidad_necesaria INT NULL, -- Cantidad mínima
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_producto_promocion PRIMARY KEY (id),
    CONSTRAINT FK_producto_promocion_producto FOREIGN KEY (id_producto) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT FK_producto_promocion_promocion FOREIGN KEY (id_promocion) REFERENCES promocion(id) ON DELETE CASCADE
);

-- 10. usuarios
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

-- 11. roles
CREATE TABLE rol (
    id INT AUTO_INCREMENT NOT NULL,  -- ID único del rol
    nombre NVARCHAR(50) NOT NULL,  -- Nombre del rol (ej. 'Administrador', 'Vendedor')
    descripcion NVARCHAR(255) NULL,  -- Descripción del rol
    CONSTRAINT PK_rol PRIMARY KEY (id)
);

INSERT INTO rol (id, nombre, descripcion) VALUES
(1, 'Administrador', 'Acceso completo al sistema'),
(2, 'Moderador', 'Puede gestionar contenido y usuarios'),
(3, 'Usuario', 'Usuario estándar con permisos básicos'),
(4, 'Invitado', 'Acceso limitado solo para ver información'),
(5, 'Soporte Técnico', 'Resuelve problemas técnicos del sistema'),
(6, 'Supervisor', 'Supervisa y monitorea actividades'),
(7, 'Desarrollador', 'Desarrolla y mantiene el sistema'),
(8, 'Auditor', 'Acceso para revisar actividades y registros'),
(9, 'Operador', 'Gestiona tareas operativas específicas'),
(10, 'Gerente', 'Toma decisiones basadas en reportes');

-- 12. detalle roles usuario
CREATE TABLE usuario_rol (
    id_usuario INT NOT NULL,  -- ID del usuario
    id_rol INT NOT NULL,  -- ID del rol
    CONSTRAINT PK_usuario_rol PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT FK_usuario_rol_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id),
    CONSTRAINT FK_usuario_rol_rol FOREIGN KEY (id_rol) REFERENCES rol(id)
);


-- 13. permisos
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


-- 14. 
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
    -- Asignamos el rol con id=3 (Usuario) al usuario recién creado
    INSERT INTO usuario_rol (id_usuario, id_rol)
    VALUES (NEW.id, 3);
END //

DELIMITER ;
