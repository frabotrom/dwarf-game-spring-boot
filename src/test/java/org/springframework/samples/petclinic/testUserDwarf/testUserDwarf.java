package org.springframework.samples.petclinic.testUserDwarf;

import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.samples.petclinic.userDwarf.UserDwarf;
import org.springframework.samples.petclinic.validatorFunction;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class testUserDwarf {

    @Test
    public void shouldNotValidateWhenParametersAreEmpty(){

    LocaleContextHolder.setLocale(Locale.ENGLISH);
    UserDwarf userDwarf = new UserDwarf();
    userDwarf.setEmail("");
    userDwarf.setUsername("");
    userDwarf.setPass("");

    Validator validator = validatorFunction.createValidator();
    Set<ConstraintViolation<UserDwarf>> constraintViolations =
    validator.validate(userDwarf);

    assertThat(constraintViolations.size()).isEqualTo(5);

    assertAll("constrainViolations", 
    () -> {
        List<String> violationsList = constraintViolations.stream()
                .filter(c -> c.getPropertyPath().toString().equals("email")).map(v -> v.getMessage())
                .collect(Collectors.toList());
        assertThat(violationsList).containsExactlyInAnyOrder("must not be empty");
    },
    () -> {
        List<String> violationsList = constraintViolations.stream()
                .filter(c -> c.getPropertyPath().toString().equals("username")).map(v -> v.getMessage())
                .collect(Collectors.toList());
        assertThat(violationsList).containsExactlyInAnyOrder("must not be empty", "size must be between 4 and 20");
    },
    () -> {
        List<String> violationsList = constraintViolations.stream()
                .filter(c -> c.getPropertyPath().toString().equals("pass")).map(v -> v.getMessage())
                .collect(Collectors.toList());
        assertThat(violationsList).containsExactlyInAnyOrder("must not be empty", "Debe contener 8 caractéres, uno mínimo en mayúsculas y otro en número");
    }
    );
    
    }

    @Test
    public void shouldValidate(){

    LocaleContextHolder.setLocale(Locale.ENGLISH);
    UserDwarf userDwarf = new UserDwarf();
    userDwarf.setEmail("test@test.com");
    userDwarf.setUsername("test");
    userDwarf.setPass("testT3st");
    userDwarf.setActive(true);

    Validator validator = validatorFunction.createValidator();
    Set<ConstraintViolation<UserDwarf>> constraintViolations =
    validator.validate(userDwarf);

    assertThat(constraintViolations.size()).isEqualTo(0);
    
    }


}
