-- MySQL Script generated by MySQL Workbench
-- 06/30/16 17:17:38
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema WPLHourLogs
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `WPLHourLogs` ;

-- -----------------------------------------------------
-- Schema WPLHourLogs
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `WPLHourLogs` DEFAULT CHARACTER SET utf8 ;
USE `WPLHourLogs` ;

-- -----------------------------------------------------
-- Table `WPLHourLogs`.`datatype`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `WPLHourLogs`.`datatype` ;

CREATE TABLE IF NOT EXISTS `WPLHourLogs`.`datatype` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `DataType` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `ID_UNIQUE` (`ID` ASC),
  UNIQUE INDEX `DataType_UNIQUE` (`DataType` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `WPLHourLogs`.`item`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `WPLHourLogs`.`item` ;

CREATE TABLE IF NOT EXISTS `WPLHourLogs`.`item` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(60) NOT NULL,
  `Active` TINYINT(1) NOT NULL DEFAULT 1,
  `Property` TINYINT(1) NOT NULL DEFAULT 0,
  `datatype_ID` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `ID_UNIQUE` (`ID` ASC),
  UNIQUE INDEX `Name_UNIQUE` (`Name` ASC),
  INDEX `fk_item_datatype_idx` (`datatype_ID` ASC),
  CONSTRAINT `fk_item_datatype`
    FOREIGN KEY (`datatype_ID`)
    REFERENCES `WPLHourLogs`.`datatype` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `WPLHourLogs`.`report`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `WPLHourLogs`.`report` ;

CREATE TABLE IF NOT EXISTS `WPLHourLogs`.`report` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(100) NOT NULL DEFAULT 'Anonymous Potatoe',
  `Date` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `ID_UNIQUE` (`ID` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `WPLHourLogs`.`record`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `WPLHourLogs`.`record` ;

CREATE TABLE IF NOT EXISTS `WPLHourLogs`.`record` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `Value` VARCHAR(1000) NULL,
  `item_ID` INT UNSIGNED NOT NULL,
  `report_ID` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `ID_UNIQUE` (`ID` ASC),
  INDEX `fk_record_item1_idx` (`item_ID` ASC),
  INDEX `fk_record_report1_idx` (`report_ID` ASC),
  CONSTRAINT `fk_record_item`
    FOREIGN KEY (`item_ID`)
    REFERENCES `WPLHourLogs`.`item` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_record_report`
    FOREIGN KEY (`report_ID`)
    REFERENCES `WPLHourLogs`.`report` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

INSERT wplhourlogs.datatype (DataType) VALUES ('Boolean');
INSERT wplhourlogs.datatype (DataType) VALUES ('Integer');
INSERT wplhourlogs.datatype (DataType) VALUES ('String');

INSERT wplhourlogs.item (`Name`, Active, Property, datatype_ID) VALUES ('Minutes', 1, 0, 2);
INSERT wplhourlogs.item (`Name`, Active, Property, datatype_ID) VALUES ('Description', 1, 0, 3);