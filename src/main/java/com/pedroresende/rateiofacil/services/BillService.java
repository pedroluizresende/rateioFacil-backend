package com.pedroresende.rateiofacil.services;

import com.pedroresende.rateiofacil.controllers.dtos.FriendConsumptionDto;
import com.pedroresende.rateiofacil.enums.BillStatus;
import com.pedroresende.rateiofacil.exceptions.NotFoundBillException;
import com.pedroresende.rateiofacil.models.entities.Bill;
import com.pedroresende.rateiofacil.models.entities.Item;
import com.pedroresende.rateiofacil.models.repositories.BillRepository;
import com.pedroresende.rateiofacil.utils.Calculator;
import com.pedroresende.rateiofacil.utils.Result;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Camada de serviço da rota /bill.
 */
@Service
public class BillService implements BasicService<Bill> {

  private final BillRepository billRepository;
  private final ItemService itemService;

  @Autowired
  public BillService(BillRepository billRepository, ItemService itemService) {
    this.billRepository = billRepository;
    this.itemService = itemService;
  }

  @Override
  public Bill create(Bill bill) {
    bill.setStatus(BillStatus.OPEN);
    bill.setTotal(0.0);
    return billRepository.save(bill);
  }

  @Override
  public Bill getById(Long id) {
    Optional<Bill> optionalBill = billRepository.findById(id);
    if (optionalBill.isEmpty()) {
      throw new NotFoundBillException();
    }
    return optionalBill.get();
  }

  @Override
  public List<Bill> getAll() {
    return billRepository.findAll();
  }

  @Override
  public Bill update(Long id, Bill bill) {
    Bill billFromDb = getById(id);
    billFromDb.setEstablishment(bill.getEstablishment());

    return billRepository.save(billFromDb);
  }

  @Override
  public Bill delete(Long id) {
    Bill bill = getById(id);

    billRepository.deleteById(id);
    return bill;
  }

  public List<Bill> getAllByUserId(Long userId) {
    return billRepository.findAllByUserId(userId);
  }

  /**
   * Método responsável por adicionar um item a uma conta.
   */
  public Item addItem(Long id, Item item) {
    Bill bill = getById(id);
    item.setBill(bill);
    final Item itemFromDb = itemService.create(item);
    bill.getItems().add(item);
    bill.setTotal(Calculator.addItem(bill.getTotal(), item.getValue()));
    billRepository.save(bill);
    return itemFromDb;
  }

  /**
   * Método responsável por recuperar items de uma conta.
   */
  public List<Item> getitems(Long id) {
    Bill bill = getById(id);

    return itemService.getAllByBillId(bill.getId());
  }

  /**
   * Método responsável por remover um item de uma conta.
   */
  @Transactional
  public Item removeItem(Long id, Long itemId) {
    Bill bill = getById(id);
    Item item = itemService.delete(id);
    bill.getItems().remove(item);
    billRepository.save(bill);

    return item;
  }

  /**
   * Método responsável por retornar um item específico pelo seu ID.
   */
  public Item getItemById(Long id, Long itemId) {
    getById(id);

    return itemService.getById(itemId);
  }

  /**
   * Método responsável por retornar o valor calculado da conta.
   */
  public Result calculate(Long id) {
    Bill bill = getById(id);
    Result result = new Result(bill.getId(), bill.getUser().getId(), bill.getEstablishment(),
        bill.getDate(), bill.getTotal(), bill.getItems());

    return result;
  }

  /**
   * Método responsável por retornar uma lista com toda a consumação de um amigo.
   */
  public List<Item> getFriendConsumption(Long id, String friend) {
    getById(id);

    List<Item> items = itemService.getByFriend(friend);

    return items;
  }
}
