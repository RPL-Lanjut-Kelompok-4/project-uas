package id.ac.uinjkt.springai.controller;

import id.ac.uinjkt.springai.dto.Dtos.*;
import id.ac.uinjkt.springai.service.ChatService;
import id.ac.uinjkt.springai.service.DocumentService;
import id.ac.uinjkt.springai.service.QuizService;
import id.ac.uinjkt.springai.service.RagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/** REST API SiAkad AI. */
@RestController
@RequestMapping("/api")
public class AiController {

    private final ChatService chatService;
    private final RagService ragService;
    private final QuizService quizService;
    private final DocumentService documentService;
    private final String modelName;

    public AiController(ChatService chatService, RagService ragService, QuizService quizService,
                        DocumentService documentService, @Value("${siakad.ai.model}") String modelName) {
        this.chatService = chatService;
        this.ragService = ragService;
        this.quizService = quizService;
        this.documentService = documentService;
        this.modelName = modelName;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request.conversationId(), request.message());
    }

    @PostMapping("/rag/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return ragService.ask(request.question());
    }

    @PostMapping(value = "/rag/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() == null ? "dokumen.txt" : file.getOriginalFilename();
        if (!name.toLowerCase().endsWith(".txt") && !name.toLowerCase().endsWith(".md")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Hanya file .txt atau .md yang didukung"));
        }
        return ResponseEntity.ok(documentService.upload(name, file.getBytes()));
    }

    @PostMapping("/quiz")
    public Quiz quiz(@Valid @RequestBody QuizRequest request) {
        return quizService.generate(request.topic(), request.jumlah());
    }

    @GetMapping("/info")
    public InfoResponse info() {
        return new InfoResponse(modelName, documentService.getTotalChunk(), documentService.getDokumen());
    }
}
