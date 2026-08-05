package com.serpanalyzer.domain;

import java.util.List;

public record Paper(
    String id,
    Category category,
    String title,
    String summary,
    List<String> sections,
    String pdfLink
) {}
