package com.monk.coupons.dto;

import com.monk.coupons.model.CouponType;
import java.math.BigDecimal;

public class ApplicableCouponDTO {
    private Long couponId;
    private String code;
    private CouponType type;
    private BigDecimal discount;
    private String reason; // if not applicable or notes

    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public CouponType getType() { return type; }
    public void setType(CouponType type) { this.type = type; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
