package com.monk.coupons.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.monk.coupons.dto.*;
import com.monk.coupons.model.Coupon;
import java.util.List;

public interface CouponService {
    Coupon create(CouponRequestDTO dto);
    List<Coupon> findAll();
    Coupon findById(Long id);
    Coupon update(Long id, CouponRequestDTO dto);
    void delete(Long id);

    List<ApplicableCouponDTO> applicableCoupons(CartDTO cart);
    ApplyCouponResponseDTO applyCoupon(Long id, CartDTO cart);
}
