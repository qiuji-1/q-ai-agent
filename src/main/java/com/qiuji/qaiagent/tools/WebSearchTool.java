package com.qiuji.qaiagent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebSearchTool {

    @Value("${spring.searchapi.api-key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    @Tool(description = "Search the web using Baidu search engine for real-time information")
    public String search(
            @ToolParam(description = "The search query to look up on Baidu") String query) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.get(SEARCH_API_URL).newBuilder();
            urlBuilder.addQueryParameter("engine", "baidu");
            urlBuilder.addQueryParameter("q", query);
            urlBuilder.addQueryParameter("api_key", apiKey);

            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return "Search failed: HTTP " + response.code();
                }

                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                return formatSearchResults(jsonResponse, query);
            }
        } catch (Exception e) {
            return "Error performing search: " + e.getMessage();
        }
    }

    private String formatSearchResults(JsonNode jsonResponse, String query) {
        StringBuilder result = new StringBuilder();
        result.append("Search results for: ").append(query).append("\n\n");

        JsonNode organicResults = jsonResponse.get("organic_results");
        if (organicResults != null && organicResults.isArray() && !organicResults.isEmpty()) {
            result.append("Found ").append(organicResults.size()).append(" results:\n\n");

            int count = 0;
            for (JsonNode item : organicResults) {
                count++;
                String title = item.has("title") ? item.get("title").asText() : "No title";
                String link = item.has("link") ? item.get("link").asText() : "";
                String snippet = item.has("snippet") ? item.get("snippet").asText() : "No description";
                String displayedLink = item.has("displayed_link") ? item.get("displayed_link").asText() : "";

                result.append(count).append(". **").append(title).append("**\n");
                if (!displayedLink.isEmpty()) {
                    result.append("   Source: ").append(displayedLink).append("\n");
                }
                result.append("   Link: ").append(link).append("\n");
                result.append("   Description: ").append(snippet).append("\n\n");

                if (count >= 10) {
                    result.append("... (showing top 10 results)\n");
                    break;
                }
            }
        } else {
            result.append("No organic results found.");
        }

        JsonNode answerBox = jsonResponse.get("answer_box");
        if (answerBox != null && answerBox.has("answer")) {
            result.append("\n**AI Answer:** \n");
            result.append(answerBox.get("answer").asText()).append("\n");
        }

        return result.toString();
    }
}
