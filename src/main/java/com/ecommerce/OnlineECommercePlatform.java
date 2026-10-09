package com.ecommerce;

import java.sql.*;



import java.util.*;

import java.io.*;

import jakarta.servlet.*;

import jakarta.servlet.annotation.*;

import jakarta.servlet.http.*;



public class OnlineECommercePlatform {



public static class OrderDAO {

    public synchronized boolean place(int buyer,int product,int qty)throws SQLException{

        Connection c=DB.getConnection();

        try{

            c.setAutoCommit(false);

            PreparedStatement p=c.prepareStatement("SELECT price,stock FROM products WHERE id=? FOR UPDATE"); p.setInt(1,product);

            ResultSet r=p.executeQuery(); if(qty<=0||!r.next()||r.getInt("stock")<qty){c.rollback();return false;}

            double total=r.getDouble("price")*qty;

            PreparedStatement u=c.prepareStatement("UPDATE products SET stock=stock-? WHERE id=?");u.setInt(1,qty);u.setInt(2,product);u.executeUpdate();

            PreparedStatement o=c.prepareStatement("INSERT INTO orders(buyer_id,product_id,quantity,total) VALUES(?,?,?,?)");

            o.setInt(1,buyer);o.setInt(2,product);o.setInt(3,qty);o.setDouble(4,total);o.executeUpdate();

            c.commit(); return true;

        }catch(SQLException e){try{c.rollback();}catch(Exception ignored){}throw e;}finally{DB.close(c);}

    }

    public ResultSet all(Connection c)throws SQLException{return c.prepareStatement(

        "SELECT o.id,o.quantity,o.total,o.status,o.ordered_at,u.name buyer,p.name product FROM orders o JOIN users u ON o.buyer_id=u.id JOIN products p ON o.product_id=p.id ORDER BY o.id DESC").executeQuery();}

    public ResultSet buyer(Connection c,int id)throws SQLException{

        PreparedStatement p=c.prepareStatement("SELECT o.*,p.name product FROM orders o JOIN products p ON o.product_id=p.id WHERE buyer_id=? ORDER BY o.id DESC");p.setInt(1,id);return p.executeQuery();

    }

    public void status(int id,String s)throws SQLException{try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("UPDATE orders SET status=? WHERE id=?")){p.setString(1,s);p.setInt(2,id);p.executeUpdate();}}

}





public static class ProductDAO {

    public List<Product> all()throws SQLException{

        List<Product> x=new ArrayList<>(); String q="SELECT * FROM products ORDER BY id DESC";

        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(q);ResultSet r=p.executeQuery()){

            while(r.next())x.add(row(r));

        } return x;

    }

    public List<Product> seller(int sellerId)throws SQLException{

        List<Product>x=new ArrayList<>(); String q="SELECT * FROM products WHERE seller_id=? ORDER BY id DESC";

        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(q)){p.setInt(1,sellerId);try(ResultSet r=p.executeQuery()){while(r.next())x.add(row(r));}}return x;

    }

    public Product one(int id)throws SQLException{

        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM products WHERE id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?row(r):null;}}

    }

    public void create(int seller,String name,String desc,double price,int stock)throws SQLException{

        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO products(seller_id,name,description,price,stock) VALUES(?,?,?,?,?)")){

            p.setInt(1,seller);p.setString(2,name);p.setString(3,desc);p.setDouble(4,price);p.setInt(5,stock);p.executeUpdate();

        }

    }

    public void updateStock(int id,int stock)throws SQLException{try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("UPDATE products SET stock=? WHERE id=?")){p.setInt(1,stock);p.setInt(2,id);p.executeUpdate();}}

    public void delete(int id)throws SQLException{try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM products WHERE id=?")){p.setInt(1,id);p.executeUpdate();}}

    private Product row(ResultSet r)throws SQLException{return new Product(r.getInt("id"),r.getInt("seller_id"),r.getString("name"),r.getString("description"),r.getDouble("price"),r.getInt("stock"));}

}





public static class UserDAO {

    public User login(String email,String password) throws SQLException {

        String q="SELECT * FROM users WHERE email=? AND password=?";

        try(Connection c=DB.getConnection(); PreparedStatement p=c.prepareStatement(q)){

            p.setString(1,email);p.setString(2,password);

            try(ResultSet r=p.executeQuery()){ if(!r.next()) return null; return make(r); }

        }

    }

