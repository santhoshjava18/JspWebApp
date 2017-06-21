package com.santhosh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AccountDatabase {
    public Connection getConnection(){
    	Connection connection = null;
    	try{  
    		Class.forName("com.mysql.jdbc.Driver");  
    		Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/accounts","root","welcome");  
    		return con;
    		}catch(Exception e){ System.out.println(e);}  
    	return connection;
    }
    
    public void closeConnection(Connection con){
    	try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
