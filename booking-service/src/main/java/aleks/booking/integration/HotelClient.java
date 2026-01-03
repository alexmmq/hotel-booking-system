package aleks.booking.integration;

import aleks.booking.dto.internal.ConfirmAvailabilityRequest;
import aleks.booking.dto.internal.ConfirmAvailabilityResponse;
import aleks.booking.dto.internal.ReleaseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class HotelClient {

    private final RestClient.Builder plainRestClientBuilder;
    private final RestClient.Builder lbRestClientBuilder;

    @Value("${hotel.base-url}")
    private String baseUrl;

    private RestClient client() {
        // если baseUrl = http://hotel-service => используем LB builder (Eureka)
        if (baseUrl.startsWith("http://hotel-service")) {
            return lbRestClientBuilder.baseUrl(baseUrl).build();
        }
        // тест/локал: localhost => plain
        return plainRestClientBuilder.baseUrl(baseUrl).build();
    }

    public ConfirmAvailabilityResponse confirm(Long roomId, ConfirmAvailabilityRequest req, String bearer) {
        return client().post()
                .uri("/internal/rooms/{id}/confirm-availability", roomId)
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .body(req)
                .retrieve()
                .body(ConfirmAvailabilityResponse.class);
    }

    public void release(Long roomId, ReleaseRequest req, String bearer) {
        client().post()
                .uri("/internal/rooms/{id}/release", roomId)
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .body(req)
                .retrieve()
                .toBodilessEntity();
    }
}

