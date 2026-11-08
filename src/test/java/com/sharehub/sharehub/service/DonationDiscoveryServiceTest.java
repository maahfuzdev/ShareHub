package com.sharehub.sharehub.service;

import com.sharehub.sharehub.entity.Donation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DonationDiscoveryServiceTest {

    @Mock
    private DonationService donationService;

    @Mock
    private AiAssistantService aiAssistantService;

    @InjectMocks
    private DonationDiscoveryService discoveryService;

    @Test
    void usesAiKeywordsAndCategoryToRankRelevantDonations() {
        Donation coat = donation("Warm coat", Donation.Category.CLOTHING, "Good winter coat", "Mirpur");
        Donation rice = donation("Rice", Donation.Category.FOOD, "Unopened rice", "Dhanmondi");
        when(donationService.findAvailable(Donation.Origin.GENERAL)).thenReturn(List.of(rice, coat));
        when(aiAssistantService.isConfigured()).thenReturn(true);
        when(aiAssistantService.interpretSearch("winter jacket")).thenReturn(
                new AiSearchIntent(List.of("coat", "jacket"), Donation.Category.CLOTHING, null)
        );

        DonationDiscoveryService.SearchResult result = discoveryService.search("winter jacket");

        assertEquals(1, result.donations().size());
        assertEquals(coat, result.donations().get(0));
        assertTrue(result.status().contains("AI-assisted"));
    }

    @Test
    void fallsBackToLocalMatchingWhenAiIsNotConfigured() {
        Donation rice = donation("Rice bag", Donation.Category.FOOD, "Unopened pantry staple", "Mirpur");
        Donation book = donation("School books", Donation.Category.BOOKS, "Used textbooks", "Uttara");
        when(donationService.findAvailable(Donation.Origin.GENERAL)).thenReturn(List.of(book, rice));
        when(aiAssistantService.isConfigured()).thenReturn(false);

        DonationDiscoveryService.SearchResult result = discoveryService.search("rice Mirpur");

        assertEquals(List.of(rice), result.donations());
        assertTrue(result.status().contains("not configured"));
    }

    @Test
    void showsLocalMatchesWhenAiSearchFails() {
        Donation rice = donation("Rice bag", Donation.Category.FOOD, "Unopened rice", "Mirpur");
        when(donationService.findAvailable(Donation.Origin.GENERAL)).thenReturn(List.of(rice));
        when(aiAssistantService.isConfigured()).thenReturn(true);
        when(aiAssistantService.interpretSearch("rice")).thenThrow(
                new AiServiceException("Gemini is temporarily unavailable.")
        );

        DonationDiscoveryService.SearchResult result = discoveryService.search("rice");

        assertEquals(List.of(rice), result.donations());
        assertTrue(result.status().contains("temporarily unavailable"));
    }

    @Test
    void rejectsQueriesLongerThanTheSupportedLimit() {
        assertThrows(IllegalArgumentException.class, () -> discoveryService.search("x".repeat(241)));
    }

    @Test
    void filtersSearchesByRequestedDonationOrigin() {
        when(donationService.findAvailable(Donation.Origin.DASHBOARD)).thenReturn(List.of());

        discoveryService.search("", Donation.Origin.DASHBOARD);

        verify(donationService).findAvailable(Donation.Origin.DASHBOARD);
    }

    private Donation donation(String title, Donation.Category category, String description, String location) {
        Donation donation = new Donation();
        donation.setTitle(title);
        donation.setCategory(category);
        donation.setDescription(description);
        donation.setLocation(location);
        donation.setQuantity("1 item");
        donation.setCreatedAt(LocalDateTime.now());
        return donation;
    }
}
