package aleks.booking.service;

import aleks.booking.domain.BookingEntity;
import aleks.booking.domain.enums.BookingStatus;
import aleks.booking.dto.*;
import aleks.booking.dto.internal.ConfirmAvailabilityRequest;
import aleks.booking.dto.internal.ReleaseRequest;
import aleks.booking.integration.HotelClient;
import aleks.booking.mapper.BookingMapper;
import aleks.booking.repo.BookingRepository;
import aleks.booking.repo.UserRepository;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class BookingFacade {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;
    private final HotelClient hotelClient;
    private final BookingMapper mapper;

    private final Retry hotelConfirmRetry = Retry.ofDefaults("hotelConfirm");
    private final TimeLimiter hotelConfirmLimiter = TimeLimiter.ofDefaults("hotelConfirm");

    private static final ExecutorService EXEC = Executors.newCachedThreadPool();

    public BookingDto createBooking(CreateBookingRequest req, String username) {
        BookingEntity booking = createPendingIfNeeded(req, username);

        if (booking.getStatus() != BookingStatus.PENDING) {
            return mapper.toDto(booking);
        }

        String bearer = currentBearerHeader();
        String bookingId = String.valueOf(booking.getId());

        MDC.put("bookingId", bookingId);
        MDC.put("requestId", req.requestId());

        var confirmReq = new ConfirmAvailabilityRequest(req.requestId(), bookingId, booking.getStartDate(), booking.getEndDate());

        try {
            // timeout + retry
            Callable<Void> remoteCall = () -> {
                hotelClient.confirm(booking.getRoomId(), confirmReq, bearer);
                return null;
            };

            Callable<Object> decorated =
                    Retry.decorateCallable(hotelConfirmRetry,
                            () -> TimeLimiter.decorateFutureSupplier(hotelConfirmLimiter,
                                    () -> CompletableFuture.supplyAsync(() -> {
                                        try {
                                            remoteCall.call();
                                            return null;
                                        } catch (Exception e) {
                                            throw new CompletionException(e);
                                        }
                                    }, EXEC)
                            ).call()
                    );

            decorated.call();

            // локально CONFIRMED
            return confirmLocally(booking.getId());

        } catch (Exception ex) {
            // локально CANCELLED
            cancelLocally(booking.getId());

            // компенсация release (идемпотентно, best-effort)
            try {
                hotelClient.release(booking.getRoomId(), new ReleaseRequest(req.requestId(), bookingId), bearer);
            } catch (Exception ignored) {
                // best-effort
            }

            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Hotel confirm failed, booking cancelled");
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public BookingEntity createPendingIfNeeded(CreateBookingRequest req, String username) {
        var existing = bookingRepo.findByRequestId(req.requestId());
        if (existing.isPresent()) return existing.get();

        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        // autoSelect упрощён: если autoSelect=true — считаем что roomId обязателен только для демо
        if (req.roomId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roomId required in this skeleton");
        }

        var b = new BookingEntity();
        b.setUser(user);
        b.setRoomId(req.roomId());
        b.setStartDate(req.startDate());
        b.setEndDate(req.endDate());
        b.setStatus(BookingStatus.PENDING);
        b.setCreatedAt(Instant.now());
        b.setRequestId(req.requestId());
        return bookingRepo.save(b);
    }

    @Transactional
    public BookingDto confirmLocally(Long id) {
        var b = bookingRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (b.getStatus() == BookingStatus.CONFIRMED) return mapper.toDto(b);
        if (b.getStatus() == BookingStatus.CANCELLED) return mapper.toDto(b);
        b.setStatus(BookingStatus.CONFIRMED);
        return mapper.toDto(b);
    }

    @Transactional
    public BookingDto cancelLocally(Long id) {
        var b = bookingRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (b.getStatus() == BookingStatus.CANCELLED) return mapper.toDto(b);
        b.setStatus(BookingStatus.CANCELLED);
        return mapper.toDto(b);
    }

    public java.util.List<BookingDto> myBookings(String username) {
        var user = userRepo.findByUsername(username).orElseThrow();
        return bookingRepo.findAllByUserOrderByCreatedAtDesc(user).stream().map(mapper::toDto).toList();
    }

    public BookingDto getById(Long id, String username) {
        var b = bookingRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!b.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return mapper.toDto(b);
    }

    @Transactional
    public BookingDto cancelByUser(Long id, String username) {
        var b = bookingRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!b.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (b.getStatus() == BookingStatus.CANCELLED) return mapper.toDto(b);
        b.setStatus(BookingStatus.CANCELLED);
        return mapper.toDto(b);
    }

    private String currentBearerHeader() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return "Bearer " + "DUMMY";
    }
}

