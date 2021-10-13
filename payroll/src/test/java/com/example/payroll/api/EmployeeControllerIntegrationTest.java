package com.example.payroll.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.example.payroll.PayrollApplication;
import com.example.payroll.domain.Employee;
import com.example.payroll.domain.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = PayrollApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@EnableAutoConfiguration
public class EmployeeControllerIntegrationTest {
	//TODO Test exceptional cases
	
	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private EmployeeRepository repository;
	
	 @Autowired 
	 private ObjectMapper mapper;
	
	@After
	public void resetDb() {
		repository.deleteAll();
	}
	
	@Test
	public void whenAll_thenShouldReturnAllEmployeesFromDb() throws Exception {
		repository.saveAndFlush(new Employee("Willian", "Teacher"));
		repository.saveAndFlush(new Employee("Frodo", "Teacher"));
		
		mvc.perform(get("/employees").contentType(MediaTypes.HAL_JSON))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
		.andExpect(jsonPath("$._embedded.employees", hasSize(greaterThanOrEqualTo(2))));
	}
	
	@Test
	public void whenNewEmployee_thenShouldSaveToDb() throws Exception {
		Employee employee = new Employee("Willian", "Teacher");
		mvc.perform(post("/employees")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsBytes(employee))
				);
		
		List<Employee> all = repository.findAll();
		assertThat(all).extracting(Employee::getName)
						.containsOnly("Willian");
	}
	
	@Test
	public void whenUpdateEmployee_thenShouldPersistChangesToDb() throws Exception {
		Employee existing = repository.saveAndFlush(new Employee("Willian", "Teacher"));
		Employee changed = new Employee(existing.getId(), existing.getName(), existing.getRole());
		changed.setRole("Developer");
		
		mvc.perform(put("/employees/" + existing.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(changed))
				).andDo(print())
				.andExpect(status().isCreated());
		
		existing = repository.findById(changed.getId()).orElseThrow();
		assertThat(existing.getId()).isEqualTo(changed.getId());
		assertThat(existing.getName()).isEqualTo(changed.getName());
		assertThat(existing.getRole()).isEqualTo(changed.getRole());
	}
	
	@Test
	public void whenDeleteExistingEmployee_thenShouldRemoveFromDb() throws Exception {
		Employee existing = repository.saveAndFlush(new Employee("Willian", "Teacher"));
		
		mvc.perform(delete("/employees/" + existing.getId()))
		.andDo(print())
		.andExpect(status().isOk());
		
		Optional<Employee> triedToFind = repository.findById(existing.getId());
		assertThat(triedToFind).isNotPresent();
	}
}
