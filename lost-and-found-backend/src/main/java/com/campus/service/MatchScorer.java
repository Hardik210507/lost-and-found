package com.campus.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class MatchScorer {

    public static final int W_CATEGORY = 40;
    public static final int W_LOCATION = 20;
    public static final int W_KEYWORDS = 25;
    public static final int W_IMAGE = 15;
    public static final int SAVE_THRESHOLD = 50;
    public static final int IMAGE_CUTOFF = 20;

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "with", "this", "that", "near", "found", "lost", "for", "from",
            "has", "have", "was", "were", "are", "its", "not", "but", "can", "did",
            "you", "your", "some", "any", "under", "inside", "outside", "left", "looks",
            "look", "there", "here", "been", "into", "onto", "over", "very", "also",
            "one", "item"
    );

    private MatchScorer() {
    }

    public record Candidate(String category, String location, String title, String description, Long imageHash) {
        public static Candidate of(String category, String location, String title, String description, String imageData) {
            return new Candidate(category, location, title, description, ImageHasher.averageHash(imageData));
        }
    }

    public record Result(int category, int location, int keywords, int image, boolean imageUsed, int total) {
        public boolean isMatch() {
            return total >= SAVE_THRESHOLD && (keywords > 0 || image > 0);
        }

        public String explain() {
            String photoPart = imageUsed ? "Photo +" + image : "Photo not compared";
            return "Category +" + category + " \u00b7 Location +" + location + " \u00b7 Keywords +" + keywords + " \u00b7 " + photoPart;
        }
    }

    public static Result score(Candidate a, Candidate b) {
        int catScore = 0;
        if (a.category() != null && b.category() != null && !a.category().isBlank() && !b.category().isBlank()) {
            String cleanA = a.category().replaceAll("\\s+", "");
            String cleanB = b.category().replaceAll("\\s+", "");
            if (cleanA.equalsIgnoreCase(cleanB)) {
                catScore = W_CATEGORY;
            }
        }

        int locScore = 0;
        if (a.location() != null && b.location() != null && !a.location().isBlank() && !b.location().isBlank()) {
            String cleanA = a.location().replaceAll("\\s+", "");
            String cleanB = b.location().replaceAll("\\s+", "");
            if (cleanA.equalsIgnoreCase(cleanB)) {
                locScore = W_LOCATION;
            }
        }

        String textA = (a.title() != null ? a.title() : "") + " " + (a.description() != null ? a.description() : "");
        String textB = (b.title() != null ? b.title() : "") + " " + (b.description() != null ? b.description() : "");
        Set<String> wordsA = keywords(textA);
        Set<String> wordsB = keywords(textB);

        int kwScore = 0;
        if (!wordsA.isEmpty() && !wordsB.isEmpty()) {
            Set<String> shared = new HashSet<>(wordsA);
            shared.retainAll(wordsB);
            if (!shared.isEmpty()) {
                int minSize = Math.min(wordsA.size(), wordsB.size());
                kwScore = (int) Math.round((double) W_KEYWORDS * shared.size() / minSize);
            }
        }

        boolean imageUsed = (a.imageHash() != null && b.imageHash() != null);
        int imgScore = 0;
        if (imageUsed) {
            int d = ImageHasher.distance(a.imageHash(), b.imageHash());
            if (d < IMAGE_CUTOFF) {
                imgScore = (int) Math.round((double) W_IMAGE * (IMAGE_CUTOFF - d) / (double) IMAGE_CUTOFF);
            }
        }

        int earned = catScore + locScore + kwScore + imgScore;
        int possible = imageUsed ? 100 : 85;
        int total = Math.min(100, (int) Math.round((double) earned * 100.0 / (double) possible));

        return new Result(catScore, locScore, kwScore, imgScore, imageUsed, total);
    }

    public static Set<String> keywords(String text) {
        if (text == null) {
            return Collections.emptySet();
        }
        String cleaned = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", " ");
        String[] tokens = cleaned.split("\\s+");
        Set<String> result = new HashSet<>();
        for (String raw : tokens) {
            if (raw.length() < 3 || STOP_WORDS.contains(raw)) {
                continue;
            }
            String token = raw;
            if (token.length() > 3 && token.endsWith("s") && !token.endsWith("ss")) {
                token = token.substring(0, token.length() - 1);
            }
            if (token.length() >= 3 && !STOP_WORDS.contains(token)) {
                result.add(token);
            }
        }
        return result;
    }
}
