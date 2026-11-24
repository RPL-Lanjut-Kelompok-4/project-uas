package id.ac.uinjkt.springai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** Kumpulan DTO (Java record) untuk request/response REST API. */
public final class Dtos {

    private Dtos() {
    }

    public record ChatRequest(String conversationId, @NotBlank String message) {
    }

    public record ChatResponse(String conversationId, String answer, String model, long durationMs) {
    }

    public record AskRequest(@NotBlank String question) {
    }

    public record AskResponse(String answer, List<Source> sources, long durationMs) {
    }

    public record Source(String fileName, String excerpt, Double score) {
    }

    public record QuizRequest(@NotBlank String topic, @Min(1) @Max(6) int jumlah) {
    }

    /** Target structured output: model mengisi struktur ini dalam format JSON. */
    public record Quiz(String topik, List<Soal> soal) {
    }

    public record Soal(String pertanyaan, List<String> pilihan, String jawaban) {
    }

    public record UploadResponse(String fileName, int jumlahChunk, int totalChunk) {
    }

    public record InfoResponse(String model, int totalChunk, List<String> dokumen) {
    }
}
