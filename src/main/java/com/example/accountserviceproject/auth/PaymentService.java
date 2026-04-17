package com.example.accountserviceproject.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    private static final DateTimeFormatter PERIOD_FORMATTER =
            DateTimeFormatter.ofPattern("MM-yyyy");

    @Transactional
    public void addPayments(List<PaymentRequest> paymentRequests) {
        Set<String> seenPayments = new HashSet<>();

        for (PaymentRequest request : paymentRequests) {
            String employee = request.getEmployee().toLowerCase();
            String period = request.getPeriod();

            parsePeriod(period);

            if (!userRepository.existsByEmailIgnoreCase(employee)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not exist!");
            }

            String paymentKey = employee + "|" + period;
            if (!seenPayments.add(paymentKey)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment already exists!");
            }

            if (paymentRepository.existsByEmployeeIgnoreCaseAndPeriod(employee, period)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment already exists!");
            }

            Payment payment = new Payment();
            payment.setEmployee(employee);
            payment.setPeriod(period);
            payment.setSalary(request.getSalary());
            paymentRepository.save(payment);
        }
    }

    @Transactional
    public void updatePayment(PaymentRequest request) {
        String employee = request.getEmployee().toLowerCase();
        String period = request.getPeriod();

        parsePeriod(period);

        if (!userRepository.existsByEmailIgnoreCase(employee)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not exist!");
        }

        Payment payment = paymentRepository.findByEmployeeIgnoreCaseAndPeriod(employee, period)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment does not exist!"));

        payment.setSalary(request.getSalary());
        paymentRepository.save(payment);
    }

    public Object getPaymentForPeriod(String employee, String period) {
        User user = userRepository.findByEmailIgnoreCase(employee)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not exist!"));

        parsePeriod(period);

        return paymentRepository.findByEmployeeIgnoreCaseAndPeriod(employee, period)
                .<Object>map(payment -> createSalaryResponse(user, payment))
                .orElseGet(Map::of);
    }

    public List<SalaryResponse> getAllPaymentsForEmployee(String employee) {
        User user = userRepository.findByEmailIgnoreCase(employee)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not exist!"));

        return paymentRepository.findAllByEmployeeIgnoreCase(employee)
                .stream()
                .sorted(Comparator.comparing(payment -> parsePeriod(payment.getPeriod()), Comparator.reverseOrder()))
                .map(payment -> createSalaryResponse(user, payment))
                .toList();
    }

    private SalaryResponse createSalaryResponse(User user, Payment payment) {
        return new SalaryResponse(
                user.getName(),
                user.getLastname(),
                formatPeriod(payment.getPeriod()),
                convertSalaryToDollarsAndCents(payment.getSalary())
        );
    }

    private String formatPeriod(String period) {
        YearMonth yearMonth = parsePeriod(period);
        String month = yearMonth.getMonth().name().charAt(0)
                + yearMonth.getMonth().name().substring(1).toLowerCase();
        return month + "-" + yearMonth.getYear();
    }

    private String convertSalaryToDollarsAndCents(Long salary) {
        long dollars = salary / 100;
        long cents = salary % 100;
        return String.format("%d dollar(s) %d cent(s)", dollars, cents);
    }

    private YearMonth parsePeriod(String period) {
        try {
            return YearMonth.parse(period, PERIOD_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong date!");
        }
    }
}



