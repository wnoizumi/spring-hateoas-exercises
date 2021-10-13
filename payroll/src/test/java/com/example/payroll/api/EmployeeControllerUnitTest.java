package com.example.payroll.api;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.payroll.assemblers.EmployeeModelAssembler;
import com.example.payroll.domain.Employee;
import com.example.payroll.domain.EmployeeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerUnitTest {
	
	//TODO Increase test coverage. At least one test for each route

	@TestConfiguration
	static class EmployeeServiceImplTestContextConfiguration {
		@Bean
		public EmployeeModelAssembler employeeService() {
			return new EmployeeModelAssembler();
		}
	}

	@Autowired
	private MockMvc mvc;
	
	 @Autowired 
	 private ObjectMapper mapper;
	
	@MockBean
	private EmployeeRepository repository;

	@Test
	public void allShouldFetchAHalDocument() throws Exception {
		List<Employee> employees = new ArrayList<>();
		employees.add(new Employee(1L, "Willian", "Teacher"));
		employees.add(new Employee(2L, "Jose", "Dean"));

		given(repository.findAll()).willReturn(employees);

		mvc.perform(get("/employees")
				.accept(MediaTypes.HAL_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
				.andExpect(jsonPath("$._embedded.employees[0].id", is(1)))
				.andExpect(jsonPath("$._embedded.employees[0].name", is("Willian")))
				.andExpect(jsonPath("$._embedded.employees[0].role", is("Teacher")))
				.andExpect(jsonPath("$._embedded.employees[0]._links.self.href", is("http://localhost/employees/1")))
				.andExpect(jsonPath("$._embedded.employees[0]._links.employees.href", is("http://localhost/employees")))
				.andExpect(jsonPath("$._embedded.employees[1].id", is(2)))
				.andExpect(jsonPath("$._embedded.employees[1].name", is("Jose")))
				.andExpect(jsonPath("$._embedded.employees[1].role", is("Dean")))
				.andExpect(jsonPath("$._embedded.employees[1]._links.self.href", is("http://localhost/employees/2")))
				.andExpect(jsonPath("$._embedded.employees[1]._links.employees.href", is("http://localhost/employees")))
				.andExpect(jsonPath("$._links.self.href", is("http://localhost/employees"))) //
				.andReturn();
	}
	
	@Test
	public void givenNewEmployeePostReturnsCorrectResponse() throws Exception {
		Employee employee = new Employee(1L, "Willian", "Teacher");
		given(repository.save(any(Employee.class))).willReturn(employee);
		
		ResultActions resultActions = mvc.perform(post("/employees")
				.content(mapper.writeValueAsBytes(employee))
				.contentType(MediaType.APPLICATION_JSON));
		
		resultActions.andExpect(status().isCreated())
						.andExpect(jsonPath("employee.id", is(employee.getId().intValue())))
						.andExpect(jsonPath("employee.name", is(employee.getName())))
						.andExpect(jsonPath("employee.role", is(employee.getRole())));
		
	}
}
