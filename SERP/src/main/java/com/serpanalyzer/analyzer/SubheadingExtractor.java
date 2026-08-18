package com.serpanalyzer.analyzer;

import com.serpanalyzer.domain.Paper;
import com.serpanalyzer.fetcher.PdfTextExtractor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Task 3.2: Isolates the section sub-headings of a paper.
 *
 * <p>Candidate headings are extracted using structural cues. When the JSON summary exposes a
 * non-empty list of sections those are used directly; otherwise Apache PDFBox is used to
 * extract the text structurally from the provided {@code pdfLink}.
 */
public class SubheadingExtractor {

    private static final int MIN_HEADING_LENGTH = 2;
    private static final int MAX_HEADING_LENGTH = 80;
    private static final int MAX_HEADING_WORDS = 8;

    private static final Set<String> FUNCTION_WORDS = Set.of(
            "a", "an", "and", "as", "at", "by", "for", "from", "in", "of",
            "on", "or", "the", "to", "vs", "with", "without", "via", "over");

    private final PdfTextExtractor pdfTextExtractor;

    public SubheadingExtractor() {
        this(new PdfTextExtractor());
    }

    SubheadingExtractor(PdfTextExtractor pdfTextExtractor) {
        this.pdfTextExtractor = pdfTextExtractor;
    }

    /**
     * Returns the distinct candidate sub-headings of the given paper, before normalization.
     */
    public Set<String> extractSubheadings(Paper paper) {
        Set<String> candidates = new HashSet<>();

        List<String> sections = paper.sections();
        if (sections != null && !sections.isEmpty()) {
            for (String section : sections) {
                if (section != null && !section.isBlank()) {
                    candidates.add(section.trim());
                }
            }
            return candidates;
        }

        String text = pdfTextExtractor.extractText(paper.pdfLink());
        if (text == null || text.isBlank()) {
            return candidates;
        }

        for (String line : text.split("\\r?\\n")) {
            String candidate = stripBullet(line);
            if (isLikelyHeading(candidate)) {
                candidates.add(candidate);
            }
        }
        return candidates;
    }

    /**
     * Structural cues used to decide whether a line extracted from a PDF looks like a heading:
     * sensible length, no sentence-ending punctuation, few words, and either a section number
     * prefix or title case.
     */
    static boolean isLikelyHeading(String line) {
        if (line == null) {
            return false;
        }
        String candidate = line.trim();
        if (candidate.length() < MIN_HEADING_LENGTH || candidate.length() > MAX_HEADING_LENGTH) {
            return false;
        }
        if (candidate.matches(".*[.!?;]$")) {
            return false;
        }
        String[] words = candidate.split("\\s+");
        if (words.length < 1 || words.length > MAX_HEADING_WORDS) {
            return false;
        }
        if (startsWithSectionNumber(candidate)) {
            return true;
        }
        return isTitleCase(words);
    }

    private static String stripBullet(String line) {
        if (line == null) {
            return "";
        }
        return line.trim().replaceAll("^[-*•·‣◦]+\\s+", "");
    }

    private static boolean startsWithSectionNumber(String candidate) {
        if (candidate.matches("^\\d+(\\.\\d+)*\\.?\\s+")) {
            return true;
        }
        return candidate.matches("^[ivxlcdm]+\\.\\s+");
    }

    private static boolean isTitleCase(String[] words) {
        boolean anyCapitalized = false;
        for (String word : words) {
            char first = word.charAt(0);
            if (Character.isUpperCase(first)) {
                anyCapitalized = true;
            } else if (Character.isLetter(first) && !FUNCTION_WORDS.contains(word)) {
                return false;
            }
        }
        return anyCapitalized;
    }
}
