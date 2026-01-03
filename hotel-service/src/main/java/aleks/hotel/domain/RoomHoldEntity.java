package aleks.hotel.domain;

import aleks.hotel.domain.enums.HoldStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(
        name = "room_holds",
        uniqueConstraints = @UniqueConstraint(name = "uk_hold_request", columnNames = "requestId")
)
public class RoomHoldEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String bookingId;

    @Column(nullable = false)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HoldStatus status;

    @Column(nullable = false)
    private Instant expiresAt;
}

