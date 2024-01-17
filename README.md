[![](https://img.shields.io/badge/Spring%20Boot%20Version-3.1.2-orange)](/build.gradle) [![](https://img.shields.io/badge/Java%20Version-17-orange)](/build.gradle)

<div align="center">
<img src="https://github.com/AnvilCoder/CheckpointManager/assets/124284597/8f2d0df9-e77e-486f-8bee-a2bd3276d492" alt="Логотип Checkpoint Manager" width="400" height="100"/>
</div>

# 


Веб-сервис, предназначенный для **контроля и управления доступом** на охраняемые территории,
такие как коттеджные поселки, жилые комплексы, производственные предприятия, деловые центры и различные режимные
объекты.

# 📚 Оглавление
- [Функции](#-функции)
- [Преимущества](#-преимущества)
- [Целевая Аудитория](#-целевая-аудитория)
- [Монетизация](#-монетизация)
- [Как пользоваться](#-как-пользоваться)
- [Помощь или Поддержка](#-помощь-или-поддержка)
- [Схемы какие-нибудь](#-схемы-какие-нибудь)
- [Технологии проекта](#-технологии-проекта)
  - [Backend](#backend)
  - [Frontend](#frontend)
  - [QA](#qa)
  - [Design](#design)
- [Наша команда](#-наша-команда)
   

## 🌟 Функции

- **Администрирование территорий** и расположенных на них контрольно-пропускных пунктов (постов охраны)
- **Прикрепление пользователей** к одной или нескольким территориям
- **Создание пропусков**: Пользователи сервиса могут легко создавать временные или постоянные пропуска для машин и
  посетителей с указанием времени их действия, а также создавать новые пропуска на основе уже существующих, отмечать
  пропуска избранными для быстрого доступа
- **Контроль режима**: пропуска используются охраной территории на КПП для контроля доступа при въезде/выезде и
  входе/выходе
- **Интеграция**: в перспективе проект предусматривает возможность интеграции с существующими системами видеонаблюдения и
  контроля доступа на объектах, может интегрироваться с другими rest-сервисами
- **Масштабируемость**: сервис подходит как для малых, так и для крупных объектов с несколькими КПП

## 💼 Преимущества

- **Эффективность и Безопасность**: Ускоряет процесс проверки на КПП и повышает уровень безопасности территории
- **Удобство для Пользователей**: Простой и интуитивно понятный интерфейс
- **Гибкость Настройки**: Возможность адаптации под специфические требования различных объектов
- **Безопасность**: Обеспечение конфиденциальности информации

## 🎯 Целевая Аудитория

- **Администрация объектов**: Компании, управляющие территориями
- **Охранные предприятия**: Обеспечивающие охрану и режим посещения
- **Жители и предприниматели**: Владельцы и арендаторы недвижимости
- **Посетители и водители**: Лица, въезжающие на охраняемые территории

## 💰 Монетизация

Модель монетизации включает подписку для объектов на использование сервиса, а также предоставление дополнительных услуг,
таких как настройка системы и техподдержка.

## 📖 Как пользоваться

1. **Перейти на сайт** [Checkpoint Manager](URL_сайта).

2. **Ввод электронной почты**:

- На стартовой странице введи свою электронную почту или выбери один из готовых аккаунтов, для обзора всего
  функционала приложения, в зависимости от роли пользователя.

<div align="center">
  <img alt="Ввод электронной почты" src="https://github.com/AnvilCoder/CheckpointManager/assets/124284597/a6784409-8162-44b9-97ef-c29d0fa60910" height="350" width="350"/>
  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
  <img alt="Выбор готового акка" src="https://github.com/AnvilCoder/CheckpointManager/assets/124284597/55f6cc8d-7c63-41c9-9dfe-7a46be1c61de" height="350" width="350"/>
</div>

3. **Определение статуса Пользователя**:

- Система проверит, зарегистрирован ли уже этот email.
- Если электронная почта уже есть в базе данных, откроется страница входа.
- Если ты новый пользователь, откроется страница регистрации.

4. **Вход или Регистрация**:

- **Для зарегестрированных пользователей пользователей**: достаточно ввести пароль для входа.

<div align="center">
<img alt="Форма входа" height="400" src="https://github.com/AnvilCoder/CheckpointManager/assets/124284597/3ef6a5c2-7824-4c5d-99b3-9c783495a6bc" width="400"/>
</div>

- **Для новых пользователей**: Заполни форму регистрации. Это быстро и просто – нужно всего
  лишь ввести несколько основных данных о себе. После регистрации тебе будет отправлено письмо со ссылкой для
  подтверждения твоей электронной почты.

<div align="center">
  <img alt="Форма регистрации" src="https://github.com/AnvilCoder/CheckpointManager/assets/124284597/a578480e-8270-4dc3-96b2-a68ec7c76afc" height="400" width="400"/>
  &nbsp; &nbsp; 
  <img alt="Письмо" src="https://github.com/AnvilCoder/CheckpointManager/assets/124284597/7fad9df1-d87d-4681-bfc1-26e9c4656280" height="400" width="400"/>
</div>

5. **Начало Работы**:

- После входа в систему перед тобой откроется главный интерфейс Checkpoint Manager.

<div align="center">
<img alt="Главный интерфейс Checkpoint Manager" height="400" src="https://github.com/AnvilCoder/CheckpointManager/assets/124284597/10f8ce61-8cee-4fa7-a6f3-e4791ab9ba4d" width="900"/>
</div>

- Если ты только зарегистрировался, то управляющий охраняемым объектом должен привязать твой аккаунт к соответствующей
  территории. Как только это произойдет,
  ты сможешь использовать сервис в полной мере!

![Скрин экрана управляющего для добавления пользователя](ссылка_на_скриншот_главного_интерфейса)

6. **Основной пользовательский функционал**:

- **Гибкость настройки пропусков**: Пользователи Checkpoint Manager обладают широкими возможностями для настройки
  пропусков в соответствии с их уникальными потребностями. Вот некоторые из ключевых функций:
    - **Выбор Территории**: Если пользователь связан с несколькими территориями, пропуск можно настроить так, чтобы он
      был действителен для конкретной локации.
    - **Тип Пропуска**: Предоставляется возможность выбора между разовыми и постоянными пропусками, что позволяет
      адаптировать доступ в соответствии с требованиями безопасности и удобства.
    - **Категория Посетителя**: Опция выбора пропуска для пешеходов или автомобилей, что упрощает управление потоками
      посетителей и транспорта.
    - **Персонализация данных**: Возможность заполнения детальной информации о посетителе, обеспечивая эффективный
      контроль доступа.
      
<div align="center">
<img alt="Создание пропуска" height="400" src="https://github.com/AnvilCoder/CheckpointManager/assets/124284597/0c84bc59-2fec-4655-ad83-44397ee42bb2" width="600"/>
</div>

- После создания, каждый пропуск обогащается **дополнительными деталями и функциональными возможностями**, что делает
  управление доступом еще более эффективным и удобным:
    - **Избранный Пропуск**: Пользователи могут отмечать часто используемые или важные пропуска. Эта функция полезна для
      фильрации и быстрого доступа.
    - **Дата Создания**: Для каждого пропуска отображается дата его создания. Это помогает вести учет и анализировать
      использование пропусков во времени.
    - **Просмотр Событий**: Пользователи имеют возможность просматривать все события, связанные с конкретным пропуском,
      что обеспечивает дополнительный уровень контроля и понимания активности на территории.
       
<div align="center">
<img alt="О пропуске" height="400" src="https://github.com/AnvilCoder/CheckpointManager/assets/124284597/a00dbd4b-430f-44ba-bf61-850c58e403a2" width="900"/>
</div>

## 🤝 Помощь или Поддержка

Если у тебя возникли вопросы, нужна помощь, или ты хочешь предложить сотрудничество, мы всегда здесь, чтобы помочь тебе:

- **Электронная Почта**: Наша команда поддержки всегда на связи и готова ответить на любые твои вопросы. Напиши нам на [anvilcoder@yandex.ru](mailto:anvilcoder@yandex.ru)
 – мы постараемся ответить как можно скорее.
- **Социальные Сети**: Следи за нами в социальных сетях, чтобы быть в курсе последних новостей и обновлений.

Мы гордимся своим проектом и всегда рады предоставить качественную поддержку нашим пользователям и партнерам.

## 📈 Схемы какие-нибудь

_ERD, UML, BPMN etc_

## 🚀 Технологии проекта

### Backend

#### Основные Компоненты

- **Spring Boot**: Ядро приложения, быстрый старт и управление.
- **Spring Data JPA**: Упрощает работу с базами данных, используя Java Persistence API.
- **Spring Boot Starter Mail**: Отправка электронной почты из приложения.
- **Springdoc OpenAPI**: Автоматическая документация API.

#### База Данных и Миграции

- **PostgreSQL**: Используется как основная система управления базами данных.
- **Liquibase**: Управляет версиями базы данных, позволяя безопасно вносить изменения.

#### Безопасность

- **Spring Security**: Защита приложения, аутентификация и авторизация. Способна интегрироваться с широким спектром
  аутентификационных методов, включая формы, LDAP, JWT и др. Обеспечивает защиту от распространенных угроз безопасности,
  таких как CSRF (Cross-Site Request Forgery) и сессионные атаки.
- **JSON Web Tokens (JWT)**: Технология, позволяющая безопасно передавать информацию между сторонами в виде
  JSON-объектов.

#### Утилиты и Вспомогательные Библиотеки

- **Jasypt Spring Boot Starter**: Шифрование конфиденциальных данных приложения.
- **ModelMapper**: Преобразование объектов между слоями.
- **Gson & Jackson Databind**: Работа с JSON.
- **Jedis**: Java клиент для Redis.

#### Разработка и Тестирование

- **Lombok**: Уменьшение шаблонного кода.
- **JUnit & Hamcrest**: Модульное тестирование и сложные условия проверки.
- **Testcontainers & Instancio**: Интеграционное тестирование с Docker и создание тестовых данных.

#### Кеширование и Валидация

- **Spring Boot Starter Cache**: Поддержка кеширования в приложении для улучшения производительности.
- **Spring Boot Starter Data Redis**: Интеграция Redis для кеширования.
- **Spring Boot Starter Validation** : Валидация данных в приложении.

Эти технологии обеспечивают **надежность, безопасность и масштабируемость** нашего приложения.

### Frontend

_..._

### QA

_..._

### Design

_..._

## 👥 Наша команда

Наша команда представляет собой прекрасное сочетание профессионалов в области **бэкенда**, **фронтенда**, **качества (
QA)**, **дизайна** и **управления проектами**. Каждый член команды был тщательно подобран с учетом технической
экспертизы,
навыков межличностного общения, а также стремления к развитию и эффективному командному взаимодействию, что обеспечило
комфортную
и дружелюбную атмосферу на всех наших встречах, а также способствовало быстрому и точному решению задач 🚀

Обращаясь к любому из участников команды, вы можете быть уверены,
что это ответственный специалист и отличный командный игрок:

|  Команда   |    Имя     |                 Контакты                 |                 Доп.ссылки                 | 
|:----------:|:----------:|:----------------------------------------:|:------------------------------------------:|
|  Backend   |  Дмитрий   |     [Telegram](https://t.me/Ldv236)      |    [GitHub](https://github.com/Ldv236)     |
|  Backend   |  Алексей   |    [Telegram](https://t.me/DiabluSun)    |    [GitHub](https://github.com/x3imal)     |
|  Backend   | Александра |    [Telegram](https://t.me/fifimova)     |   [GitHub](https://github.com/fifimova)    |
|  Backend   |  Николай   |    [Telegram](https://t.me/VeselovND)    |  [GitHub](https://github.com/veselovnd88)  |
|  Frontend  |  Евгений   |     [Telegram](https://t.me/lepehun)     | [GitHub](https://github.com/PipolaPopala)  |
|  Frontend  |   Софья    |  [Telegram](https://t.me/SofyaIakov)     |  [GitHub](https://github.com/YakovlevaSS)  |
|  Frontend  |   Диана    | [Telegram](https://t.me/@Miiniistrelia)  | [GitHub](https://github.com/DianaSemenova) |
|     QA     |  Алексей   |   [Telegram](https://t.me/@alexfef72)    |                                            |
|     QA     | Александр  | [Telegram](https://t.me/@Aleksandr_9473) |                                            |
| PM, Design |   Сергей   |  [Telegram](https://t.me/@NeonetSergey)  |                                            |
