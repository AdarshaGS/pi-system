package com.pisystem.infrastructure.migration.repository;

import com.pisystem.infrastructure.migration.domain.MigrationHistory;
import com.pisystem.infrastructure.migration.domain.MigrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MigrationHistoryRepository extends JpaRepository<MigrationHistory, Long> {

    Optional<MigrationHistory> findByVersion(String version);

    List<MigrationHistory> findByModuleOrderByExecutionOrderAsc(String module);

    List<MigrationHistory> findByStatusOrderByExecutionOrderAsc(MigrationStatus status);

    @Query("SELECT m FROM MigrationHistory m WHERE m.status = 'SUCCESS' ORDER BY m.executionOrder ASC")
    List<MigrationHistory> findAllSuccessfulMigrations();

    @Query("SELECT m FROM MigrationHistory m WHERE m.status = 'FAILED' ORDER BY m.executedAt DESC")
    List<MigrationHistory> findAllFailedMigrations();

    @Query("SELECT m FROM MigrationHistory m WHERE m.isRepeatable = TRUE ORDER BY m.executedAt DESC")
    List<MigrationHistory> findAllRepeatableMigrations();

    @Query("SELECT m FROM MigrationHistory m WHERE m.executedAt >= :since ORDER BY m.executedAt DESC")
    List<MigrationHistory> findRecentMigrations(LocalDateTime since);

    @Query("SELECT COUNT(m) FROM MigrationHistory m WHERE m.status = :status")
    Long countByStatus(MigrationStatus status);

    @Query("SELECT m.module, COUNT(m) FROM MigrationHistory m WHERE m.status = 'SUCCESS' GROUP BY m.module")
    List<Object[]> countSuccessfulMigrationsByModule();

    boolean existsByVersionAndChecksum(String version, String checksum);

    @Query("SELECT MAX(m.executionOrder) FROM MigrationHistory m")
    Integer findMaxExecutionOrder();
}
