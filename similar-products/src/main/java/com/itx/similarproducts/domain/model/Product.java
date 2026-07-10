package com.itx.similarproducts.domain.model;

import java.util.Objects;

/** Producto tal como lo devolvemos en la API. */
public final class Product {

    private final String id;
    private final String name;
    private final double price;
    private final boolean availability;

    public Product(String id, String name, double price, boolean availability) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.price = price;
        this.availability = availability;
        if (id.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException("id and name must not be blank");
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public boolean isAvailability() {
        return availability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product product)) {
            return false;
        }
        return id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
