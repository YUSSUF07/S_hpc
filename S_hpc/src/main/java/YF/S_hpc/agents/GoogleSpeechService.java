package YF.S_hpc.agents;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class GoogleSpeechService {

    /**
     * Transcription audio depuis le navigateur (WEBM/OPUS)
     */
    public String transcribeBase64Webm(String base64Audio) {

        try (SpeechClient speechClient = SpeechClient.create()) {

            // 1️⃣ Décodage Base64
            byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
            ByteString audioData = ByteString.copyFrom(audioBytes);

            // 2️⃣ Configuration Google Speech
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setSampleRateHertz(16000)
                    .setLanguageCode("fr-FR")
                    .setEnableAutomaticPunctuation(true)
                    .build();

            // 3️⃣ Audio
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioData)
                    .build();

            // 4️⃣ Requête
            RecognizeResponse response = speechClient.recognize(config, audio);

            // 5️⃣ Résultat
            StringBuilder transcription = new StringBuilder();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                transcription.append(result.getAlternatives(0).getTranscript());
            }

            return transcription.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Erreur Google Speech : " + e.getMessage();
        }
    }
}
