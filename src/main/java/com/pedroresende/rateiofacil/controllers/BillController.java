package com.pedroresende.rateiofacil.controllers;

import com.pedroresende.rateiofacil.controllers.dtos.BillDto;
import com.pedroresende.rateiofacil.controllers.dtos.FriendConsumptionDto;
import com.pedroresende.rateiofacil.controllers.dtos.ImgUrlDto;
import com.pedroresende.rateiofacil.controllers.dtos.ItemDto;
import com.pedroresende.rateiofacil.controllers.dtos.ResponseDto;
import com.pedroresende.rateiofacil.controllers.dtos.SplitItemDto;
import com.pedroresende.rateiofacil.exceptions.NotAuthorizeUserException;
import com.pedroresende.rateiofacil.models.entities.Bill;
import com.pedroresende.rateiofacil.models.entities.Item;
import com.pedroresende.rateiofacil.models.entities.User;
import com.pedroresende.rateiofacil.services.BillService;
import com.pedroresende.rateiofacil.utils.Calculator;
import com.pedroresende.rateiofacil.utils.Result;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Camada de controller da rota /bills.
 */
@RestController
@RequestMapping("/bills")
public class BillController {

  private final BillService billService;

  @Autowired
  public BillController(BillService billService) {
    this.billService = billService;
  }

  private BillDto toBillDto(Bill bill) {

    if (bill.getUser() == null) {
      return new BillDto(bill.getId(), null, bill.getDate(),
          bill.getEstablishment(), bill.getTotal(), bill.getStatus(), bill.getImgUrl());
    }

    return new BillDto(bill.getId(), bill.getUser().getId(), bill.getDate(),
        bill.getEstablishment(), bill.getTotal(), bill.getStatus(), bill.getImgUrl());
  }

  @GetMapping
  @Secured("admin")
  ResponseEntity<List<BillDto>> getAll() {
    List<Bill> bills = billService.getAll();

    List<BillDto> billDtos = bills.stream().map(
        bill -> toBillDto(bill)
    ).toList();
    return ResponseEntity.ok(billDtos);
  }

  @GetMapping("/{id}/calculate")
  ResponseEntity<Result> calculate(@PathVariable Long id,
      @AuthenticationPrincipal User user) {
    validateUserPermission(id, user);

    Result result = billService.calculate(id);

    return ResponseEntity.ok(result);
  }

  /**
   * Mapeamento da rota GET /bills/{id}/friendConsumption, responsável por retornar a consumação de
   * um amigo.
   */
  @GetMapping("/{id}/friendConsumption")
  public ResponseEntity<FriendConsumptionDto> getFriendConsumption(@PathVariable Long id,
      @RequestParam(name = "friend") String friend,
      @AuthenticationPrincipal User user) {
    validateUserPermission(id, user);

    List<Item> items = billService.getFriendConsumption(id, friend);

    List<ItemDto> itemDtos = items.stream().map(item -> toItemDto(item)).toList();
    double value = items.stream()
        .mapToDouble(Item::getValue)
        .sum();

    double taxService = Calculator.calculateTaxService(value);
    double total = Calculator.sumValues(taxService, value);

    FriendConsumptionDto friendConsumptionDto = new FriendConsumptionDto(friend, itemDtos, value,
        taxService, total);

    return ResponseEntity.ok(friendConsumptionDto);
  }

