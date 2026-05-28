package igirepay.igire_capstoneproject.lab3.controller;

import igirepay.igire_capstoneproject.lab1.exception.DuplicateTransactionException;
import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.Transaction;
import igirepay.igire_capstoneproject.lab1.util.FeeCalculator;
import igirepay.igire_capstoneproject.lab1.util.ValidationUtils;
import igirepay.igire_capstoneproject.lab3.session.UserSession;
import igirepay.igire_capstoneproject.lab3.util.AlertManager;
import igirepay.igire_capstoneproject.lab3.util.NavigationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class TransactionController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private VBox depositPanel;
    @FXML
    private VBox withdrawPanel;
    @FXML
    private VBox transferPanel;
    @FXML
    private VBox historyPanel;
    @FXML
    private TextField depositAmountField;
    @FXML
    private TextField withdrawAmountField;
    @FXML
    private TextField transferReceiverField;
    @FXML
    private TextField transferAmountField;
    @FXML
    private Label transferFeeLabel;
    @FXML
    private TextField searchField;
    @FXML
    private Label dailySummaryLabel;
    @FXML
    private TableView<Transaction> historyTable;
    @FXML
    private TableColumn<Transaction, String> colTransactionId;
    @FXML
    private TableColumn<Transaction, String> colReferenceId;
    @FXML
    private TableColumn<Transaction, String> colType;
    @FXML
    private TableColumn<Transaction, String> colAmount;
    @FXML
    private TableColumn<Transaction, String> colDate;

    private NavigationManager navigationManager;
    private UserSession session;
    private AppController appController;
    private FilteredList<Transaction> filteredTransactions;

    public void init(NavigationManager navigationManager, UserSession session, AppController appController, String mode) {
        this.navigationManager = navigationManager;
        this.session = session;
        this.appController = appController;

        if (!session.isLoggedIn()) {
            navigationManager.goToLogin();
            return;
        }
        setupHistoryTable();
        showMode(mode == null ? NavigationManager.MODE_DEPOSIT : mode);
    }

    private void setupHistoryTable() {
        colTransactionId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getReferenceId().toString().substring(0, 8).toUpperCase()));
        colReferenceId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReferenceId().toString()));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f RWF", d.getValue().getAmount())));
        colDate.setCellValueFactory(d -> {
            String text = d.getValue().getTimestamp() == null ? "--" : d.getValue().getTimestamp().format(DATE_FMT);
            return new SimpleStringProperty(text);
        });
    }

    private void showMode(String mode) {
        depositPanel.setVisible(false);
        depositPanel.setManaged(false);
        withdrawPanel.setVisible(false);
        withdrawPanel.setManaged(false);
        transferPanel.setVisible(false);
        transferPanel.setManaged(false);
        historyPanel.setVisible(false);
        historyPanel.setManaged(false);
        errorLabel.setText("");

        switch (mode) {
            case NavigationManager.MODE_WITHDRAW -> {
                pageTitleLabel.setText("Withdraw");
                withdrawPanel.setVisible(true);
                withdrawPanel.setManaged(true);
            }
            case NavigationManager.MODE_TRANSFER -> {
                pageTitleLabel.setText("Transfer");
                transferPanel.setVisible(true);
                transferPanel.setManaged(true);
                transferAmountField.textProperty().addListener((o, a, b) -> updateTransferFee(b));
            }
            case NavigationManager.MODE_HISTORY -> {
                pageTitleLabel.setText("Transaction History");
                historyPanel.setVisible(true);
                historyPanel.setManaged(true);
                loadHistory();
            }
            default -> {
                pageTitleLabel.setText("Deposit");
                depositPanel.setVisible(true);
                depositPanel.setManaged(true);
            }
        }
    }

    private void updateTransferFee(String amountText) {
        try {
            double amount = Double.parseDouble(amountText.trim());
            transferFeeLabel.setText(String.format("Transfer fee: %,.0f RWF", FeeCalculator.calculateTransferFee(amount)));
        } catch (Exception e) {
            transferFeeLabel.setText("Transfer fee: --");
        }
    }

    private void loadHistory() {
        ObservableList<Transaction> all = FXCollections.observableArrayList(
                appController.getTransactionHistoryForAccount(session.getActiveAccount())
        );
        filteredTransactions = new FilteredList<>(all, tx -> true);
        historyTable.setItems(filteredTransactions);
        searchField.textProperty().addListener((obs, old, query) -> applyFilter(query == null ? "" : query));
        updateDailySummary();
    }

    private void applyFilter(String query) {
        String q = query.trim().toLowerCase();
        filteredTransactions.setPredicate(tx -> q.isEmpty()
                || tx.getType().toLowerCase().contains(q)
                || tx.getReferenceId().toString().toLowerCase().contains(q)
                || String.valueOf(tx.getAmount()).contains(q));
        updateDailySummary();
    }

    private void updateDailySummary() {
        LocalDate today = LocalDate.now();
        long count = filteredTransactions.stream()
                .filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().toLocalDate().equals(today))
                .count();
        double total = filteredTransactions.stream()
                .filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().toLocalDate().equals(today))
                .mapToDouble(Transaction::getAmount)
                .sum();
        dailySummaryLabel.setText(String.format("Today: %d transaction(s) - %,.0f RWF", count, total));
    }

    @FXML
    private void handleDeposit() {
        errorLabel.setText("");
        double amount = parseAmount(depositAmountField.getText());
        if (!ValidationUtils.isValidAmount(amount)) {
            errorLabel.setText("Enter a valid amount greater than 0");
            return;
        }
        try {
            UUID ref = appController.generateReferenceId();
            String result = appController.deposit(session.getActiveAccount(), amount, ref);
            if (!"SUCCESS".equals(result)) {
                errorLabel.setText(result);
                return;
            }
            refreshAccount();
            AlertManager.showInfo("Deposit", String.format("Deposited %,.0f RWF\nRef: %s", amount, ref));
            navigationManager.goToDashboard();
        } catch (DuplicateTransactionException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            AlertManager.showError("Deposit failed", e.getMessage());
        }
    }

    @FXML
    private void handleWithdraw() {
        errorLabel.setText("");
        double amount = parseAmount(withdrawAmountField.getText());
        if (!ValidationUtils.isValidAmount(amount)) {
            errorLabel.setText("Enter a valid amount greater than 0");
            return;
        }
        try {
            UUID ref = appController.generateReferenceId();
            String result = appController.withdraw(session.getActiveAccount(), session.getPin(), amount, ref);
            if (!"SUCCESS".equals(result)) {
                errorLabel.setText(result);
                return;
            }
            refreshAccount();
            AlertManager.showInfo("Withdraw", String.format("Withdrew %,.0f RWF\nRef: %s", amount, ref));
            navigationManager.goToDashboard();
        } catch (DuplicateTransactionException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            AlertManager.showError("Withdraw failed", e.getMessage());
        }
    }

    @FXML
    private void handleTransfer() {
        errorLabel.setText("");
        String receiver = transferReceiverField.getText() == null ? "" : transferReceiverField.getText().trim();
        double amount = parseAmount(transferAmountField.getText());
        if (receiver.isBlank()) {
            errorLabel.setText("Enter receiver account number");
            return;
        }
        if (!ValidationUtils.isValidAmount(amount)) {
            errorLabel.setText("Enter a valid amount greater than 0");
            return;
        }
        Account destination = appController.findAccountByNumber(receiver);
        if (destination == null) {
            errorLabel.setText("Receiver account not found");
            return;
        }
        try {
            UUID ref = appController.generateReferenceId();
            String result = appController.transfer(session.getActiveAccount(), session.getPin(), destination, amount, ref);
            if (!"SUCCESS".equals(result)) {
                errorLabel.setText(result);
                return;
            }
            refreshAccount();
            AlertManager.showInfo("Transfer", String.format("Transferred %,.0f RWF\nRef: %s", amount, ref));
            navigationManager.goToDashboard();
        } catch (DuplicateTransactionException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            AlertManager.showError("Transfer failed", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        navigationManager.goToDashboard();
    }

    private void refreshAccount() {
        Account refreshed = appController.reloadAccount(session.getActiveAccount());
        if (refreshed != null) {
            session.setActiveAccount(refreshed);
        }
    }

    private double parseAmount(String text) {
        try {
            return Double.parseDouble(text == null ? "" : text.trim());
        } catch (Exception e) {
            return -1;
        }
    }
}
