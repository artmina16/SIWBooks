package it.uniroma3.siw.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Review;


public interface ReviewRepository extends CrudRepository<Review,Long>{

	List<Review> findByBook(Book book);

	Review getByBookAndCredential(Book book, Credentials credentials);

}
