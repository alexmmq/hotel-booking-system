package aleks.hotel.repo;

import aleks.hotel.domain.RoomHoldEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface RoomHoldRepository extends JpaRepository<RoomHoldEntity, Long> {
    Optional<RoomHoldEntity> findByRequestId(String requestId);

    @Query("""
    select count(h) from RoomHoldEntity h
    where h.roomId = :roomId
      and h.status = aleks.hotel.domain.enums.HoldStatus.HOLD
      and h.startDate < :endDate
      and :startDate < h.endDate
  """)
    long countOverlaps(@Param("roomId") Long roomId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate);
}

