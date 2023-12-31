package com.pedroresende.rateiofacil.utils;

import java.time.LocalDate;

/**
 * Classe Result.
 */
public class Result {

  private Long userId;
  private Long billId;
  private String establishment;
  private LocalDate date;
  private Double taxService;
  private Double value;
  private Double total;

  /**
   * Método construtor com parametros.
   */
  public Result(Long userId, Long billId, String establishment, LocalDate date, Double value) {
    this.userId = userId;
    this.billId = billId;
    this.establishment = establishment;
    this.date = date;
    this.taxService = Calculator.calculateTaxService(value);
    this.value = value;
    this.total = calculateTotal();
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getBillId() {
    return billId;
  }

  public void setBillId(Long billId) {
    this.billId = billId;
  }

  public String getEstablishment() {
    return establishment;
  }

  public void setEstablishment(String establishment) {
    this.establishment = establishment;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public Double getTaxService() {
    return taxService;
  }

  public void setTaxService(Double taxService) {
    this.taxService = taxService;
  }

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }

  public Double getTotal() {
    return total;
  }

  public void setTotal(Double total) {
    this.total = total;
  }

  private Double calculateTotal() {
    return Calculator.sumValues(taxService, value);
  }
}
