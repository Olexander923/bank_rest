package com.example.bankcards.integrations_tests;

import com.example.bankcards.entity.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TransactionRepositoryTests {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser;
    private Card fromCard;
    private Card toCard;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("test_db")
            .waitingFor(Wait.forListeningPort())
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPass");
        testUser.setEmail("test@test.com");
        testUser.setRole(Role.USER);  // или ADMIN

        fromCard = new Card();
        fromCard.setCardNumber("4111111111111111");
        fromCard.setCardStatus(CardStatus.ACTIVE);
        fromCard.setExpireDate(LocalDate.now().plusYears(1));
        fromCard.setBalance(new BigDecimal("50000.00"));

        toCard = new Card();
        toCard.setCardNumber("4222222222222222");
        toCard.setCardStatus(CardStatus.ACTIVE);
        toCard.setExpireDate(LocalDate.now().plusYears(1));
        toCard.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void testRepositorySave() {
        User savedUser = userRepository.save(testUser);
        fromCard.setUser(savedUser);
        toCard.setUser(savedUser);

        Card savedFromCard = cardRepository.save(fromCard);
        Card savedToCard = cardRepository.save(toCard);
        Transaction transaction = new Transaction(savedFromCard,savedToCard,new BigDecimal("15000.00"),TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.save(transaction);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void testFindById() {
        User savedUser = userRepository.save(testUser);
        fromCard.setUser(savedUser);
        toCard.setUser(savedUser);

        Card savedFromCard = cardRepository.save(fromCard);
        Card savedToCard = cardRepository.save(toCard);
        Transaction transaction = new Transaction(savedFromCard,savedToCard,new BigDecimal("15000.00"),TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.save(transaction);
        Optional<Transaction> found = transactionRepository.findById(saved.getId());
        assertThat(found).isPresent();
    }

    @Test
    void findAllTransactions() {
        User savedUser = userRepository.save(testUser);
        fromCard.setUser(savedUser);
        toCard.setUser(savedUser);

        Card savedFromCard = cardRepository.save(fromCard);
        Card savedToCard = cardRepository.save(toCard);
        Transaction transaction = new Transaction(savedFromCard,savedToCard,new BigDecimal("15000.00"),TransactionStatus.SUCCESS);
        Transaction transaction2 = new Transaction(savedFromCard,savedToCard,new BigDecimal("11000.00"),TransactionStatus.PENDING);
        transactionRepository.save(transaction);
        transactionRepository.save(transaction2);
        List<Transaction> all = transactionRepository.findAll();
        assertThat(all).hasSize(2);
    }
}
