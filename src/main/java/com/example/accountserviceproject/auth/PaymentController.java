package com.example.accountserviceproject.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/acct")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public StatusResponse makePayment(@RequestBody List<@Valid PaymentRequest> paymentRequests) {
        paymentService.addPayments(paymentRequests);
        return new StatusResponse("Added successfully!");
    }

    @PutMapping("/payments")
    public StatusResponse updatePayment(@RequestBody @Valid PaymentRequest paymentRequest) {
        paymentService.updatePayment(paymentRequest);
        return new StatusResponse("Updated successfully!");
    }




}
