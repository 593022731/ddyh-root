package com.ddyh.user.facade.constant;

/**
 * 角色类型
 */
public enum CharacterTypeEnum {

    USER_NORMAL_TYPE((short)1, "普通用户"),
    USER_MEMBER_TYPE((short)2, "京卡会员"),
    USER_CHANNEL_PROVIDER_TYPE((short)4, "渠道商/联创");

    private Short type;

    private String desc;

    CharacterTypeEnum(Short type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Short getType() {
        return type;
    }}