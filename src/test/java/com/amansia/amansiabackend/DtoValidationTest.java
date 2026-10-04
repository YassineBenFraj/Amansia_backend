package com.amansia.amansiabackend;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DtoValidationTest {
 @Test void materialAndColorNamesCannotExceedPersistenceLimit() throws Exception {
  try(var factory=Validation.buildDefaultValidatorFactory()){
   for(String domain:new String[]{"materiau","couleur"})for(String operation:new String[]{"Creation","Update"}){
    String name=domain.substring(0,1).toUpperCase()+domain.substring(1);
    Class<?> type=Class.forName("com.amansia.amansiabackend.dto."+domain+"."+name+operation+"DTO");Object dto=type.getConstructor().newInstance();type.getMethod("setNom",String.class).invoke(dto,"a".repeat(101));
    assertTrue(factory.getValidator().validate(dto).stream().anyMatch(v->v.getPropertyPath().toString().equals("nom")));
    type.getMethod("setNom",String.class).invoke(dto,"a".repeat(100));assertFalse(factory.getValidator().validate(dto).stream().anyMatch(v->v.getPropertyPath().toString().equals("nom")));
   }
  }
 }
}
