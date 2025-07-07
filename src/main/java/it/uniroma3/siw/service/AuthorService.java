package it.uniroma3.siw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.repository.AuthorRepository;

@Service
public class AuthorService {

	@Autowired
	private AuthorRepository authorRepository;
	
	public Iterable<Author> findAll(){
		return this.authorRepository.findAll();
	}
	
	public Author findById(Long authorId) {
		return this.authorRepository.findById(authorId).get();
	}

	public void save(Author author) {
		this.authorRepository.save(author);
		
	}
	
	public void delete(Long id) {
		this.authorRepository.deleteById(id);
		
	}

	public List<Author> findByNameAndSurnameAndBornYearAndNationality(String name, String surname, LocalDate bornYear,
			String nationality) {

		return this.authorRepository.findByNameAndSurnameAndBornYearAndNationality(name, surname, bornYear,nationality);
	}

	public Set<Author> findByNameStartingWithIgnoreCase(String name) {
		return this.authorRepository.findByNameStartingWithIgnoreCase(name);
	}

	public Set<Author> findBySurnameStartingWithIgnoreCase(String name) {
		return this.authorRepository.findBySurnameStartingWithIgnoreCase(name);
	}

	public Iterable<Author> findAllByIds(List<Long> authorIds) {
		return this.authorRepository.findAllById(authorIds);
	}

	public Set<Author> findAllAuthors() {
		return this.authorRepository.findAll();
	}
}
