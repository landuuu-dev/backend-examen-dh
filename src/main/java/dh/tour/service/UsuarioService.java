package dh.tour.service;

import dh.tour.model.Rol;
import dh.tour.model.Tour;
import dh.tour.model.Usuario;
import dh.tour.repository.TourRepository;
import dh.tour.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final TourRepository tourRepository;
    private final PasswordEncoder passwordEncoder;


    public UsuarioService(UsuarioRepository usuarioRepository, TourRepository tourRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tourRepository = tourRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public Usuario registrar(Usuario usuario) {
        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );
        return usuarioRepository.save(usuario);
    }


    public Usuario login(String correo, String password) {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no existe"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        return usuario;
    }



    public void promoteToAdmin(String userId) {

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.SUPER_ADMIN) {
            throw new RuntimeException("No se puede modificar al SUPER_ADMIN");
        }

        usuario.setRol(Rol.ADMIN);
        usuarioRepository.save(usuario);
    }

    public Usuario actualizar(String id, Usuario usuarioActualizado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setCorreo(usuarioActualizado.getCorreo());

        // Solo actualizar contraseña si viene nueva
        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.SUPER_ADMIN) {
            throw new RuntimeException("No se puede eliminar a un SUPER_ADMIN");
        }

        usuarioRepository.deleteById(id);
    }
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));
    }

    public Usuario buscarPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }


        public Usuario agregarFavorito(String usuarioId, String tourId) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!usuario.getFavoritos().contains(tourId)) {
                usuario.getFavoritos().add(tourId);
            }

            return usuarioRepository.save(usuario);
        }

        public Usuario quitarFavorito(String usuarioId, String tourId) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuario.getFavoritos().remove(tourId);

            return usuarioRepository.save(usuario);
        }

    public List<Tour> obtenerFavoritos(String usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no hay favoritos, devuelve lista vacía
        if (usuario.getFavoritos() == null || usuario.getFavoritos().isEmpty()) {
            return List.of();
        }

        // Buscar todos los tours por IDs
        return tourRepository.findAllById(usuario.getFavoritos());
    }

}




