package br.com.uepb.bsc7.www;
import java.sql.*;

public class ConexaoMySQL {
	
	public Connection getConnection(){
		try{
			return DriverManager.getConnection("jdbc:mysql://localhost/inventario", "root", "wlisses");
		}
		catch (SQLException e){
			 throw new RuntimeException(e);
		}
	}
}

