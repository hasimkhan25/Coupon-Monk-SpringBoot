package com.monk.coupons.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CartDTO {
    @Valid
    @NotEmpty
    private List<CartItemDTO> items;

    public List<CartItemDTO> getItems() { return items; }
    public void setItems(List<CartItemDTO> items) { this.items = items; }
}
