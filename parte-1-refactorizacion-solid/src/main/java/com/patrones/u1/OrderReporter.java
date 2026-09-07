package com.patrones.u1;

import java.util.List;

public class OrderReporter {

    public void print(List<String> orders) {
        System.out.println("=== Reporte de Ordenes ===");
        orders.forEach(o -> System.out.println("  " + o));
    }
}
