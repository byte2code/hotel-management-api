package com.cn.hotelDemo.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cn.hotelDemo.dto.HotelRequest;
import com.cn.hotelDemo.dto.validation.OnCreate;
import com.cn.hotelDemo.dto.validation.OnUpdate;

class HotelRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void onCreate_withMissingRequiredFields_hasViolations() {
		HotelRequest request = HotelRequest.builder().build();

		Set<ConstraintViolation<HotelRequest>> violations = validator.validate(request, OnCreate.class);

		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("rating")));
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("city")));
	}

	@Test
	void onCreate_withValidFields_noViolations() {
		HotelRequest request = HotelRequest.builder()
				.name("Grand Luxury")
				.rating(5L)
				.city("Paris")
				.email("info@grandluxury.com")
				.phoneNumber("+33123456789")
				.starCategory("5-star")
				.build();

		Set<ConstraintViolation<HotelRequest>> violations = validator.validate(request, OnCreate.class);

		assertTrue(violations.isEmpty(), "Expected no violations for valid OnCreate request");
	}

	@Test
	void onUpdate_withPartialFields_noViolationsForOmittedName() {
		// In update, name, rating, city can be omitted without causing violations
		HotelRequest request = HotelRequest.builder()
				.address("New Street 42")
				.description("Updated description")
				.email("update@example.com")
				.build();

		Set<ConstraintViolation<HotelRequest>> violations = validator.validate(request, OnUpdate.class);

		assertTrue(violations.isEmpty(), "OnUpdate should permit partial payload without mandatory create fields");
	}

	@Test
	void onUpdate_withInvalidDiscount_hasViolations() {
		HotelRequest request = HotelRequest.builder()
				.discount(150.0) // violates @Max(100)
				.build();

		Set<ConstraintViolation<HotelRequest>> violations = validator.validate(request, OnUpdate.class);

		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("discount")));
	}
}
