package id.ac.uinjkt.springai.mock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Bank soal untuk MockChatModel saat diminta membuat kuis (structured output). */
final class QuizBank {

    private static Map<String, Object> s(String q, String a, String b, String c, String d, String jawaban) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("pertanyaan", q);
        m.put("pilihan", List.of(a, b, c, d));
        m.put("jawaban", jawaban);
        return m;
    }

    private static final Map<String, List<Map<String, Object>>> BANK = Map.of(
            "spring", List.of(
                    s("Komponen Spring AI yang menjadi API utama untuk mengirim prompt ke model adalah?",
                            "ChatClient", "RestTemplate", "JdbcTemplate", "EntityManager", "ChatClient"),
                    s("Advisor yang digunakan untuk menambahkan riwayat percakapan ke prompt adalah?",
                            "SimpleLoggerAdvisor", "MessageChatMemoryAdvisor", "QuestionAnswerAdvisor", "SafeGuardAdvisor", "MessageChatMemoryAdvisor"),
                    s("Anotasi untuk menandai method Java agar bisa dipanggil oleh model AI adalah?",
                            "@Bean", "@Tool", "@Service", "@Autowired", "@Tool"),
                    s("Method ChatClient untuk mengubah jawaban model langsung menjadi objek Java adalah?",
                            "content()", "entity()", "stream()", "toString()", "entity()"),
                    s("Komponen yang menyimpan embedding dokumen untuk RAG adalah?",
                            "VectorStore", "ChatMemory", "PromptTemplate", "ToolCallback", "VectorStore"),
                    s("Advisor bawaan Spring AI untuk RAG adalah?",
                            "QuestionAnswerAdvisor", "RetryAdvisor", "CacheAdvisor", "LoggingAdvisor", "QuestionAnswerAdvisor")),
            "uml", List.of(
                    s("Diagram UML yang menggambarkan interaksi aktor dengan fungsi sistem adalah?",
                            "Class Diagram", "Use Case Diagram", "Sequence Diagram", "Deployment Diagram", "Use Case Diagram"),
                    s("Diagram yang menunjukkan urutan pesan antar objek berdasarkan waktu adalah?",
                            "Sequence Diagram", "Activity Diagram", "Component Diagram", "Object Diagram", "Sequence Diagram"),
                    s("Relasi <<include>> pada use case berarti?",
                            "Use case opsional", "Use case selalu dijalankan sebagai bagian use case lain", "Pewarisan aktor", "Relasi asosiasi", "Use case selalu dijalankan sebagai bagian use case lain"),
                    s("Simbol belah ketupat penuh pada class diagram menyatakan?",
                            "Agregasi", "Komposisi", "Generalisasi", "Dependensi", "Komposisi")),
            "oop", List.of(
                    s("Pilar OOP yang menyembunyikan detail data dari luar kelas adalah?",
                            "Inheritance", "Encapsulation", "Polymorphism", "Abstraction", "Encapsulation"),
                    s("Kemampuan objek memiliki banyak bentuk disebut?",
                            "Polymorphism", "Encapsulation", "Composition", "Instantiation", "Polymorphism"),
                    s("Kata kunci Java untuk pewarisan kelas adalah?",
                            "implements", "extends", "inherits", "super", "extends")),
            "java", List.of(
                    s("Platform yang menjalankan bytecode Java adalah?",
                            "JDK", "JVM", "JRE Compiler", "Javac", "JVM"),
                    s("Fitur Java untuk membuat kelas data immutable yang ringkas adalah?",
                            "enum", "record", "interface", "abstract class", "record"),
                    s("File konfigurasi dependency pada proyek Maven adalah?",
                            "build.gradle", "pom.xml", "package.json", "web.xml", "pom.xml")),
            "data", List.of(
                    s("Perintah SQL untuk mengambil data dari tabel adalah?",
                            "INSERT", "SELECT", "UPDATE", "DELETE", "SELECT"),
                    s("Kunci yang mengidentifikasi setiap baris secara unik adalah?",
                            "Foreign key", "Primary key", "Index", "Candidate view", "Primary key"),
                    s("Proses mengurangi redundansi data pada basis data disebut?",
                            "Normalisasi", "Denormalisasi", "Indexing", "Replikasi", "Normalisasi"))
    );

    private QuizBank() {
    }

    static List<Map<String, Object>> soal(String topik, int jumlah) {
        String t = topik.toLowerCase(Locale.ROOT);
        List<Map<String, Object>> sumber;
        if (t.contains("spring") || t.contains(" ai") || t.startsWith("ai") || t.contains("rag")) {
            sumber = BANK.get("spring");
        } else if (t.contains("uml") || t.contains("diagram")) {
            sumber = BANK.get("uml");
        } else if (t.contains("oop") || t.contains("objek")) {
            sumber = BANK.get("oop");
        } else if (t.contains("java")) {
            sumber = BANK.get("java");
        } else if (t.contains("data") || t.contains("sql")) {
            sumber = BANK.get("data");
        } else {
            sumber = generik(topik);
        }
        List<Map<String, Object>> hasil = new ArrayList<>();
        for (int i = 0; i < Math.min(jumlah, sumber.size()); i++) {
            hasil.add(sumber.get(i));
        }
        return hasil;
    }

    private static List<Map<String, Object>> generik(String topik) {
        return List.of(
                s("Langkah pertama yang tepat saat mempelajari " + topik + " adalah?",
                        "Memahami konsep dasar", "Langsung menghafal", "Mengabaikan teori", "Menunggu ujian", "Memahami konsep dasar"),
                s("Cara terbaik menguji pemahaman tentang " + topik + " adalah?",
                        "Mengerjakan latihan soal", "Membaca judul saja", "Menyalin catatan teman", "Tidak belajar", "Mengerjakan latihan soal"),
                s("Sumber belajar " + topik + " yang paling dapat dipercaya adalah?",
                        "Dokumentasi/buku resmi", "Rumor", "Tebakan", "Iklan", "Dokumentasi/buku resmi"));
    }
}
