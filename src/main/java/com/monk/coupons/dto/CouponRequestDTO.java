package com.monk.coupons.dto;

import com.monk.coupons.model.CouponType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CouponRequestDTO {
    @NotBlank
    private String code;
    @NotNull
    private CouponType type;
    @NotNull
    private JsonNode details; // Polymorphic details per type

    private LocalDateTime startsAt; // optional
    private LocalDateTime endsAt; // optional
    private Boolean active = true;

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
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
