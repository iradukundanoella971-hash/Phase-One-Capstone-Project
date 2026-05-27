package igirepay.igire_capstoneproject.lab3.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import igirepay.igire_capstoneproject.lab1.exception.DuplicateTransactionException;
import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.SavingsAccount;
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
    private ObservableList<Transaction> allTransactions;
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
        colTransactionId.setCellValueFactory(data -> {
            UUID ref = data.getValue().getReferenceId();
            String shortId = ref == null ? "" : ref.toString().substring(0, 8).toUpperCase();
            return new SimpleStringProperty(shortId);
        });
        colReferenceId.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getReferenceId().toString()));
        colType.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getType()));
        colAmount.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%,.0f RWF", data.getValue().getAmount())));
        colDate.setCellValueFactory(data -> {
            var ts = data.getValue().getTimestamp();
            String formatted = ts == null ? "—" : ts.format(DATE_FMT);
            return new SimpleStringProperty(formatted);
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
                if (session.getActiveAccount() instanceof SavingsAccount) {
                    errorLabel.setText("Savings withdrawals include a 100 RWF fee.");
                }
            }
            case NavigationManager.MODE_TRANSFER -> {
                pageTitleLabel.setText("Transfer");
                transferPanel.setVisible(true);
                transferPanel.setManaged(true);
                transferAmountField.textProperty().addListener((obs, old, val) -> updateTransferFee(val));
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
            double fee = FeeCalculator.calculateTransferFee(amount);
            transferFeeLabel.setText(String.format("Transfer fee: %,.0f RWF", fee));
        } catch (NumberFormatException e) {
            transferFeeLabel.setText("Transfer fee: —");
        }
    }

    private void loadHistory() {
        Account account = session.getActiveAccount();
        List<Transaction> txs = appController.getTransactionHistoryForAccount(account);
        allTransactions = FXCollections.observableArrayList(txs);
        filteredTransactions = new FilteredList<>(allTransactions, tx -> true);
        historyTable.setItems(filteredTransactions);

        searchField.textProperty().addListener((obs, old, query) -> {
            String q = query == null ? "" : query.trim().toLowerCase();
            filteredTransactions.setPredicate(tx -> {
                if (q.isEmpty()) {
                    return true;
                }
                return tx.getType().toLowerCase().contains(q)
                        || tx.getReferenceId().toString().toLowerCase().contains(q)
                        || String.valueOf(tx.getAmount()).contains(q);
            });
            updateDailySummary();
        });

        updateDailySummary();
    }

    private void updateDailySummary() {
        LocalDate today = LocalDate.now();
        List<Transaction> visible = filteredTransactions.stream().toList();
        double total = visible.stream()
                .filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().toLocalDate().equals(today))
                .filter(tx -> "SUCCESS".equalsIgnoreCase(tx.getStatus()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        long count = visible.stream()
                .filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().toLocalDate().equals(today))
                .count();
        dailySummaryLabel.setText(String.format("Today: %d transaction(s) • %,.0f RWF", count, total));
    }

    @FXML
    private void handleDeposit() {
        errorLabel.setText("");
        double amount = parseAmount(depositAmountField.getText());
        if (amount <= 0) {
            errorLabel.setText("Enter a valid amount greater than 0");
            return;
        }

        try {
            Account account = session.getActiveAccount();
            UUID ref = appController.generateReferenceId();
            String result = appController.deposit(account, amount, ref);
            if (!"SUCCESS".equals(result)) {
                errorLabel.setText(result);
                return;
            }
            refreshAccount();
            AlertManager.showInfo("Deposit", String.format("Deposited %,.0f RWF\nRef: %s", amount, ref));
            depositAmountField.clear();
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
            Account account = session.getActiveAccount();
            UUID ref = appController.generateReferenceId();
            String result = appController.withdraw(account, session.getPin(), amount, ref);
            if (!"SUCCESS".equals(result)) {
                errorLabel.setText(result);
                return;
            }
            refreshAccount();
            AlertManager.showInfo("Withdraw", String.format("Withdrew %,.0f RWF\nRef: %s", amount, ref));
            withdrawAmountField.clear();
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

        Account source = session.getActiveAccount();
        if (receiver.equals(source.getAccountNumber())) {
            errorLabel.setText("Cannot transfer to the same account");
            return;
        }

        Account destination = appController.findAccountByNumber(receiver);
        if (destination == null) {
            errorLabel.setText("Receiver account not found");
            return;
        }

        try {
            UUID ref = appController.generateReferenceId();
            String result = appController.transfer(source, session.getPin(), destination, amount, ref);
            if (!"SUCCESS".equals(result)) {
                errorLabel.setText(result);
                return;
            }
            refreshAccount();
            AlertManager.showInfo("Transfer",
                    String.format("Transferred %,.0f RWF to %s\nRef: %s", amount, receiver, ref));
            transferReceiverField.clear();
            transferAmountField.clear();
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
        if (text == null || text.isBlank()) {
            return -1;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
