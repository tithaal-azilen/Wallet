# Migration to Standalone + Centralized Auth Architecture

## 1. Shared Security Library (Common Alignments)
- [ ] Create a shared module/library for common security configurations (`SecurityConfig.java`, `AuthMode.java` Enum).
- [ ] Implement `JwtAuthenticationFilter` capable of downloading RSA public keys from a JWKS endpoint.
- [ ] **[MANDATORY]** Update `JwtAuthenticationFilter` to immediately reject (`401 Unauthorized`) if `userId` or `tenant_id` claims are missing.
- [ ] Implement `JitUserProvisioningInterceptor` to handle JIT creation using lightweight `SELECT 1` queries before insertion.
- [ ] Add explicit debug/info logging (`log.info("JIT user created: userId={}, tenantId={}")`) in the Interceptor.

## 2. Refactoring Existing Services (e.g., Wallet)
- [ ] Rename existing entity references from `orgId` to `tenant_id` for future-proofing.
- [ ] **[MANDATORY]** Add database constraints: `UNIQUE(user_id)` and `UNIQUE(tenant_id)` to prevent race conditions during concurrent JIT operations.
- [ ] Make `User.passwordHash` nullable in the database schema/entity to support JIT users without passwords.
- [ ] Wrap local `AuthController` behind a configuration switch using the `AuthMode` Enum (`@ConditionalOnProperty(name="app.auth.mode", havingValue="STANDALONE")`).

## 3. Developing the Central Auth Microservice (Control Plane)
- [ ] Bootstrap the new Auth Service standalone project.
- [ ] Configure RSA key pair generation and expose a standard JWKS endpoint (`/.well-known/jwks.json`).
- [ ] Implement Tenant onboarding and initial `TENANT_ADMIN` creation logic (`POST /api/auth/tenants`).
- [ ] Implement standard Registration (`POST /api/auth/users/register`) and Login (`POST /api/auth/login`) logic.

## 4. Verification and Testing
- [ ] Test Wallet service in `STANDALONE` mode.
- [ ] Test Wallet service in `CENTRALIZED` mode (verify RSA Key fetching, fail-safe 401s, lightweight `SELECT 1` checks, and JIT creation logs).
