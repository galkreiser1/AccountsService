package com.example.accountserviceproject.payroll;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empl")
public class EmployeeController {

    private final PaymentService paymentService;

    public EmployeeController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payment")
    public Object makePayment(Authentication authentication,
                              @RequestParam(required = false) String period) {
        if (period == null) {
            return paymentService.getAllPaymentsForEmployee(authentication.getName());
        }
        return paymentService.getPaymentForPeriod(authentication.getName(), period);
    }
}
