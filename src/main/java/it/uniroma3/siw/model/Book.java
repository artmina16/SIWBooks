package it.uniroma3.siw.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Book {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@NotBlank
	private String title;
	
	@NotNull
	@Max(2025)
	private Integer year;
	
	private String coverImageUrl;
	
	@Column(length=2000)
	@Size(max = 2000, message = "Description must be at most 2000 characters")
	private String description;
	
	@ManyToMany
	private List<Author> authors;
	
	@OneToMany(mappedBy = "book")
	private List<Review> reviews;
	
	@Transient
	private Double averageVote;
	
	@ElementCollection
    private List<String> imageFileNames = new ArrayList<>();
	
	
	public List<String> getImageFileNames() {
		return imageFileNames;
	}

	public void setImageFileNames(List<String> imageFileNames) {
		this.imageFileNames = imageFileNames;
	}

	public Double getAverageVote() {
		return averageVote;
	}

	public void setAverageVote(Double averageVote) {
		this.averageVote = averageVote;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
	
	
	public String getCoverImageUrl() {
		return coverImageUrl;
	}

	public void setCoverImageUrl(String coverImageUrl) {
		this.coverImageUrl = coverImageUrl;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}
	
	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}
	
	/**
     * Returns the full path (URL) to access the image for this book,
     * checking if it exists in static resources or in the uploads folder.
     */
    @Transient
    public String getImagePath(String imageFileName) {
        if (imageFileName == null)
            return "/images/default.jpg";

        Path staticPath = Paths.get("src/main/resources/static/images/", imageFileName);
        Path uploadPath = Paths.get("uploads/images/books/", imageFileName);

        if (Files.exists(staticPath)) {
            return "/images/" + imageFileName;
        } else if (Files.exists(uploadPath)) {
            return "/uploads/images/books/" + imageFileName;
        } else {
            return "/images/default.jpg";
        }
    }
	@Override
	public int hashCode() {
		return Objects.hash(id, title, year);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		return Objects.equals(id, other.id) && Objects.equals(title, other.title) && Objects.equals(year, other.year);
	}
}
