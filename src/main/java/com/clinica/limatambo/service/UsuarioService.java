package com.clinica.limatambo.service;

import com.clinica.limatambo.model.Rol;
import com.clinica.limatambo.model.Usuario;
import com.clinica.limatambo.repository.RolRepository;
import com.clinica.limatambo.repository.UsuarioRepository;
import com.clinica.limatambo.model.Paciente;
import com.clinica.limatambo.model.Medico;
import com.clinica.limatambo.repository.PacienteRepository;
import com.clinica.limatambo.repository.MedicoRepository;
import com.clinica.limatambo.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (usuario.getEstado() != null && !usuario.getEstado()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        Rol rol = rolRepository.findById(usuario.getIdRol())
                .orElseThrow(() -> new UsernameNotFoundException("Rol no encontrado para el usuario: " + username));

        String nombreRol = rol.getNombreRol().toUpperCase();
        
        String nombreReal = usuario.getUsername();
        if (nombreRol.equals("PACIENTE")) {
            Optional<Paciente> p = pacienteRepository.findByIdUsuario(usuario.getIdUsuario());
            if(p.isPresent()) {
                String[] nombres = p.get().getNombre().trim().split(" ");
                String[] apellidos = p.get().getApellido().trim().split(" ");
                nombreReal = nombres[0] + (apellidos.length > 0 ? " " + apellidos[0] : "");
            }
        } else if (nombreRol.equals("MEDICO")) {
            Optional<Medico> m = medicoRepository.findByIdUsuario(usuario.getIdUsuario());
            if(m.isPresent()) {
                String[] nombres = m.get().getNombre().trim().split(" ");
                String[] apellidos = m.get().getApellido().trim().split(" ");
                nombreReal = nombres[0] + (apellidos.length > 0 ? " " + apellidos[0] : "");
            }
        }

        if (nombreRol.equals("ADMINISTRADOR")) nombreRol = "ADMIN";
        if (nombreRol.equals("RECEPCIONISTA")) nombreRol = "RECEPCION";

        return new CustomUserDetails(
                usuario.getUsername(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + nombreRol)),
                nombreReal
        );
    }
}
