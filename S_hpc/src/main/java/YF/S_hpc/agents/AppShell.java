package YF.S_hpc.agents;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Push(transport = Transport.WEBSOCKET_XHR)
@Theme(themeClass = Lumo.class, variant = Lumo.LIGHT)
public class AppShell implements AppShellConfigurator {
    // Pas besoin de code ici
}