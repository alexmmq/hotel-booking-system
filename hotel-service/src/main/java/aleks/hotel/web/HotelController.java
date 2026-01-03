package aleks.hotel.web;

import aleks.hotel.dto.HotelDto;
import aleks.hotel.mapper.HotelMapper;
import aleks.hotel.service.HotelCrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelCrudService crud;
    private final HotelMapper mapper;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<HotelDto> list() {
        return crud.listHotels().stream().map(mapper::toDto).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public HotelDto create(@RequestBody HotelDto req) {
        var created = crud.createHotel(req.name(), req.address());
        return mapper.toDto(created);
    }
}

