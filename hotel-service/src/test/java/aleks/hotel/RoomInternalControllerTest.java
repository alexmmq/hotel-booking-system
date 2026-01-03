package aleks.hotel;

import aleks.hotel.domain.HotelEntity;
import aleks.hotel.domain.RoomEntity;
import aleks.hotel.dto.internal.ConfirmAvailabilityRequest;
import aleks.hotel.repo.HotelRepository;
import aleks.hotel.repo.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RoomInternalControllerTest {

    @Autowired MockMvc mvc;
    @Autowired HotelRepository hotelRepo;
    @Autowired RoomRepository roomRepo;

    Long roomId;

    @BeforeEach
    void setup() {
        roomRepo.deleteAll();
        hotelRepo.deleteAll();
        HotelEntity h = hotelRepo.save(new HotelEntity(null, "H1", "Addr"));
        RoomEntity r = roomRepo.save(new RoomEntity(null, h, "101", true, 0));
        roomId = r.getId();
    }

    @Test
    void confirm_ok_then_second_overlaps_conflict() throws Exception {
        var jwt = jwt();

        String body1 = """
      {"requestId":"r1","bookingId":"1","startDate":"2026-01-10","endDate":"2026-01-12"}
    """;

        mvc.perform(post("/internal/rooms/" + roomId + "/confirm-availability")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isOk());

        String body2 = """
      {"requestId":"r2","bookingId":"2","startDate":"2026-01-11","endDate":"2026-01-13"}
    """;

        mvc.perform(post("/internal/rooms/" + roomId + "/confirm-availability")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isConflict());
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("sub", "user1")
                .claim("roles", java.util.List.of("USER"))
                .claims(c -> c.putAll(Map.of("iat", 0)))
                .build();
    }
}

