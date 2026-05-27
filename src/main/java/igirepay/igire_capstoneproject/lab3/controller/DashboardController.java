package igirepay.igire_capstoneproject.lab3.controller;

import java.util.List;

import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.Customer;
import igirepay.igire_capstoneproject.lab1.model.SavingsAccount;
import igirepay.igire_capstoneproject.lab1.model.Transaction;
import igirepay.igire_capstoneproject.lab3.session.UserSession;
import igirepay.igire_capstoneproject.lab3.util.AlertManager;
import igirepay.igire_capstoneproject.lab3.util.NavigationManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class DashboardController {

    private static final String WALLET_LABEL = "Wallet";
    private static final String SAVINGS_LABEL = "Savings";

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label accountTypeLabel;
    @FXML
    private Label accountNumberLabel;
    @FXML
    private ComboBox<String> accountSelector;
    @FXML
    private ListView<String> recentTransactionsList;

    private NavigationManager navigationManager;
    private UserSession session;
    private AppController appController;
    private boolean suppressAccountChange;

    public void init(NavigationManager navigationManager, UserSession session, AppController appController) {
        this.navigationManager = navigationManager;
        this.session = session;
        this.appController = appController;

        if (!session.isLoggedIn()) {
            navigationManager.goToLogin();
            return;
        }

        setupAccountSelector();
        refreshUi();
    }

    private void setupAccountSelector() {
        suppressAccountChange = true;
        accountSelector.getItems().clear();
        Customer customer = session.getCustomer();
        Account wallet = appController.findWalletForCustomer(customer);
        Account savings = appController.findSavingsForCustomer(customer);

        if (wallet != null) {
            accountSelector.getItems().add(WALLET_LABEL);
        }
        if (savings != null) {
            accountSelector.getItems().add(SAVINGS_LABEL);
        }

        Account active = session.getActiveAccount();
        if (active instanceof SavingsAccount && savings != null) {
            accountSelector.setValue(SAVINGS_LABEL);
        } else if (wallet != null) {
            accountSelector.setValue(WALLET_LABEL);
        }

        accountSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressAccountChange || newVal == null || newVal.equals(oldVal)) {
                return;
            }
            switchAccount(newVal, customer);
        });
        suppressAccountChange = false;
    }

    private void switchAccount(String choice, Customer customer) {
        if (WALLET_LABEL.equals(choice)) {
            Account wallet = appController.findWalletForCustomer(customer);
            if (wallet != null) {
                session.setActiveAccount(wallet);
                refreshUi();
            }
            return;
        }

        Account savings = appController.findSavingsForCustomer(customer);
        if (savings != null) {
            session.setActiveAccount(savings);
            refreshUi();
        }
    }

    private void refreshUi() {
        session.setActiveAccount(appController.reloadAccount(session.getActiveAccount()));

        Customer customer = session.getCustomer();
        String name = customer.getFullName();
        String firstName = name == null || name.isBlank() ? "User" : name.split("\\s+")[0];
        welcomeLabel.setText("Welcome, " + firstName);

        Account account = session.getActiveAccount();
        double balance = appController.checkBalance(account, session.getPin());
        balanceLabel.setText(balance >= 0 ? String.format("%,.0f RWF", balance) : "—");

        String masked = appController.maskAccountNumber(account.getAccountNumber());
        accountNumberLabel.setText("Account: " + masked);

        if (account instanceof SavingsAccount) {
            accountTypeLabel.setText("Savings • 100 RWF fee on withdrawals");
        } else {
            accountTypeLabel.setText("Wallet • Instant transfers");
        }

        loadRecentTransactions(account);
    }

    private void loadRecentTransactions(Account account) {
        List<Transaction> recent = appController.getRecentTransactions(account, 5);
        List<String> lines = recent.stream()
                .map(tx -> String.format("%s  %,.0f RWF  •  %s",
                        tx.getType(),
                        tx.getAmount(),
                        tx.getStatus()))
                .toList();

        if (lines.isEmpty()) {
            recentTransactionsList.setItems(FXCollections.observableArrayList("No transactions yet"));
            recentTransactionsList.setDisable(true);
        } else {
            recentTransactionsList.setDisable(false);
            recentTransactionsList.setItems(FXCollections.observableArrayList(lines));
        }

        recentTransactionsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-padding: 8 4 8 4;");
            }
        });
    }

    @FXML
    private void handleCopyAccount() {
        String number = session.getActiveAccount().getAccountNumber();
        ClipboardContent content = new ClipboardContent();
        content.putString(number);
        Clipboard.getSystemClipboard().setContent(content);
        AlertManager.showInfo("Copied", "Account number copied to clipboard.");
    }

    @FXML
    private void handleDeposit() {
        navigationManager.goToTransaction(NavigationManager.MODE_DEPOSIT);
    }

    @FXML
    private void handleWithdraw() {
        navigationManager.goToTransaction(NavigationManager.MODE_WITHDRAW);
    }

    @FXML
    private void handleTransfer() {
        navigationManager.goToTransaction(NavigationManager.MODE_TRANSFER);
    }

    @FXML
    private void handleOpenSavings() {
        Customer customer = session.getCustomer();
        Account savings = appController.findSavingsForCustomer(customer);
        if (savings != null) {
            session.setActiveAccount(savings);
            accountSelector.setValue(SAVINGS_LABEL);
            refreshUi();
            AlertManager.showInfo("Savings", "Switched to savings account.");
            return;
        }

        Account created = appController.createSavingsAccount(customer, session.getPin());
        if (created == null) {
            AlertManager.showError("Savings", "Could not open savings account.");
            return;
        }

        if (!accountSelector.getItems().contains(SAVINGS_LABEL)) {
            accountSelector.getItems().add(SAVINGS_LABEL);
        }
        session.setActiveAccount(created);
        accountSelector.setValue(SAVINGS_LABEL);
        refreshUi();
        AlertManager.showInfo("Savings", "Savings account opened successfully.");
    }

    @FXML
    private void handleHistory() {
        navigationManager.goToTransaction(NavigationManager.MODE_HISTORY);
    }

    @FXML
    private void handleProfile() {
        navigationManager.goToProfile();
    }

    @FXML
    private void handleLogout() {
        if (!AlertManager.confirm("Logout", "Are you sure you want to sign out?")) {
            return;
        }
        session.clear();
        navigationManager.goToLogin();
    }
}
