package com.serpanalyzer.analyzer;

import java.util.Locale;

/**
 * Task 3.3: Normalization and deduplication of extracted sub-headings.
 *
 * <p>Standardizes candidate headings by trimming whitespace and folding case, then applies
 * heuristics to collapse near-duplicates (e.g. "Related Works" and "Related Work" map to the
 * same canonical key) so that downstream aggregation never produces duplicate rows.
 */
public final class HeadingNormalizer {

    private HeadingNormalizer() {
    }

    /**
     * Normalizes a raw heading into a canonical, deduplicatable key.
     *
     * <ul>
     *   <li>Trims surrounding whitespace and folds to lower case.</li>
     *   <li>Collapses runs of internal whitespace into a single space.</li>
     *   <li>Strips leading section numbers (e.g. "2.3 ", "IV. ", "5) ").</li>
     *   <li>Strips trailing punctuation (e.g. ":", ".", ";").</li>
     *   <li>Collapses plural variants into their singular key (e.g. "works" -> "work").</li>
     * </ul>
     *
     * @param rawHeading the heading as extracted from the paper
     * @return the canonical key used for aggregation
     */
    public static String normalize(String rawHeading) {
        if (rawHeading == null) {
            return "";
        }
        String normalized = rawHeading.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("\\s+", " ");
        normalized = stripLeadingSectionNumber(normalized);
        normalized = normalized.replaceAll("[.:;!?\\s]+$", "");
        normalized = collapseTrailingPlural(normalized);
        return normalized.trim();
    }

    private static String stripLeadingSectionNumber(String heading) {
        String result = heading.replaceAll("^\\d+(\\.\\d+)*\\.?\\s+", "");
        result = result.replaceAll("^[ivxlcdm]+(\\.|\\s+)\\s*", "");
        result = result.replaceAll("^\\d+\\)\\s+", "");
        result = result.replaceAll("^[a-z]\\)\\s+", "");
        return result;
    }

    private static String collapseTrailingPlural(String heading) {
        if (heading.length() <= 3) {
            return heading;
        }
        if (heading.endsWith("ies") && heading.length() > 4) {
            return heading.substring(0, heading.length() - 3) + "y";
        }
        if (heading.endsWith("sses")
                || heading.endsWith("ches")
                || heading.endsWith("shes")
                || heading.endsWith("xes")
                || heading.endsWith("zes")) {
            return heading.substring(0, heading.length() - 2);
        }
        if (heading.endsWith("es")) {
            return heading.substring(0, heading.length() - 1);
        }
        if (heading.endsWith("s")
                && !heading.endsWith("ss")
                && !heading.endsWith("us")
                && !heading.endsWith("is")) {
            return heading.substring(0, heading.length() - 1);
        }
        return heading;
    }
}
