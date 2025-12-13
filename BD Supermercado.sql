-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema Supermercado
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema Supermercado
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `Supermercado` DEFAULT CHARACTER SET utf8 ;
USE `Supermercado` ;

-- -----------------------------------------------------
-- Table `Supermercado`.`Roles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Supermercado`.`Roles` ;

CREATE TABLE IF NOT EXISTS `Supermercado`.`Roles` (
  `id_rol` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_rol` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`id_rol`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `Supermercado`.`Usuarios`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Supermercado`.`Usuarios` ;

CREATE TABLE IF NOT EXISTS `Supermercado`.`Usuarios` (
  `id_usuarios` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_completo` VARCHAR(100) NOT NULL,
  `username` VARCHAR(30) NOT NULL,
  `contraseña` VARCHAR(255) NOT NULL,
  `id_rol` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id_usuarios`),
  UNIQUE INDEX `contraseña_UNIQUE` (`contraseña` ASC) VISIBLE,
  INDEX `fk_Usuarios_Roles_idx` (`id_rol` ASC) VISIBLE,
  CONSTRAINT `fk_Usuarios_Roles`
    FOREIGN KEY (`id_rol`)
    REFERENCES `Supermercado`.`Roles` (`id_rol`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `Supermercado`.`Categorias`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Supermercado`.`Categorias` ;

CREATE TABLE IF NOT EXISTS `Supermercado`.`Categorias` (
  `id_categorias` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_categoria` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id_categorias`),
  UNIQUE INDEX `nombre_categoria_UNIQUE` (`nombre_categoria` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `Supermercado`.`Productos`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Supermercado`.`Productos` ;

CREATE TABLE IF NOT EXISTS `Supermercado`.`Productos` (
  `id_productos` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(30) NOT NULL,
  `nombre_producto` VARCHAR(100) NOT NULL,
  `precio_costo` DECIMAL(8,2) UNSIGNED NOT NULL,
  `precio_venta` DECIMAL(6,2) UNSIGNED NOT NULL,
  `stock_actual` INT UNSIGNED NOT NULL,
  `stock_minimo` INT UNSIGNED NOT NULL,
  `id_categorias` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id_productos`),
  INDEX `fk_Productos_Categorias1_idx` (`id_categorias` ASC) VISIBLE,
  CONSTRAINT `fk_Productos_Categorias1`
    FOREIGN KEY (`id_categorias`)
    REFERENCES `Supermercado`.`Categorias` (`id_categorias`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `Supermercado`.`Ventas_Encabezado`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Supermercado`.`Ventas_Encabezado` ;

CREATE TABLE IF NOT EXISTS `Supermercado`.`Ventas_Encabezado` (
  `id_venta` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `fecha_hora` DATETIME NOT NULL,
  `monto_total` DECIMAL(6,2) UNSIGNED NOT NULL,
  `id_usuarios` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id_venta`),
  INDEX `fk_Ventas_Encabezado_Usuarios1_idx` (`id_usuarios` ASC) VISIBLE,
  CONSTRAINT `fk_Ventas_Encabezado_Usuarios1`
    FOREIGN KEY (`id_usuarios`)
    REFERENCES `Supermercado`.`Usuarios` (`id_usuarios`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `Supermercado`.`Ventas_detalle`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `Supermercado`.`Ventas_detalle` ;

CREATE TABLE IF NOT EXISTS `Supermercado`.`Ventas_detalle` (
  `id_detalle` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `cantidad` INT UNSIGNED NOT NULL,
  `precio_unitario` DECIMAL(10,2) UNSIGNED NOT NULL,
  `subtotal` DECIMAL(10,2) UNSIGNED NOT NULL,
  `id_venta` INT UNSIGNED NOT NULL,
  `id_productos` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id_detalle`),
  INDEX `fk_Ventas_detalle_Ventas_Encabezado1_idx` (`id_venta` ASC) VISIBLE,
  INDEX `fk_Ventas_detalle_Productos1_idx` (`id_productos` ASC) VISIBLE,
  CONSTRAINT `fk_Ventas_detalle_Ventas_Encabezado1`
    FOREIGN KEY (`id_venta`)
    REFERENCES `Supermercado`.`Ventas_Encabezado` (`id_venta`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Ventas_detalle_Productos1`
    FOREIGN KEY (`id_productos`)
    REFERENCES `Supermercado`.`Productos` (`id_productos`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
