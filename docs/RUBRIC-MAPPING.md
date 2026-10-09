# GUVI rubric mapping

| Criterion | Code evidence |
|---|---|
| Inheritance | `Admin`, `Seller`, `Buyer` extend `User` |
| Polymorphism | subclass `roleDescription()` |
| Interface | `Dashboard` |
| Exception handling | JDBC `SQLException`, Servlet exceptions |
| Collections & generics | `List<Product>`, `ArrayList<Product>` in `ProductDAO` |
| Multithreading | `LowStockMonitor implements Runnable` |
| Synchronization | `synchronized OrderDAO.place(...)`, `SELECT ... FOR UPDATE` |
| DB operation classes | `UserDAO`, `ProductDAO`, `OrderDAO` |
| JDBC | `DB.getConnection`, `PreparedStatement`, `ResultSet`, commit/rollback |
| Web integration | Login, Admin, Dashboard, Orders and Products Servlets; JSP pages |

This mapping identifies source constructs, not independent end-to-end verification.
