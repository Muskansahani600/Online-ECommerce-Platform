E-COMMERCE 
1. Extract the entire ZIP (do not run from inside the ZIP).
2. Ensure JDK 21, MySQL Server, and Tomcat 10.1 are installed.
3. For a NEW empty database, import database/ecommerce.sql in MySQL Workbench.
   WARNING: This SQL script DROPS existing tables and data. Never re-import over data you want to keep.
4. Double-click RUN_ECOMMERCE.bat.
6. Enter the Tomcat root folder only if asked, then MySQL username and password.
7. Keep the console open; visit http://localhost:8080/ecommerce/

The launcher compiles, copies the JDBC driver, deploys and runs Tomcat.
No password is saved in the Java file or a settings file.
Tomcat must be started from this launcher so it inherits the database credentials.
This is a classroom demo, not a production-ready online shop.