    public void create(String name,String email,String password,String role) throws SQLException {

        try(Connection c=DB.getConnection(); PreparedStatement p=c.prepareStatement(

            "INSERT INTO users(name,email,password,role) VALUES(?,?,?,?)")){

            p.setString(1,name);p.setString(2,email);p.setString(3,password);p.setString(4,role);p.executeUpdate();

        }

    }

    public void delete(int id)throws SQLException{try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM users WHERE id=?")){p.setInt(1,id);p.executeUpdate();}}

    public ResultSet all(Connection c)throws SQLException{return c.prepareStatement("SELECT id,name,email,role,address,created_at FROM users ORDER BY id").executeQuery();}

    private User make(ResultSet r)throws SQLException{

        String role=r.getString("role"); int id=r.getInt("id");

        if(role.equals("ADMIN"))return new Admin(id,r.getString("name"),r.getString("email"),r.getString("password"),r.getString("address"));

        if(role.equals("SELLER"))return new Seller(id,r.getString("name"),r.getString("email"),r.getString("password"),r.getString("address"));

        return new Buyer(id,r.getString("name"),r.getString("email"),r.getString("password"),r.getString("address"));

    }

}



public static class Admin extends User {

    public Admin(int id,String n,String e,String p,String a){super(id,n,e,p,"ADMIN",a);}

    public String roleDescription(){return "Administrator Dashboard";}

}



public static class Buyer extends User {

    public Buyer(int id,String n,String e,String p,String a){super(id,n,e,p,"BUYER",a);}

    public String roleDescription(){return "Buyer Dashboard";}

}



public static interface Dashboard { String roleDescription(); }



public static class Order {

    private int id,buyerId,productId,quantity; private double total; private String status;

    public Order(int id,int buyerId,int productId,int quantity,double total,String status){

        this.id=id;this.buyerId=buyerId;this.productId=productId;this.quantity=quantity;this.total=total;this.status=status;

    }

    public int getId(){return id;} public int getBuyerId(){return buyerId;} public int getProductId(){return productId;}

    public int getQuantity(){return quantity;} public double getTotal(){return total;} public String getStatus(){return status;}

}



public static class Product {

    private int id,sellerId,stock; private String name,description; private double price;

    public Product(int id,int sellerId,String name,String description,double price,int stock){

        this.id=id;this.sellerId=sellerId;this.name=name;this.description=description;this.price=price;this.stock=stock;

    }

    public int getId(){return id;} public int getSellerId(){return sellerId;} public String getName(){return name;}

    public String getDescription(){return description;} public double getPrice(){return price;} public int getStock(){return stock;}

}



public static class Seller extends User {

    public Seller(int id,String n,String e,String p,String a){super(id,n,e,p,"SELLER",a);}

    public String roleDescription(){return "Seller Dashboard";}

}



public static abstract class User implements Dashboard {

    protected int id;

    protected String name, email, password, role, address;



    public User(int id,String name,String email,String password,String role,String address){

        this.id=id; this.name=name; this.email=email; this.password=password; this.role=role; this.address=address;

    }

    public int getId(){return id;} public String getName(){return name;} public String getEmail(){return email;}

    public String getRole(){return role;} public String getAddress(){return address;}

    public abstract String roleDescription();

}





public static class LowStockMonitor implements Runnable {

    private volatile boolean running=true;

    public void stop(){running=false;}

