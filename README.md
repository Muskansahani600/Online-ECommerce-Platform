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
Windows 10/11, a **JDK 11 or newer** (JDK 17 or 21 recommended), **Apache Tomcat 10.1**, and a running **MySQL Server 8** on localhost:3306. MySQL Connector/J is bundled. These server programs must already be installed; this ZIP does not download or install them. Java 8 and Java 9/10 are not supported by Tomcat 10.1.

## One-click setup (Windows)
1. Extract the ZIP (do not run from inside the archive).
2. Ensure MySQL Server is running, and Tomcat 10.1 is extracted under Downloads/Desktop (including nested Tomcat folders), or set `CATALINA_HOME` once to its root directory.
3. Double-click `RUN_ECOMMERCE.bat`. Enter **only the MySQL username and password** when requested.
4. The script locates a compatible JDK and Tomcat, sets `JAVA_HOME`/`JRE_HOME` **for this launcher process**, compiles Java, creates missing database tables and demo accounts, deploys the site, starts Tomcat, waits for the website, and opens the default browser.
5. Visit `http://localhost:8080/ecommerce/` if the browser doesn't open. MySQL must listen on port 3306 and Tomcat on port 8080.

The launcher does **not** change Windows system-wide environment variables or download JDK/Tomcat/MySQL. This avoids requiring administrator access and avoids breaking other Java projects. Existing database records are preserved; the included `database/ecommerce.sql` is a **legacy destructive script** and must not be run on a database with data.

## Troubleshooting
- If JDK detection fails, install a JDK 11+ (not a JRE); JDK 17 or 21 recommended.
- If Tomcat is not found, extract Tomcat 10.1 under Desktop or Downloads or set `CATALINA_HOME` to the Tomcat root folder.
- If MySQL initialization fails, check MySQL service, root credentials, permissions, and port 3306.
- If Tomcat cannot start, stop the existing instance using port 8080 and check `Tomcat/logs`.
- The automatic browser opener waits for a successful HTTP response for up to 60 seconds. If the app is not healthy, check the Tomcat logs.

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
- Unknown database: run the updated launcher; it initializes the database.
- Missing `users` table: run the updated launcher to create missing tables.
- Public Key Retrieval error: check JDBC URL and local MySQL authentication settings.
- 404: confirm `run.bat` deployed into Tomcat `webapps`.
- 500: read Tomcat console exception.

