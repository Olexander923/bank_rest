# Система управления банковскими картами

## Стек
Java 17+, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL/MySQL, Liquibase, Docker, JWT, OpenAPI

## Инструкция к запуску
Инструкция к запуску

1. Собрать проект командой mvn clean package
2. Запустить docker-контейнер командой docker-compose up --build
-произойдет подключени к БД и миграции, также будет создан профиль администратора
3. После старта приложения в терминале git bash выполнить команды:

# получить токен для ADMIN: 
curl -X POST http://localhost:8080/api/auth/login     export ADMIN_TOKEN="вставить_токен"
 -H "Content-Type: application/json" -d 
 "{\"username\":\"admin\",\"password\":\"admin\"}"
  

# сперва зарегестрировать USER'a: 
curl -X POST http://localhost:8080/api/auth/register 
-H "Content-Type: application/json" -d 
"{\"username\":\"client\",\"password\":\"pass123\",\"email\":\"c@test.com\"}"


# получить токен для USER: 
curl -X POST http://localhost:8080/api/auth/login       export USER_TOKEN="вставить_токен"
-H "Content-Type: application/json" -d 
"{\"username\":\"client\",\"password\":\"pass123\"}"


# создать карты для USER'a от ADMIN'a:
curl -X POST http://localhost:8080/api/admin/cards \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":2,\"cardNumber\":\"4000000000000002\",\"expireDate\":\"2028-12-31\",\"cardStatus\":\"ACTIVE\",\"balance\":5000.00}"

curl -X POST http://localhost:8080/api/admin/cards \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":2,\"cardNumber\":\"5555555555554444\",\"expireDate\":\"2029-06-30\",\"cardStatus\":\"ACTIVE\",\"balance\":2000.00}"

# user: просмотр списка карт и перевод
curl -H "Authorization: Bearer $USER_TOKEN" "http://localhost:8080/api/user/cards?page=0&size=10"

curl -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/user/cards/1/balance баланс первой карты

# Перевод 300.50 с карты 1 → на карту 2
curl -X POST http://localhost:8080/api/user/cards/transfer -H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json" -d "{\"fromCardId\":1,\"toCardId\":2,\"amount\":300.50}"

# Проверка баланса обоих карт после перевода
curl -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/user/cards/1/balance 
curl -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/user/cards/2/balance 

# Блокировка
curl -X POST http://localhost:8080/api/admin/cards/1/block -H "Authorization: Bearer $ADMIN_TOKEN"

# Активация
curl -X PATCH http://localhost:8080/api/admin/cards/1/activate -H "Authorization: Bearer $ADMIN_TOKEN"

# Безопасность(USER не может вызывать ADMIN-методы)
curl -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/admin/cards  
curl -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/user/cards/999/balance


### Примечание:
Спецификация API описана в файле open-api.yaml в директории docks


