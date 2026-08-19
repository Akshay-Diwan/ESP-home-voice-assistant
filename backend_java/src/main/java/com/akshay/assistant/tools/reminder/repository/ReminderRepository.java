package com.akshay.assistant.tools.reminder.repository;
import java.util.UUID;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.akshay.assistant.tools.reminder.models.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.akshay.assistant.tools.reminder.entity.ReminderEntity;
@Repository
public interface ReminderRepository extends JpaRepository<ReminderEntity, UUID> {
    // This interface will handle the database operations for reminders
    @Query("""
    SELECT r
    FROM ReminderEntity r
    LEFT JOIN FETCH r.repeat_days
    WHERE r.id = :id
    """)
    Optional<ReminderEntity> findByIdWithRepeatDays(UUID id);

    @Query(
        """
        SELECT r
        FROM ReminderEntity r
        LEFT JOIN FETCH r.repeat_days     
        """
    )
    List<ReminderEntity> findAllWithRepeatDays();
    List<ReminderEntity> findByStatusIn(Collection<Status> statuses);

}