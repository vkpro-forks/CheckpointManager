package ru.ac.checkpointmanager.utils;

public class SwaggerConstants {

    //константы конфигурации OpenApiConfig
    public static final String SWAGGER_DESCRIPTION_MESSAGE = "Аккаунты по умолчанию: security@chp.com, user@chp.com, " +
            "admin@chp.com, manager@chp.com. Пароли те же.";

    //стандартные ошибки
    public static final String INTERNAL_SERVER_ERROR_MSG = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса";
    public static final String UNAUTHORIZED_MSG = "UNAUTHORIZED: пользователь не авторизован";
    public static final String FAILED_FIELD_VALIDATION_MESSAGE = "Неуспешная валидация полей.";
    public static final String BAD_REQUEST_MESSAGE = "BAD_REQUEST: Неверные данные запроса";

    //статусы доступа
    public static final String ACCESS_ADMIN_MESSAGE = "Доступ: ADMIN.";
    public static final String ACCESS_ALL_ROLES_MESSAGE = "Доступ: ADMIN, MANAGER, SECURITY, USER.";

    //для AvatarController
    public static final String AVATAR_NOT_FOUND_MESSAGE = "NOT_FOUND: Аватар не найден";

    //для CheckpointController
    public static final String KPP_NOT_FOUND_SINGULAR = "КПП не найден";
    public static final String KPP_NOT_FOUND_PLURAL = "КПП не найдены";

    //для CarBrandController
    public static final String BRAND_PROCESSING_MESSAGE = "Для обработки Брендов Авто";
    public static final String BRAND_NOT_EXIST_MESSAGE = "Такой бренд машины не существует.";
    public static final String ADD_CAR_BRAND_MESSAGE = "Добавить новый Бренд Машины";
    public static final String CAR_BRAND_ADDED_SUCCESS_MESSAGE = "Бренд Машины успешно добавлен";
    public static final String GET_CAR_BRAND_BY_ID_MESSAGE = "Получение Бренд Машины по id";
    public static final String CAR_BRAND_RECEIVED_MESSAGE = "Бренд Машины получен.";
    public static final String DELETE_CAR_BRAND_MESSAGE = "Удалить Бренд Машины";
    public static final String CAR_BRAND_DELETED_SUCCESS_MESSAGE = "Бренд Машины успешно удален";
    public static final String UPDATE_CAR_BRAND_MESSAGE = "Обновить новый Бренд Машины";
    public static final String CAR_BRAND_UPDATED_SUCCESS_MESSAGE = "Бренд Машины успешно обновлен";
    public static final String GET_ALL_CAR_BRANDS_MESSAGE = "Получение всех Бренд Машины.";
    public static final String CAR_BRANDS_LIST_RECEIVED_MESSAGE = "Список бренд машины получен";
    public static final String GET_CAR_BRAND_BY_NAME_PART_MESSAGE = "Получение Бренд Машины по части имени.";

    //для CarController
    public static final String CAR_NOT_EXIST_MESSAGE = "Такой машины не существует.";
    public static final String CAR_DELETED_SUCCESS_MESSAGE = "Машина успешно удалена.";
    public static final String CAR_UPDATED_SUCCESS_MESSAGE = "Машина успешно обновлена.";
    public static final String CAR_LIST_RECEIVED_MESSAGE = "Список машин получен.";
    public static final String ADD_CAR_MESSAGE = "Добавить новую машину";
    public static final String CAR_ADDED_SUCCESS_MESSAGE = "Машина успешно добавлена";
    public static final String UPDATE_CAR_MESSAGE = "Обновить новую машину";
    public static final String DELETE_CAR_MESSAGE = "Удалить машину";
    public static final String GET_ALL_CARS_MESSAGE = "Получение всех машин.";
    public static final String GET_CARS_BY_USER_MESSAGE = "Получение всех машин по user.";
    public static final String FIND_CAR_BY_PHONE_NUMBER_MESSAGE = "Найти машину по номеру телефона.";
    public static final String CAR_FOUND_SUCCESS_MESSAGE =
            "Машина успешно найдена. Может вернуть пустой список если машины не найдены.";
    public static final String ACCESS_ADMIN_USER_MESSAGE =
            "Доступ: ADMIN - все машины, USER - только машины, которые фигурируют в пропусках пользователя";
    public static final String ACCESS_ADMIN_MANAGER_SECURITY_USER_MESSAGE =
            "Доступ: ADMIN - любые машины, MANAGER, SECURITY - только машины, относящиеся к их территории, " +
                    "USER - только машины, которые фигурируют в пропусках данного пользователя";

    //для CrossingEventController
    public static final String EVENTS_ARE_FOUND_MESSAGE = "События найдены";

    //для PassController
    public static final String PASSES_ARE_FOUND_MESSAGE = "Пропуска найдены";
    public static final String PASS_NOT_FOUND_MESSAGE = "Пропуск не найден";
    public static final String PASS_ACCESS_ALL_MESSAGE =
            "Доступ: ADMIN - пропуски всех пользователей, MANAGER, SECURITY, USER - только свои";
}
