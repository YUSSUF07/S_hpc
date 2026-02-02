package YF.S_hpc.views;

import YF.S_hpc.agents.RealtimeAudioTranscriptionService;
import YF.S_hpc.services.ChatService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Route("chat")
public class ChatView extends VerticalLayout {

    private final ChatService chatService;
    private final RealtimeAudioTranscriptionService transcriptionService;

    // 🔒 POOL DE THREADS CONTRÔLÉ (CRITIQUE)
    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(4);

    private final TextArea chatHistory = new TextArea("Conversation");
    private final TextField messageInput = new TextField();
    private final Button sendButton = new Button("Envoyer");
    private final Button clearButton = new Button("Effacer");
    private final Button micButton = new Button("🎤 Micro");

    public ChatView(@Autowired ChatService chatService,
                    @Autowired RealtimeAudioTranscriptionService transcriptionService) {

        this.chatService = chatService;
        this.transcriptionService = transcriptionService;

        configureChatUI();
        configureButtons();

        HorizontalLayout inputLayout = new HorizontalLayout(
                messageInput, sendButton, clearButton, micButton
        );
        inputLayout.setWidthFull();
        inputLayout.setAlignItems(Alignment.END);

        add(
                new H2("💬 Assistant IA – LLaMA 3"),
                chatHistory,
                inputLayout
        );

        setSizeFull();
        appendToChat("✨ Bienvenue ! Parlez ou écrivez votre question.\n");
    }

    private void configureChatUI() {
        chatHistory.setWidthFull();
        chatHistory.setHeight("500px");
        chatHistory.setReadOnly(true);

        messageInput.setWidthFull();
        messageInput.setPlaceholder("Posez votre question...");
        messageInput.setAutofocus(true);

        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clearButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        micButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    }

    private void configureButtons() {
        sendButton.addClickListener(e -> sendMessage());

        messageInput.addKeyDownListener(event -> {
            if (event.getKey().equals(Key.ENTER)
                    && !event.getModifiers().contains(KeyModifier.SHIFT)) {
                sendMessage();
            }
        });

        clearButton.addClickListener(e -> {
            chatHistory.clear();
            chatService.resetMemory();
            appendToChat("✨ Nouvelle conversation\n");
        });

        micButton.addClickListener(e -> startMicrophone());
    }

    // 🎤 Capture micro navigateur (court & safe)
    private void startMicrophone() {
        appendToChat("🎤 Écoute en cours...\n");

        UI.getCurrent().getPage().executeJs("""
            navigator.mediaDevices.getUserMedia({ audio: true }).then(stream => {
                const recorder = new MediaRecorder(stream, { mimeType: 'audio/webm' });
                const chunks = [];

                recorder.ondataavailable = e => chunks.push(e.data);
                recorder.onstop = () => {
                    const blob = new Blob(chunks, { type: 'audio/webm' });

                    if (blob.size > 2_000_000) {
                        console.warn("Audio trop long");
                        return;
                    }

                    const reader = new FileReader();
                    reader.onloadend = () => {
                        const base64 = reader.result.split(',')[1];
                        $0.$server.receiveAudio(base64);
                    };
                    reader.readAsDataURL(blob);
                };

                recorder.start();
                setTimeout(() => recorder.stop(), 3000);
            });
        """, getElement());
    }

    // 🔥 APPEL JS → SERVEUR (POOL DE THREADS)
    @ClientCallable
    public void receiveAudio(String base64Audio) {
        EXECUTOR.submit(() -> {
            try {
                String text = transcriptionService.transcribeBase64Audio(base64Audio);
                getUI().ifPresent(ui ->
                        ui.access(() -> appendToChat("📝 " + text + "\n"))
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void sendMessage() {
        String msg = messageInput.getValue();
        if (msg == null || msg.isBlank()) {
            Notification.show("Message vide");
            return;
        }

        appendToChat("\n👤 Vous: " + msg + "\n🤖 Assistant: ");
        messageInput.clear();
        messageInput.setEnabled(false);
        sendButton.setEnabled(false);

        EXECUTOR.submit(() -> {
            try {
                String response = chatService.chat(msg);
                getUI().ifPresent(ui -> ui.access(() -> {
                    appendToChat(response + "\n");
                    resetInput();
                }));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void appendToChat(String text) {
        chatHistory.setValue(chatHistory.getValue() + text);
    }

    private void resetInput() {
        messageInput.setEnabled(true);
        sendButton.setEnabled(true);
        messageInput.focus();
    }
}
