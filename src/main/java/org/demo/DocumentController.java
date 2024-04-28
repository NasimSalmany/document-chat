package org.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ai/document")
public class DocumentController {

    private final ChatClient aiClient;
    private final VectorStore vectorStore;

    public DocumentController(ChatClient aiClient, VectorStore vectorStore) {
        this.aiClient = aiClient;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/chat")
    public ResponseEntity<String> generateResponse(@RequestParam String query) {
        List<Document> similarDocuments = vectorStore.similaritySearch(query);
        String information = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));

        String response = "Please answer 'Not enough knowledge.' if you don't know.\n\n" + information;

        return ResponseEntity.ok(getAIResponse(query, response));
    }

    private String getAIResponse(String query, String response) {
        var systemPrompt = new SystemPromptTemplate(response);
        var userPrompt = new PromptTemplate(query);

        var prompt = new Prompt(List.of(systemPrompt.createMessage(), userPrompt.createMessage()));

        return aiClient.call(prompt).getResult().getOutput().getContent();
    }
}
