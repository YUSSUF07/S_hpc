package YF.S_hpc.views;

import YF.S_hpc.services.AssemblyAiService;
import YF.S_hpc.services.ChatService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("audio")
public class ChatView extends VerticalLayout {

    private final AssemblyAiService assemblyAiService;
    private final ChatService chatService;

    private TextArea chatHistory = new TextArea("Conversation");
    private TextField messageInput = new TextField();
    private Button sendButton = new Button("📤 Envoyer");
    private Button audioButton = new Button("🎤");
    private Button clearButton = new Button("🗑️");

    private boolean isRecording = false;

    public ChatView(
            @Autowired AssemblyAiService assemblyAiService,
            @Autowired ChatService chatService) {

        this.assemblyAiService = assemblyAiService;
        this.chatService = chatService;

        System.out.println("✅ ChatView créée");

        configureChatUI();
        configureButtons();

        // Layout des contrôles
        HorizontalLayout inputLayout = new HorizontalLayout(
                messageInput,
                audioButton,
                sendButton,
                clearButton
        );
        inputLayout.setWidthFull();
        inputLayout.setAlignItems(Alignment.END);
        inputLayout.setSpacing(true);

        // Ajout des composants
        add(
                new H2("💬 Assistant IA - Chat Vocal"),
                chatHistory,
                inputLayout
        );

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        appendToChat("✨ Bienvenue ! Tapez votre message ou utilisez le micro 🎤\n");
    }

    /**
     * Configuration de l'interface
     */
    private void configureChatUI() {
        // Zone de conversation
        chatHistory.setWidthFull();
        chatHistory.setHeight("500px");
        chatHistory.setReadOnly(true);

        // Champ de saisie
        messageInput.setWidthFull();
        messageInput.setPlaceholder("💬 Tapez votre message ici...");
        messageInput.setAutofocus(true);

        // Style des boutons
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        audioButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        audioButton.getStyle()
                .set("font-size", "20px")
                .set("min-width", "50px");

        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
    }

    /**
     * Configuration des actions des boutons
     */
    private void configureButtons() {
        // Bouton Envoyer
        sendButton.addClickListener(e -> sendTextMessage());

        // Touche Entrée
        messageInput.addKeyPressListener(Key.ENTER, e -> sendTextMessage());

        // Bouton Audio
        audioButton.addClickListener(e -> toggleAudioRecording());

        // Bouton Effacer
        clearButton.addClickListener(e -> {
            chatHistory.clear();
            chatService.resetMemory();
            appendToChat("✨ Nouvelle conversation démarrée\n");
            Notification.show("🗑️ Conversation effacée", 2000, Notification.Position.BOTTOM_CENTER);
        });
    }

    /**
     * Envoie un message texte
     */
    private void sendTextMessage() {
        String userMessage = messageInput.getValue();

        if (userMessage == null || userMessage.trim().isEmpty()) {
            showNotification("⚠️ Veuillez entrer un message", NotificationVariant.LUMO_WARNING);
            return;
        }

        final String finalMessage = userMessage.trim();
        System.out.println("✅ Message envoyé: " + finalMessage);

        appendToChat("\n👤 Vous: " + finalMessage + "\n");
        appendToChat("🤖 Assistant: ");

        messageInput.clear();
        setInputEnabled(false, "⏳ Réflexion...");

        new Thread(() -> {
            try {
                String response = chatService.chat(finalMessage);
                System.out.println("✅ Réponse reçue: " + response);

                getUI().ifPresent(ui -> ui.access(() -> {
                    appendToChat(response + "\n");
                    setInputEnabled(true, "📤 Envoyer");
                    scrollToBottom();
                }));

            } catch (Exception e) {
                System.err.println("❌ Erreur: " + e.getMessage());
                getUI().ifPresent(ui -> ui.access(() -> {
                    appendToChat("\n❌ Erreur: " + e.getMessage() + "\n");
                    setInputEnabled(true, "📤 Envoyer");
                }));
            }
        }).start();
    }

    /**
     * Gère l'enregistrement audio
     */
    private void toggleAudioRecording() {
        if (isRecording) {
            showNotification("⚠️ Enregistrement déjà en cours", NotificationVariant.LUMO_WARNING);
            return;
        }

        isRecording = true;
        audioButton.setText("⏹️");
        audioButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        setInputEnabled(false, "🎤 Enregistrement...");

        appendToChat("\n🎤 [Enregistrement audio - 5 secondes...]\n");

        new Thread(() -> {
            try {
                // Enregistrement audio (5 secondes par défaut)
                String transcription = assemblyAiService.recordAndTranscribe(5);
                System.out.println("📝 Transcription: " + transcription);

                getUI().ifPresent(ui -> ui.access(() -> {
                    if (transcription != null && !transcription.startsWith("Erreur")) {
                        appendToChat("📝 Transcription: " + transcription + "\n");
                        appendToChat("🤖 Assistant: ");

                        // Envoyer la transcription au chatbot
                        processTranscription(transcription);
                    } else {
                        appendToChat("❌ " + transcription + "\n");
                        resetAudioButton();
                    }
                }));

            } catch (Exception ex) {
                System.err.println("❌ Erreur audio: " + ex.getMessage());
                getUI().ifPresent(ui -> ui.access(() -> {
                    appendToChat("❌ Erreur d'enregistrement: " + ex.getMessage() + "\n");
                    resetAudioButton();
                }));
            }
        }).start();
    }

    /**
     * Traite la transcription audio
     */
    private void processTranscription(String transcription) {
        new Thread(() -> {
            try {
                String response = chatService.chat(transcription);
                System.out.println("✅ Réponse IA: " + response);

                getUI().ifPresent(ui -> ui.access(() -> {
                    appendToChat(response + "\n");
                    resetAudioButton();
                    scrollToBottom();
                }));

            } catch (Exception e) {
                System.err.println("❌ Erreur IA: " + e.getMessage());
                getUI().ifPresent(ui -> ui.access(() -> {
                    appendToChat("\n❌ Erreur: " + e.getMessage() + "\n");
                    resetAudioButton();
                }));
            }
        }).start();
    }

    /**
     * Réinitialise le bouton audio
     */
    private void resetAudioButton() {
        isRecording = false;
        audioButton.setText("🎤");
        audioButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
        audioButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        setInputEnabled(true, "📤 Envoyer");
    }

    /**
     * Active/désactive les contrôles
     */
    private void setInputEnabled(boolean enabled, String sendButtonText) {
        messageInput.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        audioButton.setEnabled(enabled);
        sendButton.setText(sendButtonText);

        if (enabled) {
            messageInput.focus();
        }
    }

    /**
     * Ajoute du texte à la conversation
     */
    private void appendToChat(String text) {
        String current = chatHistory.getValue();
        chatHistory.setValue((current == null ? "" : current) + text);
    }

    /**
     * Scroll automatique vers le bas
     */
    private void scrollToBottom() {
        chatHistory.getElement().executeJs("this.scrollTop = this.scrollHeight");
    }

    /**
     * Affiche une notification
     */
    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000, Notification.Position.BOTTOM_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}