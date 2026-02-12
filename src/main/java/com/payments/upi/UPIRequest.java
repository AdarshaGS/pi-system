package com.payments.upi;

public class UPIRequest {
    private String payerVPA;
    private String payeeVPA;
    private double amount;
    private String remarks;

    // Getters and setters
    public String getPayerVPA() { return payerVPA; }
    public void setPayerVPA(String payerVPA) { this.payerVPA = payerVPA; }
    public String getPayeeVPA() { return payeeVPA; }
    public void setPayeeVPA(String payeeVPA) { this.payeeVPA = payeeVPA; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
