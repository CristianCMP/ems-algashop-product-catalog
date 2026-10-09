package com.algaworks.algashop.product.catalog.infrastructure.utility;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class BeanValidationUtil {

	private final LocalValidatorFactoryBean factoryBean;

	public void validate(Object object) {
		Validator validator = factoryBean.getValidator();
		Set<ConstraintViolation<Object>> violations = validator.validate(object);
		if (!violations.isEmpty()) {
			throw new ValidationException(new ConstraintViolationException(violations));
		}
	}

}