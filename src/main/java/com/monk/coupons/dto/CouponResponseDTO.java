package com.monk.coupons.dto;

import com.monk.coupons.model.CouponType;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public class CouponResponseDTO {
    private Long id;
    private String code;
    private CouponType type;
    private JsonNode details;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public CouponType getType() { return type; }
    public void setType(CouponType type) { this.type = type; }
    public JsonNode getDetails() { return details; }
    public void setDetails(JsonNode details) { this.details = details; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
