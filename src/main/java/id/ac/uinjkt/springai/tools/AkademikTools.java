package id.ac.uinjkt.springai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Kumpulan "tool" (function calling) yang dapat diminta oleh model AI.
 * Model memutuskan kapan tool dipanggil; Spring AI yang mengeksekusi method-nya.
 */
@Component
public class AkademikTools {

    @Tool(description = "Mendapatkan hari, tanggal, dan jam saat ini di zona waktu Jakarta (WIB)")
    public String waktuSekarang() {
        return LocalDateTime.now(ZoneId.of("Asia/Jakarta"))
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm", Locale.forLanguageTag("id-ID")));
    }

    @Tool(description = "Menghitung Indeks Prestasi (IP) dari daftar nilai huruf dan SKS")
    public String hitungIp(@ToolParam(description = "Daftar nilai huruf dipisah koma, misal A,B+,B") String nilai,
                           @ToolParam(description = "Daftar SKS dipisah koma, urutan sama dengan nilai") String sks) {
        String[] n = nilai.split(",");
        String[] s = sks.split(",");
        if (n.length != s.length) {
            return "Jumlah nilai dan SKS tidak sama.";
        }
        double totalBobot = 0;
        int totalSks = 0;
        for (int i = 0; i < n.length; i++) {
            int k = Integer.parseInt(s[i].trim());
            totalBobot += bobot(n[i].trim().toUpperCase(Locale.ROOT)) * k;
            totalSks += k;
        }
        double ip = totalSks == 0 ? 0 : totalBobot / totalSks;
        return String.format(Locale.US, "IP = %.2f dari total %d SKS", ip, totalSks);
    }

    public static double bobot(String huruf) {
        return switch (huruf) {
            case "A" -> 4.0;
            case "A-" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "B-" -> 2.7;
            case "C+" -> 2.3;
            case "C" -> 2.0;
            case "D" -> 1.0;
            default -> 0.0;
        };
    }
}