  @PostMapping("/{id}/items")
  ResponseEntity<ResponseDto<ItemDto>> addItem(
      @PathVariable Long id,
      @RequestBody ItemDto itemDto,
      @AuthenticationPrincipal User user
  ) {
    validateUserPermission(id, user);
    Item item = billService.addItem(id, itemDto.toEntity());
    ItemDto itemDtoDb = toItemDto(item);
    ResponseDto<ItemDto> responseDto = new ResponseDto<>(
        "Item adicionado com sucesso", itemDtoDb
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  /**
   * Mapeamento da rota GET /bills/{id}/items, retorna lista de itens de uma conta.
   */
  @GetMapping("/{id}/items")
  public ResponseEntity<List<ItemDto>> getItems(@PathVariable Long id,
      @AuthenticationPrincipal User user) {
    validateUserPermission(id, user);

    List<Item> items = billService.getItems(id);

    List<ItemDto> itemDtos = items.stream()
        .map(item -> toItemDto(item)).toList();

    return ResponseEntity.ok(itemDtos);
  }

  /**
   * Mapeamento da rota GET /bills/{id}/items/{itemId}, retorna item de uma conta.
   */
  @GetMapping("/{id}/items/{itemId}")
  public ResponseEntity<ItemDto> getItemById(@PathVariable Long id,
      @PathVariable Long itemId,
      @AuthenticationPrincipal User user) {
    validateUserPermission(id, user);

    Item itemById = billService.getItemById(id, itemId);

    return ResponseEntity.ok(toItemDto(itemById));
  }

  /**
   * Mapeamento da rota DELETE /bills/{id}/items/{itemId}, deleta item de uma conta.
   */
  @DeleteMapping("/{id}/items/{itemId}")
  public ResponseEntity<ResponseDto> removeItem(@PathVariable Long id,
      @PathVariable Long itemId,
      @RequestParam(name = "split") Boolean split,
      @AuthenticationPrincipal User user) {
    validateUserPermission(id, user);

    Item item;

    if (split) {
      item = billService.removeItem(id, itemId);
      ItemDto itemDto = toItemDto(item);
      ResponseDto<ItemDto> responseDto = new ResponseDto<>("Item Removido com sucesso!", itemDto);
      return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
    List<Item> items = billService.removeSplitItem(id, itemId);
    List<ItemDto> itemDtoList = items.stream().map(item1 -> toItemDto(item1)).toList();
    ResponseDto<List<ItemDto>> responseDto = new ResponseDto<>("Item Removido com sucesso!",
        itemDtoList);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Mapeamento da rota /bills/{id}/finish responsável por finalizar uma conta.
   *
   * @param id   identificador da conta.
   * @param user usuário autenticado.
   * @return status 200 e mensagem de sucesso
   */
  @PutMapping("/{id}/finish")
  public ResponseEntity<ResponseDto<BillDto>> finish(@PathVariable Long id,
      @AuthenticationPrincipal User user) {
    validateUserPermission(id, user);

    Bill bill = billService.finish(id);
    BillDto billDto = toBillDto(bill);

    ResponseDto<BillDto> responseDto = new ResponseDto<>("Conta finalizada com sucesso!", billDto);

    return ResponseEntity.ok(responseDto);
  }

  private void validateUserPermission(Long billId, User authUser) {

    if (!authUser.getRole().equals("admin")) {
      Bill bill = billService.getById(billId);
      User user = bill.getUser();
      if (!authUser.getId().equals(user.getId())) {
        throw new NotAuthorizeUserException();
      }
    }
  }

  private ItemDto toItemDto(Item item) {
    return new ItemDto(item.getId(), item.getBill().getId(), item.getFriend(),
        item.getDescription(),
        item.getValue());
  }

  /**
   * Mapeamento da rota /bills/{id}/items/split, reponsavél por adicionar pedidos que foram
   * dividos.
   *
   * @param id           identificador da conta.
   * @param splitItemDto Pedido no de uma instancia de SplitItemDto.
   * @param user         usuário autenticado.
   * @return status 201 e mensagem de sucesso.
   */
  @PostMapping("/{id}/items/split")
  public ResponseEntity<ResponseDto<List<ItemDto>>> addSplitItem(
      @PathVariable Long id,
      @RequestBody SplitItemDto splitItemDto,
      @AuthenticationPrincipal User user
  ) {
    validateUserPermission(id, user);

    List<Item> items = billService.addSplitItem(id, splitItemDto.toEntityList());
    List<ItemDto> itemDtos = items.stream().map(item -> toItemDto(item)).toList();

    ResponseDto<List<ItemDto>> responseDto = new ResponseDto<>(
        "Conta adicionada e dividida corretamente!", itemDtos);

    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  @PutMapping("/{id}/imgUrl")
  ResponseEntity<ResponseDto<BillDto>> addImgUrl(@PathVariable Long id,
      @RequestBody ImgUrlDto imgUrlDto,
      @AuthenticationPrincipal User user
  ) {
    validateUserPermission(id, user);

    Bill bill = billService.addImgUrl(id, imgUrlDto.imgUrl());

    BillDto billDto = toBillDto(bill);

    ResponseDto<BillDto> responseDto = new ResponseDto<>("Url associada com sucesso", billDto);

    return ResponseEntity.ok(responseDto);
  }
}
