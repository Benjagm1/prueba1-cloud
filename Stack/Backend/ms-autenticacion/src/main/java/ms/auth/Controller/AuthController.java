package ms.auth.Controller;

import ms.dto.ActualizarUsuarioRequest;
import ms.dto.AuthResponse;
import ms.dto.CrearUsuarioRequest;
import ms.dto.CrearUsuarioResponse;
import ms.dto.LoginRequest;
import ms.dto.RegistroRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ms.auth.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(@RequestBody RegistroRequest request) {
        try {
            return ResponseEntity.ok(authService.registrar(request));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PostMapping("/usuarios")
    public ResponseEntity<CrearUsuarioResponse> crearUsuario(@RequestBody CrearUsuarioRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.crearUsuario(request));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios() {
        return ResponseEntity.ok(authService.obtenerUsuarios());
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(authService.eliminarUsuario(id));
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<String> actualizarUsuario(
            @PathVariable Long id,
            @RequestBody ActualizarUsuarioRequest request) {
        return ResponseEntity.ok(authService.actualizarUsuario(id, request));
    }
}