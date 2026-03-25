package com.example.demo.inventory;

public class InventoryRecord {
    private Long storeId;
    private Long productId;
    private double onHand;
    private double reserved;

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public double getOnHand() {
        return onHand;
    }

    public void setOnHand(double onHand) {
        this.onHand = onHand;
    }

    public double getReserved() {
        return reserved;
    }

    public void setReserved(double reserved) {
        this.reserved = reserved;
    }
}
