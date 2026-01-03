package aleks.hotel.mapper;

import aleks.hotel.domain.RoomEntity;
import aleks.hotel.dto.RoomDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(target = "hotelId", source = "hotel.id")
    RoomDto toDto(RoomEntity e);
}