    public void run(){

        while(running){

            try{

                for(Product p:new ProductDAO().all()) if(p.getStock()<=5)

                    System.out.println("[LOW STOCK ALERT] "+p.getName()+" has "+p.getStock()+" units.");

                Thread.sleep(15000);

            }catch(Exception e){System.out.println("Monitor: "+e.getMessage());}

        }

    }

}



@WebServlet("/admin") public static class AdminServlet extends HttpServlet{

 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{try{

  User u=(User)q.getSession().getAttribute("user");if(u==null||!u.getRole().equals("ADMIN")){r.sendError(403);return;}

  Connection c=DB.getConnection();q.setAttribute("users",new UserDAO().all(c));q.setAttribute("orders",new OrderDAO().all(c));q.setAttribute("products",new ProductDAO().all());q.getRequestDispatcher("/admin.jsp").forward(q,r);c.close();

 }catch(Exception e){throw new ServletException(e);}}

 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{try{

  User current=(User)q.getSession().getAttribute("user"); if(current==null||!"ADMIN".equals(current.getRole())){r.sendError(403);return;} String a=q.getParameter("action"); if("createUser".equals(a))new UserDAO().create(q.getParameter("name"),q.getParameter("email"),q.getParameter("password"),q.getParameter("role"));

  if("deleteUser".equals(a))new UserDAO().delete(Integer.parseInt(q.getParameter("id")));

  if("deleteProduct".equals(a))new ProductDAO().delete(Integer.parseInt(q.getParameter("id")));

  if("status".equals(a))new OrderDAO().status(Integer.parseInt(q.getParameter("id")),q.getParameter("status"));

  r.sendRedirect("admin");

 }catch(Exception e){throw new ServletException(e);}}

}



@WebServlet("/dashboard") public static class DashboardServlet extends HttpServlet{

 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{

  User u=(User)req.getSession().getAttribute("user"); if(u==null){res.sendRedirect("index.jsp");return;}

  req.getRequestDispatcher("/dashboard.jsp").forward(req,res);

 }

}



@WebServlet("/login")

public static class LoginServlet extends HttpServlet{

 protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException,ServletException{

  try{User u=new UserDAO().login(req.getParameter("email"),req.getParameter("password"));

   if(u==null){res.sendRedirect("index.jsp?error=Invalid+email+or+password");return;}

   req.getSession().setAttribute("user",u); res.sendRedirect("dashboard");

  }catch(Exception e){throw new ServletException(e);}

 }

}



@WebServlet("/logout") public static class LogoutServlet extends HttpServlet{protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException{r.getSession().invalidate();s.sendRedirect("index.jsp");}}



@WebServlet("/orders") public static class OrderServlet extends HttpServlet{

 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{try{

  User u=(User)q.getSession().getAttribute("user"); if(u==null){r.sendRedirect("index.jsp");return;}

  Connection c=DB.getConnection(); ResultSet rs=new OrderDAO().buyer(c,u.getId()); q.setAttribute("orders",rs);q.getRequestDispatcher("/orders.jsp").forward(q,r); c.close();

 }catch(Exception e){throw new ServletException(e);}}

 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{try{

  User u=(User)q.getSession().getAttribute("user"); if(u==null||!"BUYER".equals(u.getRole())){r.sendError(403);return;}

  int pid=Integer.parseInt(q.getParameter("productId"));int qty=Integer.parseInt(q.getParameter("quantity"));

  if(!new OrderDAO().place(u.getId(),pid,qty)) r.sendRedirect("products?error=Not+enough+stock"); else r.sendRedirect("orders?success=Order+placed");

 }catch(Exception e){throw new ServletException(e);}}

}



@WebServlet("/products") public static class ProductServlet extends HttpServlet{

 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{try{if(q.getSession().getAttribute("user")==null){r.sendRedirect("index.jsp");return;}q.setAttribute("products",new ProductDAO().all());q.getRequestDispatcher("/products.jsp").forward(q,r);}catch(Exception e){throw new ServletException(e);}}

 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException,ServletException{try{

  User u=(User)q.getSession().getAttribute("user"); if(u==null||!"SELLER".equals(u.getRole())){r.sendError(403);return;}

  new ProductDAO().create(u.getId(),q.getParameter("name"),q.getParameter("description"),Double.parseDouble(q.getParameter("price")),Integer.parseInt(q.getParameter("stock")));

  r.sendRedirect("products");

 }catch(Exception e){throw new ServletException(e);}}

}



public static class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db?allowPublicKeyRetrieval=true&useSSL=false";
    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new ExceptionInInitializerError(e); }
    }
    public static Connection getConnection() throws SQLException {
        String username = System.getenv("ECOMMERCE_DB_USER");
        String password = System.getenv("ECOMMERCE_DB_PASSWORD");
        if (username == null || username.isBlank() || password == null)
            throw new SQLException("Start Tomcat with RUN_ECOMMERCE.bat to configure MySQL credentials.");
        return DriverManager.getConnection(URL, username, password);
    }
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) try { r.close(); } catch (Exception ignored) {}
        }
    }
}

}
