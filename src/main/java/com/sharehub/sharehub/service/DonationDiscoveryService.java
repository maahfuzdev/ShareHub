package com.sharehub.sharehub.service;

import com.sharehub.sharehub.entity.Donation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationDiscoveryService {

    private static final int MAX_QUERY_LENGTH = 240;
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "available", "can", "could", "for", "find", "give", "have",
            "i", "in", "is", "it", "me", "near", "of", "please", "some", "the", "to", "want",
            "with", "looking", "need"
    );

    private final DonationService donationService;
    private final AiAssistantService aiAssistantService;

    public SearchResult search(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim();
        if (query.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException("Search must be 240 characters or fewer.");
        }

        List<Donation> available = donationService.findAvailable();
        if (query.isBlank()) {
            return new SearchResult(available, available.size(), query, null);
        }

        AiSearchIntent intent = null;
        String status = null;
        if (aiAssistantService.isConfigured()) {
            try {
                intent = aiAssistantService.interpretSearch(query);
                status = "AI-assisted matching is on. Only your search text was sent to Gemini.";
            } catch (AiServiceException exception) {
                log.warn("AI search interpretation failed; using local keyword matching");
                status = "AI search is temporarily unavailable; showing local keyword matches.";
            }
        } else {
            status = "AI search is not configured; showing local keyword matches.";
        }

        List<String> searchTerms = extractTerms(query);
        if (intent != null) {
            for (String keyword : intent.keywords()) {
                searchTerms.addAll(extractTerms(keyword));
            }
        }
        Set<String> terms = new LinkedHashSet<>(searchTerms);

        AiSearchIntent searchIntent = intent;
        List<Donation> matches = available.stream()
                .map(donation -> new ScoredDonation(donation, score(donation, query, terms, searchIntent)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt(ScoredDonation::score).reversed()
                        .thenComparing(scored -> scored.donation().getCreatedAt(),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ScoredDonation::donation)
                .toList();

        return new SearchResult(matches, available.size(), query, status);
    }

    private int score(Donation donation, String query, Set<String> terms, AiSearchIntent intent) {
        String title = normalize(donation.getTitle());
        String description = normalize(donation.getDescription());
        String location = normalize(donation.getLocation());
        String category = donation.getCategory().name().toLowerCase(Locale.ROOT);
        String quantity = normalize(donation.getQuantity());
        String normalizedQuery = normalize(query);

        int score = 0;
        if (!normalizedQuery.isBlank() && title.contains(normalizedQuery)) {
            score += 12;
        }
        for (String term : terms) {
            if (title.contains(term)) score += 6;
            if (category.contains(term)) score += 4;
            if (location.contains(term)) score += 4;
            if (description.contains(term)) score += 2;
            if (quantity.contains(term)) score += 1;
        }
        if (intent != null && intent.category() == donation.getCategory()) {
            score += 8;
        }
        if (intent != null && intent.location() != null
                && location.contains(normalize(intent.location()))) {
            score += 8;
        }
        return score;
    }

    private List<String> extractTerms(String value) {
        String normalized = normalize(value);
        List<String> terms = new ArrayList<>();
        for (String term : normalized.split("\\s+")) {
            if (!term.isBlank() && !STOP_WORDS.contains(term)) {
                terms.add(term);
            }
        }
        return terms;
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim();
    }

    private record ScoredDonation(Donation donation, int score) {
    }

    public record SearchResult(List<Donation> donations, int totalAvailable, String query, String status) {
    }
}
