package com.ddyh.product.facade.param;

/**
 * @author: cqry2017
 * @Date: 2019/8/30 10:36
 * @descript:
 */
@SuppressWarnings("all")
public enum ProductLabelEnum {
    RECOMMEND_TYPE("recommendType", "首页精选"),
    POINTED_CARGO("pointedCargo", "超级尖货"),
    EXPERIENCE_CARD_INVITE("experienceCardInvite", "体验卡邀请"),
    ;

    private String name;
    private String desc;

    ProductLabelEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
