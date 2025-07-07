package it.uniroma3.siw.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.validator.AuthorValidator;
import it.uniroma3.siw.validator.BookValidator;
import jakarta.validation.Valid;

@Controller
public class AuthorController {

	@Autowired
	private AuthorService authorService;
	
	@Autowired
	private AuthorValidator authorValidator;
	
	@Autowired
	private BookService bookService;
	
	@Autowired
	private BookValidator bookValidator;
	
	@GetMapping(value = "/authors")
	public String authors(@RequestParam(value = "name", required = false) String name,Model model) {
		Set<Author> authors;
		
		if (name != null && !name.isEmpty()) {
            authors = authorService.findByNameStartingWithIgnoreCase(name);
            authors.addAll(authorService.findBySurnameStartingWithIgnoreCase(name));
        } else {
            authors = authorService.findAllAuthors();
        }
		model.addAttribute("authors", authors);
		return "authors";
	}
	
	@GetMapping(value = "/author/{id}")
	public String authors(@PathVariable("id") Long authorId, Model model) {
		model.addAttribute("author", this.authorService.findById(authorId));
		return "author";
	}
	

	@GetMapping(value = "/admin/manageAuthors")
	public String manageAuthors(@RequestParam(value = "name", required = false) String name,Model model) {
		Set<Author> authors;
		
		if (name != null && !name.isEmpty()) {
            authors = authorService.findByNameStartingWithIgnoreCase(name);
            authors.addAll(authorService.findBySurnameStartingWithIgnoreCase(name));
        } else {
            authors =  authorService.findAllAuthors();
        }
		model.addAttribute("authors", authors);
		return "/admin/manageAuthors";
	}
	
	@GetMapping("/admin/formNewAuthor")
    public String newAuthor(Model model) {
        model.addAttribute("author", new Author());
        return "/admin/formNewAuthor";
    }

    @PostMapping("/author/new")
    public String submitAuthorForm(@Valid @ModelAttribute("author") Author author,
                                   BindingResult bindingResult,
                                   @RequestParam("image") MultipartFile imageFile,
                                   Model model) {
    	this.authorValidator.validate(author, bindingResult);
    	
        if (bindingResult.hasErrors()) {
        	model.addAttribute("author", author);
            return "/admin/formNewAuthor";
        }

        // Gestione upload immagine
        if (!imageFile.isEmpty()) {
            try {
                String fileName = imageFile.getOriginalFilename();
                Path uploadPath = Paths.get("uploads","images", "authors");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Salva il nome del file nel campo imageFileName
                author.setImageFileName(fileName); // se servito staticamente
            } catch (IOException e) {
                e.printStackTrace();
                // eventuale gestione messaggio d'errore nel model
            }
        }

        authorService.save(author);
        return "redirect:/authors"; 
    }
    
    @GetMapping("/admin/author/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id, Model model) {
        Author author = this.authorService.findById(id);
        for(Book book : this.bookService.findAll()) {
            if(book.getAuthors().contains(author)) {
                if(book.getAuthors().size()==1) {
                    model.addAttribute("lastAuthorError", "Cannot delete the author because is the only author for at least one book.");
                    model.addAttribute("authors", this.authorService.findAll());
                    return "admin/manageAuthors";
                }
                book.getAuthors().remove(author);
            }
        }
        this.authorService.delete(id);
        model.addAttribute("authors", this.authorService.findAll());
        model.addAttribute("successMessage", "Author successfully deleted.");
        return "admin/manageAuthors";
    }
    
    
    @GetMapping("/admin/updateAuthor/{id}")
	public String updateAuthor(@PathVariable("id")Long id,@RequestParam(value = "title", required = false) String title, Model model) {
		Author author = this.authorService.findById(id);
		return this.prepareUpdateAuthorPage(author, model, title);
	}
	
	@PostMapping("/admin/author/update/{id}")
	public String updateAuthorInfo(@PathVariable("id") Long id,
								   @Valid @ModelAttribute("author") Author authorForm,
								   BindingResult bindingResult,
	                               @RequestParam("image") MultipartFile imageFile,
	                               @RequestParam(value = "books", required = false) List<Long> booksIds,
	                               @RequestParam(value = "title", required = false) String title,
	                               Model model) {
		Author author = this.authorService.findById(id);
		
		// Ricostruisci libri
	    List<Book> selectedBooks = new ArrayList<>();
	    if (booksIds != null) {
	        for (Long bookId : booksIds) {
	            Book book = this.bookService.findById(bookId);
	            if (book != null) {
	                selectedBooks.add(book);
	            }
	        }
	    }
		
		authorForm.setImageFileName(author.getImageFileName());
		authorForm.setBooks(selectedBooks);
		
		this.authorValidator.validate(authorForm, bindingResult);
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("genericError", "Something went wrong. Please try again.");
	        return this.prepareUpdateAuthorPage(authorForm, model, title);
	    }
		
