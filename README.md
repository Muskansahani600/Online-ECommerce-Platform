# Online E-Commerce Platform



## Features
- Admin: user management, product/order overview, order status.
- Seller: product creation and stock management.
- Buyer: product browsing, ordering and order history.
- Java: `Admin`, `Seller`, `Buyer` extend `User`; `Dashboard` interface; polymorphic `roleDescription()`; `List<Product>`; `LowStockMonitor implements Runnable`; synchronized order placement.
- JDBC: `UserDAO`, `ProductDAO`, `OrderDAO`, prepared statements, transactions.

## Structure
- `src/main/java/com/ecommerce/OnlineECommercePlatform.java`: application Java source (nested classes).
- `web/`: JSP, CSS and Tomcat resources.
- `database/ecommerce.sql`: database schema and sample data.
- `lib/mysql-connector-j.jar`: local JDBC driver.
- `run.bat`: Windows compilation and deployment.
- `docs/`: review notes and checklist.

## Requirements
JDK 21, MySQL Server 8, Apache Tomcat 10.1, MySQL Connector/J and Windows CMD.

## Setup
1. Connect to MySQL using Workbench. For a **fresh demo database only**, run `database/ecommerce.sql`. WARNING: the SQL script drops existing demo tables; do not re-run on data you need to keep.
2. Edit the nested `DB` class in `src/main/java/com/ecommerce/OnlineECommercePlatform.java`: set your own MySQL password and JDBC URL for `ecommerce_db`. The local-only URL `jdbc:mysql://localhost:3306/ecommerce_db?allowPublicKeyRetrieval=true&useSSL=false` can help with local authentication; use TLS in production.
3. Ensure `lib/mysql-connector-j.jar` is present.
4. Stop Tomcat, run `run.bat`, and enter the Tomcat **root** path, not the `bin` directory. Wait for successful deployment.
5. Start Tomcat using `bin\catalina.bat run`, keep the CMD open, and visit `http://localhost:8080/ecommerce/`.
6. In MySQL Workbench, verify with `USE ecommerce_db; SHOW TABLES;`.

## Demo accounts (from SQL seed)
| Role | Email | Password |
|---|---|---|
| Admin | admin@gmail.com | admin123 |
| Seller | seller@gmail.com | seller123 |
| Buyer | buyer@gmail.com | buyer123 |

## Reviewer walkthrough
Login with each role; demonstrate Admin management, Seller inventory, Buyer ordering and order history; show MySQL tables; explain Java OOP and DAO classes using `docs/RUBRIC-MAPPING.md`.

## Notes and limitations
This is a classroom demo, not a production store. Demo credentials are stored in plaintext: do not expose the application publicly as-is. The wishlist and browsing-history **tables** exist, but complete UI functionality is not verified. One large Java source file is less modular than separate source files. Screenshots and runtime validation must be captured on the student's own running installation.

## Common errors
- Unknown database: import SQL.
- Missing `users` table: run full schema in Workbench.
- Public Key Retrieval error: check JDBC URL and local MySQL authentication settings.
- 404: confirm `run.bat` deployed into Tomcat `webapps`.
- 500: read Tomcat console exception.

