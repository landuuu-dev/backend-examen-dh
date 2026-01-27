package dh.tour.config;

import dh.tour.model.Rol;
import dh.tour.model.Usuario;
import dh.tour.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${super-admin.email}")
    private String email;

    @Value("${super-admin.password}")
    private String password;

    public SuperAdminInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {

        if (usuarioRepository.findByCorreo(email).isPresent()) {
            return;
        }

        Usuario superAdmin = new Usuario();
        superAdmin.setNombre("Super Admin");
        superAdmin.setCorreo(email);
        superAdmin.setPassword(passwordEncoder.encode(password));
        superAdmin.setRol(Rol.SUPER_ADMIN);

        usuarioRepository.save(superAdmin);
    }
}
