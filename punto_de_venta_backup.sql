-- MySQL dump 10.13  Distrib 9.2.0, for Linux (x86_64)
--
-- Host: localhost    Database: punto_de_venta
-- ------------------------------------------------------
-- Server version	9.2.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cState`
--

DROP TABLE IF EXISTS `cState`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cState` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre_estado` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cState`
--

LOCK TABLES `cState` WRITE;
/*!40000 ALTER TABLE `cState` DISABLE KEYS */;
INSERT INTO `cState` VALUES (1,'Disponible','2025-04-04 18:05:49','2025-04-04 18:05:49'),(2,'Cancelado','2025-04-04 18:05:49','2025-04-04 18:05:49'),(3,'Pendiente','2025-04-04 18:05:49','2025-04-04 18:05:49'),(4,'Vendido','2025-04-04 18:05:49','2025-04-04 18:05:49'),(5,'En espera','2025-04-04 18:05:49','2025-04-04 18:05:49'),(6,'Pagado','2025-04-04 18:05:49','2025-04-04 18:05:49'),(7,'No disponible','2025-04-04 18:05:49','2025-04-04 18:05:49'),(8,'Caducado','2025-04-04 18:05:49','2025-04-04 18:05:49'),(9,'Reservado','2025-04-04 18:05:49','2025-04-04 18:05:49'),(10,'En proceso','2025-04-04 18:05:49','2025-04-04 18:05:49');
/*!40000 ALTER TABLE `cState` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_entrada`
--

DROP TABLE IF EXISTS `detalle_entrada`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_entrada` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_entrada` int NOT NULL,
  `id_producto` int NOT NULL,
  `id_lote` int NOT NULL,
  `cantidad` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_detalle_entrada_entrada` (`id_entrada`),
  KEY `FK_detalle_entrada_producto` (`id_producto`),
  KEY `FK_detalle_entrada_lote` (`id_lote`),
  CONSTRAINT `FK_detalle_entrada_entrada` FOREIGN KEY (`id_entrada`) REFERENCES `entrada` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_detalle_entrada_lote` FOREIGN KEY (`id_lote`) REFERENCES `lote` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_detalle_entrada_producto` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_entrada`
--

LOCK TABLES `detalle_entrada` WRITE;
/*!40000 ALTER TABLE `detalle_entrada` DISABLE KEYS */;
/*!40000 ALTER TABLE `detalle_entrada` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_venta`
--

DROP TABLE IF EXISTS `detalle_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_venta` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_venta` int NOT NULL,
  `id_producto` int NOT NULL,
  `id_lote` int DEFAULT NULL,
  `id_state` int DEFAULT '6',
  `costo_unitario` decimal(10,2) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `cantidad` int NOT NULL,
  `id_promocion` int DEFAULT NULL,
  `descuento_aplicado` decimal(10,2) DEFAULT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_detalle_venta_venta` (`id_venta`),
  KEY `FK_detalle_venta_producto` (`id_producto`),
  KEY `FK_detalle_venta_lote` (`id_lote`),
  KEY `fk_detalle_venta_state` (`id_state`),
  CONSTRAINT `FK_detalle_venta_lote` FOREIGN KEY (`id_lote`) REFERENCES `lote` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_detalle_venta_producto` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_detalle_venta_state` FOREIGN KEY (`id_state`) REFERENCES `cState` (`id`),
  CONSTRAINT `FK_detalle_venta_venta` FOREIGN KEY (`id_venta`) REFERENCES `venta` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_venta`
--

LOCK TABLES `detalle_venta` WRITE;
/*!40000 ALTER TABLE `detalle_venta` DISABLE KEYS */;
INSERT INTO `detalle_venta` VALUES (1,1,1,1,6,8.00,10.00,1,NULL,0.00,10.00,'2025-04-04 22:30:06','2025-04-04 22:30:06'),(2,2,1,NULL,6,8.00,10.00,1,NULL,0.00,10.00,'2025-04-05 06:03:40','2025-04-05 06:03:40'),(3,3,1,NULL,6,8.00,10.00,1,NULL,0.00,10.00,'2025-04-05 06:08:05','2025-04-05 06:08:05'),(4,9,1,NULL,6,8.00,10.00,1,NULL,0.00,10.00,'2025-04-05 06:18:47','2025-04-05 06:18:47'),(5,9,2,3,6,12.00,10.00,1,NULL,0.00,10.00,'2025-04-05 06:18:47','2025-04-05 06:18:47'),(6,10,1,NULL,6,8.00,10.00,1,NULL,0.00,10.00,'2025-04-05 06:20:49','2025-04-05 06:20:49'),(7,10,2,3,6,12.00,10.00,1,NULL,0.00,10.00,'2025-04-05 06:20:49','2025-04-05 06:20:49'),(8,12,1,NULL,6,8.00,10.00,1,NULL,0.00,10.00,'2025-04-05 18:01:35','2025-04-05 18:01:35');
/*!40000 ALTER TABLE `detalle_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `entrada`
--

DROP TABLE IF EXISTS `entrada`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `entrada` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `id_state` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_entrada_cState` (`id_state`),
  CONSTRAINT `FK_entrada_cState` FOREIGN KEY (`id_state`) REFERENCES `cState` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `entrada`
--

LOCK TABLES `entrada` WRITE;
/*!40000 ALTER TABLE `entrada` DISABLE KEYS */;
/*!40000 ALTER TABLE `entrada` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lote`
--

DROP TABLE IF EXISTS `lote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lote` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_producto` int NOT NULL,
  `cantidad` int NOT NULL,
  `fecha_caducidad` date DEFAULT NULL,
  `fecha_entrada` datetime NOT NULL,
  `id_state` int NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `id_usuario` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_lote_producto` (`id_producto`),
  KEY `FK_lote_state` (`id_state`),
  KEY `FK_usuario_lote` (`id_usuario`),
  CONSTRAINT `FK_lote_producto` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_lote_state` FOREIGN KEY (`id_state`) REFERENCES `cState` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_usuario_lote` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lote`
--

LOCK TABLES `lote` WRITE;
/*!40000 ALTER TABLE `lote` DISABLE KEYS */;
INSERT INTO `lote` VALUES (1,1,9,'2025-04-05','2025-03-30 00:00:00',1,'2025-04-04 20:24:50','2025-04-04 22:30:06',1),(3,2,8,'2025-04-12','2025-04-12 00:00:00',1,'2025-04-05 06:18:34','2025-04-05 06:20:49',1);
/*!40000 ALTER TABLE `lote` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `perdida`
--

DROP TABLE IF EXISTS `perdida`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `perdida` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_producto` int NOT NULL,
  `id_lote` int NOT NULL,
  `cantidad` int NOT NULL,
  `costo_unitario` decimal(10,2) NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_perdida_producto` (`id_producto`),
  KEY `FK_perdida_lote` (`id_lote`),
  CONSTRAINT `FK_perdida_lote` FOREIGN KEY (`id_lote`) REFERENCES `lote` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_perdida_producto` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id`) ON DELETE CASCADE,
  CONSTRAINT `perdida_chk_1` CHECK ((`cantidad` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `perdida`
--

LOCK TABLES `perdida` WRITE;
/*!40000 ALTER TABLE `perdida` DISABLE KEYS */;
/*!40000 ALTER TABLE `perdida` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permiso`
--

DROP TABLE IF EXISTS `permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permiso` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `descripcion` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permiso`
--

LOCK TABLES `permiso` WRITE;
/*!40000 ALTER TABLE `permiso` DISABLE KEYS */;
INSERT INTO `permiso` VALUES (1,'Crear','Permite crear nuevos registros o elementos'),(2,'Leer','Permite leer informacin del sistema'),(3,'Actualizar','Permite modificar registros existentes'),(4,'Eliminar','Permite eliminar registros'),(5,'Configurar','Permite acceder a configuraciones del sistema'),(6,'Gestionar Usuarios','Permite agregar, modificar o eliminar usuarios'),(7,'Acceder a Reportes','Permite ver reportes generados por el sistema'),(8,'Exportar Datos','Permite exportar informacin a archivos externos'),(9,'Importar Datos','Permite importar informacin desde archivos externos'),(10,'Ver Estadsticas','Permite ver estadsticas y mtricas del sistema'),(11,'Aprobar Solicitudes','Permite aprobar solicitudes o procesos pendientes'),(12,'Revertir Cambios','Permite deshacer cambios realizados'),(13,'Realizar Backups','Permite crear respaldos de informacin'),(14,'Restaurar Backups','Permite restaurar informacin desde respaldos'),(15,'Acceso API','Permite interactuar con la API del sistema'),(16,'Gestionar Roles','Permite crear, modificar o eliminar roles'),(17,'Auditar Actividades','Permite revisar y auditar actividades'),(18,'Gestionar Permisos','Permite asignar o revocar permisos'),(19,'Gestionar Inventario','Permite gestionar productos o recursos'),(20,'Ver Log de Actividades','Permite ver registros de actividades realizadas');
/*!40000 ALTER TABLE `permiso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `codigo` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `descripcion` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `categoria` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `precio` decimal(10,2) NOT NULL,
  `costo` decimal(10,2) NOT NULL,
  `id_usuario` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_producto_usuario` (`id_usuario`),
  CONSTRAINT `FK_producto_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
INSERT INTO `producto` VALUES (1,'Pulparindo','725226003412','dulce','dulce mexicano',10.00,8.00,1,'2025-04-04 20:09:21','2025-04-04 21:19:49'),(2,'chicles','chicles','asd	','dulce',10.00,12.00,1,'2025-04-05 06:17:40','2025-04-05 06:17:40');
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto_imagen`
--

DROP TABLE IF EXISTS `producto_imagen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto_imagen` (
  `id` int NOT NULL AUTO_INCREMENT,
  `producto_id` int NOT NULL,
  `imagen` longblob NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `producto_id` (`producto_id`),
  CONSTRAINT `producto_imagen_ibfk_1` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto_imagen`
--

LOCK TABLES `producto_imagen` WRITE;
/*!40000 ALTER TABLE `producto_imagen` DISABLE KEYS */;
/*!40000 ALTER TABLE `producto_imagen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promocion`
--

DROP TABLE IF EXISTS `promocion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promocion` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_producto` int NOT NULL,
  `nombre` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `tipo` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `valor_descuento` decimal(10,2) DEFAULT NULL,
  `precio_final` decimal(10,2) DEFAULT NULL,
  `cantidad_necesaria` int NOT NULL DEFAULT '1',
  `activo` bit(1) NOT NULL DEFAULT b'1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_promocion_producto` (`id_producto`),
  CONSTRAINT `FK_promocion_producto` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promocion`
--

LOCK TABLES `promocion` WRITE;
/*!40000 ALTER TABLE `promocion` DISABLE KEYS */;
/*!40000 ALTER TABLE `promocion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `descripcion` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'Administrador','Acceso completo al sistema'),(2,'Moderador','Puede gestionar contenido y usuarios'),(3,'Vendedor','Usuario estndar con permisos bsicos'),(4,'Invitado','Acceso limitado solo para ver informacin'),(5,'Soporte Tcnico','Resuelve problemas tcnicos del sistema'),(6,'Supervisor','Supervisa y monitorea actividades'),(7,'Desarrollador','Desarrolla y mantiene el sistema'),(8,'Auditor','Acceso para revisar actividades y registros'),(9,'Operador','Gestiona tareas operativas especficas'),(10,'Gerente','Toma decisiones basadas en reportes');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol_permiso`
--

DROP TABLE IF EXISTS `rol_permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol_permiso` (
  `id_rol` int NOT NULL,
  `id_permiso` int NOT NULL,
  PRIMARY KEY (`id_rol`,`id_permiso`),
  KEY `FK_rol_permiso_permiso` (`id_permiso`),
  CONSTRAINT `FK_rol_permiso_permiso` FOREIGN KEY (`id_permiso`) REFERENCES `permiso` (`id`),
  CONSTRAINT `FK_rol_permiso_rol` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol_permiso`
--

LOCK TABLES `rol_permiso` WRITE;
/*!40000 ALTER TABLE `rol_permiso` DISABLE KEYS */;
INSERT INTO `rol_permiso` VALUES (1,1),(7,1),(1,2),(2,2),(3,2),(4,2),(5,2),(6,2),(7,2),(8,2),(9,2),(10,2),(1,3),(2,3),(7,3),(9,3),(1,4),(2,4),(7,4),(9,4),(1,5),(5,5),(7,5),(1,6),(2,6),(1,7),(2,7),(3,7),(6,7),(8,7),(10,7),(1,8),(10,8),(1,9),(1,10),(2,10),(3,10),(4,10),(6,10),(8,10),(10,10),(1,11),(10,11),(1,12),(1,13),(5,13),(1,14),(5,14),(1,15),(7,15),(1,16),(1,17),(6,17),(8,17),(1,18),(1,19),(7,19),(9,19),(1,20),(5,20),(6,20),(7,20),(8,20),(10,20);
/*!40000 ALTER TABLE `rol_permiso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `correo` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `telefono` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `contrasena` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `estado` bit(1) NOT NULL DEFAULT b'1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_ultima_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `correo` (`correo`),
  UNIQUE KEY `telefono` (`telefono`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'Administrador','admin','1234567890','$2a$10$XGDDF17aaBDqQPo7rdtdjO90Us67BlwVEfSUjEGNjY63WEPZKdyGW',_binary '','2025-01-14 04:25:03','2025-04-04 19:09:28');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `asignar_rol_por_defecto` AFTER INSERT ON `usuario` FOR EACH ROW BEGIN
    -- Asignamos el rol con id=3 (vendedor) al usuario recin creado
    INSERT INTO usuario_rol (id_usuario, id_rol)
    VALUES (NEW.id, 3);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `usuario_rol`
--

DROP TABLE IF EXISTS `usuario_rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_rol` (
  `id_usuario` int NOT NULL,
  `id_rol` int NOT NULL,
  PRIMARY KEY (`id_usuario`,`id_rol`),
  KEY `FK_usuario_rol_rol` (`id_rol`),
  CONSTRAINT `FK_usuario_rol_rol` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id`),
  CONSTRAINT `FK_usuario_rol_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_rol`
--

LOCK TABLES `usuario_rol` WRITE;
/*!40000 ALTER TABLE `usuario_rol` DISABLE KEYS */;
INSERT INTO `usuario_rol` VALUES (1,7);
/*!40000 ALTER TABLE `usuario_rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `venta`
--

DROP TABLE IF EXISTS `venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `venta` (
  `id` int NOT NULL AUTO_INCREMENT,
  `total` decimal(10,2) NOT NULL,
  `fecha` datetime NOT NULL,
  `id_state` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_venta_cState` (`id_state`),
  KEY `FK_usuario_usuario` (`id_usuario`),
  CONSTRAINT `FK_usuario_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FK_venta_cState` FOREIGN KEY (`id_state`) REFERENCES `cState` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `venta`
--

LOCK TABLES `venta` WRITE;
/*!40000 ALTER TABLE `venta` DISABLE KEYS */;
INSERT INTO `venta` VALUES (1,10.00,'2025-04-04 16:30:06',6,'2025-04-04 22:30:06','2025-04-04 22:30:06',1),(2,10.00,'2025-04-05 00:03:41',6,'2025-04-05 06:03:40','2025-04-05 06:03:40',1),(3,10.00,'2025-04-05 00:08:06',6,'2025-04-05 06:08:05','2025-04-05 06:08:05',1),(4,0.00,'2025-04-05 00:08:28',6,'2025-04-05 06:08:28','2025-04-05 06:08:28',1),(5,0.00,'2025-04-05 00:09:50',6,'2025-04-05 06:09:49','2025-04-05 06:09:49',1),(6,0.00,'2025-04-05 00:10:35',6,'2025-04-05 06:10:35','2025-04-05 06:10:35',1),(7,0.00,'2025-04-05 00:11:04',6,'2025-04-05 06:11:04','2025-04-05 06:11:04',1),(8,0.00,'2025-04-05 00:16:36',6,'2025-04-05 06:16:36','2025-04-05 06:16:36',1),(9,20.00,'2025-04-05 00:18:48',6,'2025-04-05 06:18:47','2025-04-05 06:18:47',1),(10,20.00,'2025-04-05 00:20:49',6,'2025-04-05 06:20:49','2025-04-05 06:20:49',1),(11,0.00,'2025-04-05 11:51:34',6,'2025-04-05 17:51:33','2025-04-05 17:51:33',1),(12,10.00,'2025-04-05 12:01:35',6,'2025-04-05 18:01:35','2025-04-05 18:01:35',1);
/*!40000 ALTER TABLE `venta` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-05 18:03:14
