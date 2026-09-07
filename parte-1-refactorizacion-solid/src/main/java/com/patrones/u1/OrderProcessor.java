package com.patrones.u1;

import java.util.List;
import java.util.ArrayList;

/**
 * Clase original del ejercicio (God Object).
 * Se conserva sin modificar como linea base del analisis documentado en el README.
 * No es utilizada por Main: el flujo vigente es OrderService.
 */
@Deprecated
public class OrderProcessor {
    private List<String> orders = new ArrayList<>();
    private double taxRate = 0.19;

    public double calculateTotal(List<Double> prices) {
        double subtotal = 0;
        for (double p : prices) subtotal += p;
        return subtotal + (subtotal * taxRate);
    }

    public double applyDiscount(double total, String customerType) {
        if (customerType.equals("VIP")) return total * 0.85;
        if (customerType.equals("REGULAR")) return total * 0.95;
        return total;
    }

    public void saveOrder(String orderId, double total) {
        orders.add(orderId + ":" + total);
        System.out.println("[DB] Orden guardada: " + orderId);
    }

    public void sendEmail(String email, String orderId) {
        System.out.println("[EMAIL] Enviando a " + email + " confirmacion de orden " + orderId);
    }

    public void printReport() {
        System.out.println("=== Reporte de Ordenes ===");
        for (String o : orders) System.out.println("  " + o);
    }
}
