package aleks.booking.repo;

import aleks.booking.domain.BookingEntity;
import aleks.booking.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    Optional<BookingEntity> findByRequestId(String requestId);
    List<BookingEntity> findAllByUserOrderByCreatedAtDesc(UserEntity user);
}

