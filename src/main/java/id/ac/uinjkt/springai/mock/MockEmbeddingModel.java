package id.ac.uinjkt.springai.mock;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Mock EmbeddingModel: mengubah teks menjadi vektor 512 dimensi memakai teknik
 * "hashing bag-of-words" (setiap kata di-hash ke salah satu dimensi), lalu dinormalisasi.
 * Teks yang memiliki banyak kata yang sama akan memiliki cosine similarity tinggi,
 * sehingga pencarian di VectorStore tetap bekerja tanpa model embedding sungguhan.
 */
public class MockEmbeddingModel implements EmbeddingModel {

    public static final int DIMENSIONS = 512;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> result = new ArrayList<>();
        List<String> inputs = request.getInstructions();
        for (int i = 0; i < inputs.size(); i++) {
            result.add(new Embedding(vectorize(inputs.get(i)), i));
        }
        return new EmbeddingResponse(result);
    }

    @Override
    public float[] embed(Document document) {
        return vectorize(document.getText());
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }

    static float[] vectorize(String text) {
        float[] v = new float[DIMENSIONS];
        if (text == null) {
            return v;
        }
        for (String token : TextUtil.tokens(text.toLowerCase(Locale.ROOT))) {
            int idx = Math.floorMod(token.hashCode(), DIMENSIONS);
            v[idx] += 1f;
        }
        double norm = 0;
        for (float x : v) {
            norm += x * x;
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < v.length; i++) {
                v[i] = (float) (v[i] / norm);
            }
        }
        return v;
    }
}
