package com.sharehub.sharehub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharehub.sharehub.config.AiProperties;
import com.sharehub.sharehub.entity.Donation;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class GeminiAiAssistantServiceTest {

    @Test
    void createsDonationSuggestionFromGeminiJsonResponse() {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions";
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiProperties properties = new AiProperties();
        properties.setApiKey("test-key");
        properties.setEndpoint(endpoint);
        properties.setModel("gemini-3.8-flash");

        server.expect(requestTo(endpoint))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"{\\"title\\":\\"Fresh produce\\",\\"description\\":\\"A box of vegetables\\",\\"category\\":\\"FOOD\\",\\"missingDetails\\":[\\"Quantity\\",\\"Pickup area\\"]}"}}]}
                        """, MediaType.APPLICATION_JSON));

        GeminiAiAssistantService service = new GeminiAiAssistantService(builder.build(), properties, new ObjectMapper());
        var result = service.suggestDonation("A box of vegetables");

        assertEquals("Fresh produce", result.title());
        assertEquals(Donation.Category.FOOD, result.category());
        assertEquals(2, result.missingDetails().size());
        server.verify();
    }
}
