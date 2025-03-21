package com.amalitech.test.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private String id;
    private String userId;
    private List<CartItem> items;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;

    public Cart() {
        this.items = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
        recalculateTotals();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    // Helper methods
    public void addItem(CartItem item) {
        this.items.add(item);
        recalculateTotals();
    }

    public void removeItem(Long productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotals();
    }

    public void updateItemQuantity(Long productId, int quantity) {
        this.items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    recalculateTotals();
                });
    }

    private void recalculateTotals() {
        this.subtotal = this.items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assume tax rate of 10%
        this.tax = this.subtotal.multiply(new BigDecimal("0.1"));

        this.total = this.subtotal.add(this.tax);
    }
}
