package id.ac.uinjkt.springai.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mock ChatModel (rule-based) yang mengimplementasikan interface {@link ChatModel} milik Spring AI.
 *
 * Tujuan: menjalankan dan menguji seluruh alur Spring AI (ChatClient, Advisor, ChatMemory, RAG,
 * Tool Calling, Structured Output) secara offline tanpa API key / GPU. Model ini TIDAK memakai
 * jaringan saraf; jawaban dihasilkan dari aturan sederhana:
 * <ol>
 *   <li>Prompt berisi instruksi format JSON (dari BeanOutputConverter) -> membuat JSON kuis.</li>
 *   <li>Prompt berisi konteks dokumen (dari QuestionAnswerAdvisor) -> memilih kalimat konteks paling relevan.</li>
 *   <li>Pertanyaan waktu / IP dan tool tersedia -> mengembalikan permintaan tool call,
 *       lalu ToolCallingManager Spring AI mengeksekusi tool dan hasilnya dirangkai jadi jawaban.</li>
 *   <li>Mengingat nama pengguna dari riwayat percakapan (ChatMemory).</li>
 *   <li>Basis pengetahuan berbasis kata kunci.</li>
 * </ol>
 */
public class MockChatModel implements ChatModel {

    public static final String MODEL_NAME = "mock-rule-based-v1";

    private static final String RAG_DELIMITER = "---------------------";
    private static final Pattern NAMA = Pattern.compile("nama (?:saya|aku) (?:adalah )?([\\p{L}]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern NILAI = Pattern.compile("(?<!\\w)(A-|B\\+|B-|C\\+|A|B|C|D|E)(?![\\w+\\-])");
    private static final Pattern ANGKA = Pattern.compile("\\b(\\d{1,2})\\s*sks\\b", Pattern.CASE_INSENSITIVE);

    private final ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ChatOptions getDefaultOptions() {
        return ToolCallingChatOptions.builder().model(MODEL_NAME).build();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        ChatResponse response = generate(prompt);
        // Loop tool calling: jika model meminta tool, Spring AI mengeksekusinya lalu model dipanggil lagi.
        if (response.hasToolCalls()) {
            ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, response);
            return call(new Prompt(result.conversationHistory(), prompt.getOptions()));
        }
        return response;
    }

    // ------------------------------------------------------------------------------------------

    private ChatResponse generate(Prompt prompt) {
        List<Message> messages = prompt.getInstructions();
        Message last = messages.get(messages.size() - 1);

        if (last instanceof ToolResponseMessage toolResponse) {
            return reply(jawabDariTool(toolResponse));
        }

        String text = last.getText() == null ? "" : last.getText();
        String lower = text.toLowerCase(Locale.ROOT);

        if (text.contains("JSON") && text.contains("$schema")) {
            return reply(buatKuisJson(text));
        }
        if (text.contains(RAG_DELIMITER)) {
            return reply(jawabRag(text));
        }

        Set<String> tools = availableTools(prompt);
        if (tools.contains("waktuSekarang") && containsAny(lower, "jam berapa", "tanggal", "hari ini", "waktu sekarang", "hari apa")) {
            return toolCall("waktuSekarang", "{}");
        }
        if (tools.contains("hitungIp") && containsAny(lower, "ip ", "ipk", "indeks prestasi", "hitung ip")) {
            String args = argumenIp(text);
            if (args != null) {
                return toolCall("hitungIp", args);
            }
            return reply("Untuk menghitung IP, tuliskan nilai dan SKS-nya. Contoh: \"hitung IP saya: A 3 sks, B+ 2 sks, B 3 sks\".");
        }

        String memori = jawabMemori(messages, text);
        if (memori != null) {
            return reply(memori);
        }
        return reply(KnowledgeBase.jawab(lower));
    }

    // ---------- Tool calling ----------

    private Set<String> availableTools(Prompt prompt) {
        Set<String> names = new HashSet<>();
        if (prompt.getOptions() instanceof ToolCallingChatOptions opts) {
            for (ToolCallback cb : opts.getToolCallbacks()) {
                names.add(cb.getToolDefinition().name());
            }
            names.addAll(opts.getToolNames());
        }
        return names;
    }

    private ChatResponse toolCall(String name, String argsJson) {
        var call = new AssistantMessage.ToolCall("call_" + UUID.randomUUID().toString().substring(0, 8),
                "function", name, argsJson);
        return wrap(new AssistantMessage("", Map.of(), List.of(call)));
    }

