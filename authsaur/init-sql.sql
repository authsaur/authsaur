# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20046
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: 192.168.2.240 (MySQL 8.0.32)
# 数据库: cas
# 生成时间: 2023-04-06 02:50:30 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# 转储表 access_strategy
# ------------------------------------------------------------

DROP TABLE IF EXISTS `access_strategy`;

CREATE TABLE `access_strategy` (
  `id` bigint NOT NULL,
  `access_Strategy` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `access_Strategy_Type` varchar(255) NOT NULL,
  `app_Id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7r6m5onohjenncbvpdsoknv5l` (`app_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 Authentication_Handlers
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Authentication_Handlers`;

CREATE TABLE `Authentication_Handlers` (
  `id` varchar(255) NOT NULL,
  `body` longtext NOT NULL,
  `body_Type` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `related_Authn_Id` varchar(255) DEFAULT NULL,
  `related_With_Mail` bit(1) DEFAULT NULL,
  `related_With_Phone` bit(1) DEFAULT NULL,
  `state` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 Cas_Event
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Cas_Event`;

CREATE TABLE `Cas_Event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creation_Time` varchar(255) NOT NULL,
  `principal_Id` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 CAS_SETTINGS
# ------------------------------------------------------------

DROP TABLE IF EXISTS `CAS_SETTINGS`;

CREATE TABLE `CAS_SETTINGS` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(512) DEFAULT NULL,
  `value` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 COM_AUDIT_TRAIL
# ------------------------------------------------------------

DROP TABLE IF EXISTS `COM_AUDIT_TRAIL`;

CREATE TABLE `COM_AUDIT_TRAIL` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `AUD_ACTION` varchar(255) DEFAULT NULL,
  `APPLIC_CD` varchar(255) DEFAULT NULL,
  `AUD_CLIENT_IP` varchar(255) DEFAULT NULL,
  `AUD_DATE` datetime(6) NOT NULL,
  `AUD_RESOURCE` varchar(2048) DEFAULT NULL,
  `AUD_SERVER_IP` varchar(255) DEFAULT NULL,
  `AUD_USER` varchar(255) DEFAULT NULL,
  `AUD_USERAGENT` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 events_properties
# ------------------------------------------------------------

DROP TABLE IF EXISTS `events_properties`;

CREATE TABLE `events_properties` (
  `event_Id` bigint NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`event_Id`,`name`),
  CONSTRAINT `FKnhtycho45cd8rblq0ka0gaufg` FOREIGN KEY (`event_Id`) REFERENCES `Cas_Event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 Google_Authenticator_Registration_Record
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Google_Authenticator_Registration_Record`;

CREATE TABLE `Google_Authenticator_Registration_Record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `registration_Date` datetime(6) DEFAULT NULL,
  `secret_Key` varchar(2048) NOT NULL,
  `username` varchar(255) NOT NULL,
  `validation_Code` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKbfdvvfhi8n022v1yket0jajih` (`username`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 Google_Authenticator_Token
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Google_Authenticator_Token`;

CREATE TABLE `Google_Authenticator_Token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `issued_Date_Time` datetime(6) NOT NULL,
  `token` int NOT NULL,
  `user_Id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 Jpa_Multifactor_Authentication_Trust_Record
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Jpa_Multifactor_Authentication_Trust_Record`;

CREATE TABLE `Jpa_Multifactor_Authentication_Trust_Record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_Fingerprint` varchar(2048) NOT NULL,
  `expiration_Date` datetime(6) NOT NULL,
  `record_Name` varchar(4000) NOT NULL,
  `principal` varchar(255) NOT NULL,
  `record_Date` timestamp NOT NULL,
  `record_Key` varchar(4000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 native
# ------------------------------------------------------------

DROP TABLE IF EXISTS `native`;

CREATE TABLE `native` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 org
# ------------------------------------------------------------

DROP TABLE IF EXISTS `org`;

CREATE TABLE `org` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `order_Sort` bigint DEFAULT NULL,
  `parent_Id` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `source` varchar(255) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 org_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `org_user`;

CREATE TABLE `org_user` (
  `id` bigint NOT NULL,
  `org_Id` varchar(255) NOT NULL,
  `org_Path` varchar(255) NOT NULL,
  `principal` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `org_Id` (`org_Id`,`principal`),
  UNIQUE KEY `UKda5y655gsqbxeckcasync9lqm` (`org_Id`,`principal`),
  KEY `uniq_p` (`principal`),
  KEY `user` (`principal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 Passwordless_Authentication_Token
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Passwordless_Authentication_Token`;

CREATE TABLE `Passwordless_Authentication_Token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `EXP_DATE` datetime(6) NOT NULL,
  `token` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 pm_table_accounts
# ------------------------------------------------------------

DROP TABLE IF EXISTS `pm_table_accounts`;

CREATE TABLE `pm_table_accounts` (
  `id` int DEFAULT NULL,
  `userid` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 pm_table_questions
# ------------------------------------------------------------

DROP TABLE IF EXISTS `pm_table_questions`;

CREATE TABLE `pm_table_questions` (
  `id` int DEFAULT NULL,
  `userid` varchar(255) DEFAULT NULL,
  `question` varchar(255) DEFAULT NULL,
  `answer` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 Registered_Services
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Registered_Services`;

CREATE TABLE `Registered_Services` (
  `id` bigint NOT NULL,
  `body` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `evaluation_Order` int NOT NULL,
  `evaluation_Priority` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `service_Id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 Saml_IdPMetadata_Document
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Saml_IdPMetadata_Document`;

CREATE TABLE `Saml_IdPMetadata_Document` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applies_To` varchar(512) DEFAULT NULL,
  `encryption_Certificate` text,
  `encryption_Key` text,
  `metadata` text,
  `signing_Certificate` text,
  `signing_Key` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_g1t159ol669a7g69u6xkpw3bx` (`applies_To`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 Saml_Metadata_Document
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Saml_Metadata_Document`;

CREATE TABLE `Saml_Metadata_Document` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `signature` longtext,
  `value` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 scratch_codes
# ------------------------------------------------------------

DROP TABLE IF EXISTS `scratch_codes`;

CREATE TABLE `scratch_codes` (
  `id` bigint NOT NULL,
  `scratch_Codes` int NOT NULL,
  KEY `FKmneuc3ux4ho26jqepo36wfoj9` (`id`),
  CONSTRAINT `FKmneuc3ux4ho26jqepo36wfoj9` FOREIGN KEY (`id`) REFERENCES `Google_Authenticator_Registration_Record` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 service_config
# ------------------------------------------------------------

DROP TABLE IF EXISTS `service_config`;

CREATE TABLE `service_config` (
  `id` bigint NOT NULL,
  `app_Id` bigint NOT NULL,
  `app_Type` varchar(255) NOT NULL,
  `config` longtext NOT NULL,
  `config_Type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_eqvnsr10935mspdw7fmhjcaty` (`app_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 service_sequence
# ------------------------------------------------------------

DROP TABLE IF EXISTS `service_sequence`;

CREATE TABLE `service_sequence` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 t_attribute
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_attribute`;

CREATE TABLE `t_attribute` (
  `id` int DEFAULT NULL,
  `userid` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `mail` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 t_cas
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_cas`;

CREATE TABLE `t_cas` (
  `id` int NOT NULL AUTO_INCREMENT,
  `userId` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `loginId` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `source` bigint DEFAULT NULL,
  `name` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `password` varchar(100) DEFAULT NULL,
  `email` varchar(256) DEFAULT NULL,
  `mobile` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `state` bit(1) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_userId` (`userId`),
  UNIQUE KEY `uniq_login_source` (`loginId`,`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;



# 转储表 user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint NOT NULL,
  `principal` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_Id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `source` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `avatar` longtext DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `state` bit(1) DEFAULT NULL,
  `created` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpepg6phi59kkgs77pa8atm5a3` (`user_Id`,`source`),
  UNIQUE KEY `UK_konrexqncmw6p8tc97r2f2nd3` (`principal`),
  KEY `source` (`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


set GLOBAL max_connections=256;
set global wait_timeout=100;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
