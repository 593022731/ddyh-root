package com.ddyh.product.facade.param;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author: cqry2017
 * @Date: 2019/6/11 16:06
 */
public class LabelUpdateParam implements Serializable {

    @NotNull(message = "商品sku不能为空")
    private Long sku;

    private String[] label;

    private Integer recommendType;

    private Integer pointedCargo;

    private Integer experienceCardInvite;

    public Integer getRecommendType() {
        return getLabelVal(ProductLabelEnum.RECOMMEND_TYPE);
    }

    public void setRecommendType(Integer recommendType) {
        this.recommendType = recommendType;
    }

    public Integer getPointedCargo() {
        return getLabelVal(ProductLabelEnum.POINTED_CARGO);
    }

    public Integer getExperienceCardInvite() {
        return getLabelVal(ProductLabelEnum.EXPERIENCE_CARD_INVITE);
    }

    public void setExperienceCardInvite(Integer experienceCardInvite) {
        this.experienceCardInvite = experienceCardInvite;
    }

    public void setPointedCargo(Integer pointedCargo) {
        this.pointedCargo = pointedCargo;
    }

    public Long getSku() {
        return sku;
    }

    public void setSku(Long sku) {
        this.sku = sku;
    }

    public String[] getLabel() {
        return label;
    }

    public void setLabel(String[] label) {
        this.label = label;
    }

    private int getLabelVal(ProductLabelEnum labelType) {
        if (label == null || label.length == 0) {
            return 0;
        }
        for (String s : label) {
            if (labelType.getName().equals(s)) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
