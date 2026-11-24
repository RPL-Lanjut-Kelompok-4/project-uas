package id.ac.uinjkt.springai.mock;

import java.util.List;

/** Basis pengetahuan berbasis kata kunci untuk MockChatModel. */
final class KnowledgeBase {

    private record Entri(List<String> kunci, String jawaban) {
    }

    private static final List<Entri> DATA = List.of(
            new Entri(List.of("halo", "hai", "hello", "selamat pagi", "selamat siang", "selamat malam", "assalamualaikum"),
                    "Halo! Saya SiAkad AI, asisten akademik kamu. Saya bisa menjawab pertanyaan seputar perkuliahan, "
                            + "menjelaskan konsep Spring AI, mencari informasi dari dokumen panduan akademik, "
                            + "menghitung IP, dan membuat soal latihan. Mau mulai dari mana?"),
            new Entri(List.of("terima kasih", "makasih", "thanks"),
                    "Sama-sama! Semoga sukses dengan perkuliahanmu. 😊"),
            new Entri(List.of("spring ai"),
                    "Spring AI adalah proyek dari ekosistem Spring yang menyediakan abstraksi untuk membangun "
                            + "aplikasi AI di Java. Komponen utamanya antara lain ChatClient (API untuk berkomunikasi dengan model), "
                            + "ChatModel & EmbeddingModel (abstraksi model), Advisor (interceptor prompt, misal memori dan RAG), "
                            + "VectorStore, Tool Calling, dan Structured Output. Dengan abstraksi ini, penyedia model "
                            + "(OpenAI, Ollama, Anthropic, dll.) bisa diganti tanpa mengubah logika aplikasi."),
            new Entri(List.of("rag", "retrieval"),
                    "RAG (Retrieval Augmented Generation) adalah teknik menambahkan konteks dari dokumen ke dalam prompt. "
                            + "Alurnya: dokumen dipecah menjadi chunk, diubah menjadi vektor (embedding), disimpan di VectorStore. "
                            + "Saat ada pertanyaan, chunk yang paling mirip dicari lalu disisipkan ke prompt sehingga jawaban "
                            + "model didasarkan pada dokumen tersebut. Di Spring AI, ini ditangani oleh QuestionAnswerAdvisor."),
            new Entri(List.of("advisor"),
                    "Advisor di Spring AI bekerja seperti interceptor/filter: ia dapat memodifikasi request sebelum dikirim "
                            + "ke model dan memproses response sesudahnya. Contohnya MessageChatMemoryAdvisor (menambahkan riwayat chat), "
                            + "QuestionAnswerAdvisor (RAG), dan SimpleLoggerAdvisor (logging)."),
            new Entri(List.of("embedding", "vektor", "vector"),
                    "Embedding adalah representasi teks dalam bentuk vektor angka. Teks yang maknanya mirip akan memiliki "
                            + "vektor yang berdekatan (cosine similarity tinggi). Embedding dipakai untuk pencarian semantik di VectorStore."),
            new Entri(List.of("tool calling", "function calling", "tool"),
                    "Tool calling memungkinkan model meminta aplikasi menjalankan method Java tertentu. Di Spring AI, "
                            + "method cukup diberi anotasi @Tool. Model mengembalikan permintaan pemanggilan tool, Spring AI "
                            + "mengeksekusinya, lalu hasilnya dikirim kembali ke model untuk menyusun jawaban akhir."),
            new Entri(List.of("llm", "large language model", "model bahasa"),
                    "LLM (Large Language Model) adalah model AI yang dilatih dengan data teks dalam jumlah sangat besar "
                            + "sehingga mampu memahami dan menghasilkan bahasa alami. Contoh: GPT, Claude, Llama, Gemini."),
            new Entri(List.of("prompt"),
                    "Prompt adalah instruksi/masukan yang diberikan ke model AI. Di Spring AI, prompt terdiri dari beberapa "
                            + "message: SystemMessage (peran & aturan), UserMessage (pertanyaan pengguna), dan AssistantMessage (jawaban model)."),
            new Entri(List.of("uml", "diagram"),
                    "UML (Unified Modeling Language) adalah bahasa pemodelan standar untuk merancang perangkat lunak. "
                            + "Diagram yang umum dipakai: Use Case Diagram (fungsi sistem dari sudut pandang aktor), "
                            + "Class Diagram (struktur kelas), Sequence Diagram (urutan interaksi antar objek), dan Activity Diagram (alur proses)."),
            new Entri(List.of("oop", "object oriented", "berorientasi objek"),
                    "OOP memiliki 4 pilar: Encapsulation (menyembunyikan data), Inheritance (pewarisan), "
                            + "Polymorphism (satu antarmuka banyak bentuk), dan Abstraction (menyederhanakan kompleksitas)."),
            new Entri(List.of("spring boot"),
                    "Spring Boot adalah framework Java yang mempermudah pembuatan aplikasi Spring dengan auto-configuration, "
                            + "embedded server (Tomcat), dan starter dependency sehingga aplikasi bisa langsung dijalankan."),
            new Entri(List.of("java"),
                    "Java adalah bahasa pemrograman berorientasi objek yang berjalan di atas JVM sehingga bersifat "
                            + "\"write once, run anywhere\". Java banyak dipakai untuk aplikasi enterprise (J2EE/Jakarta EE), Android, dan backend."),
            new Entri(List.of("maven"),
                    "Maven adalah build tool untuk Java yang mengelola dependency melalui file pom.xml dan menjalankan "
                            + "siklus build seperti compile, test, dan package."),
            new Entri(List.of("belajar", "tips", "ujian", "uas", "uts"),
                    "Tips belajar efektif: 1) buat jadwal belajar rutin, 2) pahami konsep lalu praktikkan dengan kode, "
                            + "3) kerjakan latihan soal (coba fitur Generator Kuis), 4) diskusi dengan teman kelompok, "
                            + "dan 5) istirahat yang cukup sebelum ujian.")
    );

    private KnowledgeBase() {
    }

    static String jawab(String lower) {
        Entri terbaik = null;
        int skorTerbaik = 0;
        for (Entri e : DATA) {
            int skor = 0;
            for (String k : e.kunci()) {
                if (lower.contains(k)) {
                    skor += k.length();
                }
            }
            if (skor > skorTerbaik) {
                skorTerbaik = skor;
                terbaik = e;
            }
        }
        if (terbaik != null) {
            return terbaik.jawaban();
        }
        return "Maaf, saya belum memiliki informasi tentang itu. Coba tanyakan tentang Spring AI, RAG, "
                + "UML, OOP, atau gunakan menu Tanya Dokumen untuk mencari di panduan akademik.";
    }
}
