package com.serpanalyzer.fetcher;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Task 3.2: Extracts raw text from a PDF hosted at the provided {@code pdfLink} using Apache
 * PDFBox. Used as a fallback when the JSON summary does not expose section headings.
 */
public class PdfTextExtractor {

    private final HttpClient httpClient;

    public PdfTextExtractor() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Downloads the PDF at the given URL and returns its extracted text.
     *
     * @param pdfUrl the URL of the paper's PDF
     * @return the raw text content of the PDF, or an empty string when unavailable
     */
    public String extractText(String pdfUrl) {
        if (pdfUrl == null || pdfUrl.isBlank()) {
            return "";
        }
        byte[] pdfBytes = download(pdfUrl);
        if (pdfBytes.length == 0) {
            return "";
        }
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from PDF: " + pdfUrl, e);
        }
    }

    private byte[] download(String pdfUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pdfUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("User-Agent", "SerpAnalyzer/1.0")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                return response.body();
            }
            return new byte[0];
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while downloading PDF from " + pdfUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download PDF from " + pdfUrl, e);
        }
    }
}
