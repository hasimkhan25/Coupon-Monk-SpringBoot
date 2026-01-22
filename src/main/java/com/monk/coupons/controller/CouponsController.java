package com.monk.coupons.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.coupons.dto.*;
import com.monk.coupons.model.Coupon;
import com.monk.coupons.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class CouponsController {
    private final CouponService service;
    private final ObjectMapper mapper;

    public CouponsController(CouponService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping("/coupons")
    public ResponseEntity<Coupon> create(@Valid @RequestBody CouponRequestDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/coupons")
    public List<Coupon> all() { return service.findAll(); }

    @GetMapping("/coupons/{id}")
    public Coupon get(@PathVariable Long id) { return service.findById(id); }

    @PutMapping("/coupons/{id}")
    public Coupon update(@PathVariable Long id, @Valid @RequestBody CouponRequestDTO dto) { return service.update(id, dto); }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }

    @PostMapping("/applicable-coupons")
    public List<ApplicableCouponDTO> applicable(@Valid @RequestBody CartDTO cart) { return service.applicableCoupons(cart); }

    @PostMapping("/apply-coupon/{id}")
    public ApplyCouponResponseDTO apply(@PathVariable Long id, @Valid @RequestBody CartDTO cart) { return service.applyCoupon(id, cart); }
}
