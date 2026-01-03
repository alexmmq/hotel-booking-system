package aleks.booking.mapper;

import aleks.booking.domain.BookingEntity;
import aleks.booking.dto.BookingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "roomId", source = "roomId")
    BookingDto toDto(BookingEntity e);
}

