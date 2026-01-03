package aleks.booking.web;

import aleks.booking.dto.BookingDto;
import aleks.booking.dto.CreateBookingRequest;
import aleks.booking.service.BookingFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingFacade facade;

    @PostMapping("/api/booking")
    @PreAuthorize("hasRole('USER')")
    public BookingDto create(@RequestHeader("Authorization") String bearer,
                             @RequestBody @Valid CreateBookingRequest req,
                             Authentication auth) {
        // В этом skeleton фасад берёт bearer из контролера - через ThreadLocal было бы плохо.
        // Поэтому — самый простой вариант: временно прокидываем bearer в HotelClient напрямую:
        // (для краткости — используем SecurityContext в hotel-service, так как jwt будет валиден)
        return facade.createBooking(req, auth.getName());
    }

    @GetMapping("/api/bookings")
    @PreAuthorize("hasRole('USER')")
    public List<BookingDto> myBookings(Authentication auth) {
        return facade.myBookings(auth.getName());
    }

    @GetMapping("/api/booking/{id}")
    @PreAuthorize("hasRole('USER')")
    public BookingDto get(@PathVariable Long id, Authentication auth) {
        return facade.getById(id, auth.getName());
    }

    @DeleteMapping("/api/booking/{id}")
    @PreAuthorize("hasRole('USER')")
    public BookingDto cancel(@PathVariable Long id, Authentication auth) {
        return facade.cancelByUser(id, auth.getName());
    }
}

