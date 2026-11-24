package id.ac.uinjkt.springai.config;

import id.ac.uinjkt.springai.mock.MockChatModel;
import id.ac.uinjkt.springai.mock.MockEmbeddingModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurasi komponen Spring AI.
 *
 * ChatModel dan EmbeddingModel di sini adalah implementasi MOCK (rule-based, offline).
 * Seluruh komponen lain (ChatClient, ChatMemory, Advisor, VectorStore, Tool Calling,
 * Structured Output) adalah komponen asli Spring AI dan tidak bergantung pada model mana
 * yang dipakai. Mengganti mock dengan LLM sungguhan cukup dengan mengganti 2 bean ini.
 */
@Configuration
public class AiConfig {

    public static final String SYSTEM_PROMPT = """
            Kamu adalah "SiAkad AI", asisten akademik untuk mahasiswa.
            Jawab selalu dalam Bahasa Indonesia yang jelas, sopan, dan ringkas.
            """;

    @Bean
    public ChatModel chatModel() {
        return new MockChatModel();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new MockEmbeddingModel();
    }

    /** Menyimpan 20 pesan terakhir per percakapan (in-memory). */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(20).build();
    }

    /** ChatClient utama: system prompt + memori percakapan + logger. */
    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor())
                .build();
    }

    /** Vector store sederhana di memori untuk RAG. */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
