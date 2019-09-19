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
