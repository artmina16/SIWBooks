package it.uniroma3.siw.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.model.Author;

public interface AuthorRepository extends CrudRepository<Author,Long>{

	List<Author> findByNameAndSurnameAndBornYearAndNationality(String name, String surname, LocalDate bornYear,
			String nationality);

	Set<Author> findByNameStartingWithIgnoreCase(String name);

	Set<Author> findBySurnameStartingWithIgnoreCase(String name);
	
	Set<Author> findAll();

}
