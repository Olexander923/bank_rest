# Система управления банковскими картами

Инструкция к запуску

1. Собрать проект командой mvn clean package
2. Запустить docker-контейнер командой docker-compose up --build
-произойдет подключени к БД и миграции, также будет создан профиль администратора
3. После старта приложения в терминале git bash выполнить команды:

* получить токен для admin: 
curl -X POST http://localhost:8080/api/auth/login 
* -H "Content-Type: application/json" -d 
* "{\"username\":\"admin\",\"password\":\"admin\"}"




