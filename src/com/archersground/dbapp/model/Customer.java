package com.archersground.dbapp.model;

public class Customer {
    private final int customerId;
    private final String lastName;
    private final String firstName;
    private final String customerType;
    private final String dlsuIdNumber;

    public Customer(int customerId, String lastName, String firstName, String customerType, String dlsuIdNumber) {
        this.customerId = customerId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.customerType = customerType;
        this.dlsuIdNumber = dlsuIdNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getCustomerType() {
        return customerType;
    }

    public String getDlsuIdNumber() {
        return dlsuIdNumber;
    }

    public boolean isLasallian() {
        return "LASALLIAN".equalsIgnoreCase(customerType);
    }

    @Override
    public String toString() {
        return customerId + " - " + firstName + " " + lastName + " [" + customerType + "]";
    }
}
