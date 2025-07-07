package it.uniroma3.siw.utility;

import org.springframework.stereotype.Component;

@Component("starUtils")
public class StarUtils {

    public String renderStars(double rating) {
        int percentage = (int) Math.round((rating / 5.0) * 100);

        StringBuilder html = new StringBuilder();
        html.append("<div class='star-rating-wrapper'>");

        html.append("<div class='stars-empty'>");
        for (int i = 0; i < 5; i++) {
            html.append("<i class='bi bi-star-fill'></i>");
        }
        html.append("</div>");

        html.append("<div class='stars-full' style='width: ").append(percentage).append("%;'>");
        for (int i = 0; i < 5; i++) {
            html.append("<i class='bi bi-star-fill'></i>");
        }
        html.append("</div>");

        html.append("</div>");
        return html.toString();
    }
}
