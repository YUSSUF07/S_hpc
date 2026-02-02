package YF.S_hpc.agents;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CvService {

    public String extractName(Resource cvResource) {
        try {
            Document document = new ApachePdfBoxDocumentParser().parse(cvResource.getInputStream());
            String text = document.text();

            // Pattern simple pour Nom ou Name
            Pattern pattern = Pattern.compile("(Nom|Name)\\s*[:\\-]\\s*([A-Za-zÀ-ÖØ-öø-ÿ\\s]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(2).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Nom non trouvé";
    }
}
