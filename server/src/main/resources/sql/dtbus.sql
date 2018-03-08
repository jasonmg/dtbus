CREATE database dtbus DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE USER 'dtbus'@'%' IDENTIFIED BY 'dtbus';
GRANT ALL PRIVILEGES ON dtbus.* TO 'dtbus'@'%'  IDENTIFIED BY 'dtbus' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON dtbus.* TO 'dtbus'@'localhost'  IDENTIFIED BY 'dtbus' WITH GRANT OPTION;
flush privileges;

USE dtbus;
CREATE TABLE  IF NOT EXISTS `dt_audit` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `audit_ip` varchar(15) NOT NULL  COMMENT '库Ip',
  `audit_port` varchar(6) NOT NULL COMMENT '库端口',
  `audit_name` varchar(50) NOT NULL COMMENT '鉴权用户名',
  `audit_pwd` varchar(50) DEFAULT NULL COMMENT '鉴权用户密码',
  `ctime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鉴权时间',
  `is_valid` int NOT NULL DEFAULT 1 COMMENT '是否有效，默认有效',
  `type` varchar(20) DEFAULT NULL COMMENT 'mysql 为 mysql鉴权  os 为操作系统鉴权',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `user_id` varchar(200) DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dtaudit_uk` (`audit_ip`,`audit_name`,`is_valid`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8  COMMENT '鉴权表';


CREATE TABLE IF NOT EXISTS `dt_db` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `db_name` varchar(50) NOT NULL  COMMENT '库名',
  `cs_name` varchar(50) NOT NULL  COMMENT '字符集',
  `ct_name` varchar(50) NOT NULL  COMMENT '排序字符集',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT '库信息表';


CREATE TABLE IF NOT EXISTS `dt_audit_db` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `audit_id` int(11) NOT NULL  COMMENT '鉴权用户ID',
  `db_id`  int(11) NOT NULL  COMMENT '库Id',
  `is_vaild` int(11) NOT NULL DEFAULT 1 COMMENT '是否有效，默认有效',
  `begdate` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `ovedate` datetime DEFAULT '2099-12-30',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dt_audit_db_index` (`audit_id`,`db_id`,`is_vaild`,`begdate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT '鉴权和库关系表';

CREATE TABLE IF NOT EXISTS `dt_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `db_id`  int(11) NOT NULL  COMMENT '库Id',
  `table_name` varchar(200) NOT NULL  COMMENT '表名称',
  `engine` varchar(500) NOT NULL  COMMENT '引擎名称',
  `table_rows` int(11) DEFAULT NULL  COMMENT '表行数',
  `auto_increment`  int(11) DEFAULT NULL  COMMENT '表最大id',
  `create_time` datetime DEFAULT NULL COMMENT '表创建时间',
  `table_collation` varchar(500) DEFAULT NULL COMMENT '表字符集',
  `table_comment` varchar(500) DEFAULT NULL  COMMENT '表备注',
  `is_vaild` int(11) NOT NULL DEFAULT 1 COMMENT '是否有效，默认有效',
  `full_pulled` tinyint(1) DEFAULT '0' COMMENT '表是否全量拉取是否有效，默认无',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dttable_uk` (`db_id`,`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT '表表';


CREATE TABLE IF NOT EXISTS `dt_field` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `db_id`  int(11) NOT NULL  COMMENT '库Id',
  `table_id` int(11) NOT NULL  COMMENT '表Id',
  `column_name` varchar(200) NOT NULL  COMMENT '列名称',
  `data_type` varchar(250)  NOT NULL  COMMENT '数据类型',
  `character_maximum_length` int  DEFAULT NULL  COMMENT '最大长度',
  `column_comment` varchar(500)  DEFAULT NULL  COMMENT '字段备注',
  `is_vaild` int(11) NOT NULL DEFAULT 1 COMMENT '是否有效，默认有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dtfield_uk` (`db_id`,`table_id`,`column_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT '字段表';

CREATE TABLE IF NOT EXISTS `dt_apply` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `audit_id` int(11) NOT NULL  COMMENT '鉴权Id',
  `db_id`  int(11) NOT NULL  COMMENT '库Id',
  `table_id` int(11) NOT NULL  COMMENT '表Id(如果全库为0）',
  `is_full` int(11)  DEFAULT 0 COMMENT '是否全量，默认增量',
  `is_valid` int(11) DEFAULT 1 COMMENT '是否有效，默认有效',
  `kafka_server` varchar(500) DEFAULT NULL  COMMENT 'kafka服务器+端口',
  `kafka_topic` varchar(500) DEFAULT NULL  COMMENT 'kafka topic',
  `optime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dtapply_uk` (`audit_id`,`db_id`,`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT '同步申请表';


CREATE TABLE IF NOT EXISTS `log_apply` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `audit_id` int(11) NOT NULL  COMMENT '鉴权Id',
  `file_name` varchar(200) NOT NULL  COMMENT '文件名称（如果目录，则为/*）',
  `is_full` int(11)  DEFAULT 0 COMMENT '是否全量，默认增量',
  `full_pulled` int(11) DEFAULT '0' COMMENT '全量是否已经拉取，未拉取0，拉取为1',
  `is_valid` int(11) DEFAULT 1 COMMENT '是否有效，默认有效',
  `kafka_server` varchar(500) DEFAULT NULL  COMMENT 'kafka服务器+端口',
  `kafka_topic` varchar(500) DEFAULT NULL  COMMENT 'kafka topic',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `optime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dtapply_uk` (`audit_id`,`file_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT '文件监控表';

 CREATE TABLE dtbus.`tb_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID 自增主键',
  `user_id` varchar(50) DEFAULT NULL COMMENT '用户登录Id(oa账号）',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户名',
  `user_pwd` varchar(50) DEFAULT NULL COMMENT '用户密码',
  `is_valid` int(11) DEFAULT 1 COMMENT '是否有效，默认有效',
  `opdate` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '数据增改操作日期，默认当前日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户登录表';


-- modify by jimingjiang  at 2017-11-15
alter table dt_field MODIFY COLUMN `character_maximum_length` BIGINT DEFAULT NULL COMMENT '最大长度';
alter table dt_table MODIFY COLUMN `auto_increment` VARCHAR(200) DEFAULT NULL COMMENT '表最大id';
