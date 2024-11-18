package com.sivalabs.bookstore.webapp.models;

import java.math.BigDecimal;

public class Cart {
    private LineItem item;

    public LineItem getItem() {
        return item;
    }

    public void setItem(LineItem item) {
        this.item = item;
    }

    public BigDecimal getTotalAmount() {
        if (item == null) {
            return BigDecimal.ZERO;
        }
        return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    public void removeItem() {
        this.item = null;
    }

    public void updateItemQuantity(int quantity) {
        if (quantity <= 0) {
            removeItem();
            return;
        }
        item.setQuantity(quantity);
    }

    public static class LineItem {
        private String code;
        private String name;
        private BigDecimal price;
        private int quantity;

        public LineItem() {}

        public LineItem(String code, String name, BigDecimal price, int quantity) {
            this.code = code;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
