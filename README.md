# SiAkad AI – Asisten Akademik berbasis Spring AI

Project UAS mata kuliah **Rekayasa Perangkat Lunak Lanjut**: eksperimen membangun aplikasi web Java (Spring Boot) dengan **Spring AI 1.0.3**.

## Fitur
| Fitur | Komponen Spring AI |
|---|---|
| Chat asisten dengan memori percakapan | `ChatClient`, `MessageChatMemoryAdvisor`, `MessageWindowChatMemory` |
| Tool calling (tanggal/jam & hitung IP) | `@Tool`, `ToolCallingManager` |
| Tanya dokumen (RAG) | `QuestionAnswerAdvisor`, `SimpleVectorStore`, `EmbeddingModel` |
| Upload & indexing dokumen | `TextReader`, `TokenTextSplitter`, `VectorStore` (ETL pipeline) |
| Generator kuis | Structured Output (`.entity(Quiz.class)`, `BeanOutputConverter`) |

## Model AI yang dipakai
Supaya bisa dijalankan offline tanpa API key/GPU, project ini memakai implementasi **mock** dari interface Spring AI:

- `MockChatModel` mengimplementasikan `ChatModel` dan menjawab dengan aturan (rule-based). Ia tetap menjalankan alur tool calling asli Spring AI lewat `ToolCallingManager`.
- `MockEmbeddingModel` mengimplementasikan `EmbeddingModel` dengan hashing bag-of-words (512 dimensi).

Seluruh komponen lain adalah komponen asli Spring AI. Untuk memakai LLM sungguhan (mis. Ollama/OpenAI), tambahkan starter model terkait di `pom.xml` lalu ganti bean `chatModel()` dan `embeddingModel()` di `AiConfig`.

## Menjalankan
Prasyarat: JDK 21+ dan Maven 3.9+.

```bash
mvn clean test          # menjalankan 6 test integrasi
mvn spring-boot:run     # atau: java -jar target/springai-asisten-akademik-1.0.0.jar
```
Buka http://localhost:8080

> Jika di Windows muncul `Unable to establish loopback connection`, jalankan dengan
> `java -Djdk.net.unixdomain.tmpdir=C:\tmp -jar target/springai-asisten-akademik-1.0.0.jar`.

## REST API
| Method | Endpoint | Body |
|---|---|---|
| POST | `/api/chat` | `{"conversationId": "...", "message": "..."}` |
| POST | `/api/rag/ask` | `{"question": "..."}` |
| POST | `/api/rag/upload` | multipart `file` (.txt/.md) |
| POST | `/api/quiz` | `{"topic": "Spring AI", "jumlah": 3}` |
| GET | `/api/info` | – |

## Dokumentasi
- Diagram UML (source PlantUML): `docs/uml/`
- Contoh dokumen untuk diunggah: `contoh-dokumen/`
