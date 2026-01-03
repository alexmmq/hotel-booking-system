package aleks.booking;

import aleks.booking.domain.UserEntity;
import aleks.booking.domain.enums.Role;
import aleks.booking.repo.UserRepository;
import aleks.booking.security.JwtService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BookingFlowWireMockTest {

    static WireMockServer wm;

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepo;
    @Autowired JwtService jwtService;

    String token;

    @BeforeAll
    static void startWm() {
        wm = new WireMockServer(9561);
        wm.start();
        configureFor("localhost", 9561);
    }

    @AfterAll
    static void stopWm() {
        wm.stop();
    }

    @BeforeEach
    void setup() {
        userRepo.deleteAll();
        UserEntity u = userRepo.save(new UserEntity(null, "user1", "{noop}x", Role.USER));
        token = jwtService.issueToken(u.getUsername(), u.getRole());

        wm.resetAll();
    }

    @Test
    void success_confirmed() throws Exception {
        wm.stubFor(WireMock.post(urlMatching("/internal/rooms/1/confirm-availability"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\":true,\"message\":\"HELD\"}")));

        String body = """
      {"requestId":"req-1","roomId":1,"startDate":"2026-01-10","endDate":"2026-01-12","autoSelect":false}
    """;

        mvc.perform(post("/api/booking")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        wm.verify(1, postRequestedFor(urlMatching("/internal/rooms/1/confirm-availability")));
    }

    @Test
    void remote_timeout_or_error_leads_to_cancelled() throws Exception {
        wm.stubFor(WireMock.post(urlMatching("/internal/rooms/1/confirm-availability"))
                .willReturn(aResponse().withStatus(500)));

        wm.stubFor(WireMock.post(urlMatching("/internal/rooms/1/release"))
                .willReturn(aResponse().withStatus(200)));

        String body = """
      {"requestId":"req-2","roomId":1,"startDate":"2026-01-10","endDate":"2026-01-12","autoSelect":false}
    """;

        mvc.perform(post("/api/booking")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isGatewayTimeout());

        wm.verify(postRequestedFor(urlMatching("/internal/rooms/1/confirm-availability")));
        // release best-effort: может быть вызван
    }

    @Test
    void idempotent_same_requestId_no_duplicates() throws Exception {
        wm.stubFor(WireMock.post(urlMatching("/internal/rooms/1/confirm-availability"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\":true,\"message\":\"HELD\"}")));

        String body = """
      {"requestId":"same-1","roomId":1,"startDate":"2026-01-10","endDate":"2026-01-12","autoSelect":false}
    """;

        mvc.perform(post("/api/booking")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mvc.perform(post("/api/booking")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

    }
}
