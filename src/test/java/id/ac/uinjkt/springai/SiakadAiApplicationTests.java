package id.ac.uinjkt.springai;

import id.ac.uinjkt.springai.dto.Dtos.AskResponse;
import id.ac.uinjkt.springai.dto.Dtos.ChatResponse;
import id.ac.uinjkt.springai.dto.Dtos.Quiz;
import id.ac.uinjkt.springai.service.ChatService;
import id.ac.uinjkt.springai.service.DocumentService;
import id.ac.uinjkt.springai.service.QuizService;
import id.ac.uinjkt.springai.service.RagService;
import id.ac.uinjkt.springai.tools.AkademikTools;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Pengujian integrasi seluruh fitur Spring AI pada SiAkad AI. */
@SpringBootTest
class SiakadAiApplicationTests {

    @Autowired ChatService chatService;
    @Autowired RagService ragService;
    @Autowired QuizService quizService;
    @Autowired DocumentService documentService;
    @Autowired AkademikTools tools;

    @Test
    void chatMemoryMengingatNama() {
        ChatResponse r1 = chatService.chat("test-1", "Halo, nama saya Budi");
        ChatResponse r2 = chatService.chat("test-1", "Siapa nama saya?");
        assertThat(r1.answer()).contains("Budi");
        assertThat(r2.answer()).contains("Budi");
    }

    @Test
    void toolCallingHitungIp() {
        ChatResponse r = chatService.chat("test-2", "Tolong hitung IP saya: A 3 sks, B+ 2 sks, B 3 sks");
        assertThat(r.answer()).contains("3.45");
    }

    @Test
    void toolCallingWaktu() {
        ChatResponse r = chatService.chat("test-3", "Hari ini tanggal berapa?");
        assertThat(r.answer()).startsWith("Sekarang adalah");
    }

    @Test
    void ragMenjawabDariDokumen() {
        AskResponse r = ragService.ask("Berapa minimal kehadiran agar bisa ikut UAS?");
        assertThat(r.answer()).contains("75 persen");
        assertThat(r.sources()).isNotEmpty();
    }

    @Test
    void structuredOutputMenghasilkanObjekQuiz() {
        Quiz quiz = quizService.generate("Spring AI", 3);
        assertThat(quiz.soal()).hasSize(3);
        assertThat(quiz.soal().get(0).pilihan()).hasSize(4);
    }

    @Test
    void hitungIpManual() {
        assertThat(tools.hitungIp("A,B", "3,3")).isEqualTo("IP = 3.50 dari total 6 SKS");
    }
}
