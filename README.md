#IGIREPAY PROJECT
Project summary
IgirePay is a JavaFX mobile banking application built with Maven and PostgreSQL. It supports user registration, login, wallet and savings accounts, deposits, withdrawals, transfers, loan requests, loan repayment, profile updates, notifications, and transaction history export.
Architecture
The project is split into three layers: lab1 contains the core models, exceptions, and validation rules; lab2 contains the database services and DAO layer; lab3 contains the JavaFX screens, controllers, session handling, navigation, and alerts.
How the app works
1. The app starts in MainApplication and opens the splash screen.
2. NavigationManager loads FXML screens and blocks protected pages unless the user is logged in.
3. LoginController verifies the phone number and PIN, then stores the current customer and active account in UserSession.
4. DashboardController shows balance, recent transactions, notifications, and account switching between wallet and savings.
5. TransactionController handles deposit, withdraw, transfer, history search, and CSV export.
6. LoanController handles loan request and repayment, while ProfileController updates customer details and PIN.
