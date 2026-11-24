package id.ac.uinjkt.springai.service;

import id.ac.uinjkt.springai.dto.Dtos.AskResponse;
import id.ac.uinjkt.springai.dto.Dtos.Source;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/** Fitur 2: Tanya-jawab dokumen dengan RAG (QuestionAnswerAdvisor + VectorStore). */
@Service
public class RagService {

    private final ChatClient ragClient;

    public RagService(ChatModel chatModel, VectorStore vectorStore) {
        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(3).similarityThreshold(0.05).build())
                .build();
        this.ragClient = ChatClient.builder(chatModel)
                .defaultAdvisors(qaAdvisor, new SimpleLoggerAdvisor())
                .build();
    }

    @SuppressWarnings("unchecked")
    public AskResponse ask(String question) {
        long start = System.currentTimeMillis();
        ChatClientResponse response = ragClient.prompt()
                .user(question)
                .call()
                .chatClientResponse();

        String answer = response.chatResponse().getResult().getOutput().getText();
        List<Document> docs = (List<Document>) response.context()
                .getOrDefault(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS, List.of());
        List<Source> sources = docs.stream()
                .map(d -> new Source(String.valueOf(d.getMetadata().getOrDefault("source", "-")),
                        potong(d.getText(), 160), d.getScore()))
                .toList();
        return new AskResponse(answer, sources, System.currentTimeMillis() - start);
    }

    private static String potong(String s, int max) {
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}
