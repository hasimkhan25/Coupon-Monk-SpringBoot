package com.monk.coupons.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.coupons.dto.*;
import com.monk.coupons.model.Coupon;
import com.monk.coupons.model.CouponType;
import com.monk.coupons.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class CouponServiceImplTest {

    private CouponServiceImpl service;
    private CouponRepository repo;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(CouponRepository.class);
        mapper = new ObjectMapper();
        service = new CouponServiceImpl(repo, mapper);
    }

    private CartDTO sampleCart() {
        CartDTO cart = new CartDTO();
        CartItemDTO a = new CartItemDTO(); a.setProductId(1L); a.setQuantity(6); a.setPrice(BigDecimal.valueOf(50));
        CartItemDTO b = new CartItemDTO(); b.setProductId(2L); b.setQuantity(3); b.setPrice(BigDecimal.valueOf(30));
        CartItemDTO c = new CartItemDTO(); c.setProductId(3L); c.setQuantity(2); c.setPrice(BigDecimal.valueOf(25));
        cart.setItems(List.of(a,b,c));
        return cart;
    }

    @Test
    void testCartWise() throws Exception {
        Coupon c = new Coupon();
        c.setId(1L); c.setCode("CART10"); c.setType(CouponType.CART_WISE);
        c.setDetailsJson("{"threshold":100, "percent":10}");
        when(repo.findById(1L)).thenReturn(Optional.of(c));
        ApplyCouponResponseDTO resp = service.applyCoupon(1L, sampleCart());
        assertEquals(new BigDecimal("49.00"), resp.getTotalDiscount()); // 10% of 490 after proportional distribution
    }

    @Test
    void testProductWise() throws Exception {
        Coupon c = new Coupon();
        c.setId(2L); c.setCode("PROD20_A"); c.setType(CouponType.PRODUCT_WISE);
        c.setDetailsJson("{"products":[{"productId":1,"percent":20}]}");
        when(repo.findById(2L)).thenReturn(Optional.of(c));
        ApplyCouponResponseDTO resp = service.applyCoupon(2L, sampleCart());
        assertEquals(new BigDecimal("60.00"), resp.getTotalDiscount()); // 20% of 6*50
    }

    @Test
    void testBxGy() throws Exception {
        Coupon c = new Coupon();
        c.setId(3L); c.setCode("B2G1"); c.setType(CouponType.BXGY);
        c.setDetailsJson("{"buyProducts":[{"productId":1,"quantity":2}], "getProducts":[{"productId":3,"quantity":1}], "repetitionLimit":2}");
        when(repo.findById(3L)).thenReturn(Optional.of(c));
        ApplyCouponResponseDTO resp = service.applyCoupon(3L, sampleCart());
        assertEquals(new BigDecimal("50.00"), resp.getTotalDiscount());
    }
}
