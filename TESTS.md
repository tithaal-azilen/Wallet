# Test Scenarios

This document lists all the test scenarios implemented in the Wallet application, covering unit tests for services and integration tests for critical flows.

## 1. User Service Tests (`UserServiceImplTest.java`)
These tests verify the logic for user registration, authentication, and profile management.

### Registration
- **Success**: Verifies that a user can register successfully with valid data.
- **Fail - Username Taken**: Ensures registration fails if the username already exists.
- **Fail - Email Taken**: Ensures registration fails if the email is already registered.
- **Fail - Invalid Input**: Validates that null or empty fields reject registration.

### Authentication (Login)
- **Success**: Verifies successful login with correct credentials.
- **Fail - User Not Found**: Ensures login fails for non-existent users.
- **Fail - Invalid Password**: Ensures login fails with incorrect passwords.
- **Fail - Invalid Input**: Validates that empty credentials reject login.

### User Management
- **Get All Users**: Verifies retrieval of the user list.
- **Get User Details**:
    - **Success**: Verifies fetching correct user details including wallet summaries.
    - **Fail**: Handles user not found scenarios.
- **Update User**:
    - **Full Update**: Verifies updating all allowed fields.
    - **Partial Update**: Verifies updating specific fields while keeping others unchanged.
    - **Fail**: Prevents updating email to one that is already taken.
- **Delete User**:
    - **Success**: Verifies soft/hard deletion of user and associated data.
    - **Fail**: Prevents deletion if the user has a wallet with an active balance.

### Wallet Creation
- **Add Wallet**: Verifies that a wallet can be successfully created and linked to a user.

## 2. Wallet Core Service Tests (`WalletServiceImplTest.java`)
These tests focus on fund operations and validation logic.

### Top-Up (Credit)
- **Success**: Verifies successful addition of funds to a wallet.
- **Fail - Invalid Amount**: Rejects zero or negative amounts.
- **Fail - Wallet Not Found**: Handles non-existent wallet IDs properly.

### Fund Transfer (Debit)
- **Success**: Verifies successful transfer between two valid wallets.
- **Fail - Invalid Amount**: Rejects invalid transfer amounts.
- **Fail - Same Wallet**: Prevents transferring funds to the same wallet.
- **Fail - Insufficient Balance**: Ensures transfers fail if the sender has not enough funds.
- **Fail - Wallet Not Found**: Handles scenarios where sender or receiver wallets don't exist.

### Validation
- **Ownership Check**: Verifies that operations are only performed by the wallet owner.

## 3. Transaction History Tests (`WalletHistoryServiceImplTest.java`)
These tests ensure the ledger accurately reflects transaction records.

- **Get User History**: Verifies retrieval of all transactions for a user, sorted by date.
- **Get Wallet History**: Verifies retrieval of transactions for a specific wallet.
- **Empty History**: Verifies behavior when no transactions exist.

## 4. Fee Deduction Tests (`FeeDeductionServiceTest.java`)
These tests cover the automated monthly fee logic.

- **Batch Processing**: Verifies that the service identifies and processes all eligible wallets.
- **Sufficient Balance**: Verifies fee is deducted and next deduction date is advanced.
- **Insufficient Balance**: Verifies fee is NOT deducted, but the attempt is recorded.
- **Idempotency**: Verifies that fees are not deducted twice on the same day.

## 5. Integration Tests (`EmailNotificationIntegrationTest.java`)
These tests verify the event-driven architecture for notifications.

- **Wallet Creation Email**: Verifies an email event is triggered when a wallet is created.
- **Fee Deduction Email**: Verifies an email event is triggered when a monthly fee is deducted.
