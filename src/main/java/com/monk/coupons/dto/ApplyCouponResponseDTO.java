package com.monk.coupons.dto;

import java.math.BigDecimal;
import java.util.List;

public class ApplyCouponResponseDTO {
    public static class ItemWithDiscount {
        private Long productId;
        private int quantity;
        private BigDecimal price;
        private BigDecimal totalDiscount;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getTotalDiscount() { return totalDiscount; }
        public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }
    }

    private List<ItemWithDiscount> items;
    private BigDecimal totalPrice; // sum of qty*price (after quantity adjustments for bxgy)
    private BigDecimal totalDiscount;
    private BigDecimal finalPrice; // totalPrice - totalDiscount

    public List<ItemWithDiscount> getItems() { return items; }
    public void setItems(List<ItemWithDiscount> items) { this.items = items; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
}
