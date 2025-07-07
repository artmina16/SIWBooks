package it.uniroma3.siw.validator;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.service.BookService;

@Component
public class BookValidator implements Validator{

@Autowired
private BookService bookService;

 	private boolean sameAuthors(List<Author> a1, List<Author> a2) {
        if (a1 == null || a2 == null) return false;
        if (a1.size() != a2.size()) return false;
        return a1.containsAll(a2) && a2.containsAll(a1); // confronta in modo "set equality"
    }

 	@Override
	 public void validate(Object target, Errors errors) {
	     Book book = (Book) target;

	     if (book.getTitle() != null && book.getAuthors() != null && !book.getAuthors().isEmpty()) {
	         Set<Book> existingBooks = bookService.findByTitle(book.getTitle());

	         for (Book existing : existingBooks) {
	             Long existingId = existing.getId();
	             Long currentId = book.getId();

	             // Evita il confronto se currentId è null (cioè libro ancora non salvato)
	             boolean isSameBook = currentId != null && currentId.equals(existingId);

	             if (!isSameBook && sameAuthors(existing.getAuthors(), book.getAuthors())) {
	                 errors.reject("book.duplicate");
	                 break;
	             }
	         }
	     }
	 }
   
	@Override
	public boolean supports(Class<?> aClass) {
	return Book.class.equals(aClass);
	}
}
