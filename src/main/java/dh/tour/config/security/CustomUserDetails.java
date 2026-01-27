package dh.tour.config.security;

import dh.tour.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private String id;
    private String username; // correo
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        this.id = usuario.getId();
        this.username = usuario.getCorreo();
        this.password = usuario.getPassword();
        this.authorities = authorities;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
