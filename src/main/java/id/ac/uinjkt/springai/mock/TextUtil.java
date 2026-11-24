package id.ac.uinjkt.springai.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Utilitas teks sederhana untuk mock model. */
final class TextUtil {

    private static final Set<String> STOPWORDS = Set.of(
            "yang", "dan", "di", "ke", "dari", "untuk", "pada", "adalah", "itu", "ini", "dengan",
            "apa", "apakah", "bagaimana", "berapa", "kapan", "siapa", "saya", "aku", "kamu", "the",
            "a", "an", "of", "is", "to", "in", "tidak", "juga", "atau", "akan", "bisa", "dapat",
            "tolong", "jelaskan", "sebutkan", "tentang", "mahasiswa");

    private TextUtil() {
    }

    static List<String> tokens(String text) {
        List<String> out = new ArrayList<>();
        for (String t : text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
            if (t.length() > 1 && !STOPWORDS.contains(t)) {
                out.add(t);
            }
        }
        return out;
    }
}
