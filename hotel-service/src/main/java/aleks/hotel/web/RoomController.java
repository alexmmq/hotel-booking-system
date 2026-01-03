package aleks.hotel.web;

import aleks.hotel.dto.RoomDto;
import aleks.hotel.mapper.RoomMapper;
import aleks.hotel.service.HotelCrudService;
import aleks.hotel.service.RoomAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final HotelCrudService crud;
    private final RoomAvailabilityService availability;
    private final RoomMapper mapper;

    public record CreateRoomReq(Long hotelId, String number, boolean available) {}

    @PostMapping("/api/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public RoomDto createRoom(@RequestBody CreateRoomReq req) {
        return mapper.toDto(crud.createRoom(req.hotelId(), req.number(), req.available()));
    }

    @GetMapping("/api/rooms")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<RoomDto> freeRooms(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return availability.findFreeRooms(startDate, endDate, false).stream().map(mapper::toDto).toList();
    }

    @GetMapping("/api/rooms/recommend")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<RoomDto> recommend(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return availability.findFreeRooms(startDate, endDate, true).stream().map(mapper::toDto).toList();
    }
}

