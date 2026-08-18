package com.serpanalyzer.analyzer;

import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.concurrency.ConcurrentMapAggregator;
import com.serpanalyzer.domain.Category;
import com.serpanalyzer.domain.Paper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CrimeReportingAnalyzerTest {

    private CrimeReportingAnalyzer analyzer;
    private Aggregator<String> aggregator;

    @BeforeEach
    void setUp() {
        analyzer = new CrimeReportingAnalyzer();
        aggregator = new ConcurrentMapAggregator<>();
    }

    @Test
    void testAnalyzeFiltersStopWordsAndCountsDocumentFrequency() {
        // Paper 1: Contains "suspect" 4 times, plus stop words
        Paper paper1 = new Paper(
                "1",
                Category.CRIME_REPORTING,
                "Crime Report Analysis",
                "The suspect was seen near the suspect vehicle. The suspect and the suspect were identified.",
                new ArrayList<>(),
                "http://example.com/paper1"
        );

        // Paper 2: Contains "suspect" 2 times and "evidence" 3 times, plus stop words
        Paper paper2 = new Paper(
                "2",
                Category.CRIME_REPORTING,
                "Evidence Collection",
                "The evidence was collected and the evidence shows suspect involvement. More evidence found.",
                new ArrayList<>(),
                "http://example.com/paper2"
        );

        // Paper 3: Contains "evidence" 1 time and "witness" 2 times, plus stop words
        Paper paper3 = new Paper(
                "3",
                Category.CRIME_REPORTING,
                "Witness Testimony",
                "The witness provided testimony and the witness confirmed evidence details.",
                new ArrayList<>(),
                "http://example.com/paper3"
        );

        // Execute analysis
        analyzer.analyze(paper1, aggregator);
        analyzer.analyze(paper2, aggregator);
        analyzer.analyze(paper3, aggregator);

        // Get results
        Map<String, Integer> results = aggregator.snapshot();

        // Assert stop words are filtered out
        assertNull(results.get("the"), "Stop word 'the' should be filtered out");
        assertNull(results.get("and"), "Stop word 'and' should be filtered out");
        assertNull(results.get("was"), "Stop word 'was' should be filtered out");

        // Assert short words (<=2 chars) are filtered out
        assertNull(results.get("a"), "Single character words should be filtered out");
        assertNull(results.get("in"), "Two character words should be filtered out");

        // CRITICAL: Assert document frequency, not raw term frequency
        // "suspect" appears in 2 papers (paper1 and paper2), NOT 6 times total
        assertEquals(2, results.get("suspect"), 
                "Document frequency for 'suspect' should be 2 (appears in 2 papers), not raw count");

        // "evidence" appears in 2 papers (paper2 and paper3), NOT 4 times total
        assertEquals(2, results.get("evidence"), 
                "Document frequency for 'evidence' should be 2 (appears in 2 papers), not raw count");

        // "witness" appears in 1 paper (paper3), NOT 2 times total
        assertEquals(1, results.get("witness"), 
                "Document frequency for 'witness' should be 1 (appears in 1 paper), not raw count");

        // Assert no term exceeds the total number of papers
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            assertTrue(entry.getValue() <= 3, 
                    "No term frequency should exceed the total number of papers (3). Found: " + 
                    entry.getKey() + " = " + entry.getValue());
        }

        // Assert valid terms are present
        assertTrue(results.containsKey("suspect"), "Valid term 'suspect' should be present");
        assertTrue(results.containsKey("evidence"), "Valid term 'evidence' should be present");
        assertTrue(results.containsKey("witness"), "Valid term 'witness' should be present");
    }
}
