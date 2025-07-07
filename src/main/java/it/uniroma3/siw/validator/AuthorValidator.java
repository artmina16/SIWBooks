package it.uniroma3.siw.validator;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.service.AuthorService;

@Component
public class AuthorValidator implements Validator {

    @Autowired
    private AuthorService authorService;

    @Override
    public boolean supports(Class<?> clazz) {
        return Author.class.equals(clazz);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Author author = (Author) o;

        LocalDate born = author.getBornYear();
        LocalDate death = author.getDeathYear();

        // Controllo 1: se deathYear è presente, deve essere dopo bornYear
        if (born != null && death != null && !death.isAfter(born)) {
        	errors.rejectValue("deathYear", "author.invalidDates");
        }

        // Controllo 2: autore duplicato (nome, cognome, data di nascita, nazionalità)
        List<Author> existing = authorService.findByNameAndSurnameAndBornYearAndNationality(
            author.getName(), author.getSurname(), author.getBornYear(), author.getNationality());

        if (!existing.isEmpty()) {
            boolean isDuplicate = author.getId() == null || 
                existing.stream().anyMatch(a -> !a.getId().equals(author.getId()));
            if (isDuplicate) {
            	errors.reject("author.duplicate");
            }
        }
    }
}