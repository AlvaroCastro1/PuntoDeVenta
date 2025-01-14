-- 1. Tabla para estados
CREATE TABLE cState (
    id INT IDENTITY(1,1) NOT NULL,
    nombre_estado NVARCHAR(50) NOT NULL, -- Por ejemplo: 'Pendiente', 'Completado'
    CONSTRAINT PK_cState PRIMARY KEY CLUSTERED (id ASC)
);

-- 2. Tabla para productos
CREATE TABLE producto (
    id INT IDENTITY(1,1) NOT NULL,
    nombre NVARCHAR(100) NOT NULL,
    descripcion NVARCHAR(255) NULL,
    categoria NVARCHAR(50) NULL,
    precio DECIMAL(10, 2) NOT NULL,
    costo DECIMAL(10, 2) NOT NULL,
    existencia INT NOT NULL,
    CONSTRAINT PK_producto PRIMARY KEY CLUSTERED (id ASC)
);

-- 2.1. Tabla para imagenes del producto
CREATE TABLE producto_imagen (
    id INT AUTO_INCREMENT NOT NULL,
    producto_id INT NOT NULL,
    imagen LONGBLOB NOT NULL, -- Almacena la imagen como un archivo binario
    descripcion VARCHAR(255) NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE
);

-- 3. Tabla para lotes
CREATE TABLE lote (
    id INT IDENTITY(1,1) NOT NULL,
    id_producto INT NOT NULL, -- Relación con producto
    cantidad INT NOT NULL, -- Cantidad disponible en este lote
    fecha_caducidad DATE NOT NULL, -- Fecha de caducidad
    estado NVARCHAR(50) NOT NULL DEFAULT 'disponible', -- Estado del lote
    CONSTRAINT PK_lote PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_lote_producto FOREIGN KEY (id_producto) REFERENCES producto(id)
);

-- 4. Tabla para entradas
CREATE TABLE entrada (
    id INT IDENTITY(1,1) NOT NULL,
    fecha DATETIME NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    id_state INT NOT NULL, -- Relación con cState
    CONSTRAINT PK_entrada PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_entrada_cState FOREIGN KEY (id_state) REFERENCES cState(id)
);

-- 5. Detalle de entradas
CREATE TABLE detalle_entrada (
    id INT IDENTITY(1,1) NOT NULL,
    id_entrada INT NOT NULL, -- Relación con entrada
    id_producto INT NOT NULL, -- Relación con producto
    costo_unitario DECIMAL(10, 2) NOT NULL,
    cantidad INT NOT NULL,
    CONSTRAINT PK_detalle_entrada PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_detalle_entrada_entrada FOREIGN KEY (id_entrada) REFERENCES entrada(id),
    CONSTRAINT FK_detalle_entrada_producto FOREIGN KEY (id_producto) REFERENCES producto(id)
);

-- 6. Tabla para pérdidas
CREATE TABLE perdida (
    id INT IDENTITY(1,1) NOT NULL,
    fecha DATETIME NOT NULL,
    id_state INT NOT NULL, -- Relación con cState
    total DECIMAL(10, 2) NOT NULL,
    CONSTRAINT PK_perdida PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_perdida_cState FOREIGN KEY (id_state) REFERENCES cState(id)
);

-- 7. Detalle de pérdidas
CREATE TABLE detalle_perdida (
    id INT IDENTITY(1,1) NOT NULL,
    id_perdida INT NOT NULL, -- Relación con perdida
    id_producto INT NOT NULL, -- Relación con producto
    costo_unitario DECIMAL(10, 2) NOT NULL,
    cantidad INT NOT NULL,
    CONSTRAINT PK_detalle_perdida PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_detalle_perdida_perdida FOREIGN KEY (id_perdida) REFERENCES perdida(id),
    CONSTRAINT FK_detalle_perdida_producto FOREIGN KEY (id_producto) REFERENCES producto(id)
);

-- 8. Tabla para ventas
CREATE TABLE venta (
    id INT IDENTITY(1,1) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    fecha DATETIME NOT NULL,
    id_state INT NOT NULL, -- Relación con cState
    CONSTRAINT PK_venta PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_venta_cState FOREIGN KEY (id_state) REFERENCES cState(id)
);

-- 9. Detalle de ventas
CREATE TABLE detalle_venta (
    id INT IDENTITY(1,1) NOT NULL,
    id_venta INT NOT NULL, -- Relación con venta
    id_producto INT NOT NULL, -- Relación con producto
    id_lote INT NULL, -- Relación con lote
    costo_unitario DECIMAL(10, 2) NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    cantidad INT NOT NULL,
    id_promocion INT NULL, -- Relación con promocion
    descuento_aplicado DECIMAL(10, 2) NULL, -- Descuento aplicado
    CONSTRAINT PK_detalle_venta PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_detalle_venta_venta FOREIGN KEY (id_venta) REFERENCES venta(id),
    CONSTRAINT FK_detalle_venta_producto FOREIGN KEY (id_producto) REFERENCES producto(id),
    CONSTRAINT FK_detalle_venta_lote FOREIGN KEY (id_lote) REFERENCES lote(id)
);

-- 10. Tabla para promociones
CREATE TABLE promocion (
    id INT IDENTITY(1,1) NOT NULL,
    nombre NVARCHAR(100) NOT NULL,
    tipo NVARCHAR(50) NOT NULL, -- Tipo de promoción
    valor_descuento DECIMAL(10, 2) NULL, -- Porcentaje o monto fijo
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activo BIT NOT NULL DEFAULT 1, -- Si la promoción está activa
    CONSTRAINT PK_promocion PRIMARY KEY CLUSTERED (id ASC)
);

-- 11. Tabla para productos en promociones
CREATE TABLE producto_promocion (
    id INT IDENTITY(1,1) NOT NULL,
    id_producto INT NOT NULL, -- Relación con producto
    id_promocion INT NOT NULL, -- Relación con promocion
    cantidad_necesaria INT NULL, -- Cantidad mínima
    CONSTRAINT PK_producto_promocion PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_producto_promocion_producto FOREIGN KEY (id_producto) REFERENCES producto(id),
    CONSTRAINT FK_producto_promocion_promocion FOREIGN KEY (id_promocion) REFERENCES promocion(id)
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

-- 14. 
CREATE TABLE rol_permiso (
    id_rol INT NOT NULL,  -- ID del rol
    id_permiso INT NOT NULL,  -- ID del permiso
    CONSTRAINT PK_rol_permiso PRIMARY KEY (id_rol, id_permiso),
    CONSTRAINT FK_rol_permiso_rol FOREIGN KEY (id_rol) REFERENCES rol(id),
    CONSTRAINT FK_rol_permiso_permiso FOREIGN KEY (id_permiso) REFERENCES permiso(id)
);
