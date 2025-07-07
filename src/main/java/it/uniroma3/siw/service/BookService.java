package it.uniroma3.siw.service;

import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.repository.BookRepository;

@Service
public class BookService {
	
	@Autowired
	private BookRepository bookRepository;
	
	public Iterable<Book> findAll(){
		return this.bookRepository.findAll();
	}

	public Book findById(Long bookId) {
		return this.bookRepository.findById(bookId).get();
	}
	
	public void save(Book book) {
		this.bookRepository.save(book);
	}

	public Set<Book> findByTitle(String title) {
		return this.bookRepository.findByTitle(title);
	}

	public void delete(Long id) {
		this.bookRepository.deleteById(id);
		
	}

	public List<Book> findTopRatedBooks(int i) {
		return this.bookRepository.findTopRatedBooks(i);
	}

	public List<Book> findByTitleStartingWithIgnoreCase(String title) {
		return this.bookRepository.findByTitleStartingWithIgnoreCase(title);
	}

	public boolean removeImage(Book book, String imageName) {

        if (book == null || imageName == null) return false;

        // Copia mutabile della lista
        List<String> images = new ArrayList<>(book.getImageFileNames());

        boolean removed = false;
        if(images.remove(imageName)) {
            removed = true;
        }


        if (removed) {
            System.out.println("Cancellata");

            book.setImageFileNames(images);


            bookRepository.save(book);
            for( String img : book.getImageFileNames()) {
                System.out.println(img);
            }
        }

        return removed;
    }
}
