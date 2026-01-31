package dh.tour.service;
import org.springframework.dao.DuplicateKeyException;
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
    private final EmailService emailService;


    public UsuarioService(UsuarioRepository usuarioRepository, TourRepository tourRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.tourRepository = tourRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    public Usuario registrar(Usuario usuario) {
        try {
            usuario.setPassword(
                    passwordEncoder.encode(usuario.getPassword())
            );

            return usuarioRepository.save(usuario);
        } catch (DuplicateKeyException e) {
            // Aquí capturamos el error de MongoDB y lo traducimos
            System.err.println("⚠️ INTENTO DE REGISTRO FALLIDO: El correo " + usuario.getCorreo() + " ya existe.");

            // Lanzamos una excepción personalizada o una de Spring para que el Controller sepa qué pasó
            throw new RuntimeException("El usuario con este correo ya está registrado.");
        }
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

    // En dh.tour.service.UsuarioService
    public List<Tour> obtenerFavoritos(String usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFavoritos() == null || usuario.getFavoritos().isEmpty()) {
            return List.of();
        }

        // 1. Buscamos los tours reales en la DB usando los IDs del usuario
        List<Tour> toursExistentes = tourRepository.findAllById(usuario.getFavoritos());

        // 2. Si la lista de tours encontrados es más corta que la de IDs guardados...
        if (toursExistentes.size() < usuario.getFavoritos().size()) {
            // Limpiamos la lista del usuario dejando solo los que sí existen
            List<String> idsLimpios = toursExistentes.stream()
                    .map(Tour::getId)
                    .toList();
            usuario.setFavoritos(new java.util.ArrayList<>(idsLimpios));
            usuarioRepository.save(usuario);
            System.out.println("🧹 Favoritos limpiados: se eliminaron IDs de tours borrados.");
        }

        return toursExistentes;
    }

    // En dh.tour.service.UsuarioService.java agrega:

    public void solicitarRecuperacion(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Correo no encontrado"));

        String token = java.util.UUID.randomUUID().toString();
        usuario.setResetToken(token);
        usuario.setResetTokenExpiration(java.time.LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        emailService.enviarEmailRecuperacion(correo, token);
    }

    public void completarRecuperacion(String token, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> token.equals(u.getResetToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (usuario.getResetTokenExpiration().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setResetToken(null); // Limpiar token usado
        usuario.setResetTokenExpiration(null);
        usuarioRepository.save(usuario);
    }
}




