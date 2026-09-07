package com.patrones.u1;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        OrderRepository repository = new OrderRepository();
        EmailNotifier notifier = new EmailNotifier();
        TaxCalculator taxCalculator = new TaxCalculator(0.19);
        OrderReporter reporter = new OrderReporter();

        OrderService vipService = new OrderService(
                taxCalculator, repository, notifier, new VipDiscount());
        vipService.processOrder("ORD-001", "vip@mail.com", List.of(100.0, 200.0, 50.0));

        OrderService regularService = new OrderService(
                taxCalculator, repository, notifier, new RegularDiscount());
        regularService.processOrder("ORD-002", "reg@mail.com", List.of(80.0, 120.0));

        OrderService standardService = new OrderService(
                taxCalculator, repository, notifier, new NoDiscount());
        standardService.processOrder("ORD-003", "std@mail.com", List.of(300.0));

        reporter.print(repository.findAll());
    }
}
