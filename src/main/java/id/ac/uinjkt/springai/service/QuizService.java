package id.ac.uinjkt.springai.service;

import id.ac.uinjkt.springai.dto.Dtos.Quiz;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/** Fitur 3: Generator kuis dengan Structured Output (jawaban model -> objek Java). */
@Service
public class QuizService {

    private final ChatClient chatClient;

    public QuizService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    public Quiz generate(String topic, int jumlah) {
        return chatClient.prompt()
                .user(u -> u.text("""
                        Buat {jumlah} soal pilihan ganda tentang topik: {topik}
                        Setiap soal memiliki 4 pilihan dan satu jawaban benar.
                        """)
                        .param("jumlah", jumlah)
                        .param("topik", topic))
                .call()
                .entity(Quiz.class);
    }
}
