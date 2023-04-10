package com.example.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import com.example.user.Role;
import com.example.user.User;
import com.example.user.UserRepository;

@DataJpaTest
// HAce prueba a las clases anotadas con entities, prueba lo que tiene que ver
// con la capa de repositorio
// Voy a usar una base de datos en memoria por lo que al cerrar el servidor todo
// se ira,para que esto no
// pase tengo que anotar con la anotacion @AutoconfigureTestDatabase
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    private User user0;

    @BeforeEach

    void setUp() {
        user0 = User.builder()
                .firstName("Test User 0")
                .lastName("Agulló")
                .password("54321")
                .email("USER0@gmail.com")
                .role(Role.USER)
                .build();

    }

    // Metodo a probar primero: BDD
    /**
     * Quiero comprobar que al agregar un usuario coincide con lo que me esperaba
     */

    @Test
    @DisplayName("Test para agregar un user")
    public void testAddUser() {
        /**
         * Segun el enfoque: Una prueba unitaria se divide en tres partes
         *
         * 1. Arrange: Setting up the data that is required for this test case
         * 2. Act: Calling a method or Unit that is being tested.
         * 3. Assert: Verify that the expected result is right or wrong.
         *
         * Segun el enfoque BDD
         *
         * 1. given
         * 2. when
         * 3. then
         */
        // given - dado que:

        User user1 = User.builder()
                .firstName("Test User 1")
                .lastName("Asbayti")
                .password("123456")
                .email("sfd@daffdz.com")
                .role(Role.USER)
                .build();

        // when

        User userAdded = userRepository.save(user1);

        // Then

        assertThat(userAdded).isNotNull();
        assertThat(userAdded.getId()).isGreaterThan(0L);

    }

    @DisplayName("Test para listar usuario")

    @Test

    public void testFindAllUsers() {

        // given

        User user1 = User.builder()
                .firstName("Test User 1")
                .lastName("Agulló")
                .password("12345")
                .email("elisabetaudiovisual@gmail.com")
                .role(Role.USER)
                .build();

        userRepository.save(user0);
        userRepository.save(user1);

        // when

        List<User> usuarios = userRepository.findAll();

        // then

        assertThat(usuarios).isNotNull();
        assertThat(usuarios.size()).isEqualTo(3);

    }

    @DisplayName("Test para recuperar un user por ID")
     @Test
    public void testFindUserById() {
        // given
        userRepository.save(user0);
        // when
        User user = userRepository.findById(user0.getId()).get();
        // then
        assertThat(user.getId()).isNotEqualTo(0L);

    }
    @Test
    @DisplayName("Test para actualizar un user")
    public void testUpdateUser(){
        //given

        userRepository.save(user0);
        // When

        User userGuardado = userRepository.findByEmail(user0.getEmail()).get();

        userGuardado.setLastName(("Perico"));
        userGuardado.setFirstName("Juan");
        userGuardado.setEmail("juani98@gmail.com");

        User userUpdated = userRepository.save(userGuardado);

        //then
        assertThat(userUpdated.getEmail()).isEqualTo("juani98@gmail.com");
        assertThat(userUpdated.getFirstName()).isEqualTo("Juan");
    }

    @DisplayName("Test para eliminar un user")
    @Test
    public void testDeleteUser(){

        //given
        userRepository.save(user0);
        //when
        userRepository.delete(user0);
        Optional<User> optionalUser = userRepository.findByEmail(user0.getEmail());

        //then
        assertThat(optionalUser).isEmpty();
    }
}