		author.setName(authorForm.getName());
		author.setSurname(authorForm.getSurname());
		author.setNationality(authorForm.getNationality());
		author.setBornYear(authorForm.getBornYear());
		author.setDeathYear(authorForm.getDeathYear());
		author.setDescription(authorForm.getDescription());
		author.setBooks(selectedBooks);
		
		if (!imageFile.isEmpty()) {
			try {
				String fileName = imageFile.getOriginalFilename();

				// Nuovo path: uploads/images/books
				Path uploadPath = Paths.get("uploads", "images", "authors");

				// Crea la cartella se non esiste
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				// Salva l'immagine nel percorso completo
				Path filePath = uploadPath.resolve(fileName);
				Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

				// Salva solo il nome del file nel database
				author.setImageFileName(fileName);

			} catch (IOException e) {
				e.printStackTrace(); // Puoi anche loggare o aggiungere errori al model
			}
		}
		this.authorService.save(author);
		model.addAttribute("success", "Changes saved successfully!");
		return this.prepareUpdateAuthorPage(author, model,title);
	}
	
	@PostMapping("/admin/author/{authorId}/add-book/{bookId}")
	public String addBookToAuthor(@PathVariable("authorId") Long authorId,
									@PathVariable("bookId") Long bookId, @RequestParam(value = "title", required = false) String title,Model model) {
		Book book = this.bookService.findById(bookId);
	    Author author = this.authorService.findById(authorId);
	    
	    if (!book.getAuthors().contains(author)) {
	        book.getAuthors().add(author);
	    }
	    // Validazione custom
	    BeanPropertyBindingResult result = new BeanPropertyBindingResult(book, "book");
	    this.bookValidator.validate(book, result);
	    
	    if (result.hasErrors()) {
	        // Ripristina l’autore rimosso
	        book.getAuthors().remove(author);
	        model.addAttribute("genericError", "Something went wrong. Please try again.");
	        model.addAttribute("authorErrorBookDuplicate", "This change would create a duplicate book.");
	        return this.prepareUpdateAuthorPage(author, model,title);
	    }
	    author.getBooks().add(book);
	    this.authorService.save(author);
	    model.addAttribute("success", "Changes saved successfully!");
		return this.prepareUpdateAuthorPage(author, model,title);
	}
	
	@PostMapping("/admin/author/{authorId}/remove-book/{bookId}")
	public String removeBookToAuthor(@PathVariable("authorId") Long authorId,
									@PathVariable("bookId") Long bookId, @RequestParam(value = "title", required = false) String title,Model model) {
		Book book = this.bookService.findById(bookId);
	    Author author = this.authorService.findById(authorId);
	    
	    if(book.getAuthors().size()==1 && (book.getAuthors().contains(author))) {
            model.addAttribute("genericError", "Something went wrong. Please try again.");
            model.addAttribute("noAuthors", "A book must have at least one author.");
            return this.prepareUpdateAuthorPage(author, model,title);
        }
	    
	    book.getAuthors().remove(author);
	 
	    // Validazione custom
	    BeanPropertyBindingResult result = new BeanPropertyBindingResult(book, "book");
	    this.bookValidator.validate(book, result);
	    
	    if (result.hasErrors()) {
	        // Ripristina l’autore rimosso
	        book.getAuthors().add(author);
	        model.addAttribute("genericError", "Something went wrong. Please try again.");
	        model.addAttribute("authorErrorBookDuplicate", "This change would create a duplicate book.");
	        return this.prepareUpdateAuthorPage(author, model,title);
	    }
	    author.getBooks().remove(book);
	    this.authorService.save(author);
	    model.addAttribute("success", "Changes saved successfully!");
		return this.prepareUpdateAuthorPage(author, model,title);
	}
	
	
	private String prepareUpdateAuthorPage(Author author, Model model, String title) {
		List<Book> allBooks = (List<Book>) this.bookService.findAll();
		List<Book> booksNotInAuthor = new ArrayList<>(allBooks);
		if (title != null && !title.isEmpty()) {
			booksNotInAuthor = bookService.findByTitleStartingWithIgnoreCase(title);

        } else {
        	booksNotInAuthor = (List<Book>) bookService.findAll();
        }
		booksNotInAuthor.removeAll(author.getBooks());
		model.addAttribute("author",author);
		model.addAttribute("booksNotInAuthor",booksNotInAuthor);
		return "admin/updateAuthor";
	}
}
