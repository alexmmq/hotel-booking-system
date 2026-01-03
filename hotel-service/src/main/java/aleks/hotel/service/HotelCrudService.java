package aleks.hotel.service;

import aleks.hotel.domain.HotelEntity;
import aleks.hotel.domain.RoomEntity;
import aleks.hotel.repo.HotelRepository;
import aleks.hotel.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelCrudService {

    private final HotelRepository hotelRepo;
    private final RoomRepository roomRepo;

    public List<HotelEntity> listHotels() {
        return hotelRepo.findAll();
    }

    @Transactional
    public HotelEntity createHotel(String name, String address) {
        return hotelRepo.save(new HotelEntity(null, name, address));
    }

    @Transactional
    public RoomEntity createRoom(Long hotelId, String number, boolean available) {
        var hotel = hotelRepo.findById(hotelId).orElseThrow();
        var room = new RoomEntity(null, hotel, number, available, 0);
        return roomRepo.save(room);
    }
}

