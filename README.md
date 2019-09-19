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

现有的返利表
```
CREATE TABLE `t_rebate` (
  `rebate_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `consumer_id` bigint(20) NOT NULL COMMENT '消费用户id',
  `reward_user_id` bigint(20) NOT NULL COMMENT '返利(奖励)用户id',
  `reward_amount` decimal(8,2) NOT NULL COMMENT '返利额度=返利数/100',
  `state` smallint(6) NOT NULL DEFAULT '0' COMMENT '-1为已取消，0为未入账，1为未复核，2为已复核(其中1、2都代表已入账，用户在兑换时会复核一遍所有未复核的返利，然后状态改成已复核)',
  `create_time` datetime NOT NULL COMMENT '创建(返利)时间',
  `settlement_time` datetime DEFAULT NULL COMMENT '结算时间',
  `consumer_phone` varchar(11) NOT NULL COMMENT '消费用户手机号',
  `order_type` smallint(6) NOT NULL COMMENT '订单类型：1为大礼包，2为京东商品，3为团购，5为体验卡',
  `price` decimal(7,2) NOT NULL COMMENT '订单裸价',
  `time_to_settlement` datetime NOT NULL COMMENT '可结算时间',
  `rebate_user_type` smallint(6) NOT NULL COMMENT '1为会员/分销商，2为渠道商',
  `rebate_num` int(11) NOT NULL COMMENT '返利数=返利额度*100',
  `rebate_type` smallint(6) NOT NULL COMMENT '1为大礼包一级，2为大礼包二级，3为大礼包额外返利，4为大礼包渠道，5为jd一级，6为jd二级，7为jd额外返利，8为jd渠道，9为体验卡购卡返利，10为体验卡购物返利，11为分享赚购物返利(其中1,2,5,6,9,10,11为A系统返利)',
  `order_profit` decimal(8,2) NOT NULL COMMENT '订单总返利值',
  `reward_user_phone` varchar(15) NOT NULL COMMENT '奖励人手机号',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`rebate_id`),
  KEY `idx_reward_user_phone` (`reward_user_phone`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='返利表'


CREATE TABLE `t_rebate_already` (
  `rebate_already_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rebate_user_id` bigint(20) NOT NULL COMMENT '申请人uid',
  `rebate_num` int(11) NOT NULL COMMENT '兑换值',
  `rebate_time` datetime NOT NULL COMMENT '申请兑换时间',
  `state` smallint(6) NOT NULL COMMENT '状态：-1已驳回，0已提交，1已审核，2已兑换',
  `account_num` char(32) DEFAULT NULL COMMENT '平台打款编号',
  `out_account_num` char(64) DEFAULT NULL COMMENT '美差打款编号',
  `account_time` char(32) DEFAULT NULL COMMENT '美差打款时间',
  `rebate_from` smallint(6) NOT NULL COMMENT '兑换来源(1:A系统，2:B系统)',
  `rebate_fee` decimal(8,2) DEFAULT NULL COMMENT '美差打款手续费',
  `reward_user_phone` varchar(50) DEFAULT NULL COMMENT '奖励人手机号',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`rebate_already_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='已返利表(兑换返利记录表，用户兑换一次，生成一次记录)'



CREATE TABLE `t_total_rebate` (
  `total_rebate_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rebate_user_id` bigint(20) NOT NULL COMMENT '返利人id',
  `account_entry_total_num` int(11) NOT NULL COMMENT '已入账总返利数(可以理解为已经入账的返利数，大礼包返利直接++，京东商品需要30天后++)',
  `un_account_entry_total_num` int(11) NOT NULL COMMENT '未入账总返利数(可以理解为京东商品还未入账返利数,刚购买时此字段++,30天到期后此字段--)',
  `rebated_num` int(11) NOT NULL COMMENT '已兑换数(每次兑换就++，且插入一条已返利表已提交状态的记录，如果驳回了，就先根据联创/渠道商的记录--，不够或不存在联创/渠道商身份，去用会员/分销商的记录--，然后改变已返利表的状态为-1未通过)',
  `check_num` int(11) NOT NULL COMMENT '已复核数(每次兑换时，会去返利表累计所有未复核状态的返利数，再累加到此字段中)',
  `last_rebate_time` datetime DEFAULT NULL COMMENT '上次返利时间',
  `rebate_user_type` smallint(6) NOT NULL COMMENT '返利用户类型：(1:会员/分销商，2:渠道商/联创)',
  `reward_user_phone` varchar(15) NOT NULL COMMENT '奖励人手机号(通过此字段可以直接查询联创和会员/分销商的sum(返利)，因为手机号一致)(可提现余额=sum(account_entry_total_num-rebated_num)，账户余额(包含未入账)=可提现余额+sum(un_account_entry_total_num),账户总金额=账户余额+sum(rebated_num))',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`total_rebate_id`),
  KEY `idx_reward_user_phone` (`reward_user_phone`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='总返利表'

```

现在用户表
```

CREATE TABLE `t_user` (
  `uid` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'UID',
  `nick_name` varchar(128) NOT NULL COMMENT '用户昵称',
  `phone` varchar(15) NOT NULL COMMENT '注册账号',
  `sex` smallint(6) NOT NULL DEFAULT '0' COMMENT '性别：0代表女，1代表男',
  `status` smallint(6) NOT NULL DEFAULT '0' COMMENT '用户状态：0未激活，1正常，2冻结',
  `password` varchar(50) NOT NULL COMMENT '密码',
  `head_img` varchar(255) DEFAULT NULL COMMENT '头像',
  `open_id` varchar(50) DEFAULT NULL COMMENT '登陆token',
  `channel_id` bigint(20) DEFAULT NULL COMMENT '所属渠道id(当character_type=4时，代表此用户是渠道商/联创)',
  `fcode` char(8) NOT NULL COMMENT 'F码',
  `parent_fcode` char(8) DEFAULT NULL COMMENT '上级F码',
  `wx_open_id` varchar(50) DEFAULT NULL COMMENT '微信openID',
  `character_type` smallint(6) NOT NULL COMMENT '角色类型(1:普通用户，2：京卡会员，4：渠道商/联创)',
  `exp_card_type` int(11) NOT NULL DEFAULT '0' COMMENT '体验卡类型(0代表从未购买，-1代表之前购买过，大于0代表已经是体验卡对应的类型)',
  `registration_time` datetime NOT NULL COMMENT '注册时间(老数据是日期类型)',
  `bind_time` timestamp NULL DEFAULT NULL COMMENT '绑定上下级时间(上下级更新，时间会更新)',
  `is_wx_auth` int(11) DEFAULT '0' COMMENT '是否已经微信授权 (0:未授权,1:已授权,-1:用户拒绝授权)(微信回调时set(1),0和-1暂时没找到代码，默认新用户应该是null)',
  `location` varchar(255) DEFAULT NULL COMMENT '用户所在地区(微信回调时，获取微信用户的所在地址拼接)',
  `wx_img` varchar(255) DEFAULT NULL COMMENT '微信二维码图片',
  `wx_account` varchar(50) DEFAULT NULL COMMENT '微信号',
  `path` varchar(255) DEFAULT NULL COMMENT '用户路径(通过此字段可以查询团队下属，如/1/2/3其中1是用户的id，可以通过 /1/%来查询他的下级用户)',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `phone` (`phone`),
  KEY `idx_path` (`path`),
  KEY `idx_fcode` (`fcode`),
  KEY `idx_parent_fcode` (`parent_fcode`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户表'


CREATE TABLE `t_user_device` (
  `uid` bigint(20) NOT NULL COMMENT 'UID',
  `phone` varchar(15) NOT NULL COMMENT '注册账号',
  `app_open_id` varchar(50) DEFAULT NULL COMMENT 'app token',
  `web_open_id` varchar(50) DEFAULT NULL COMMENT 'h5 token',
  `b_open_id` varchar(50) DEFAULT NULL COMMENT 'b系统 token',
  `last_app_type` smallint(5) DEFAULT NULL COMMENT '最后一次登录设备类型',
  `last_version` varchar(20) DEFAULT NULL COMMENT '最后一次登录设备版本',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户设备信息表'


CREATE TABLE `t_member` (
  `member_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT 'UID',
  `member_level` smallint(6) NOT NULL DEFAULT '1' COMMENT '会员等级(目前查了下代码只有1，变成会员就是1，可能是扩展字段)',
  `upgrade_time` datetime NOT NULL COMMENT '升级时间(第一次创建时间，此时间不会更新)',
  `valid_time` date NOT NULL COMMENT '有效期(过期日期，精确到天)',
  `state` smallint(6) NOT NULL DEFAULT '1' COMMENT '0为失效，1为有效',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='会员表'

CREATE TABLE `t_member_team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT 'UID',
  `user_parent_id` bigint(20) NOT NULL COMMENT '上级UID',
  `low_level_member_num` int(11) NOT NULL COMMENT '下级会员数量',
  `identity` smallint(6) NOT NULL COMMENT '0：只是会员(没有购买或升级身份)，1：经理，2：总监，3：总裁，9：联创',
  `upgrade_method` smallint(6) NOT NULL COMMENT '升级方式(0:自动升级，下属团队人数达标;1:手动修改,数据库人为修改;2:购买团购)',
  `upgrade_time` datetime NOT NULL COMMENT '升级时间(会更新)',
  `channel_id` bigint(20) NOT NULL COMMENT '渠道ID(从user表中获取的渠道ID)',
  `use_num` int(11) DEFAULT '0' COMMENT '团购名额实际使用数量(转增给别人+大礼包购买)',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='会员团队表(与会员表一对一关联)'

```

目前除了订单表重新设计，其他都需要重构
