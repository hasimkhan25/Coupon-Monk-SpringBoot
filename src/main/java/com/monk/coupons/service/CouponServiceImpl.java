package com.monk.coupons.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.coupons.dto.*;
import com.monk.coupons.exception.NotFoundException;
import com.monk.coupons.model.Coupon;
import com.monk.coupons.model.CouponType;
import com.monk.coupons.repository.CouponRepository;
import com.monk.coupons.service.calculators.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CouponServiceImpl implements CouponService {
    private final CouponRepository repo;
    private final ObjectMapper mapper;

    public CouponServiceImpl(CouponRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Coupon create(CouponRequestDTO dto) {
        Coupon c = new Coupon();
        c.setCode(dto.getCode());
        c.setType(dto.getType());
        try { c.setDetailsJson(mapper.writeValueAsString(dto.getDetails())); }
        catch (JsonProcessingException e) { throw new IllegalArgumentException("Invalid details JSON"); }
        c.setStartsAt(dto.getStartsAt());
        c.setEndsAt(dto.getEndsAt());
        c.setActive(dto.getActive() == null || dto.getActive());
        return repo.save(c);
    }

    @Override
    public List<Coupon> findAll() { return repo.findAll(); }

    @Override
    public Coupon findById(Long id) { return repo.findById(id).orElseThrow(() -> new NotFoundException("Coupon not found")); }

    @Override
    public Coupon update(Long id, CouponRequestDTO dto) {
        Coupon c = findById(id);
        if (dto.getCode() != null) c.setCode(dto.getCode());
        if (dto.getType() != null) c.setType(dto.getType());
        if (dto.getDetails() != null) {
            try { c.setDetailsJson(mapper.writeValueAsString(dto.getDetails())); }
            catch (JsonProcessingException e) { throw new IllegalArgumentException("Invalid details JSON"); }
        }
        if (dto.getStartsAt() != null) c.setStartsAt(dto.getStartsAt());
        if (dto.getEndsAt() != null) c.setEndsAt(dto.getEndsAt());
        if (dto.getActive() != null) c.setActive(dto.getActive());
        return repo.save(c);
    }

    @Override
    public void delete(Long id) { repo.deleteById(id); }

    private boolean withinValidity(Coupon c) {
        LocalDateTime now = LocalDateTime.now();
        if (!c.isActive()) return false;
        if (c.getStartsAt() != null && now.isBefore(c.getStartsAt())) return false;
        if (c.getEndsAt() != null && now.isAfter(c.getEndsAt())) return false;
        return true;
    }

    @Override
    public List<ApplicableCouponDTO> applicableCoupons(CartDTO cart) {
        List<Coupon> all = repo.findAll();
        return all.stream().map(c -> {
            ApplicableCouponDTO dto = new ApplicableCouponDTO();
            dto.setCouponId(c.getId());
            dto.setCode(c.getCode());
            dto.setType(c.getType());
            if (!withinValidity(c)) { dto.setReason("Coupon inactive or expired"); dto.setDiscount(BigDecimal.ZERO); return dto; }
            try {
                BigDecimal discount = calculateDiscount(c, cart);
                dto.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));
                dto.setReason(discount.signum() > 0 ? "Applicable" : "Not applicable");
            } catch (IllegalArgumentException ex) {
                dto.setDiscount(BigDecimal.ZERO); dto.setReason(ex.getMessage());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ApplyCouponResponseDTO applyCoupon(Long id, CartDTO cart) {
        Coupon c = findById(id);
        if (!withinValidity(c)) throw new IllegalArgumentException("Coupon inactive or expired");
        return applyInternal(c, cart);
    }

    private BigDecimal calculateDiscount(Coupon coupon, CartDTO cart) {
        switch (coupon.getType()) {
            case CART_WISE -> {
                CartWiseDetails d = readDetails(coupon, CartWiseDetails.class);
                return Calculators.cartWise(cart, d).discount();
            }
            case PRODUCT_WISE -> {
                ProductWiseDetails d = readDetails(coupon, ProductWiseDetails.class);
                return Calculators.productWise(cart, d).discount();
            }
            case BXGY -> {
                BxGyDetails d = readDetails(coupon, BxGyDetails.class);
                return Calculators.bxgy(cart, d).discount();
            }
            default -> throw new IllegalArgumentException("Unsupported type");
        }
    }

    private ApplyCouponResponseDTO applyInternal(Coupon coupon, CartDTO cart) {
        switch (coupon.getType()) {
            case CART_WISE -> {
                CartWiseDetails d = readDetails(coupon, CartWiseDetails.class);
                return Calculators.cartWise(cart, d).response();
            }
            case PRODUCT_WISE -> {
                ProductWiseDetails d = readDetails(coupon, ProductWiseDetails.class);
                return Calculators.productWise(cart, d).response();
            }
            case BXGY -> {
                BxGyDetails d = readDetails(coupon, BxGyDetails.class);
                return Calculators.bxgy(cart, d).response();
            }
            default -> throw new IllegalArgumentException("Unsupported type");
        }
    }

    private <T> T readDetails(Coupon c, Class<T> type) {
        try { return mapper.readValue(c.getDetailsJson(), type); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid coupon details for type " + c.getType()); }
    }
}
