package com.example.bankcards.integrations_tests;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CardRepositoryTests {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

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

    @Test
    void testSaveAndFindCard() {
        //создать и сохранить карту
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );

        var saveCard = cardRepository.save(card);

        //проверка что карта сохранилась и есть id
        assertThat(saveCard.getId()).isNotNull();
        //находим все карты и поверяем что новая карта там есть
        List<Card> cards = cardRepository.findAll();
        assertThat(cards).hasSize(1);
    }

    @Test
    void testFindAllEmpty() {
        //проверка что репозиторий пустой перед добавлением
        List<Card> cards = cardRepository.findAll();
        assertThat(cards).isEmpty();
    }

    @Test
    void findCardByIdExisting() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card existingCard = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );
        var saveCard = cardRepository.save(existingCard);
        Optional<Card> foundCard = cardRepository.findById(saveCard.getId());
        assertThat(foundCard).isPresent();
        assertThat(foundCard.get().getId()).isEqualTo(saveCard.getId());
    }

    @Test
    void findCardByIdNotExisting() {
        Optional<Card> foundCard = cardRepository.findById(999L);
        assertThat(foundCard).isEmpty();
    }

    @Test
    void findByUserId_UserHasNoCardsEmptyList() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Card> foundCards = cardRepository.findByUserId(user.getId(), pageable);
        assertThat(foundCards).isEmpty();
    }

    @Test
    void findByUserId_ReturnPageWithTwoCards() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card card1 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );
        Card card2 = new Card(
                "400000******0003",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("3000.00"),
                user
        );
        cardRepository.save(card1);
        cardRepository.save(card2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> result = cardRepository.findByUserId(saveUser.getId(), pageable);

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findByUserId_UserHasCardsButDifferentUser_ReturnsEmptyPage() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        User user2 = new User("testuser2", "ValidPass2@", "user@example.com", Role.USER);
        Card card1 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );

        Card card2 = new Card(
                "400000******0003",
                LocalDate.of(2029, 11, 28),
                CardStatus.ACTIVE,
                new BigDecimal("2000.00"),
                saveUser
        );

        cardRepository.save(card1);
        cardRepository.save(card2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> result = cardRepository.findByUserId(user2.getId(), pageable);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByCardStatus_ACTIVE() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card card1 = new Card(
                "encrypted_number",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );
        Card card2 = new Card(
                "encrypted_number2",
                LocalDate.of(2029, 11, 30),
                CardStatus.ACTIVE,
                new BigDecimal("3000.00"),
                user
        );
        cardRepository.save(card1);
        cardRepository.save(card2);

        User user2 = new User("testuser2", "ValidPass2@", "user@example.com", Role.USER);
        var saveUser2 = userRepository.save(user2);
        Card user2Card = new Card(
                "400000******0001",
                LocalDate.of(2028, 12, 31),
                CardStatus.BLOCKED,
                new BigDecimal("5000.00"),
                saveUser2
        );
        cardRepository.save(user2Card);

        User user3 = new User("testuser3", "ValidPass3@", "user3@example.com", Role.USER);
        var saveUser3 = userRepository.save(user3);
        Card user3Card1 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser3
        );

        Card user3Card2 = new Card(
                "400000******0003",
                LocalDate.of(2029, 11, 28),
                CardStatus.EXPIRED,
                new BigDecimal("2000.00"),
                saveUser3
        );
        cardRepository.save(user3Card1);
        cardRepository.save(user3Card2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> result = cardRepository.findByCardStatus(CardStatus.ACTIVE, pageable);
        assertThat(result.getContent()).hasSize(3);//проверка что 3 активные карты
        assertThat(result.getContent())
                .extracting(Card::getCardStatus)
                .containsOnly(CardStatus.ACTIVE); //что карты активны
        assertThat(result.getContent())
                .extracting(card -> card.getUser().getId())
                .containsExactlyInAnyOrder(saveUser.getId(), saveUser.getId(), saveUser3.getId());
    }

    @Test
    void findByCardStatus_BLOCKED() {
        User user2 = new User("testuser2", "ValidPass2@", "user@example.com", Role.USER);
        var saveUser2 = userRepository.save(user2);
        Card user2Card = new Card(
                "encrypted_number",
                LocalDate.of(2028, 12, 31),
                CardStatus.BLOCKED,
                new BigDecimal("5000.00"),
                saveUser2
        );
        cardRepository.save(user2Card);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> result = cardRepository.findByCardStatus(CardStatus.BLOCKED, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent())
                .extracting(Card::getCardStatus)
                .containsOnly(CardStatus.BLOCKED);
    }

    @Test
    void findByCardStatus_EXPIRED() {
        User user3 = new User("testuser3", "ValidPass3@", "user3@example.com", Role.USER);
        var saveUser3 = userRepository.save(user3);

        Card user3Card2 = new Card(
                "encrypted_number",
                LocalDate.of(2024, 11, 28),
                CardStatus.EXPIRED,
                new BigDecimal("2000.00"),
                saveUser3
        );
        cardRepository.save(user3Card2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> result = cardRepository.findByCardStatus(CardStatus.EXPIRED, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent())
                .extracting(Card::getCardStatus)
                .containsOnly(CardStatus.EXPIRED);
    }

    @Test
    void existsByUserIdAndCardStatus_ACTIVE() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card card1 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );
        Card card2 = new Card(
                "400000******0003",
                LocalDate.of(2028, 12, 31),
                CardStatus.BLOCKED,
                new BigDecimal("3000.00"),
                user
        );
        Card card3 = new Card(
                "400000******0004",
                LocalDate.of(2024, 12, 30),
                CardStatus.EXPIRED,
                new BigDecimal("3000.00"),
                user
        );
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        assertTrue(cardRepository.existsByUserIdAndCardStatus(user.getId(), CardStatus.ACTIVE));
        assertTrue(cardRepository.existsByUserIdAndCardStatus(user.getId(), CardStatus.BLOCKED));
        assertTrue(cardRepository.existsByUserIdAndCardStatus(user.getId(), CardStatus.EXPIRED));
    }

    @Test
    void testWithDuplicationCard() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card card1 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );
        Card card2 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("3000.00"),
                user
        );
        cardRepository.save(card1);
        assertThrows(DataIntegrityViolationException.class, () -> cardRepository.save(card2));
    }

    @Test
    void testUpdateStatusCard() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );
        Card savedCard = cardRepository.save(card);
        savedCard.setCardStatus(CardStatus.BLOCKED);
        cardRepository.save(savedCard);
        Optional<Card> updated = cardRepository.findById(savedCard.getId());
        assertThat(updated.get().getCardStatus() == CardStatus.BLOCKED).isTrue();
        assertThat(updated.get().getCardNumber()).isEqualTo("400000******0002");
        assertThat(updated.get().getUser().getId()).isEqualTo(saveUser.getId());
    }

    @Test
    void findCardWithExpiringDate() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card card1 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );
        Card card2 = new Card(
                "400000******0003",
                LocalDate.of(2025, 12, 20),
                CardStatus.ACTIVE,
                new BigDecimal("3000.00"),
                user
        );
        cardRepository.save(card1);
        cardRepository.save(card2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> result = cardRepository.findByExpireDateBefore(LocalDate.now().plusDays(30), pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testDeleteCard() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card card = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );
        var saveCard = cardRepository.save(card);
        cardRepository.deleteById(saveCard.getId());
        Optional<Card> found = cardRepository.findById(saveCard.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testRequestSortByBalance() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card card1 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );
        Card card2 = new Card(
                "400000******0003",
                LocalDate.of(2027, 12, 20),
                CardStatus.ACTIVE,
                new BigDecimal("3000.00"),
                user
        );
        Card card3 = new Card(
                "400000******0004",
                LocalDate.of(2026, 7, 15),
                CardStatus.ACTIVE,
                new BigDecimal("2500.00"),
                user
        );
        Card card4 = new Card(
                "400000******0005",
                LocalDate.of(2026, 8, 19),
                CardStatus.ACTIVE,
                new BigDecimal("1700.00"),
                user
        );
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);
        cardRepository.save(card4);

        Sort sort = Sort.by("balance").descending();
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Card> result = cardRepository.findByCardStatus(CardStatus.ACTIVE, pageable);
        List<BigDecimal> balances = result.getContent().stream()
                .map(Card::getBalance)
                .collect(Collectors.toList());
        //проверка что балансы в убывающем порядке
        assertThat(balances).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void testRequestSortByDate() {
        User user = new User("testuser", "ValidPass1@", "test@example.com", Role.USER);
        var saveUser = userRepository.save(user);

        Card card1 = new Card(
                "400000******0002",
                LocalDate.of(2028, 12, 31),
                CardStatus.ACTIVE,
                new BigDecimal("5000.00"),
                saveUser
        );
        Card card2 = new Card(
                "400000******0003",
                LocalDate.of(2027, 12, 20),
                CardStatus.ACTIVE,
                new BigDecimal("3000.00"),
                user
        );
        Card card3 = new Card(
                "400000******0004",
                LocalDate.of(2026, 7, 15),
                CardStatus.ACTIVE,
                new BigDecimal("2500.00"),
                user
        );
        Card card4 = new Card(
                "400000******0005",
                LocalDate.of(2026, 8, 19),
                CardStatus.ACTIVE,
                new BigDecimal("1700.00"),
                user
        );
        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);
        cardRepository.save(card4);

        Sort sortByDate = Sort.by("expireDate").descending();
        Pageable pageable = PageRequest.of(0, 10, sortByDate);
        Page<Card> result = cardRepository.findByCardStatus(CardStatus.ACTIVE, pageable);

        List<LocalDate> dates = result.getContent().stream()
                .map(Card::getExpireDate)
                .collect(Collectors.toList());
        assertThat(dates).isSortedAccordingTo(Comparator.reverseOrder());
    }

}
