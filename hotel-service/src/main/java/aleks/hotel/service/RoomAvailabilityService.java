package aleks.hotel.service;

import aleks.hotel.domain.RoomHoldEntity;
import aleks.hotel.domain.enums.HoldStatus;
import aleks.hotel.dto.internal.ConfirmAvailabilityRequest;
import aleks.hotel.dto.internal.ConfirmAvailabilityResponse;
import aleks.hotel.dto.internal.ReleaseRequest;
import aleks.hotel.repo.RoomHoldRepository;
import aleks.hotel.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {

    private final RoomRepository roomRepo;
    private final RoomHoldRepository holdRepo;

    @Transactional
    public ConfirmAvailabilityResponse confirmAvailability(Long roomId, ConfirmAvailabilityRequest req) {
        // идемпотентность
        var existing = holdRepo.findByRequestId(req.requestId());
        if (existing.isPresent()) {
            var h = existing.get();
            return (h.getStatus() == HoldStatus.HOLD)
                    ? new ConfirmAvailabilityResponse(true, "ALREADY_HELD")
                    : new ConfirmAvailabilityResponse(false, "ALREADY_RELEASED");
        }

        var room = roomRepo.lockById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND"));

        if (!room.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ROOM_DISABLED");
        }

        long overlaps = holdRepo.countOverlaps(roomId, req.startDate(), req.endDate());
        if (overlaps > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ROOM_BUSY");
        }

        var hold = new RoomHoldEntity();
        hold.setRoomId(roomId);
        hold.setStartDate(req.startDate());
        hold.setEndDate(req.endDate());
        hold.setBookingId(req.bookingId());
        hold.setRequestId(req.requestId());
        hold.setStatus(HoldStatus.HOLD);
        hold.setExpiresAt(Instant.now().plusSeconds(90)); // пример TTL
        holdRepo.save(hold);

        return new ConfirmAvailabilityResponse(true, "HELD");
    }

    @Transactional
    public void release(Long roomId, ReleaseRequest req) {
        var holdOpt = holdRepo.findByRequestId(req.requestId());
        if (holdOpt.isEmpty()) return;

        var hold = holdOpt.get();
        if (hold.getStatus() == HoldStatus.RELEASED) return;

        hold.setStatus(HoldStatus.RELEASED);
    }

    public List<aleks.hotel.domain.RoomEntity> findFreeRooms(LocalDate start, LocalDate end, boolean recommend) {
        var all = roomRepo.findAll().stream()
                .filter(r -> r.isAvailable())
                .filter(r -> holdRepo.countOverlaps(r.getId(), start, end) == 0)
                .toList();

        if (!recommend) return all;

        return all.stream()
                .sorted(Comparator.comparingLong(aleks.hotel.domain.RoomEntity::getTimesBooked)
                        .thenComparingLong(aleks.hotel.domain.RoomEntity::getId))
                .toList();
    }
}

