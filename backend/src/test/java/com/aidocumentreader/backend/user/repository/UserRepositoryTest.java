package com.aidocumentreader.backend.user.repository;

import com.aidocumentreader.backend.TestcontainersConfiguration;
import com.aidocumentreader.backend.user.entity.Role;
import com.aidocumentreader.backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmailIgnoreCase(){
        User user = new User();
        user.setEmail("TEST@EXAMPLE.COM");
        user.setPasswordHash("hashed_password_123");
        user.setDisplayName("Sample User");
        user.setRole(Role.USER);
        user.setEnabled(true);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmailIgnoreCase("test@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("TEST@EXAMPLE.COM");
    }
}
