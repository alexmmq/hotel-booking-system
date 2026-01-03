package aleks.booking.dto;

import aleks.booking.domain.enums.BookingStatus;

import java.time.Instant;
import java.time.LocalDate;

public record BookingDto(
        Long id,
        Long roomId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status,
        Instant createdAt,
        String requestId
) {}

