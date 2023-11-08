package com.pedroresende.rateiofacil.listeners;

import com.pedroresende.rateiofacil.exceptions.BadRequestException;
import com.pedroresende.rateiofacil.models.entities.User;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class UserListener {

  private void validateEmail(String email) {
    if (email == null) {
      throw new BadRequestException("Email é obrigatório!");
    }

    String regex = "^[A-Za-z0-9+_.-]+@(.+)?(\\.com|\\.br)$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(email);

    if (!matcher.matches()) {
      throw new BadRequestException("Email no formato errado!");
    }
  }

  private void validateName(String name) {
    if (name == null) {
      throw new BadRequestException("Nome é obrigatório!");
    }

    if (name.length() < 2) {
      throw new BadRequestException("Nome deve ter no mínimo 2 caracteres");
    }
  }

  private void validateUsername(String username) {
    if (username == null) {
      throw new BadRequestException("Username é obrigatório!");
    }

    if (username.length() < 5) {
      throw new BadRequestException("Username deve ter no mínimo 6 caracteres");
    }
  }

  private void validateRole(String role) {
    if (role == null) {
      throw new BadRequestException("Role é obrigatório!");
    }
  }

  private void validateUser(User user) {
    validateEmail(user.getEmail());
    validateName(user.getName());
    validateUsername(user.getUsername());
    validateRole(user.getRole());
  }

  @PrePersist
  public void validateCreateUser(User user) {
    validateUser(user);
  }

  @PreUpdate
  public void validateUpdateUser(User user) {
    validateUser(user);
  }
}