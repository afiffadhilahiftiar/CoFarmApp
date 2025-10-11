package com.example.contractfarmingapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ImageClassifier {
    private final Interpreter interpreter;
    private final List<String> labels;

    public ImageClassifier(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context, "rice_disease_model.tflite"));
        labels = loadLabels(context, "labels.txt");
    }

    public ClassificationResult classify(Bitmap image) {
        Bitmap resized = Bitmap.createScaledBitmap(image, 224, 224, true);
        float[][][][] input = new float[1][224][224][3];

        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                int pixel = resized.getPixel(x, y);
                input[0][y][x][0] = Color.red(pixel) / 255.0f;
                input[0][y][x][1] = Color.green(pixel) / 255.0f;
                input[0][y][x][2] = Color.blue(pixel) / 255.0f;
            }
        }

        float[][] output = new float[1][labels.size()];
        interpreter.run(input, output);

        int maxIndex = 0;
        float maxConfidence = output[0][0];
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > maxConfidence) {
                maxConfidence = output[0][i];
                maxIndex = i;
            }
        }

        String label = labels.get(maxIndex);
        int confidencePercent = Math.round(maxConfidence * 100);

        // Dapatkan detail penyakit
        DiseaseDetail detail = getDiseaseDetail(label);

        return new ClassificationResult(label, confidencePercent, detail.description, detail.symptoms, detail.cause);
    }

    private MappedByteBuffer loadModelFile(Context context, String modelFile) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabels(Context context, String labelFile) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(labelFile)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line.trim());
        }
        reader.close();
        return labelList;
    }

    // Deskripsi penyakit
    private DiseaseDetail getDiseaseDetail(String label) {
        switch (label) {
            case "Bacterial_leaf_blight":
                return new DiseaseDetail(
                        "❗ Hawar Daun Bakteri (Bacterial Leaf Blight) adalah penyakit bakteri serius yang menyerang daun padi",
                        "- Daun menguning dari ujung ke bawah, seperti terkena air rebusan, lalu menjadi coklat nekrotik.\n" +
                                "- Jika menyerang saat fase awal, pertumbuhan padi terganggu berat.\n\n" +
                                "➤ Faktor pendukung:\n" +
                                "- Kelembaban tinggi, hujan, pemupukan nitrogen berlebih, aliran air dari lahan terinfeksi.",
                        "- Disebabkan oleh bakteri Xanthomonas oryzae pv. oryzae, menyebar cepat melalui percikan air hujan.\n" +
                                "- Masuk melalui stomata atau luka kecil di daun.\n" +
                                "➤ Pengendalian Terpadu:\n" +
                                "- Gunakan varietas tahan (Inpari 32, IR64, Situ Patenggang)\n" +
                                "- Pupuk seimbang, hindari urea berlebih\n" +
                                "- Sanitasi lahan, jangan pakai benih sisa panen\n" +
                                "- Semprot bakterisida: streptomisin atau oksitetrasiklin"
                );
            case "Rice_Blast":
                return new DiseaseDetail(
                        "🔥 Penyakit jamur serius yang menyerang seluruh bagian tanaman.",
                        "Bercak berbentuk belah ketupat pada daun, bagian tengah abu-abu dan tepi coklat. Jika menyerang leher malai, batang mudah rebah, gabah kosong.\n" +
                                "➤ Lingkungan mendukung:\n" +
                                "Kelembaban >90%, suhu malam 20–25°C, tanaman terlalu rapat, drainase buruk.",
                        "Jamur (Magnaporthe oryzae), menyebar lewat angin, bisa membentuk ras baru yang tahan fungisida.\n" +
                                "➤ Pengendalian Terpadu:\n" +
                                "- Gunakan varietas tahan (Ciherang, Inpari 13, Situ Bagendit)\n" +
                                "- Atur jarak tanam\n" +
                                "- Semprot fungisida sistemik: azoxystrobin, tricyclazole, propiconazole\n" +
                                "- Lakukan rotasi tanaman"
                );
            case "Tungro":
                return new DiseaseDetail(
                        "🦠 Penyakit virus yang ditularkan oleh wereng hijau.",
                        "Daun kuning-oranye dari bawah ke atas, kaku dan menegang, tanaman kerdil dan malai kosong.\n" +
                        "➤ Faktor pendukung:\n" +
                                "Tanam tidak serempak, varietas rentan, wereng tidak terkendali.",
                        "Disebabkan oleh kombinasi virus RTSV dan RTBV, ditularkan oleh vektor wereng hijau.\n" +
                                "➤ Pengendalian Terpadu:\n" +
                                "- Gunakan varietas tahan (Inpari 30, Inpari 42 Agritan)\n" +
                                "- Tanam serempak\n" +
                                "- Semprot insektisida saat ambang kendali (≥5 ekor/rumpun): imidacloprid, buprofezin, acetamiprid"
                );
            case "Brownspot":
                return new DiseaseDetail(
                        "🔘 Penyakit bercak coklat yang merusak daun dan butir padi.",
                        "Bercak bulat atau oval coklat pada daun, bisa meluas dan menyebabkan daun mongering.\n" +
                                "➤ Faktor pemicu:\n" +
                                "Kekurangan kalium (K) dan zink (Zn), benih tidak sehat, drainase buruk.",
                        "Disebabkan oleh jamur *Bipolaris oryzae*, diperparah oleh kekurangan nutrisi.\n" +
                                "➤ Pengendalian Terpadu:\n" +
                                "- Gunakan benih bersertifikat, rendam dengan fungisida\n" +
                                "- Tambah pupuk Zn dan K bila defisiensi\n" +
                                "- Semprot fungisida: mancozeb (kontak) atau difenoconazole (sistemik)"
                );
            default:
                return new DiseaseDetail("Tidak dikenal", "-", "-");
        }
    }

    public static class ClassificationResult {
        public final String label;
        public final int confidence;
        public final String description;
        public final String symptoms;
        public final String cause;

        public ClassificationResult(String label, int confidence, String description, String symptoms, String cause) {
            this.label = label;
            this.confidence = confidence;
            this.description = description;
            this.symptoms = symptoms;
            this.cause = cause;
        }
    }

    private static class DiseaseDetail {
        public final String description;
        public final String symptoms;
        public final String cause;

        public DiseaseDetail(String description, String symptoms, String cause) {
            this.description = description;
            this.symptoms = symptoms;
            this.cause = cause;
        }
    }
}
