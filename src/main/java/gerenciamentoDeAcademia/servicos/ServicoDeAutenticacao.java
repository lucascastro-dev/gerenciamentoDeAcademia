package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.AutenticacaoDto;
import gerenciamentoDeAcademia.dto.RegistroDto;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.infra.seguranca.TokenService;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ServicoDeAutenticacao implements UserDetailsService {
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLogin(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }

        return new Usuario(usuario.getUsername(), usuario.getPassword(), usuario.getRole());
    }

    public ResponseEntity realizarLogin(AutenticacaoDto data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = authenticationManager.authenticate(usernamePassword);
            var token = tokenService.gerarToken((Usuario) auth.getPrincipal());

            return ResponseEntity.status(HttpStatus.CREATED).body(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    public ResponseEntity cadastrarUsuario(RegistroDto data) {
        try {
            if (usuarioRepository.findByLogin(data.login()) != null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario já cadastrado!");

            String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
            usuarioRepository.save(new Usuario(data.login(), encryptedPassword, data.role()));

            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario cadastrado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao cadastrar usuário: " + e.getMessage());
        }
    }
}
