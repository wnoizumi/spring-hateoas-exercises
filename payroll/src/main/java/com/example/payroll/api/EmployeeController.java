package com.example.payroll.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.ResponseEntity;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.payroll.assemblers.EmployeeModelAssembler;
import com.example.payroll.domain.Employee;
import com.example.payroll.domain.EmployeeRepository;
import com.example.payroll.domain.exceptions.EmployeeNotFoundException;

@RestController
public class EmployeeController {

	private final EmployeeRepository repository;
	private EmployeeModelAssembler assembler;

	public EmployeeController(EmployeeRepository repository, EmployeeModelAssembler assembler) {
		this.repository = repository;
		this.assembler = assembler;
	}

	@GetMapping("/employees")
	public ResponseEntity<CollectionModel<EntityModel<Employee>>> all() {
		List<EntityModel<Employee>> employees = repository.findAll().stream().map(assembler::toModel)
				.collect(Collectors.toList());

		return ResponseEntity.ok( //
				CollectionModel.of(employees, //
						linkTo(methodOn(EmployeeController.class).all()).withSelfRel()));
	}

	@PostMapping("/employees")
	public ResponseEntity<?> newEmployee(@RequestBody Employee newEmployee) {
		EntityModel<Employee> entityModel = assembler.toModel(repository.save(newEmployee));
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@GetMapping("/employees/{id}")
	public EntityModel<Employee> one(@PathVariable Long id) {
		Employee employee = repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

		return assembler.toModel(employee);
	}

	@PutMapping("/employees/{id}")
	public ResponseEntity<?> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {
		EntityModel<Employee> entityModel = null;
		Optional<Employee> optEmployee = repository.findById(id);
		if (optEmployee.isPresent()) {
			Employee employee = optEmployee.get();
			employee.setName(newEmployee.getName());
			employee.setRole(newEmployee.getRole());
			entityModel = assembler.toModel(repository.save(employee));
		} else {
			newEmployee.setId(id);
			entityModel = assembler.toModel(repository.save(newEmployee));
		}

		//TODO check if it is better to return 201 or 200 in this case
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@DeleteMapping("/employees/{id}")
	public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
		repository.deleteById(id);
		return ResponseEntity.ok().build();
	}
}
