package com.serpanalyzer.analyzer;

import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.concurrency.ConcurrentMapAggregator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Task 3.6: Tests for the normalization engine.
 *
 * <p>Verifies that normalization merges case variants (e.g. "Introduction" and "introduction")
 * and plural variants (e.g. "Related Works" and "Related Work") without creating duplicate rows.
 */
class HeadingNormalizerTest {

    @Test
    void testCaseVariantsNormalizeToSameKey() {
        assertEquals(
                HeadingNormalizer.normalize("Introduction"),
                HeadingNormalizer.normalize("introduction"),
                "Case variants should collapse to a single normalized key");
        assertEquals(
                HeadingNormalizer.normalize("Related Work"),
                HeadingNormalizer.normalize("related work"),
                "Case variants should collapse to a single normalized key");
    }

    @Test
    void testPluralVariantsNormalizeToSameKey() {
        assertEquals(
                HeadingNormalizer.normalize("Related Works"),
                HeadingNormalizer.normalize("Related Work"),
                "Plural variants should collapse to a single normalized key");
        assertEquals(
                HeadingNormalizer.normalize("Conclusions"),
                HeadingNormalizer.normalize("conclusion"),
                "Plural variants should collapse to a single normalized key");
        assertEquals(
                HeadingNormalizer.normalize("Methods"),
                HeadingNormalizer.normalize("method"),
                "Plural variants should collapse to a single normalized key");
    }

    @Test
    void testWhitespaceAndPunctuationAreNormalized() {
        assertEquals(
                HeadingNormalizer.normalize("  Experimental Setup: "),
                HeadingNormalizer.normalize("experimental setup"),
                "Surrounding whitespace and trailing punctuation should be ignored");
        assertEquals(
                HeadingNormalizer.normalize("2.1  Related Work."),
                HeadingNormalizer.normalize("related works"),
                "Section numbers and trailing periods should be ignored");
    }

    @Test
    void testSingularNounsEndingInSAreNotMangled() {
        assertEquals("analysis", HeadingNormalizer.normalize("Analysis"),
                "Words ending in 'is' are singular and must not be altered");
        assertEquals("research", HeadingNormalizer.normalize("Research"),
                "Words ending in 'ch' are singular and must not be altered");
    }

    @Test
    void testAggregationDoesNotCreateDuplicateRows() {
        Aggregator<String> aggregator = new ConcurrentMapAggregator<>();

        aggregator.increment(HeadingNormalizer.normalize("Introduction"));
        aggregator.increment(HeadingNormalizer.normalize("introduction"));
        aggregator.increment(HeadingNormalizer.normalize("Related Works"));
        aggregator.increment(HeadingNormalizer.normalize("related work"));

        Map<String, Integer> snapshot = aggregator.snapshot();

        assertEquals(2, snapshot.size(),
                "Case and plural variants must merge into a single row each, not duplicate rows");
        assertEquals(2, snapshot.get("introduction"),
                "Both 'Introduction' and 'introduction' should aggregate under one key");
        assertEquals(2, snapshot.get("related work"),
                "Both 'Related Works' and 'related work' should aggregate under one key");
    }

    @Test
    void testDifferentHeadingsRemainDistinct() {
        assertNotEquals(
                HeadingNormalizer.normalize("Introduction"),
                HeadingNormalizer.normalize("Conclusion"),
                "Different headings must not be merged");
        assertTrue(HeadingNormalizer.normalize("introduction").equals("introduction"));
    }
}
