package it.uniroma3.siw.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.ReviewService;
import jakarta.validation.Valid;

@Controller
public class ReviewController {

	@Autowired
	private ReviewService reviewService;
	
	@Autowired
	private BookService bookService;
	
	@Autowired
	private CredentialsService credentialsService;
	
	@GetMapping("/formReview/{id}")
	public String newReview(@PathVariable("id") Long bookId, Model model) {
		Review review = new Review();
		Book book = this.bookService.findById(bookId);
		model.addAttribute("review", review);
		model.addAttribute("book",book);
		return "formReview";
		
	}
	
	@PostMapping("/saveReview/{id}")
	public String saveReview(@PathVariable("id") Long bookId,
							 @Valid@ModelAttribute("review") Review review,
							 BindingResult bindingResult, Model model){


		if(bindingResult.hasErrors()) {
			Book book = this.bookService.findById(bookId);
			model.addAttribute("review", review);
			model.addAttribute("book",book);
			return "formReview";
		}
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/login";
		}
		else {
			UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			Book book = this.bookService.findById(bookId);
			review.setCredential(credentials);
			review.setBook(book);
		
			
			this.reviewService.save(review);
			return "redirect:/book/" + bookId;
		}
		
	}
	
	@GetMapping("/deleteReview/{id}")
	public String deleteReview(@PathVariable("id") Long reviewId, Model model, RedirectAttributes redirectAttributes) {
		Review review = this.reviewService.findById(reviewId);
		Long bookId = review.getBook().getId();
		Credentials credential = review.getCredential();
		credential.getReviews().remove(review);
		this.reviewService.deleteById(reviewId);
		redirectAttributes.addFlashAttribute("successMessage", "Your review has been successfully deleted.");
		return "redirect:/book/" + bookId;
	}
	
	@GetMapping("/editReview/{id}")
	public String editReview(@PathVariable("id") Long reviewId, Model model) {
		Review review = this.reviewService.findById(reviewId);
		model.addAttribute("review", review);
		
		return "/updateReview";
	}
	
	@PostMapping("/updateReview/{id}")
	public String saveEditReview(@PathVariable("id") Long reviewId,
							 @Valid@ModelAttribute("review") Review review,
							 BindingResult bindingResult, Model model,
							 RedirectAttributes redirectAttributes) {
		
		Review original = this.reviewService.findById(reviewId);
		Book book = original.getBook();
		review.setBook(book);
		if(bindingResult.hasErrors()) {
			model.addAttribute("review", review);
			return "updateReview";
		}


		original.setTitle(review.getTitle());
		original.setText(review.getText());
		original.setVote(review.getVote());
		
		this.reviewService.save(original);
		redirectAttributes.addFlashAttribute("successMessage", "Your review has been successfully edited.");
		return "redirect:/book/" + book.getId();
	}
	
	@GetMapping("/admin/manageReviews")
	public String manageReviews(Model model) {
		model.addAttribute("reviews", this.reviewService.findAll());
		return "/admin/manageReviews";
	}
	
	@GetMapping("/admin/review/delete/{id}")
	public String deleteAdminReview(@PathVariable("id") Long reviewId, Model model, RedirectAttributes redirectAttributes) {
		Review review = this.reviewService.findById(reviewId);
		Credentials credential = review.getCredential();
		credential.getReviews().remove(review);
		this.reviewService.deleteById(reviewId);
		redirectAttributes.addFlashAttribute("successMessage", "Review successfully deleted.");
		return "redirect:/admin/manageReviews";
	}
	
	
	@GetMapping("/deleteReviewPersonalArea/{id}")
	public String deleteReviewPersonalArea(@PathVariable("id") Long reviewId, Model model, RedirectAttributes redirectAttributes) {
		Review review = this.reviewService.findById(reviewId);
		Long bookId = review.getBook().getId();
		Credentials credential = review.getCredential();
		credential.getReviews().remove(review);
		this.reviewService.deleteById(reviewId);
		redirectAttributes.addFlashAttribute("successMessage", "Your review has been successfully deleted.");
		return "redirect:/profile";
	}
	
	@GetMapping("/editReviewPersonalArea/{id}")
	public String editReviewPersonalArea(@PathVariable("id") Long reviewId, Model model) {
		Review review = this.reviewService.findById(reviewId);
		model.addAttribute("review", review);
		
		return "/updateReviewPersonalArea";
	}
	
	@PostMapping("/updateReviewPersonalArea/{id}")
	public String saveEditReviewPersonalArea(@PathVariable("id") Long reviewId,
							 @Valid@ModelAttribute("review") Review review,
							 BindingResult bindingResult, Model model,
							 RedirectAttributes redirectAttributes) {
		
		Review original = this.reviewService.findById(reviewId);
		Book book = original.getBook();
		review.setBook(book);
		if(bindingResult.hasErrors()) {
			model.addAttribute("review", review);
			return "updateReviewPersonalArea";
		}


		original.setTitle(review.getTitle());
		original.setText(review.getText());
		original.setVote(review.getVote());
		
		this.reviewService.save(original);
		redirectAttributes.addFlashAttribute("successMessage", "Your review has been successfully edited.");
		return "redirect:/profile";
	}
}

