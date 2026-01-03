package aleks.hotel.mapper;


import aleks.hotel.domain.HotelEntity;
import aleks.hotel.dto.HotelDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HotelMapper {
    HotelDto toDto(HotelEntity e);
}

