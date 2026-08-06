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
    
    public SemanticScholarFetcher() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    public void fetch(String query, Category category, Buffer<Paper> buffer) {
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
            
            Thread.sleep(1000);
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray data = jsonResponse.optJSONArray("data");
                
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
                        
                        buffer.write(paper);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during fetch", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch papers", e);
        }
    }
}
