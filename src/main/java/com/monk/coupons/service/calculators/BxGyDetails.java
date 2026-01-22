package com.monk.coupons.service.calculators;

import java.util.List;

public class BxGyDetails {
    public static class Entry {
        private Long productId;
        private int quantity;
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    private List<Entry> buyProducts; // products considered towards X
    private List<Entry> getProducts; // products eligible as Y
    private Integer repetitionLimit; // maximum number of times coupon can be applied

    public List<Entry> getBuyProducts() { return buyProducts; }
    public void setBuyProducts(List<Entry> buyProducts) { this.buyProducts = buyProducts; }
    public List<Entry> getGetProducts() { return getProducts; }
    public void setGetProducts(List<Entry> getProducts) { this.getProducts = getProducts; }
    public Integer getRepetitionLimit() { return repetitionLimit; }
    public void setRepetitionLimit(Integer repetitionLimit) { this.repetitionLimit = repetitionLimit; }
}
