package id.ac.uinjkt.springai.service;

import id.ac.uinjkt.springai.dto.Dtos.ChatResponse;
import id.ac.uinjkt.springai.tools.AkademikTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Fitur 1: Chat dengan memori percakapan + tool calling. */
@Service
public class ChatService {

    private final ChatClient chatClient;
    private final AkademikTools tools;

    public ChatService(ChatClient chatClient, AkademikTools tools) {
        this.chatClient = chatClient;
        this.tools = tools;
    }

    public ChatResponse chat(String conversationId, String message) {
        String id = (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString() : conversationId;
        long start = System.currentTimeMillis();

        org.springframework.ai.chat.model.ChatResponse response = chatClient.prompt()
                .user(message)
                .tools(tools)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))
                .call()
                .chatResponse();

        String answer = response.getResult().getOutput().getText();
        String model = response.getMetadata().getModel();
        return new ChatResponse(id, answer, model, System.currentTimeMillis() - start);
    }
}
