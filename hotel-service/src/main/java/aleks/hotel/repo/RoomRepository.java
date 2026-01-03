package aleks.hotel.repo;

import aleks.hotel.domain.RoomEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomEntity r where r.id = :id")
    Optional<RoomEntity> lockById(@Param("id") Long id);
}
