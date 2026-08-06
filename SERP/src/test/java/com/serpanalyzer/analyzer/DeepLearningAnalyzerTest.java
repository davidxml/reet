package com.serpanalyzer.analyzer;

import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.concurrency.ConcurrentMapAggregator;
import com.serpanalyzer.domain.Category;
import com.serpanalyzer.domain.Paper;
import com.serpanalyzer.fetcher.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Deep Learning sub-heading analyzer (Tasks 3.1, 3.2 and 3.4).
 */
class DeepLearningAnalyzerTest {

    private Aggregator<String> aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new ConcurrentMapAggregator<>();
    }

    @Test
    void testUsesSectionsFromJsonSummaryWhenAvailable() {
        DeepLearningAnalyzer analyzer = new DeepLearningAnalyzer();

        Paper paper = new Paper(
                "1",
                Category.DEEP_LEARNING,
                "Deep Learning Paper",
                "abstract text",
                List.of("Introduction", "introduction", "Related Work", "Related Works"),
                "http://example.com/paper1");

        analyzer.analyze(paper, aggregator);

        Map<String, Integer> snapshot = aggregator.snapshot();
        assertEquals(2, snapshot.size(), "Duplicates within a paper must be collapsed");
        assertEquals(1, snapshot.get("introduction"));
        assertEquals(1, snapshot.get("related work"));
    }

    @Test
    void testPdfFallbackExtractsHeadingsWhenSectionsAreMissing() {
        DeepLearningAnalyzer analyzer = new DeepLearningAnalyzer(
                new SubheadingExtractor(new PdfTextExtractor() {
                    @Override
                    public String extractText(String pdfUrl) {
                        return "1 Introduction\n"
                                + "Deep learning models have grown substantially.\n"
                                + "2 Related Work\n"
                                + "Prior art is discussed in this section.\n";
                    }
                }));

        Paper paper = new Paper(
                "2",
                Category.DEEP_LEARNING,
                "Deep Learning Paper",
                "abstract text",
                new ArrayList<>(),
                "http://example.com/paper2.pdf");

        analyzer.analyze(paper, aggregator);

        Map<String, Integer> snapshot = aggregator.snapshot();
        assertEquals(2, snapshot.size(), "Headings should be extracted structurally from the PDF text");
        assertEquals(1, snapshot.get("introduction"));
        assertEquals(1, snapshot.get("related work"));
    }

    @Test
    void testCountsDocumentFrequencyAcrossPapers() {
        DeepLearningAnalyzer analyzer = new DeepLearningAnalyzer();

        Paper paper1 = new Paper(
                "3",
                Category.DEEP_LEARNING,
                "Paper A",
                "abstract",
                List.of("Introduction", "Related Work"),
                "http://example.com/paper3");
        Paper paper2 = new Paper(
                "4",
                Category.DEEP_LEARNING,
                "Paper B",
                "abstract",
                List.of("Introduction"),
                "http://example.com/paper4");

        analyzer.analyze(paper1, aggregator);
        analyzer.analyze(paper2, aggregator);

        Map<String, Integer> snapshot = aggregator.snapshot();
        assertEquals(2, snapshot.get("introduction"),
                "Document frequency counts papers, not occurrences");
        assertEquals(1, snapshot.get("related work"));
    }

    @Test
    void testIgnoresPapersFromOtherCategories() {
        DeepLearningAnalyzer analyzer = new DeepLearningAnalyzer();

        Paper paper = new Paper(
                "5",
                Category.CRIME_REPORTING,
                "Crime Report",
                "abstract",
                List.of("Introduction"),
                "http://example.com/paper5");

        analyzer.analyze(paper, aggregator);

        assertTrue(aggregator.snapshot().isEmpty(),
                "Non-Deep-Learning papers must not contribute sub-headings");
    }
}
