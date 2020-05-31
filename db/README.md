参考美团leaf实现

```sql
DROP TABLE IF 	EXISTS `id_alloc`;
CREATE TABLE `id_alloc` (
`biz_tag` VARCHAR ( 128 ) NOT NULL DEFAULT '' COMMENT '业务key',
`max_id` BIGINT ( 20 ) NOT NULL DEFAULT '1' COMMENT '当前已经分配了的最大id',
`step` INT ( 11 ) NOT NULL COMMENT '初始步长，也是动态调整的最小步长',
`description` VARCHAR ( 256 ) DEFAULT NULL COMMENT '业务key的描述',
`update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '数据库维护的更新时间',
PRIMARY KEY ( `biz_tag` ) 
) ENGINE = INNODB;
```