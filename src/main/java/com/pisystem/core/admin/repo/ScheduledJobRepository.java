package com.pisystem.core.admin.repo;

import com.pisystem.core.admin.data.ScheduledJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {
    Optional<ScheduledJob> findByJobName(String jobName);
}
