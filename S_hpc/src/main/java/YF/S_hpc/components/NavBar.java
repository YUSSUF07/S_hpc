package YF.S_hpc.components;

import YF.S_hpc.views.ChatView;
import YF.S_hpc.views.HomeView;
import YF.S_hpc.views.TransactionView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;

public class NavBar extends AppLayout {
    public NavBar() {
        // Navigation bar implementation goes here
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        // Add header components like logo, title, etc.
        H2 title = new H2("S_HPC");
        title.getStyle().set("margin", "0");
        DrawerToggle toggle = new DrawerToggle();
        addToNavbar(toggle, title);
    }

    private void createDrawer() {
        // Add navigation links to the drawer
        RouterLink link = new RouterLink("Home", HomeView.class);
        RouterLink transactionsLink = new RouterLink("Transactions", TransactionView.class);
        RouterLink chatLink = new RouterLink("Chat", ChatView.class);
        Tab chatTabs = new Tab(chatLink);
        Tab homeTab = new Tab(link);
        Tab transactionTab = new Tab(transactionsLink);


        Tabs tabs = new Tabs(homeTab, transactionTab, chatTabs);
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        addToDrawer(tabs);
    }
}
