# 东东优汇商城重构
目前支付系统可以用，订单系统设计过

##分层说明：
- api 是http接口，APP、H5接口
- admin 是http接口，管理后台
- facade 是dubbo接口，对内使用
- service 是核心逻辑
- dao 是数据访问层 

##模块说明：
- commons 通用模块，工具栏，result等
- order 订单系统
- pay 支付(退款、打款)系统
- product 商品系统
- rebate 返利系统
- user 用户系统(会员体系)
- business 除去以上模块，其他不好拆分的领域业务相关都放在这里

订单表重构：

```
CREATE TABLE `t_order_new` (
  `order_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `uid` bigint(20) DEFAULT NULL COMMENT 'UID',
  `order_num` varchar(25) NOT NULL COMMENT '订单号',
  `order_from` smallint(6) NOT NULL COMMENT '订单出处：1为大礼包，2为jd商品，3为团购，4为京东大礼盒，5为体验卡', 
  `order_price` decimal(8,2) NOT NULL COMMENT '订单裸价 + 运费',
  `order_status` smallint(6) NOT NULL COMMENT '0待付款，1已付款，2未发货，3已发货，4已完成，5取消中，6已取消',
  `state` smallint(6) NOT NULL DEFAULT '0' COMMENT '逻辑删除(0:删除，1:正常)',
  `pay_price` decimal(8,2) DEFAULT NULL COMMENT '支付金额',
  `pay_channel` varchar(50) DEFAULT NULL COMMENT '支付渠道',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `FK_Reference_5` (`uid`),
  CONSTRAINT `FK_Reference_5` FOREIGN KEY (`uid`) REFERENCES `t_user` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='订单主表';


CREATE TABLE `t_order_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单主表ID',
  `name` varchar(30) NOT NULL COMMENT '收货人',
  `phone` varchar(30) NOT NULL COMMENT '收货电话',
  `province_id` int(11) NOT NULL DEFAULT '0' COMMENT '省id',
  `city_id` int(11) NOT NULL DEFAULT '0' COMMENT '市id',
  `county_id` int(11) NOT NULL DEFAULT '0' COMMENT '区/县id',
  `town_id` int(11) NOT NULL DEFAULT '0' COMMENT '乡镇id',
  `detail` varchar(200) NOT NULL DEFAULT '' COMMENT '详细地址',
  `address` varchar(200) NOT NULL COMMENT '收获地址(省市区详细地址拼接的全地址)',  
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_Reference_29` (`order_id`),
  CONSTRAINT `FK_Reference_29` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='订单收货地址表';


CREATE TABLE `t_order_jd` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单主表ID',
  `order_naked_price` decimal(8,2) NOT NULL COMMENT '订单裸价(不包含运费)=所有的商品销售价(京东价/会员价)*购买数量之和',
  `order_floor_price` decimal(8,2) NOT NULL COMMENT '底价裸价+运费',
  `order_floor_naked_price` decimal(8,2) NOT NULL COMMENT '底价裸价=供货价(订单项供货价*购买数量)',
  `jd_order_id` bigint(20) NOT NULL COMMENT '京东订单号',
  `hq_order_num` varchar(25) NOT NULL COMMENT '环球订单号',
  `share_profit_fcode` varchar(20) NULL COMMENT '分享赚标识(用户fcode)',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_Reference_29` (`order_id`),
  CONSTRAINT `FK_Reference_29` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='京东子订单表';

CREATE TABLE `t_order_gift` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单主表ID',
  `sku_id` bigint(20) NOT NULL COMMENT '京东大礼盒商品表ID',
  `invitation_code` varchar(50) DEFAULT NULL COMMENT '邀请码(购买大礼包时，需要记录此字段，用作支付回调更新上下级使用)',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_Reference_29` (`order_id`),
  CONSTRAINT `FK_Reference_29` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='大礼包子订单表';

CREATE TABLE `t_order_jd_gift` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单主表ID',
  `gift_skus` varchar(255) DEFAULT NULL COMMENT '京东大礼盒订单的选品skus',
  `order_id_fromjd` bigint(20) NOT NULL COMMENT '手动生成的京东商品类型的订单ID',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_Reference_29` (`order_id`),
  CONSTRAINT `FK_Reference_29` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='京东大礼盒子订单表';


CREATE TABLE `t_order_pre_buy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单主表ID',
  `identity` smallint(6) NOT NULL COMMENT '预购身份(1:经理，2：总监)只能买经理和总监',  
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_Reference_29` (`order_id`),
  CONSTRAINT `FK_Reference_29` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='团购子订单表';


CREATE TABLE `t_order_item_new` (
  `order_item_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NOT NULL COMMENT '订单表ID',
  `sku_id` bigint(20) NOT NULL COMMENT 'sku',
  `num` int(11) NOT NULL COMMENT '购买数量',
  `sale_price` decimal(8,2) NOT NULL COMMENT '销售价格(会员价/零售价)',
  `floor_price` decimal(8,2) NOT NULL COMMENT '供货价',
  `platform_price` decimal(8,2) NOT NULL COMMENT '零售价',
  `member_price` decimal(8,2) NOT NULL COMMENT '会员价',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `product_type` int(11) NOT NULL COMMENT '1：唯品会，2：京东',
  `product_picture` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_item_id`),
  KEY `FK_Reference_29` (`order_id`),
  CONSTRAINT `FK_Reference_29` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='订单项表';
```
