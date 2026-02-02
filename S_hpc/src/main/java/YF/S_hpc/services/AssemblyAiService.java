package YF.S_hpc.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class AssemblyAiService {

    @Value("${assemblyai.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Transcrit un fichier audio uploadé
     */
    public String transcribeFile(MultipartFile file) {
        try {
            byte[] audioBytes = file.getBytes();
            return transcribeAudio(audioBytes);
        } catch (Exception e) {
            System.err.println("❌ Erreur lecture fichier: " + e.getMessage());
            return "Erreur lors de la lecture du fichier";
        }
    }

    /**
     * Enregistre l'audio depuis le micro et le transcrit
     */
    public String recordAndTranscribe(int durationSeconds) {
        try {
            System.out.println("🎤 Début de l'enregistrement...");
            byte[] audioBytes = recordAudio(durationSeconds);
            System.out.println("✅ Enregistrement terminé, transcription en cours...");
            return transcribeAudio(audioBytes);
        } catch (Exception e) {
            System.err.println("❌ Erreur enregistrement: " + e.getMessage());
            return "Erreur lors de l'enregistrement";
        }
    }

    /**
     * Transcrit des bytes audio via AssemblyAI
     */
    public String transcribeAudio(byte[] audioBytes) {
        try {
            // 1. Upload du fichier
            String uploadUrl = uploadAudio(audioBytes);
            System.out.println("📤 Audio uploadé: " + uploadUrl);

            // 2. Demande de transcription
            String transcriptId = requestTranscription(uploadUrl);
            System.out.println("🔄 Transcription en cours (ID: " + transcriptId + ")...");

            // 3. Attendre la transcription
            String result = pollTranscription(transcriptId);
            System.out.println("✅ Transcription terminée");

            return result;

        } catch (Exception e) {
            System.err.println("❌ Erreur AssemblyAI: " + e.getMessage());
            e.printStackTrace();
            return "Erreur de transcription: " + e.getMessage();
        }
    }

    /**
     * Upload l'audio sur AssemblyAI
     */
    private String uploadAudio(byte[] audioBytes) {
        String url = "https://api.assemblyai.com/v2/upload";

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> request = new HttpEntity<>(audioBytes, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, request, Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("Erreur lors de l'upload");
        }

        return (String) response.getBody().get("upload_url");
    }

    /**
     * Demande la transcription
     */
    private String requestTranscription(String audioUrl) {
        String url = "https://api.assemblyai.com/v2/transcript";

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("audio_url", audioUrl);
        body.put("language_code", "fr"); // Français
        // Pour l'anglais : "en"

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, request, Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("Erreur lors de la demande de transcription");
        }

        return (String) response.getBody().get("id");
    }

    /**
     * Attend que la transcription soit terminée
     */
    private String pollTranscription(String transcriptId) throws InterruptedException {
        String url = "https://api.assemblyai.com/v2/transcript/" + transcriptId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", apiKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        int attempts = 0;
        int maxAttempts = 60; // 60 secondes max

        while (attempts < maxAttempts) {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new RuntimeException("Réponse vide d'AssemblyAI");
            }

            String status = (String) body.get("status");
            System.out.println("📊 Status: " + status);

            if ("completed".equals(status)) {
                String text = (String) body.get("text");
                return text != null ? text : "Aucun texte détecté";
            } else if ("error".equals(status)) {
                String error = (String) body.get("error");
                throw new RuntimeException("Erreur de transcription: " + error);
            }

            Thread.sleep(1000); // Attendre 1 seconde
            attempts++;
        }

        throw new RuntimeException("Timeout: la transcription a pris trop de temps");
    }

    /**
     * Enregistre l'audio depuis le micro
     */
    public byte[] recordAudio(int durationSeconds) throws Exception {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new Exception("Microphone non disponible");
        }

        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        System.out.println("🎤 Enregistrement en cours (" + durationSeconds + " secondes)...");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bufferSize = (int) (format.getSampleRate() * format.getFrameSize());
        byte[] buffer = new byte[bufferSize];

        long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);

        while (System.currentTimeMillis() < endTime) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                out.write(buffer, 0, bytesRead);
            }
        }

        microphone.stop();
        microphone.close();

        System.out.println("✅ Enregistrement terminé");

        return convertToWav(out.toByteArray(), format);
    }

    /**
     * Convertit les bytes audio en format WAV
     */
    private byte[] convertToWav(byte[] audioBytes, AudioFormat format) throws IOException {
        ByteArrayOutputStream wavOutput = new ByteArrayOutputStream();

        try (AudioInputStream audioInputStream = new AudioInputStream(
                new ByteArrayInputStream(audioBytes),
                format,
                audioBytes.length / format.getFrameSize()
        )) {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavOutput);
        }

        return wavOutput.toByteArray();
    }
}