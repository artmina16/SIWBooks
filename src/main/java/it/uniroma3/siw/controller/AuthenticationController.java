package it.uniroma3.siw.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.ReviewService;
import it.uniroma3.siw.service.UserService;
import jakarta.validation.Valid;

@Controller
public class AuthenticationController {
	
	@Autowired
	private CredentialsService credentialsService;

    @Autowired
	private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private ReviewService reviewService;

	
	@GetMapping(value = "/register") 
	public String showRegisterForm (Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("credentials", new Credentials());
		return "formRegisterUser";
	}
	
	@GetMapping(value = "/login") 
	public String showLoginForm (Model model) {
		return "formLogin";
	}

	@GetMapping(value = "/") 
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.loadRecommendedBooks(model);
        if (authentication instanceof AnonymousAuthenticationToken) {
            return "home";
        }

        Object principal = authentication.getPrincipal();

        // Caso login standard (UserDetails)
        if (principal instanceof UserDetails userDetails) {
            Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
            if (credentials != null && credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
                return "admin/homeAdmin";
            }
        } 

        return "home";
    }

		
    @GetMapping(value = "/success")
    public String defaultAfterLogin(Model model) {
        
//    	UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    	Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
//    	this.loadRecommendedBooks(model);
//    	if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
//            return "admin/homeAdmin";
//        }
    	return "redirect:/";
    	//return "home";
    }

    @PostMapping(value = { "/register" })
    public String registerUser(@Valid @ModelAttribute("user") User user,
            BindingResult userBindingResult, @Valid
            @ModelAttribute("credentials") Credentials credentials,
            BindingResult credentialsBindingResult,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        boolean error = false;
        // se user e credential hanno entrambi contenuti validi, memorizza User e the Credentials nel DB
        if(!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
            if (!credentials.getPassword().equals(confirmPassword)) {
                error = true;
                model.addAttribute("passwordMismatchError", "The passwords do not match.");
            }
            if(this.credentialsService.existsByUsername(credentials.getUsername())) {
                error = true;
                model.addAttribute("usernameAlreadyInUseError", "Username already in use.");
            }
            if(this.userService.existsByEmail(user.getEmail())) {
                error = true;
                model.addAttribute("emailAlreadyInUseError", "Email already in use.");
            }
            if(error)
                return "formRegisterUser";
            credentials.setUser(user);
            credentialsService.saveCredentials(credentials);
            return "registrationSuccessful";
        }
        return "formRegisterUser";
    }
    
    private void loadRecommendedBooks(Model model) {
        List<Book> recommendedBooks = bookService.findTopRatedBooks(12); // mostra 12 libri
        for(Book book: recommendedBooks) {
            Double avgVote = reviewService.getAverageVoteForBook(book);
            if(avgVote!=null && avgVote > 0) {
                book.setAverageVote(avgVote);
            }
            else {
                book.setAverageVote(0.0);
            }
        }
        model.addAttribute("recommendedBooks", recommendedBooks);
    }
}