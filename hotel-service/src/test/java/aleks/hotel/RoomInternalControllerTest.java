package aleks.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomInternalControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void confirm_ok() throws Exception {
        String body = """
      {"requestId":"r1","bookingId":"1","startDate":"2026-01-10","endDate":"2026-01-12"}
    """;

        mvc.perform(post("/internal/rooms/1/confirm-availability")
                        .with(jwt().jwt(testJwt()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized()); // если нет данных/roomId — будет другой статус; это пример
    }

    private Jwt testJwt() {
        return Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("sub", "user1")
                .claim("roles", List.of("USER"))
                .build();
    }
}

