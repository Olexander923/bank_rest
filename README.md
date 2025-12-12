## Стек
Java 17, PostgreSQL, Spring(boot/security/mvc/data), Hibernate, Docker, Liquibase


## Инструкция к запуску
1. Собрать проект командой mvn clean package
2. Запустить приложение Docker Compose командой docker-compose up --build
3. Выполнить следующие команды:

# Получить токены:
  curl -X POST http://localhost:8080/api/auth/login 
  -H "Content-Type: application/json" 
  -d "{\"username\":\"adminuser\",\"password\":\"ValidPass1@\"}"

→ скопируй токен
export ADMIN_TOKEN="вставь_токен"

# Для пользователя сперва регистрация, потом получение токена:
curl -X POST http://localhost:8080/api/auth/register 
-H "Content-Type: application/json" -d "{\"username\":\"clientuser\",\"password\":\"Pass123@\",\"email\":\"c@test.com\"}"

curl -X POST http://localhost:8080/api/auth/login 
-H "Content-Type: application/json" -d "{\"username\":\"clientuser\",\"password\":\"Pass123@\"}"

 → скопируй токен
export USER_TOKEN="вставь_токен"

## Admin создает две карты для user'a
curl -X POST http://localhost:8080/api/admin/cards \
-H "Authorization: Bearer $ADMIN_TOKEN" \
-H "Content-Type: application/json" \
-d "{\"userId\":2,\"cardNumber\":\"4000000000000002\",\"expireDate\":\"2028-12-31\",\"cardStatus\":\"ACTIVE\",\"balance\":5000.00}"

curl -X POST http://localhost:8080/api/admin/cards \
-H "Authorization: Bearer $ADMIN_TOKEN" \
-H "Content-Type: application/json" \
-d "{\"userId\":2,\"cardNumber\":\"5555555555554444\",\"expireDate\":\"2029-06-30\",\"cardStatus\":\"ACTIVE\",\"balance\":2000.00}"

## User: просмотр и перевод
список карт:
curl -H "Authorization: Bearer $USER_TOKEN" "http://localhost:8080/api/user/cards?page=0&size=10"

проверка баланса:
curl -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/user/cards/1/balance

# Перевод 300.50 с карты 1 → на карту 2
curl -X POST http://localhost:8080/api/user/cards/transfer 
-H "Authorization: Bearer $USER_TOKEN" 
-H "Content-Type: application/json" 
-d "{\"fromCardId\":1,\"toCardId\":2,\"amount\":300.50}"

# Проверь балансы после перевода
curl -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/user/cards/1/balance  
curl -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/user/cards/2/balance  

# Тестирование конкурентности/запуск 3 переводов одновременно
USER_TOKEN="вставь_токен"
for i in 1 2 3; do
curl -X POST http://localhost:8080/api/user/cards/transfer \
-H "Authorization: Bearer $USER_TOKEN" \
-H "Content-Type: application/json" \
-d "{\"fromCardId\":1,\"toCardId\":2,\"amount\":${i}0.00}" &
sleep 0.05
done
wait

## Admin: блокировка и активация карт
# Блокировка
curl -X POST http://localhost:8080/api/admin/cards/1/block -H "Authorization: Bearer $ADMIN_TOKEN"

# Активация
curl -X PATCH http://localhost:8080/api/admin/cards/1/activate -H "Authorization: Bearer $ADMIN_TOKEN"

## Безопасность
USER не может вызывать ADMIN-методы
curl -v -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/admin/cards 

USER не видит чужие карты (если создать второго пользователя)
curl -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/user/cards/999/balance 

### Примечание:
- Роль admin автоматически создается в Liquibase, можно зарегестрировать нового admin'a командой:
  curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"adminuser","password":"ValidPass1@","email":"admin@bank.com"}'