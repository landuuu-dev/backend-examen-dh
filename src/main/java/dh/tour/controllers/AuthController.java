package dh.tour.controllers;

import dh.tour.config.JwtUtil;
import dh.tour.dto.LoginRequest;
import dh.tour.dto.RegisterRequest;
import dh.tour.model.Rol;
import dh.tour.model.Usuario;
import dh.tour.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;


    public AuthController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Usuario usuario = usuarioService.login(
                request.getCorreo(),
                request.getPassword()
        );

        String token = jwtUtil.generateToken(usuario);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "rol", usuario.getRol()
        ));
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(request.getPassword());
        usuario.setRol(Rol.USER);


        Usuario creado = usuarioService.registrar(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }



}
