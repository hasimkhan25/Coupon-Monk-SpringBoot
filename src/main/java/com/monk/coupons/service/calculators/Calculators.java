package com.monk.coupons.service.calculators;

import com.monk.coupons.dto.ApplyCouponResponseDTO;
import com.monk.coupons.dto.CartDTO;
import com.monk.coupons.dto.CartItemDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class Calculators {

    public record Result(BigDecimal discount, ApplyCouponResponseDTO response) {}

    private static ApplyCouponResponseDTO.ItemWithDiscount copyItem(CartItemDTO item) {
        ApplyCouponResponseDTO.ItemWithDiscount out = new ApplyCouponResponseDTO.ItemWithDiscount();
        out.setProductId(item.getProductId());
        out.setQuantity(item.getQuantity());
        out.setPrice(item.getPrice());
        out.setTotalDiscount(BigDecimal.ZERO);
        return out;
    }

    private static BigDecimal lineTotal(int qty, BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(qty));
    }

    private static void finalizeTotals(ApplyCouponResponseDTO resp) {
        BigDecimal total = resp.getItems().stream()
                .map(i -> lineTotal(i.getQuantity(), i.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = resp.getItems().stream()
                .map(ApplyCouponResponseDTO.ItemWithDiscount::getTotalDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resp.setTotalPrice(total.setScale(2, RoundingMode.HALF_UP));
        resp.setTotalDiscount(discount.setScale(2, RoundingMode.HALF_UP));
        resp.setFinalPrice(total.subtract(discount).setScale(2, RoundingMode.HALF_UP));
    }

    public static Result cartWise(CartDTO cart, CartWiseDetails d) {
        BigDecimal threshold = d.getThreshold() == null ? BigDecimal.ZERO : d.getThreshold();
        BigDecimal percent = d.getPercent();
        if (percent == null) throw new IllegalArgumentException("percent missing");
        BigDecimal total = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ApplyCouponResponseDTO resp = new ApplyCouponResponseDTO();
        resp.setItems(cart.getItems().stream().map(Calculators::copyItem).collect(Collectors.toList()));
        if (total.compareTo(threshold) < 0) {
            finalizeTotals(resp); // discount 0
            return new Result(BigDecimal.ZERO, resp);
        }
        BigDecimal discount = total.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (d.getMaxDiscount() != null) {
            discount = discount.min(d.getMaxDiscount());
        }
        // distribute discount proportionally across lines for visibility
        BigDecimal remaining = discount;
        BigDecimal runningTotal = total;
        for (int idx = 0; idx < resp.getItems().size(); idx++) {
            ApplyCouponResponseDTO.ItemWithDiscount item = resp.getItems().get(idx);
            BigDecimal lt = lineTotal(item.getQuantity(), item.getPrice());
            BigDecimal lineDisc = (idx == resp.getItems().size()-1)
                    ? remaining
                    : lt.multiply(discount).divide(runningTotal, 2, RoundingMode.HALF_UP);
            item.setTotalDiscount(lineDisc);
            remaining = remaining.subtract(lineDisc);
        }
        finalizeTotals(resp);
        return new Result(discount, resp);
    }

    public static Result productWise(CartDTO cart, ProductWiseDetails d) {
        Map<Long, BigDecimal> percentByProduct = new HashMap<>();
        if (d.getProducts() != null) {
            for (ProductWiseDetails.ProductDiscount pd : d.getProducts()) {
                percentByProduct.put(pd.getProductId(), pd.getPercent());
            }
        }
        ApplyCouponResponseDTO resp = new ApplyCouponResponseDTO();
        resp.setItems(cart.getItems().stream().map(Calculators::copyItem).collect(Collectors.toList()));
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (ApplyCouponResponseDTO.ItemWithDiscount item : resp.getItems()) {
            BigDecimal pct = percentByProduct.get(item.getProductId());
            if (pct != null && pct.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal lineDisc = item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                item.setTotalDiscount(lineDisc);
                totalDiscount = totalDiscount.add(lineDisc);
            }
        }
        finalizeTotals(resp);
        return new Result(totalDiscount, resp);
    }

    public static Result bxgy(CartDTO cart, BxGyDetails d) {
        if (d.getBuyProducts() == null || d.getBuyProducts().isEmpty())
            throw new IllegalArgumentException("buyProducts missing");
        if (d.getGetProducts() == null || d.getGetProducts().isEmpty())
            throw new IllegalArgumentException("getProducts missing");
        int repetitionLimit = d.getRepetitionLimit() == null ? Integer.MAX_VALUE : d.getRepetitionLimit();
        // Compute required X and Y from details: sum of buy quantities defines X per application; sum of get quantities defines Y per application
        int xPer = d.getBuyProducts().stream().mapToInt(BxGyDetails.Entry::getQuantity).sum();
        int yPer = d.getGetProducts().stream().mapToInt(BxGyDetails.Entry::getQuantity).sum();
        // Count how many buy items are present in cart among buy list
        Set<Long> buyIds = d.getBuyProducts().stream().map(BxGyDetails.Entry::getProductId).collect(Collectors.toSet());
        Set<Long> getIds = d.getGetProducts().stream().map(BxGyDetails.Entry::getProductId).collect(Collectors.toSet());
        int buyCount = cart.getItems().stream()
                .filter(i -> buyIds.contains(i.getProductId()))
                .mapToInt(CartItemDTO::getQuantity).sum();
        int applications = Math.min(buyCount / xPer, repetitionLimit);
        ApplyCouponResponseDTO resp = new ApplyCouponResponseDTO();
        // Copy items
        List<ApplyCouponResponseDTO.ItemWithDiscount> items = cart.getItems().stream().map(Calculators::copyItem).collect(Collectors.toList());
        resp.setItems(items);
        if (applications <= 0) {
            finalizeTotals(resp);
            return new Result(BigDecimal.ZERO, resp);
        }
        int freebies = applications * yPer;
        // Consider only get items that exist in cart (assumption: cannot add new line items without price)
        List<ApplyCouponResponseDTO.ItemWithDiscount> eligibleGet = items.stream()
                .filter(i -> getIds.contains(i.getProductId()))
                .sorted((a,b) -> b.getPrice().compareTo(a.getPrice())) // give free the most expensive first
                .collect(Collectors.toList());
        int remaining = freebies;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (ApplyCouponResponseDTO.ItemWithDiscount item : eligibleGet) {
            if (remaining <= 0) break;
            int freeHere = Math.min(remaining, item.getQuantity());
            if (freeHere > 0) {
                // Increase quantity to reflect free items like the example, and discount equals free qty * price
                item.setQuantity(item.getQuantity() + freeHere);
                BigDecimal disc = item.getPrice().multiply(BigDecimal.valueOf(freeHere));
                item.setTotalDiscount(item.getTotalDiscount().add(disc));
                totalDiscount = totalDiscount.add(disc);
                remaining -= freeHere;
            }
        }
        finalizeTotals(resp);
        return new Result(totalDiscount, resp);
    }
}
