package id.ac.uinjkt.springai.service;

import id.ac.uinjkt.springai.dto.Dtos.UploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Proses ingestion dokumen untuk RAG (ETL pipeline Spring AI):
 * Extract (TextReader) -> Transform (TokenTextSplitter) -> Load (VectorStore).
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter = new TokenTextSplitter(120, 50, 5, 1000, true);
    private final List<String> dokumen = new CopyOnWriteArrayList<>();
    private int totalChunk = 0;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /** Memuat dokumen bawaan saat aplikasi siap. */
    @EventListener(ApplicationReadyEvent.class)
    public void loadDefaultDocuments() {
        int n = ingest(new ClassPathResource("docs/panduan-akademik.txt"), "panduan-akademik.txt");
        log.info("Dokumen bawaan dimuat ke VectorStore: {} chunk", n);
    }

    public UploadResponse upload(String fileName, byte[] content) {
        Resource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        int n = ingest(resource, fileName);
        return new UploadResponse(fileName, n, totalChunk);
    }

    private synchronized int ingest(Resource resource, String fileName) {
        TextReader reader = new TextReader(resource);
        reader.getCustomMetadata().put("source", fileName);
        List<Document> chunks = splitter.apply(reader.get());
        vectorStore.add(chunks);
        dokumen.add(fileName);
        totalChunk += chunks.size();
        return chunks.size();
    }

    public List<String> getDokumen() {
        return List.copyOf(dokumen);
    }

    public int getTotalChunk() {
        return totalChunk;
    }
}
