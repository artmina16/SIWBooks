package it.uniroma3.siw.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.utility.PasswordChangeDTO;
import it.uniroma3.siw.utility.UsernameChangeDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.UserService;

@Controller
public class UserController {
	
	@Autowired
	private CredentialsService credentialsService;
	
	@Autowired
	private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
	
	@GetMapping("/profile")
	public String getPersonalArea(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		boolean isOauth = false;
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		Object principal = authentication.getPrincipal();
		if(principal instanceof OAuth2User) {
			isOauth = true;
			model.addAttribute("isOauth", isOauth);
		}
		User user = credentials.getUser();
		
		model.addAttribute("user", user);
		model.addAttribute("credentials", credentials);
		model.addAttribute("passwordForm", new PasswordChangeDTO());
		model.addAttribute("usernameForm", new UsernameChangeDTO(credentials.getUsername()));


		if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
			model.addAttribute("credentials", credentials);
			model.addAttribute("passwordForm", new PasswordChangeDTO());
			model.addAttribute("usernameForm", new UsernameChangeDTO(credentials.getUsername()));
			model.addAttribute("users", this.credentialsService.findAll());
            return "admin/personalAreaAdmin";
        }

		model.addAttribute("reviews", credentials.getReviews());
				
		return "personalArea";
		
	}
	
	@GetMapping("/edit")
	public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
	    Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
	    User currentUser = credentials.getUser();
	    model.addAttribute("credentials", credentials);
	    model.addAttribute("user", currentUser);
	    model.addAttribute("passwordForm", new PasswordChangeDTO());
	    model.addAttribute("usernameForm", new UsernameChangeDTO(credentials.getUsername()));

	    return "editProfile";
	}
	
	@PostMapping("/edit")
	public String updateProfile(@Valid @ModelAttribute("user") User formUser,
	                            BindingResult result,
	                            @AuthenticationPrincipal UserDetails userDetails,
	                            Model model,
	                            RedirectAttributes redirectAttributes) {

	    Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
	    User existingUser = credentials.getUser();

	    // Verifica se l’email è già usata da un altro utente (PRIMA di settarla!)
	    if (!formUser.getEmail().equals(existingUser.getEmail())
	        && userService.emailExistsAndIsNot(formUser.getEmail(), existingUser.getId())) {
	        result.rejectValue("email", "error.user", "Email is already in use.");
	    }

	    if (result.hasErrors()) {
	        model.addAttribute("passwordForm", new PasswordChangeDTO());
	        model.addAttribute("usernameForm", new UsernameChangeDTO(credentials.getUsername()));

	        return "editProfile";
	    }

	    // ✅ Ora aggiorna
	    existingUser.setName(formUser.getName());
	    existingUser.setSurname(formUser.getSurname());
	    existingUser.setEmail(formUser.getEmail());

	    userService.saveUser(existingUser);

	    redirectAttributes.addFlashAttribute("successMessage", "Your profile has been successfully updated!");
	    return "redirect:/profile";
	}



	@PostMapping("/password")
	public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordChangeDTO form,
	                             BindingResult result,
	                             @AuthenticationPrincipal UserDetails userDetails,
	                             Model model,
	                             HttpServletRequest request,
	                             HttpServletResponse response) {

	    Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());

	    if (credentials == null || credentials.getUser() == null) {
	        result.reject("user.not.found", "User not found");
	        model.addAttribute("usernameForm", new UsernameChangeDTO());
	        model.addAttribute("usernameForm", new UsernameChangeDTO(credentials.getUsername()));
	        return "editProfile";
	    }

	    User user = credentials.getUser();

	    // Verifica la password attuale
	    if (!result.hasFieldErrors("oldPassword") &&
	        !passwordEncoder.matches(form.getOldPassword(), credentials.getPassword())) {
	        result.rejectValue("oldPassword", "error.passwordForm", "Current password is incorrect");
	    }

	    // Verifica che le nuove password coincidano
	    if (!result.hasFieldErrors("newPassword") &&
	        !result.hasFieldErrors("confirmPassword") &&
	        !form.getNewPassword().equals(form.getConfirmPassword())) {
	        result.rejectValue("confirmPassword", "error.passwordForm", "Passwords do not match");
	    }

	    if (result.hasErrors()) {
	        model.addAttribute("user", user);
	        model.addAttribute("usernameForm", new UsernameChangeDTO()); 
	        model.addAttribute("usernameForm", new UsernameChangeDTO(credentials.getUsername()));
	        return "editProfile";
	    }

	    // ✅ Aggiorna la password
	    credentials.setPassword(passwordEncoder.encode(form.getNewPassword()));
	    credentialsService.save(credentials);

	    // ✅ Logout manuale (invalida la sessione)
	    request.getSession().invalidate();
	    SecurityContextHolder.clearContext();

	    // ✅ Mostra direttamente la pagina di successo
	    return "logout-success";
	}




	@PostMapping("/edit-username")
	public String updateUsername(@Valid @ModelAttribute("usernameForm") UsernameChangeDTO form,
	                             BindingResult result,
	                             @AuthenticationPrincipal UserDetails userDetails,
	                             HttpServletRequest request,
	                             Model model) {

	    Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());

	    // Se è cambiato, verifica che non sia già in uso
	    if (!form.getUsername().equals(credentials.getUsername()) &&
	        credentialsService.existsByUsername(form.getUsername())) {
	        result.rejectValue("username", "error.username", "Username already exists");
	    }

	    if (result.hasErrors()) {
	        model.addAttribute("user", credentials.getUser());
	        model.addAttribute("passwordForm", new PasswordChangeDTO());
	        return "editProfile";
	    }

	    credentials.setUsername(form.getUsername());
	    credentialsService.save(credentials);


	    request.getSession().invalidate();
	    SecurityContextHolder.clearContext();

	    return "logout-success";
	}

	
	@GetMapping("/logout-success")
	public String logoutSuccess() {
	    return "logout-success"; 
	}




}
