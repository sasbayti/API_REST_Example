package com.example.user;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // Creo los constructores que estan en final, esto es porque lo quiero ya
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User add(User user) {
        Optional<User> theUser = userRepository.findByEmail(user.getEmail());

        if(theUser.isPresent()) {
            // Deberiamos devolver una exception personalizada
            return null;
        }

        // Encriptamos la password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
    @Override
    public void deleteByEmail(String email) {
        userRepository.deleteByEmail(email);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository
            .findByEmail(email)
            .orElseThrow(() -> //Si usuario existe lo devuelve sino le devuelve una excepcion
               new UsernameNotFoundException("No existe el usuario con el email: " + email));
    }

    @Override
    public User update(User user) {
        User existingUser = userRepository.findByEmail(user.getEmail())
            .orElseThrow(
                () -> new UsernameNotFoundException("No existe el usuario con el email: " + user.getEmail()));

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        existingUser.setRole(user.getRole());

        return userRepository.save(existingUser);
    }
    
}