package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Book;

public interface BookRepository extends JpaRepository<Book,Long> {

	
	Set<Book> findByTitle(String title);

	@Query(value = """
            SELECT b.*
            FROM book b
            LEFT JOIN review r ON r.book_id = b.id
            GROUP BY b.id
            ORDER BY AVG(r.vote) DESC
            LIMIT :limit
        """, nativeQuery = true)
        List<Book> findTopRatedBooks(@Param("limit") int limit);

	List<Book> findByTitleStartingWithIgnoreCase(String title);

	

	
	

}
