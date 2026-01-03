package aleks.hotel.web.internal;

import aleks.hotel.dto.internal.ConfirmAvailabilityRequest;
import aleks.hotel.dto.internal.ConfirmAvailabilityResponse;
import aleks.hotel.dto.internal.ReleaseRequest;
import aleks.hotel.service.RoomAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/rooms")
public class RoomInternalController {

    private final RoomAvailabilityService service;

    @PostMapping("/{id}/confirm-availability")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // учебно: вызываем с user JWT
    public ConfirmAvailabilityResponse confirm(@PathVariable Long id, @RequestBody ConfirmAvailabilityRequest req) {
        return service.confirmAvailability(id, req);
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public void release(@PathVariable Long id, @RequestBody ReleaseRequest req) {
        service.release(id, req);
    }
}

