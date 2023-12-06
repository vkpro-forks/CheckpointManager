package ru.ac.checkpointmanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.projection.PassInOutViewProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository class for <code>Pass</code> domain objects
 *
 * @author Dmitry Ldv236
 */
@Repository
public interface PassRepository extends JpaRepository<Pass, UUID> {

    /**
     * Фрагмент SQL, определяющий логику сортировки списка пропусков.
     * Эта логика сортирует сущности в первую очередь на основе их статуса в определённом порядке:
     * WARNING, ACTIVE, DELAYED, за которыми следуют все остальные статусы.
     * При одинаковых статусах сортируются по времени начала действия в порядке убывания
     */
    String SORT_LOGIC = "ORDER BY CASE " +
            "p.status WHEN 'WARNING' THEN 1 WHEN 'ACTIVE' THEN 2 WHEN 'DELAYED' THEN 3 ELSE 4 END, " +
            "p.start_time DESC";

    /**
     * Получает страницу объектов Pass, отсортированных по заданной логике.
     * @param pageable объект {@link Pageable}, содержащий информацию о пагинации
     * @return {@link Page} объектов Pass, отсортированных в соответствии с заданной логикой
     */
    @Query(value = "SELECT * FROM passes p " + SORT_LOGIC, nativeQuery = true)
    Page<Pass> findAll(Pageable pageable);

    /**
     * Получает страницу объектов Pass для конкретного пользователя без сортировки и пагинации
     * для проверки перекрытия пропусков при добавлении нового пропуска.
     * @param userId запрашиваемый пользователь
     * @return список найденных пропусков
     */
    List<Pass> findAllPassesByUserId(UUID userId);

    /**
     * Получает страницу объектов Pass для конкретного пользователя, отсортированных по заданной логике
     * @param userId UUID пользователя.
     * @param pageable объект {@link Pageable}, содержащий информацию о пагинации
     * @return {@link Page} объектов Pass, связанных с указанным пользователем
     * и отсортированных в соответствии с заданной логикой
     */
    @Query(value = "SELECT * FROM passes p WHERE p.user_id = :userId " + SORT_LOGIC, nativeQuery = true)
    Page<Pass> findPassesByUserId(UUID userId, Pageable pageable);

    /**
     * Получает страницу объектов Pass для конкретной территории, отсортированных по заданной логике.
     * @param territoryId UUID территории.
     * @param pageable объект {@link Pageable}, содержащий информацию о пагинации
     * @return {@link Page} объектов Pass, связанных с указанной территорией
     * и отсортированных в соответствии с заданной логикой
     */
    @Query(value = "SELECT * FROM passes p WHERE p.territory_id = :territoryId " + SORT_LOGIC, nativeQuery = true)
    Page<Pass> findPassesByTerritoryId(UUID territoryId, Pageable pageable);

    /**
     * Ищет пропуски по статусу и достигнутому времени начала или окончания.
     * @param status Предполагается передача значения PassStatus.?.toString().
     * @param timeColumn строковое значение имени столбца для сравнения времени.
     * @param time дата и время для сравнения со столбцом timeColumn.
     * @return список найденных пропусков.
     */
    @Query(value = "SELECT * FROM passes WHERE status = :status AND " +
            "CASE WHEN :column = 'startTime' THEN start_time " +
            "WHEN :column = 'endTime' THEN end_time " +
            "END < :time" , nativeQuery = true)
    List<Pass> findPassesByStatusAndTimeBefore(@Param("status") String status
            , @Param("column") String timeColumn, @Param("time") LocalDateTime time);

    /**
     * Проверяет связь между пользователем и территориями
     * (разрешение пользователя на создание пропуска на эту территорию).
     * @param userId ID проверяемого пользователя
     * @param territoryId ID проверяемой территории
     * @return boolean результат проверки
     */
    @Query(value = "SELECT EXISTS (SELECT FROM user_territory WHERE user_id = :uId AND territory_id = :tId)"
            , nativeQuery = true)
    boolean checkUserTerritoryRelation(@Param("uId") UUID userId, @Param("tId") UUID territoryId);

    @Query(value = "SELECT * FROM pass_in_out_view p WHERE p.user_id = :userId ", nativeQuery = true)
    Page<PassInOutViewProjection> findEventsByUser(UUID userId, Pageable pageable);

    @Query(value = "SELECT * FROM pass_in_out_view p WHERE p.territory_id = :terId ", nativeQuery = true)
    Page<PassInOutViewProjection> findEventsByTerritory(UUID terId, Pageable pageable);
}