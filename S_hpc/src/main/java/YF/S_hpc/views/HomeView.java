package YF.S_hpc.views;

import YF.S_hpc.components.NavBar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = NavBar.class)
public class HomeView extends VerticalLayout {

    public HomeView() {
        H1 title = new H1("Welcome to your new application");
        Paragraph text = new Paragraph("This is the home view.");

        add(title, text);
        setSizeFull();
        setPadding(true);
    }
}
