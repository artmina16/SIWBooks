package it.uniroma3.siw.utility;

import jakarta.validation.constraints.NotBlank;

public class UsernameChangeDTO {
    @NotBlank
    private String username;
    
    public UsernameChangeDTO() {}

    public UsernameChangeDTO(String username) {
        this.username = username;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

    
}
