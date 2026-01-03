package aleks.hotel.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "rooms")
public class RoomEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private HotelEntity hotel;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private long timesBooked = 0;
}

