package com.lolmeida.repository;

import com.lolmeida.entity.User;
import com.lolmeida.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("UserRepository")
class UserRepositoryTest {

    @Inject
    UserRepository userRepository;

    private User testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up database before each test
        userRepository.deleteAll();
        
        // Create test user
        testUser = createTestUser();
    }

    @AfterEach
    @Transactional
    void tearDown() {
        // Clean up after each test
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @Transactional
    @DisplayName("Should find user by username")
    void testFindByUsernameSuccess() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        
        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
    }

    @Test
    @Order(2)
    @Transactional
    @DisplayName("Should return empty when username not found")
    void testFindByUsernameNotFound() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        Optional<User> foundUser = userRepository.findByUsername("nonexistentuser");
        
        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Order(3)
    @Transactional
    @DisplayName("Should handle null username")
    void testFindByUsernameNull() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername(null);
        
        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Order(4)
    @Transactional
    @DisplayName("Should find user by email")
    void testFindByEmailSuccess() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        
        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
    }

    @Test
    @Order(5)
    @Transactional
    @DisplayName("Should return empty when email not found")
    void testFindByEmailNotFound() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        
        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Order(6)
    @Transactional
    @DisplayName("Should handle null email")
    void testFindByEmailNull() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail(null);
        
        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Order(7)
    @Transactional
    @DisplayName("Should search by username")
    void testSearchByUsername() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        Optional<User> foundUser = userRepository.search("username", "testuser");
        
        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
    }

    @Test
    @Order(8)
    @Transactional
    @DisplayName("Should search by email")
    void testSearchByEmail() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        Optional<User> foundUser = userRepository.search("email", "test@example.com");
        
        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
    }

    @Test
    @Order(9)
    @Transactional
    @DisplayName("Should search by passwordHash")
    void testSearchByPasswordHash() {
        // Arrange
        userRepository.persist(testUser);
        String passwordHash = testUser.getPasswordHash();
        
        // Act
        Optional<User> foundUser = userRepository.search("passwordHash", passwordHash);
        
        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(passwordHash, foundUser.get().getPasswordHash());
    }

    @Test
    @Order(10)
    @Transactional
    @DisplayName("Should return empty when search finds nothing")
    void testSearchNotFound() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        Optional<User> foundUser = userRepository.search("username", "notfound");
        
        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Order(11)
    @Transactional
    @DisplayName("Should return true when username exists")
    void testExistsByUsernameTrue() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        boolean exists = userRepository.existsByUsername("testuser");
        
        // Assert
        assertTrue(exists);
    }

    @Test
    @Order(12)
    @Transactional
    @DisplayName("Should return false when username does not exist")
    void testExistsByUsernameFalse() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");
        
        // Assert
        assertFalse(exists);
    }

    @Test
    @Order(13)
    @Transactional
    @DisplayName("Should handle null username in existsByUsername")
    void testExistsByUsernameNull() {
        // Act
        boolean exists = userRepository.existsByUsername(null);
        
        // Assert
        assertFalse(exists);
    }

    @Test
    @Order(14)
    @Transactional
    @DisplayName("Should return true when email exists")
    void testExistsByEmailTrue() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");
        
        // Assert
        assertTrue(exists);
    }

    @Test
    @Order(15)
    @Transactional
    @DisplayName("Should return false when email does not exist")
    void testExistsByEmailFalse() {
        // Arrange
        userRepository.persist(testUser);
        
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        
        // Assert
        assertFalse(exists);
    }

    @Test
    @Order(16)
    @Transactional
    @DisplayName("Should handle null email in existsByEmail")
    void testExistsByEmailNull() {
        // Act
        boolean exists = userRepository.existsByEmail(null);
        
        // Assert
        assertFalse(exists);
    }

    @Test
    @Order(17)
    @Transactional
    @DisplayName("Should find user by id")
    void testFindByIdOptionalSuccess() {
        // Arrange
        userRepository.persist(testUser);
        Long userId = testUser.getId();
        
        // Act
        Optional<User> foundUser = userRepository.findByIdOptional(userId);
        
        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
    }

    @Test
    @Order(18)
    @Transactional
    @DisplayName("Should return empty when id not found")
    void testFindByIdOptionalNotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByIdOptional(999999L);
        
        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Order(19)
    @Transactional
    @DisplayName("Should handle null id in findByIdOptional")
    void testFindByIdOptionalNull() {
        // Act
        Optional<User> foundUser = userRepository.findByIdOptional(null);
        
        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Order(20)
    @Transactional
    @DisplayName("Should delete user by id")
    void testDeleteSuccess() {
        // Arrange
        userRepository.persist(testUser);
        Long userId = testUser.getId();
        
        // Act
        boolean deleted = userRepository.delete(userId);
        
        // Assert
        assertTrue(deleted);
        assertFalse(userRepository.findByIdOptional(userId).isPresent());
    }

    @Test
    @Order(21)
    @Transactional
    @DisplayName("Should return false when deleting non-existent user")
    void testDeleteNotFound() {
        // Act
        boolean deleted = userRepository.delete(999999L);
        
        // Assert
        assertFalse(deleted);
    }

    @Test
    @Order(22)
    @Transactional
    @DisplayName("Should create new user")
    void testCreateNewUser() {
        // Arrange
        User newUser = createTestUser();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        
        // Act
        userRepository.createOrUpdate(newUser);
        
        // Assert
        assertNotNull(newUser.getId());
        Optional<User> foundUser = userRepository.findByUsername("newuser");
        assertTrue(foundUser.isPresent());
        assertEquals("new@example.com", foundUser.get().getEmail());
    }

    @Test
    @Order(23)
    @Transactional
    @DisplayName("Should update existing user")
    void testUpdateExistingUser() {
        // Arrange
        userRepository.persist(testUser);
        Long userId = testUser.getId();
        
        // Modify user
        testUser.setEmail("updated@example.com");
        testUser.setPasswordHash("newhashedpassword");
        testUser.setUpdatedAt(LocalDateTime.now().plusMinutes(1));
        
        // Act
        userRepository.createOrUpdate(testUser);
        
        // Assert
        Optional<User> updatedUser = userRepository.findByIdOptional(userId);
        assertTrue(updatedUser.isPresent());
        assertEquals("updated@example.com", updatedUser.get().getEmail());
        assertEquals("newhashedpassword", updatedUser.get().getPasswordHash());
        assertNotEquals(testUser.getCreatedAt(), updatedUser.get().getUpdatedAt());
    }

    @Test
    @Order(24)
    @Transactional
    @DisplayName("Should handle null entity in createOrUpdate")
    void testCreateOrUpdateNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            userRepository.createOrUpdate(null);
        });
    }

    @Test
    @Order(25)
    @Transactional
    @DisplayName("Should persist and find all users")
    void testPersistAndFindAll() {
        // Arrange
        User user1 = createTestUser();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        
        User user2 = createTestUser();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        
        // Act
        userRepository.persist(user1, user2);
        List<User> allUsers = userRepository.listAll();
        
        // Assert
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("user1")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("user2")));
    }

    @Test
    @Order(26)
    @Transactional
    @DisplayName("Should count users")
    void testCount() {
        // Arrange
        User user1 = createTestUser();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        
        User user2 = createTestUser();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        
        userRepository.persist(user1, user2);
        
        // Act
        long count = userRepository.count();
        
        // Assert
        assertEquals(2, count);
    }

    @Test
    @Order(27)
    @Transactional
    @DisplayName("Should find by id using inherited method")
    void testFindById() {
        // Arrange
        userRepository.persist(testUser);
        Long userId = testUser.getId();
        
        // Act
        User foundUser = userRepository.findById(userId);
        
        // Assert
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
    }

    @Test
    @Order(28)
    @Transactional
    @DisplayName("Should delete all users")
    void testDeleteAll() {
        // Arrange
        User user1 = createTestUser();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        
        User user2 = createTestUser();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        
        userRepository.persist(user1, user2);
        assertEquals(2, userRepository.count());
        
        // Act
        long deletedCount = userRepository.deleteAll();
        
        // Assert
        assertEquals(2, deletedCount);
        assertEquals(0, userRepository.count());
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("hashedpassword123");
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}