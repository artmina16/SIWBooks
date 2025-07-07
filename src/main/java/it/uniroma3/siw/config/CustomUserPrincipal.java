package it.uniroma3.siw.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomUserPrincipal implements UserDetails, OAuth2User {

    private Long id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes; // attributi OAuth2

    // Costruttore per login form (UserDetails)
    public CustomUserPrincipal(String username, String password,
                               Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    // Costruttore per OAuth2
    public CustomUserPrincipal(String username,
                               Collection<? extends GrantedAuthority> authorities,
                               Map<String, Object> attributes) {
        this.id = id;
        this.username = username;
        this.password = null; // OAuth2 non ha password locale
        this.authorities = authorities;
        this.attributes = attributes;
    }

    // --- UserDetails methods ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // personalizza se serve
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- OAuth2User methods ---

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return attributes.get("sub").toString(); // ID Google univoco
    }
}

