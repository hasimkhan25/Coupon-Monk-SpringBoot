package com.monk.coupons.service.calculators;

import java.math.BigDecimal;

public class CartWiseDetails {
    private BigDecimal threshold; // minimum cart total to apply
    private BigDecimal percent;   // percentage discount e.g., 10 means 10%
    private BigDecimal maxDiscount; // optional cap

    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }
    public BigDecimal getPercent() { return percent; }
    public void setPercent(BigDecimal percent) { this.percent = percent; }
    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount; }
}
