package com.sharehub.sharehub.service;

import com.sharehub.sharehub.dto.response.AiDonationSuggestionResponse;

public interface AiAssistantService {

    boolean isConfigured();

    AiDonationSuggestionResponse suggestDonation(String notes);

    AiSearchIntent interpretSearch(String query);
}
