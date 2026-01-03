package aleks.booking.dto.internal;

import java.time.LocalDate;

public record ConfirmAvailabilityRequest(
        String requestId,
        String bookingId,
        LocalDate startDate,
        LocalDate endDate
) {}

