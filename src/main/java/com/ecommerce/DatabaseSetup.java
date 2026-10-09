package com.ecommerce;
import java.sql.*;
public class DatabaseSetup {
    static void exec(Statement s, String sql) throws SQLException { s.execute(sql); }
    public static void main(String[] args) {
        String user=System.getenv("ECOMMERCE_DB_USER"), pass=System.getenv("ECOMMERCE_DB_PASSWORD");
        if(user==null || pass==null) {System.err.println("Missing MySQL credentials.");System.exit(1);}
        String base="jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection c=DriverManager.getConnection(base,user,pass);Statement s=c.createStatement()) {
                exec(s,"CREATE DATABASE IF NOT EXISTS ecommerce_db");
                exec(s,"USE ecommerce_db");
                exec(s,"CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(100) NOT NULL,email VARCHAR(120) UNIQUE NOT NULL,password VARCHAR(100) NOT NULL,role ENUM('ADMIN','SELLER','BUYER') NOT NULL,address VARCHAR(255) DEFAULT '',created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB");
                exec(s,"CREATE TABLE IF NOT EXISTS products (id INT AUTO_INCREMENT PRIMARY KEY,seller_id INT NOT NULL,name VARCHAR(150) NOT NULL,description VARCHAR(500),price DECIMAL(10,2) NOT NULL,stock INT NOT NULL DEFAULT 0,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB");
                exec(s,"CREATE TABLE IF NOT EXISTS orders (id INT AUTO_INCREMENT PRIMARY KEY,buyer_id INT NOT NULL,product_id INT NOT NULL,quantity INT NOT NULL,total DECIMAL(10,2) NOT NULL,status ENUM('PLACED','PROCESSING','SHIPPED','DELIVERED','CANCELLED') DEFAULT 'PLACED',ordered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE) ENGINE=InnoDB");
                exec(s,"CREATE TABLE IF NOT EXISTS wishlist (buyer_id INT NOT NULL,product_id INT NOT NULL,PRIMARY KEY (buyer_id,product_id),FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE) ENGINE=InnoDB");
                exec(s,"CREATE TABLE IF NOT EXISTS browsing_history (id INT AUTO_INCREMENT PRIMARY KEY,buyer_id INT NOT NULL,product_id INT NOT NULL,viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE) ENGINE=InnoDB");
                try(PreparedStatement q=c.prepareStatement("INSERT INTO users(name,email,password,role,address) SELECT ?,?,?,?,? WHERE NOT EXISTS (SELECT 1 FROM users WHERE email=?)")) {
                    String[][] accounts={{"System Admin","admin@gmail.com","admin123","ADMIN","Main Office"},{"Demo Seller","seller@gmail.com","seller123","SELLER","Seller Office"},{"Demo Buyer","buyer@gmail.com","buyer123","BUYER","Delhi"}};
                    for(String[] a:accounts){for(int i=0;i<5;i++)q.setString(i+1,a[i]);q.setString(6,a[1]);q.executeUpdate();}
                }
                try(PreparedStatement id=c.prepareStatement("SELECT id FROM users WHERE email='seller@gmail.com'");ResultSet r=id.executeQuery()) {
                    if(r.next()) {int seller=r.getInt(1);
                        try(PreparedStatement count=c.prepareStatement("SELECT COUNT(*) FROM products WHERE seller_id=?")) {
                            count.setInt(1,seller);
                            try(ResultSet n=count.executeQuery()) {n.next();if(n.getInt(1)==0) {
                                try(PreparedStatement add=c.prepareStatement("INSERT INTO products(seller_id,name,description,price,stock) VALUES(?,?,?,?,?)")) {
                                    Object[][] items={{"Laptop","Core i5 laptop for students and professionals",55000,10},{"Smartphone","5G smartphone with large display",25000,20},{"Headphones","Wireless noise-reduction headphones",2500,30},{"Keyboard","Mechanical USB keyboard",1800,4}};
                                    for(Object[] item:items){add.setInt(1,seller);add.setString(2,(String)item[0]);add.setString(3,(String)item[1]);add.setInt(4,(Integer)item[2]);add.setInt(5,(Integer)item[3]);add.executeUpdate();}
                                }
                            }}
                        }
                    }
                }
                System.out.println("Database ready. Existing tables and records were preserved.");
            }
        }catch(Exception e){System.err.println("DATABASE SETUP FAILED: "+e.getMessage());System.exit(1);}
    }
}
