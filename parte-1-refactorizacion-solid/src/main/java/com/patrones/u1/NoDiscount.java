package com.patrones.u1;

public class NoDiscount implements DiscountStrategy {

    @Override
    public double apply(double total) {
        return total;
    }
}
