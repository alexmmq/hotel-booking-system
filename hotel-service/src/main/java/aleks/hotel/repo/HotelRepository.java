package aleks.hotel.repo;

import aleks.hotel.domain.HotelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<HotelEntity, Long> {}

