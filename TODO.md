# IGIREPAY Capstone - Lab 2 (JDBC + PostgreSQL)

## Plan Steps
- [x] Update `pom.xml` with PostgreSQL JDBC dependency
- [x] Update `module-info.java` for PostgreSQL and export new `lab2` packages

- [x] Create PostgreSQL schema SQL for: `customers`, `accounts`, `transactions`, `processed_requests`


- [ ] Create `lab2/config/DatabaseConnection.java`
- [ ] Create DAO interfaces in `lab2/dao`
- [ ] Implement DAOs in `lab2/dao/impl` using `PreparedStatement` only
- [ ] Create Lab2 services in `lab2/service` with required business rules + idempotency via `processed_requests`
- [ ] Create `lab2/Main.java` console menu delegating to services
- [ ] Build & run checks (mvn package / run main)

## Notes
- Do NOT modify Lab 1.
- Do NOT recreate Lab 1 model classes in Lab 2.
- Reuse models from `lab1.model.*`.

