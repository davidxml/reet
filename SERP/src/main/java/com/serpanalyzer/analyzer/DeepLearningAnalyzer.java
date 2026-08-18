package com.serpanalyzer.analyzer;

import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.domain.Category;
import com.serpanalyzer.domain.Paper;

import java.util.HashSet;
import java.util.Set;

/**
 * Task 3.1 &amp; 3.4: Analyzer for Deep Learning papers.
 *
 * <p>Extracts the paper's section sub-headings, normalizes and deduplicates them, then
 * increments the shared {@link Aggregator} once per distinct heading per paper (document
 * frequency).
 * 
 * <p>When sections are not available from the API, falls back to extracting key technical
 * terms from the abstract as a proxy for paper topics.
 */
public class DeepLearningAnalyzer implements PaperAnalyzer {

    private final SubheadingExtractor subheadingExtractor;
    
    private static final Set<String> DL_KEYWORDS = Set.of(
        "neural", "network", "deep", "learning", "model", "training", "optimization",
        "architecture", "transformer", "cnn", "rnn", "lstm", "attention", "layer",
        "dataset", "accuracy", "performance", "classification", "detection", "segmentation",
        "embedding", "feature", "representation", "supervised", "unsupervised", "reinforcement",
        "gradient", "backpropagation", "loss", "function", "parameter", "hyperparameter",
        "convolution", "pooling", "dropout", "batch", "normalization", "activation",
        "vision", "language", "nlp", "computer", "image", "text", "speech",
        "benchmark", "evaluation", "metric", "precision", "recall", "baseline"
    );

    public DeepLearningAnalyzer() {
        this(new SubheadingExtractor());
    }

    DeepLearningAnalyzer(SubheadingExtractor subheadingExtractor) {
        this.subheadingExtractor = subheadingExtractor;
    }

    @Override
    public void analyze(Paper paper, Aggregator<String> sink) {
        if (paper == null || paper.category() != Category.DEEP_LEARNING) {
            return;
        }

        // Try to extract section headings first
        Set<String> rawHeadings = subheadingExtractor.extractSubheadings(paper);
        
        if (!rawHeadings.isEmpty()) {
            // Use section headings if available
            Set<String> distinctNormalized = new HashSet<>();
            for (String heading : rawHeadings) {
                String key = HeadingNormalizer.normalize(heading);
                if (!key.isEmpty()) {
                    distinctNormalized.add(key);
                }
            }
            
            for (String key : distinctNormalized) {
                sink.increment(key);
            }
        } else {
            // Fallback: Extract key technical terms from abstract
            extractKeyTermsFromAbstract(paper, sink);
        }
    }
    
    private void extractKeyTermsFromAbstract(Paper paper, Aggregator<String> sink) {
        String summary = paper.summary();
        if (summary == null || summary.isEmpty()) {
            return;
        }
        
        String cleaned = summary.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", " ")
            .replaceAll("\\s+", " ");
        
        String[] words = cleaned.split("\\s+");
        Set<String> foundTerms = new HashSet<>();
        
        // Extract single-word technical terms
        for (String word : words) {
            if (DL_KEYWORDS.contains(word) && word.length() > 2) {
                foundTerms.add(word);
            }
        }
        
        // Extract common bigrams (two-word phrases)
        for (int i = 0; i < words.length - 1; i++) {
            String bigram = words[i] + " " + words[i + 1];
            
            // Common deep learning bigrams
            if (bigram.equals("deep learning") || bigram.equals("neural network") ||
                bigram.equals("machine learning") || bigram.equals("computer vision") ||
                bigram.equals("natural language") || bigram.equals("training data") ||
                bigram.equals("model architecture") || bigram.equals("transfer learning") ||
                bigram.equals("feature extraction") || bigram.equals("image classification") ||
                bigram.equals("object detection") || bigram.equals("batch normalization") ||
                bigram.equals("attention mechanism") || bigram.equals("residual network")) {
                foundTerms.add(bigram);
            }
        }
        
        // Increment each distinct term once per paper
        for (String term : foundTerms) {
            sink.increment(term);
        }
    }
}
