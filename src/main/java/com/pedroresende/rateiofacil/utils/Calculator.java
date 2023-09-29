package com.pedroresende.rateiofacil.utils;

import com.pedroresende.rateiofacil.controllers.dtos.FriendConsumptionDto;
import com.pedroresende.rateiofacil.models.entities.Item;
import java.util.List;

/**
 * Classe Calculator.
 */
public class Calculator {

  /**
   * Método que adicionar valor de um item ao total.
   */
  public static Double addItem(Double total, Double itemValue) {
    double result = total + itemValue;

    return result;
  }

  /**
   * Método que calcula a taxa de serviço de dez por cento.
   */
  public static Double calculateTaxService(Double value) {
    double tax = 0.10;

    double taxService = value * tax;

    taxService = Math.round(taxService * 100) / 100.0;

    return taxService;
  }

  /**
   * Método que soma dois valores do tipo double e o arredonda para duas casas decimais.
   */
  public static Double sumValues(double value1, double value2) {
    double result = value1 + value2;

    result = Math.round(result * 100.0) / 100.0;

    return result;
  }
}
