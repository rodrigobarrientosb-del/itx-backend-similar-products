package com.itx.similarproducts.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void createsValidProduct() {
        Product product = new Product("1", "Shirt", 9.99, true);
        assertEquals("1", product.getId());
        assertEquals("Shirt", product.getName());
        assertEquals(9.99, product.getPrice());
        assertEquals(true, product.isAvailability());
    }

    @Test
    void rejectsBlankId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(" ", "Shirt", 9.99, true));
    }
}
