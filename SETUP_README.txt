ONE-CLICK WINDOWS SETUP

1. Install and start MySQL Server 8. Install JDK 11+ (17/21 recommended). Extract Tomcat 10.1 under Downloads or Desktop.
2. Extract this project ZIP.
3. Double-click RUN_ECOMMERCE.bat.
4. Enter MySQL username and password when asked.
5. The script automatically sets JAVA_HOME for its own process, compiles, initializes missing database tables, deploys, starts Tomcat, and opens http://localhost:8080/ecommerce/ .

Java, MySQL and Tomcat must already be installed. No universal compatibility with every JDK is possible: Tomcat 10.1 requires Java 11+.

If Tomcat is elsewhere, set CATALINA_HOME to the root folder. Existing database records are not deleted. Do not run database/ecommerce.sql against existing data (it has DROP TABLE commands).
