package com.rk.pulseboard;

import com.rk.pulseboard.entity.User;
import com.rk.pulseboard.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class IncidentRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveCreatorUser() {
        //Arrange
        User user1 = new User();
        user1.setName("John");
        user1.setEmail("john@pb.com");
        user1.setPasswordHash("hashed-password-for-test");
        user1.setRole("USER");

        //Act & Assert
        User savedUser = userRepository.save(user1);
        assertThat(savedUser.getId()).isNotNull();

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john@pb.com");
    }
}