    private String argumenIp(String text) {
        List<String> nilai = new ArrayList<>();
        List<String> sks = new ArrayList<>();
        Matcher n = NILAI.matcher(text);
        while (n.find()) {
            nilai.add(n.group(1));
        }
        Matcher s = ANGKA.matcher(text);
        while (s.find()) {
            sks.add(s.group(1));
        }
        if (nilai.isEmpty() || nilai.size() != sks.size()) {
            return null;
        }
        try {
            return mapper.writeValueAsString(Map.of("nilai", String.join(",", nilai), "sks", String.join(",", sks)));
        } catch (Exception e) {
            return null;
        }
    }

    private String jawabDariTool(ToolResponseMessage msg) {
        StringBuilder sb = new StringBuilder();
        for (ToolResponseMessage.ToolResponse r : msg.getResponses()) {
            String data = r.responseData().replaceAll("^\"|\"$", "");
            switch (r.name()) {
                case "waktuSekarang" -> sb.append("Sekarang adalah ").append(data).append(" WIB. ");
                case "hitungIp" -> sb.append("Hasil perhitungan: ").append(data)
                        .append(". Pertahankan dan terus tingkatkan prestasimu! ");
                default -> sb.append(data);
            }
        }
        return sb.toString().trim();
    }

    // ---------- Chat memory ----------

    private String jawabMemori(List<Message> messages, String text) {
        Matcher sekarang = NAMA.matcher(text);
        if (sekarang.find() && !text.toLowerCase(Locale.ROOT).contains("siapa")) {
            return "Halo " + kapital(sekarang.group(1)) + ", senang berkenalan! Saya SiAkad AI. "
                    + "Ada yang bisa saya bantu terkait perkuliahan?";
        }
        if (text.toLowerCase(Locale.ROOT).contains("siapa nama")) {
            for (int i = messages.size() - 2; i >= 0; i--) {
                if (messages.get(i) instanceof UserMessage um && um.getText() != null) {
                    Matcher m = NAMA.matcher(um.getText());
                    if (m.find()) {
                        return "Nama kamu " + kapital(m.group(1)) + ", tadi kamu sudah memperkenalkan diri.";
                    }
                }
            }
            return "Kamu belum memberi tahu namamu di percakapan ini.";
        }
        return null;
    }

    // ---------- RAG ----------

    private String jawabRag(String text) {
        int idx = text.indexOf("Context information");
        String query = idx > 0 ? text.substring(0, idx).trim() : text;
        String context = Arrays.stream(text.split(RAG_DELIMITER))
                .max(Comparator.comparingInt(String::length)).orElse("");
        if (idx > 0) {
            String[] parts = text.split(RAG_DELIMITER);
            if (parts.length >= 3) {
                context = parts[2];
            }
        }
        List<String> queryTokens = TextUtil.tokens(query);
        record Skor(String kalimat, int skor) {
        }
        List<Skor> kandidat = new ArrayList<>();
        for (String kalimat : context.split("(?<=[.!?])\\s+|\\n+")) {
            String k = kalimat.trim();
            if (k.length() < 15) {
                continue;
            }
            List<String> kt = TextUtil.tokens(k);
            int skor = 0;
            for (String q : queryTokens) {
                if (kt.contains(q)) {
                    skor++;
                }
            }
            if (skor > 0) {
                kandidat.add(new Skor(k, skor));
            }
        }
        if (kandidat.isEmpty()) {
            return "Maaf, informasi tersebut tidak ditemukan pada dokumen yang tersedia.";
        }
        kandidat.sort(Comparator.comparingInt(Skor::skor).reversed());
        StringBuilder sb = new StringBuilder("Berdasarkan dokumen yang tersedia:\n");
        kandidat.stream().limit(2).forEach(s -> sb.append("• ").append(s.kalimat()).append('\n'));
        return sb.toString().trim();
    }

    // ---------- Structured output (kuis) ----------

    private String buatKuisJson(String text) {
        Matcher t = Pattern.compile("topik:\\s*(.+)").matcher(text);
        String topik = t.find() ? t.group(1).trim() : "Umum";
        Matcher j = Pattern.compile("Buat (\\d+) soal").matcher(text);
        int jumlah = j.find() ? Integer.parseInt(j.group(1)) : 3;
        try {
            return mapper.writeValueAsString(Map.of("topik", topik, "soal", QuizBank.soal(topik, jumlah)));
        } catch (Exception e) {
            return "{\"topik\":\"" + topik + "\",\"soal\":[]}";
        }
    }

    // ---------- Helpers ----------

    private static boolean containsAny(String text, String... keys) {
        for (String k : keys) {
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private static String kapital(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    private ChatResponse reply(String content) {
        return wrap(new AssistantMessage(content));
    }

    private ChatResponse wrap(AssistantMessage message) {
        ChatResponseMetadata metadata = ChatResponseMetadata.builder().model(MODEL_NAME).build();
        return new ChatResponse(List.of(new Generation(message)), metadata);
    }
}
