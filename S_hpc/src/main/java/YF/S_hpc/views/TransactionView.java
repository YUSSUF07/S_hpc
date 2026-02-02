// views/TransactionView.java
package YF.S_hpc.views;

import YF.S_hpc.components.NavBar;
import YF.S_hpc.entities.Transaction;
import YF.S_hpc.services.TransactionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value="transactions", layout = NavBar.class)
public class TransactionView extends VerticalLayout {

    private final TransactionService transactionService;
    private Grid<Transaction> grid = new Grid<>(Transaction.class);

    public TransactionView(@Autowired TransactionService transactionService) {
        this.transactionService = transactionService;

        configureGrid();

        add(createToolbar(), grid);
        updateList();

        setSizeFull();
    }

    private void configureGrid() {
        grid.setSizeFull();
        // NE PAS spécifier de colonnes - Vaadin les génère automatiquement
        // basées sur les propriétés de votre entité Transaction
    }

    private HorizontalLayout createToolbar() {
        Button refreshButton = new Button("Actualiser");
        refreshButton.addClickListener(e -> updateList());

        return new HorizontalLayout(refreshButton);
    }

    private void updateList() {
        grid.setItems(transactionService.getAllTransactions());
    }
}