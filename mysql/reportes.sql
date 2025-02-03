-- reporte inventario
-- todos los productos con proxima fecha caducidad
SELECT 
    p.nombre AS producto, 
    p.codigo, 
    p.categoria, 
    COALESCE(SUM(l.cantidad), 0) AS cantidad_total,
    MIN(l.fecha_caducidad) AS fecha_caducidad_proxima
FROM producto p
LEFT JOIN lote l ON p.id = l.id_producto AND l.cantidad > 0
GROUP BY p.id, p.nombre, p.codigo, p.categoria
ORDER BY p.nombre;

-- reporte de entradas
SELECT 
    e.fecha, 
    p.nombre AS producto, 
    de.cantidad, 
    cs.nombre_estado AS estado,
    l.fecha_caducidad,
    CONCAT(
        TIMESTAMPDIFF(YEAR, CURDATE(), l.fecha_caducidad), ' anios ',
        TIMESTAMPDIFF(MONTH, CURDATE(), l.fecha_caducidad) % 12, ' meses ',
        DATEDIFF(l.fecha_caducidad, DATE_ADD(CURDATE(), INTERVAL TIMESTAMPDIFF(MONTH, CURDATE(), l.fecha_caducidad) MONTH)), ' días'
    ) AS margen_para_vender
FROM detalle_entrada de
JOIN entrada e ON de.id_entrada = e.id
JOIN producto p ON de.id_producto = p.id
JOIN cState cs ON e.id_state = cs.id
JOIN lote l ON de.id_lote = l.id
ORDER BY e.fecha DESC;

-- reporte de perdidad
SELECT p.nombre AS producto, l.id AS lote, pe.cantidad, pe.costo_unitario, pe.total
FROM perdida pe
JOIN producto p ON pe.id_producto = p.id
JOIN lote l ON pe.id_lote = l.id
ORDER BY p.nombre;

--reporte de ventas
SELECT 
    v.fecha, 
    v.total AS total_venta,
    COUNT(dv.id_producto) AS cantidad_productos
FROM venta v
JOIN detalle_venta dv ON v.id = dv.id_venta
GROUP BY v.fecha, v.total
ORDER BY v.fecha DESC;

-- promociones Activas
SELECT 
    p.nombre AS promocion,
    COUNT(dv.id_promocion) AS cantidad_vendida
FROM promocion p
LEFT JOIN detalle_venta dv ON p.id = dv.id_promocion
LEFT JOIN venta v ON dv.id_venta = v.id
WHERE p.activo = 1  -- Solo promociones activas
GROUP BY p.nombre
ORDER BY cantidad_vendida DESC;


--reporte productos mas vendidos
SELECT 
    p.nombre AS producto,
    SUM(dv.cantidad) AS cantidad_vendida
FROM detalle_venta dv
JOIN producto p ON dv.id_producto = p.id
JOIN venta v ON dv.id_venta = v.id
WHERE v.id_state = 6  -- Considerando solo ventas pagadas
GROUP BY p.nombre
ORDER BY cantidad_vendida DESC;

-- pocos vendidos
SELECT 
    p.nombre AS producto,
    SUM(dv.cantidad) AS cantidad_vendida
FROM detalle_venta dv
JOIN producto p ON dv.id_producto = p.id
JOIN venta v ON dv.id_venta = v.id
WHERE v.id_state = 6  -- Considerando solo ventas pagadas
GROUP BY p.nombre
ORDER BY cantidad_vendida ASC;
