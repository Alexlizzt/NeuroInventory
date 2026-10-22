package com.alexlizzt.inventory_service.infraestructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.alexlizzt.inventory_service.application.command.CreateCategoryCommand;
import com.alexlizzt.inventory_service.application.dto.request.CreateCategoryRequest;
import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.service.CreateCategoryService;
import com.alexlizzt.inventory_service.domain.service.FindCategoryService;
import com.alexlizzt.inventory_service.domain.service.GetCategoriesService;
import com.alexlizzt.inventory_service.domain.service.UpdateCategoryService;


@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {
	@Mock
	private CreateCategoryService createCategoryService;
	@Mock
	private GetCategoriesService getCategoriesService;
	@Mock
	private FindCategoryService findCategoryService;
	@Mock
	private UpdateCategoryService updateCategoryService;

	@InjectMocks
	private CategoryController categoryController;

	@Test
	void shouldCreateCategorySuccessfully() {
		// Arrange
		CreateCategoryRequest request = new CreateCategoryRequest(
				"Electrónica",
				"Productos electrónicos"
		);

		CategoryResponse expectedResponse = new CategoryResponse(
				"cat-123456",
				"Electrónica",
				"Productos electrónicos"
		);

		when(createCategoryService.execute(any(CreateCategoryCommand.class)))
				.thenReturn(expectedResponse);

		// Act
		ResponseEntity<CategoryResponse> response =
				categoryController.createCategory(request);

		// Assert
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedResponse, response.getBody());

		verify(createCategoryService).execute(any(CreateCategoryCommand.class));
	}


	@Test
	void shouldSendCorrectCommandToCreateCategoryUseCase() {
		// Arrange
		CreateCategoryRequest request = new CreateCategoryRequest(
				"Electrónica",
				"Productos electrónicos"
		);

		CategoryResponse expectedResponse = new CategoryResponse(
				"cat-123456",
				"Electrónica",
				"Productos electrónicos"
		);

		when(createCategoryService.execute(any(CreateCategoryCommand.class)))
				.thenReturn(expectedResponse);

		// Act
		categoryController.createCategory(request);

		// Assert
		ArgumentCaptor<CreateCategoryCommand> captor =
				ArgumentCaptor.forClass(CreateCategoryCommand.class);

		verify(createCategoryService).execute(captor.capture());

		CreateCategoryCommand command = captor.getValue();

		assertEquals("Electrónica", command.name());
		assertEquals("Productos electrónicos", command.description());
	}


	@Test
	void shouldGetCategoriesSuccessfully() {
		// Arrange
		CategoryResponse category1 = new CategoryResponse(
				"cat-001",
				"Electrónica",
				"Productos electrónicos"
		);

		CategoryResponse category2 = new CategoryResponse(
				"cat-002",
				"Hogar",
				"Productos para el hogar"
		);

		PageResult<CategoryResponse> expectedResult = new PageResult<>(
				List.of(category1, category2),
				0,
				10,
				2,
				1
		);

		when(getCategoriesService.execute(any(PageQuery.class)))
				.thenReturn(expectedResult);

		// Act
		ResponseEntity<PageResult<CategoryResponse>> response =
				categoryController.getCategories(
						0,
						10,
						"name",
						"ASC"
				);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedResult, response.getBody());

		verify(getCategoriesService).execute(any(PageQuery.class));
	}
	

	@Test
	void shouldSendCorrectPageQueryToListCategoriesUseCase() {
		// Arrange
		PageResult<CategoryResponse> expectedResult = new PageResult<>(
				List.of(),
				2,
				20,
				0,
				0
		);

		when(getCategoriesService.execute(any(PageQuery.class)))
				.thenReturn(expectedResult);

		// Act
		categoryController.getCategories(
				2,
				20,
				"description",
				"DESC"
		);

		// Assert
		ArgumentCaptor<PageQuery> captor =
				ArgumentCaptor.forClass(PageQuery.class);

		verify(getCategoriesService).execute(captor.capture());

		PageQuery query = captor.getValue();

		assertEquals(2, query.page());
		assertEquals(20, query.size());
		assertEquals("description", query.sortBy());
		assertEquals("DESC", query.direction());
	}
}
