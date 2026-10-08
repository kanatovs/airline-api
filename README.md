# Airline API

Учебный сервис управления рейсами на Java и Spring Boot. REST API хранит рейсы в PostgreSQL, а встроенная веб-страница позволяет добавлять, искать, редактировать и удалять записи.

![Веб-интерфейс управления рейсами](docs/screenshots/airline-desktop.png)

[Мобильный интерфейс](docs/screenshots/airline-mobile.png). На снимках — демонстрационные записи из временной тестовой БД.

## Возможности

- CRUD по адресу `/flights` и интерфейс на `http://127.0.0.1:8080`.
- Проверка номера рейса, трёхбуквенных кодов аэропортов и времени вылета.
- Приведение номера и кодов аэропортов к верхнему регистру, удаление пробелов по краям.
- Ответы `400` для неверных данных и `404` для отсутствующих рейсов в формате Problem Details.
- Миграции схемы Flyway; Hibernate проверяет схему и не меняет её автоматически.
- Интеграционные тесты HTTP → сервис → настоящая тестовая БД H2 и сборка в GitHub Actions.

## Стек и структура

Java 17, Spring Boot 4.0.2, Spring MVC, Jakarta Validation, Spring Data JPA, PostgreSQL, Flyway, Gradle Wrapper. Интерфейс написан на HTML/CSS/JavaScript без дополнительных зависимостей.

```text
src/main/java/com/example/airlineapi/
  flight/FlightController.java    # HTTP API
  flight/dto/                    # Отдельные модели запросов и ответов
  flight/service/                # Операции с рейсами и транзакции
  flight/repository/             # Доступ к БД
  flight/domain/                 # JPA-сущность
  common/ApiExceptionHandler.java
src/main/resources/
  db/migration/                  # Версионируемая схема
  static/                        # Веб-интерфейс
```

## Локальный запуск

Нужны JDK 17 и PostgreSQL. Gradle устанавливать отдельно не требуется: Wrapper загружает нужную версию при первом запуске.

### 1. Создать отдельную БД

В `psql` под администратором PostgreSQL:

```sql
CREATE ROLE airline_user LOGIN;
\password airline_user
CREATE DATABASE airline OWNER airline_user;
```

Команда `\password` интерактивно запрашивает новый пароль. Запускайте проект на отдельной пустой базе `airline`: первая миграция создаст таблицу `flights`. Пользователь БД должен иметь права создавать таблицы в своей базе.

Если раньше использовалась другая база с данными, сначала сохраните резервную копию. Приложение намеренно не делает автоматический baseline существующей схемы: безопасный перенос требует проверить таблицу и историю миграций. Для простого локального запуска используйте новую базу.

### 2. Задать переменные окружения

PowerShell:

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-17'
$env:DB_URL = 'jdbc:postgresql://localhost:5432/airline'
$env:DB_USERNAME = 'airline_user'
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'Пароль БД' -AsSecureString)).Password
.\gradlew.bat bootRun
```

Linux/macOS:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/airline'
export DB_USERNAME='airline_user'
read -rsp 'Пароль БД: ' DB_PASSWORD; echo
export DB_PASSWORD
./gradlew bootRun
```

Значение `DB_URL` по умолчанию — `jdbc:postgresql://localhost:5432/airline`; `DB_USERNAME` и `DB_PASSWORD` задаются явно. Файл `.env.example` показывает имена настроек, но Spring Boot сам `.env` не загружает. Настоящие пароли не храните в Git.

Откройте `http://127.0.0.1:8080`. Интерфейс обращается к API на том же адресе, поэтому работает и при изменении порта (`SERVER_PORT`) без правок JavaScript.

## API

| Метод | Путь | Результат |
| --- | --- | --- |
| GET | `/flights` | `200`, список рейсов |
| GET | `/flights/{id}` | `200`, рейс; `404`, если его нет |
| POST | `/flights` | `201`, созданный рейс |
| PUT | `/flights/{id}` | `200`, обновлённый рейс; `404`, если его нет |
| DELETE | `/flights/{id}` | `204`; `404`, если его нет |

Тело POST/PUT:

```json
{
  "flightNumber": "KZ100",
  "fromAirport": "ALA",
  "toAirport": "NQZ",
  "departureTime": "2026-10-09T14:30:00"
}
```

Ответ содержит эти же поля и числовой `id`, который создаётся сервером. `flightNumber` — от 1 до 10 букв латиницы, цифр или дефисов; коды аэропортов — 3 латинские буквы. Аэропорты отправления и прибытия должны отличаться. Это проверка формата IATA, а не проверка кода по внешнему справочнику. Время используется как `LocalDateTime` без часового пояса; исторические рейсы разрешены.

Пример создания в PowerShell:

```powershell
$flight = @{
    flightNumber = 'KZ100'
    fromAirport = 'ALA'
    toAirport = 'NQZ'
    departureTime = '2026-10-09T14:30:00'
} | ConvertTo-Json
Invoke-RestMethod 'http://127.0.0.1:8080/flights' -Method Post -ContentType 'application/json' -Body $flight
```

Ошибка валидации:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Flight data failed validation",
  "instance": "/flights",
  "errors": {"fromAirport": "Departure airport must be a three-letter IATA code"}
}
```

## Проверки и сборка

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

На Linux/macOS: `./gradlew test` и `./gradlew build`. PostgreSQL и переменные `DB_*` для тестов не нужны: профиль `test` поднимает H2 в памяти, применяет ту же миграцию и проверяет совместимость JPA-схемы.

Тесты проверяют полный CRUD, сохранение изменений, отсутствующие записи, неверные поля, неправильные JSON/даты/ID, нормализацию и сохранность записи после невалидного PUT. H2 не заменяет проверку на PostgreSQL перед производственным развёртыванием.

При подготовке этой версии сборка на JDK 17 прошла все 18 тестов. Интерфейс также проверен в headless Microsoft Edge с запущенным исполняемым JAR и временной H2: создание, поиск, редактирование, удаление, сообщения валидации и сохранение записей после обновления страницы. Отдельно проверено, что HTML из подменённого ответа API выводится как текст и не выполняется. Скриншоты сняты на ширинах 1440 и 390 пикселей; на мобильной ширине записи отображаются карточками с доступными кнопками действий.

Собранный исполняемый JAR: `build/libs/airline-api-0.0.1-SNAPSHOT.jar`; запуск — `java -jar build/libs/airline-api-0.0.1-SNAPSHOT.jar` с теми же переменными окружения.

## Ограничения и следующие шаги

Приложение предназначено для локальной учебной демонстрации. По умолчанию оно слушает только `127.0.0.1`, а изменение данных не требует авторизации. Для публикации в интернете сначала нужны аутентификация, права доступа, HTTPS и настройки развёртывания. `SERVER_ADDRESS` позволяет явно изменить адрес прослушивания. Произвольный CORS отключён: встроенный интерфейс работает на том же origin.

В проекте нет пассажиров, бронирований, вместимости самолёта, пагинации и учёта часовых поясов. Добавлять их стоит отдельными изменениями с моделью предметной области и тестами.

Ранее опубликованный пароль необходимо сменить в PostgreSQL, если он ещё используется. Перенос настроек в окружение защищает будущие коммиты, но не удаляет секрет из прежней истории Git. Рекомендации GitHub: [Removing sensitive data from a repository](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository).
