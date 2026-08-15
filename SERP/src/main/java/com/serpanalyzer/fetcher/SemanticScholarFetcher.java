package com.serpanalyzer.fetcher;

import com.serpanalyzer.concurrency.Buffer;
import com.serpanalyzer.domain.Category;
import com.serpanalyzer.domain.Paper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SemanticScholarFetcher {
    private final HttpClient httpClient;
    private static final Object RATE_LIMIT_LOCK = new Object();
    private static long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL_MS = 1000; // 1 second between requests (Semantic Scholar limit)
    
    public SemanticScholarFetcher() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    public void fetch(String query, Category category, Buffer<Paper> buffer) {
        // Check if we should use mock data
        String useMock = System.getenv("USE_MOCK_DATA");
        if ("true".equalsIgnoreCase(useMock)) {
            System.out.println("[FETCHER] Using mock data for: " + query);
            fetchMockData(category, buffer);
            return;
        }
        
        int maxRetries = 3;
        int retryCount = 0;
        long retryDelayMs = 1000; // Start with 1 second
        
        while (retryCount < maxRetries) {
            try {
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String url = "https://api.semanticscholar.org/graph/v1/paper/search?query=" 
                    + encodedQuery + "&fields=title,abstract,url,year,venue&limit=10";
                
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET();
                
                String apiKey = System.getenv("S2_API_KEY");
                if (apiKey != null && !apiKey.isEmpty()) {
                    requestBuilder.header("x-api-key", apiKey);
                }
                
                HttpRequest request = requestBuilder.build();
                
                HttpResponse<String> response;
                
                // Enforce rate limiting across all fetcher instances
                synchronized (RATE_LIMIT_LOCK) {
                    long now = System.currentTimeMillis();
                    long timeSinceLastRequest = now - lastRequestTime;
                    if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
                        long sleepTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest;
                        System.out.println("[FETCHER] Rate limiting: sleeping " + sleepTime + "ms");
                        Thread.sleep(sleepTime);
                    }
                    lastRequestTime = System.currentTimeMillis();
                }
                
                // Perform the actual HTTP request outside the lock to allow parallelism
                System.out.println("[FETCHER] Fetching from: " + url + " (attempt " + (retryCount + 1) + ")");
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                System.out.println("[FETCHER] Response status: " + response.statusCode());
                
                if (response.statusCode() == 429) {
                    // Rate limited, retry with backoff
                    retryCount++;
                    if (retryCount < maxRetries) {
                        System.err.println("[FETCHER] Rate limited (429), retrying in " + retryDelayMs + "ms...");
                        Thread.sleep(retryDelayMs);
                        retryDelayMs *= 2; // Exponential backoff
                        continue;
                    } else {
                        System.err.println("[FETCHER] Max retries reached. Try setting USE_MOCK_DATA=true environment variable.");
                        return;
                    }
                }
                
                if (response.statusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    JSONArray data = jsonResponse.optJSONArray("data");
                    
                    System.out.println("[FETCHER] Data array length: " + (data != null ? data.length() : 0));
                    
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject paperJson = data.getJSONObject(i);
                            
                            String id = paperJson.optString("paperId", "");
                            String title = paperJson.optString("title", "");
                            String summary = paperJson.optString("abstract", "");
                            String pdfLink = paperJson.optString("url", "");
                            
                            Paper paper = new Paper(
                                id,
                                category,
                                title,
                                summary,
                                new ArrayList<>(),
                                pdfLink
                            );
                            
                            System.out.println("[FETCHER] Writing paper: " + title);
                            buffer.write(paper);
                        }
                    }
                    return; // Success, exit retry loop
                } else {
                    System.err.println("[FETCHER] Failed with status: " + response.statusCode());
                    System.err.println("[FETCHER] Response body: " + response.body());
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[FETCHER] Interrupted: " + e.getMessage());
                throw new RuntimeException("Interrupted during fetch", e);
            } catch (Exception e) {
                System.err.println("[FETCHER] Exception: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to fetch papers", e);
            }
        }
    }
    
    private void fetchMockData(Category category, Buffer<Paper> buffer) {
        if (category == Category.CRIME_REPORTING) {
            buffer.write(new Paper("mock1", category, "Automated Crime Reporting Systems",
                "This paper discusses automated crime reporting systems and their impact on policing efficiency and community safety through digital platforms.",
                new ArrayList<>(), "https://example.com/paper1"));
            buffer.write(new Paper("mock2", category, "Mobile Crime Reporting Applications", 
                "Analysis of mobile applications for crime reporting including features such as real-time reporting emergency alerts and location tracking.",
                new ArrayList<>(), "https://example.com/paper2"));
            buffer.write(new Paper("mock3", category, "Privacy in Crime Reporting Systems",
                "Examining privacy concerns and security measures in modern crime reporting systems including data protection and anonymity features.",
                new ArrayList<>(), "https://example.com/paper3"));
        } else if (category == Category.DEEP_LEARNING) {
            List<String> sections1 = List.of("Introduction", "Related Work", "Model Architecture", 
                "Experimental Results", "Conclusion");
            buffer.write(new Paper("mock4", category, "Deep Learning for Computer Vision",
                "Survey of deep learning models for computer vision tasks including convolutional neural networks image classification and object detection.",
                sections1, "https://example.com/paper4"));
            
            List<String> sections2 = List.of("Introduction", "Background", "Training Methods", 
                "Optimization Strategies", "Distributed Training", "Results");
            buffer.write(new Paper("mock5", category, "Training Large Deep Learning Models",
                "Techniques for training large-scale deep learning models including optimization strategies distributed training and memory efficiency.",
                sections2, "https://example.com/paper5"));
            
            List<String> sections3 = List.of("Introduction", "Model Architecture", "Attention Mechanisms", 
                "Transformer Networks", "Evaluation", "Conclusion");
            buffer.write(new Paper("mock6", category, "Deep Learning Model Architectures",
                "Comparison of various deep learning architectures including transformers recurrent networks and attention mechanisms for sequence modeling.",
                sections3, "https://example.com/paper6"));
        }
    }
}
