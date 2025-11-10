package com.smartlogi.sdms.application.validation.password;

import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator
        implements ConstraintValidator<PasswordValid, Object> {

    @Override
    public void initialize(PasswordValid constraintAnnotation) {
    }
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context){
      UserRequestRegisterDTO user= (UserRequestRegisterDTO) obj;
        return user.getPassword().equals(user.getMatchingPassword());
    }
}