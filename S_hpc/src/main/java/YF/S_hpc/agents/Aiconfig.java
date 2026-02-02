package YF.S_hpc.agents;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
public class Aiconfig {

    private static final Logger log = LoggerFactory.getLogger(Aiconfig.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("classpath:/docs/doc.pdf") // chemin du CV ou PDF
    private Resource pdfResource;

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database(dbName)
                .user(dbUser)
                .password(dbPassword)
                .table("document_embeddings")
                .dimension(384)
                .createTable(true)
                .dropTableFirst(false)
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                             EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.6)
                .build();
    }

    @PostConstruct
    public void loadDocuments() {
        try {
            log.info("📄 Début de l'ingestion du CV...");

            if (!pdfResource.exists()) {
                log.error("❌ Fichier CV introuvable : " + pdfResource.getFilename());
                return;
            }

            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = parser.parse(pdfResource.getInputStream());

            var splitter = DocumentSplitters.recursive(500, 50);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(splitter)
                    .embeddingModel(embeddingModel())
                    .embeddingStore(embeddingStore())
                    .build();

            ingestor.ingest(document);

            log.info("✅ CV indexé avec succès dans PgVector");

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'ingestion du CV", e);
        }
    }

    /**
     * Méthode générique pour ingérer des documents PDF avec images
     */
    public void loadDataIntoVectorStore(Resource resource, ChatLanguageModel chatModel) {
        try {
            if (!resource.exists()) {
                log.error("❌ Ressource introuvable : " + resource.getFilename());
                return;
            }

            PDDocument pdDocument = PDDocument.load(resource.getInputStream());
            PDFTextStripper pdfStripper = new PDFTextStripper();

            int pageIndex = 0;

            for (PDPage pdPage : pdDocument.getPages()) {
                pageIndex++;
                pdfStripper.setStartPage(pageIndex);
                pdfStripper.setEndPage(pageIndex);

                String pageText = pdfStripper.getText(pdDocument);

                PDResources pdResources = pdPage.getResources();
                int imageIndex = 0;

                if (pdResources != null) {
                    for (var xObjectName : pdResources.getXObjectNames()) {
                        imageIndex++;

                        var pdImage = pdResources.getXObject(xObjectName);

                        if (pdImage instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) {
                            BufferedImage bImage = ((org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) pdImage).getImage();

                            // Convert image to Base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(bImage, "png", baos);
                            String imageBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

                            Image img = new Image.Builder()
                                    .base64Data(imageBase64)
                                    .mimeType("image/png")
                                    .build();

                            ImageContent imageContent = ImageContent.from(img);

                            UserMessage userMessage = UserMessage.from(
                                    TextContent.from("Voici une image extraite du document : "),
                                    imageContent
                            );

                            Response<AiMessage> response = chatModel.generate(userMessage);
                            String imageDescription = response.content().text();

                            pageText += "\nDescription de l'image " + imageIndex + ": " + imageDescription;
                        }
                    }
                }

                Metadata metadata = new Metadata();
                metadata.add("source", "page_" + pageIndex);

                Document pageDoc = new Document(pageText, metadata);
EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                        .documentSplitter(DocumentSplitters.recursive(500, 50))
                        .embeddingModel(embeddingModel())
                        .embeddingStore(embeddingStore())
                        .build();
                ingestor.ingest(pageDoc);            }

            pdDocument.close();
            log.info("✅ Document avec images ingéré avec succès");

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'ingestion du document avec images", e);
        }
    }
}
