package com.example.accountserviceproject.payroll;

import jakarta.persistence.*;

@Entity
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee", "period"})
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String period;
    private Long salary;

    private String employee;

    public Payment() {
    }

    public Long getId() {
        return id;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }
}
