package it.uniroma3.siw.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.repository.ReviewRepository;

@Service
public class ReviewService {
	
	@Autowired
	private ReviewRepository reviewRepository;
	
	public Iterable<Review> findAll(){
		return this.reviewRepository.findAll();
	}

	public List<Review> findByBook(Book book) {
		return reviewRepository.findByBook(book);
	}

	public Review getByBookAndCredential(Book book, Credentials credentials) {
		return reviewRepository.getByBookAndCredential(book, credentials);
	}

	public void save(Review review) {
		System.out.println(">>> [ReviewService] Salvataggio su DB");
		this.reviewRepository.save(review);
		
	}
	
	public Double getAverageVoteForBook(Book book) {
        List<Review> reviews = reviewRepository.findByBook(book);
        if (reviews.isEmpty()) return null;
        return reviews.stream()
                      .mapToInt(Review::getVote)
                      .average()
                      .orElse(0.0);
    }

	public void deleteAll(List<Review> allBookReviews) {
		this.reviewRepository.deleteAll(allBookReviews);
		
	}

	public Review findById(Long reviewId) {
		return this.reviewRepository.findById(reviewId).get();
	}


	public void deleteById(Long id) {
		this.reviewRepository.deleteById(id);
		
	}


}
