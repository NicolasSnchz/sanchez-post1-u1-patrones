package com.patrones.u1;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase original del ejercicio (God Object).
 * Se conserva sin modificar como linea base del analisis documentado en el README.
 * No es utilizada por Main: el flujo vigente es OrderService.
 */
@Deprecated
public class OrderProcessor {

    private List<String> orders = new ArrayList<>();
    private double taxRate = 0.19;

    // Responsabilidad 1: logica de negocio
    public double calculateTotal(List<Double> prices) {
        double subtotal = 0;
        for (double p : prices) subtotal += p;
        return subtotal + (subtotal * taxRate);
    }

    // Responsabilidad 2: descuentos
    public double applyDiscount(double total, String customerType) {
        if (customerType.equals("VIP")) return total * 0.85;
        if (customerType.equals("REGULAR")) return total * 0.95;
        return total;
    }

    // Responsabilidad 3: persistencia
    public void saveOrder(String orderId, double total) {
        orders.add(orderId + ":" + total);
        System.out.println("[DB] Orden guardada: " + orderId);
    }

    // Responsabilidad 4: notificacion
    public void sendEmail(String email, String orderId) {
        System.out.println("[EMAIL] Enviando a " + email + " confirmacion de orden " + orderId);
    }

    // Responsabilidad 5: reporte
    public void printReport() {
        System.out.println("=== Reporte de Ordenes ===");
        for (String o : orders) System.out.println("  " + o);
    }
}
