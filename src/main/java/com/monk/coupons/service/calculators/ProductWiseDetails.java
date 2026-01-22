package com.monk.coupons.service.calculators;

import java.math.BigDecimal;
import java.util.List;

public class ProductWiseDetails {
    public static class ProductDiscount {
        private Long productId;
        private BigDecimal percent; // percent discount for this product
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public BigDecimal getPercent() { return percent; }
        public void setPercent(BigDecimal percent) { this.percent = percent; }
    }
    private List<ProductDiscount> products;

    public List<ProductDiscount> getProducts() { return products; }
    public void setProducts(List<ProductDiscount> products) { this.products = products; }
}
