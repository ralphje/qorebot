package qorebot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A singleton class representing a database connection with a SQL server.
 *
 * @author Ralph Broenink
 */
public class Database {
	private String connectionUrl = null;
    private static Database instance = null;
    private Connection conn = null;

    /**
     * Creates a Database instance. Can only be used locally, since it's a 
     * singleton.
     */
    private Database() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE,
                    "Could not load JDBC driver.", ex);
        }
    }

    /**
     * Shortcut for Database.getInstance().getConnection()
     *
     * @see Database#getInstance()
     * @see Database#getConnection()
     */
    public static Connection gc() {
        return Database.getInstance().getConnection();
    }

    /**
     * Shortcut for Database.getInstance().getStatement()
     *
     * @see Database#getInstance()
     * @see Database#getStatement()
     */
    public static Statement gs() {
        return Database.getInstance().getStatement();
    }

    /**
     * Shortcut for Database.getInstance().getPreparedStatement(sql)
     *
     * @see Database#getInstance()
     * @see Database#getPreparedStatement(java.lang.String)
     */
    public static PreparedStatement gps(String sql) {
        return Database.getInstance().getPreparedStatement(sql);
    }

    /**
     * Creates a new instance of the Database connection class or, if it already
     * exists, the existing instance.
     */
    public static Database getInstance() {
        if (Database.instance == null)
            Database.instance = new Database();
        return Database.instance;
    }

    /**
     * Will determine whether the current connection still works and returns
     * the old one or creates a new one when neccesary.
     * When a connection cannot be made, returns null.
     * @return if current connection is active: result == {@link Database#getCurrentConnection()};
     *         else: result == {@link Database#getNewConnection()}
     */
    public Connection getConnection() {
        if (!this.isActiveConnection()) {
            if (this.conn != null)
                Logger.getLogger(Database.class.getName()).log(Level.WARNING,
                        "Link to database lost. New link will be set up.");
            return this.getNewConnection();
        } else {
            return this.getCurrentConnection();
        }
    }

    /**
     * Returns the current connection; might be null, closed or dead.
     */
    public Connection getCurrentConnection() {
        return this.conn;
    }

    /**
     * Creates a new connection and returns it, or null if connecting fails.
     */
    public Connection getNewConnection() {
        if (this.createConnection()) {
            return this.conn;
        } else {
            return null;
        }
    }

    /**
     * Returns a new statement for the current connection. If the connection does
     * not exist or is invalid, it will create a new connection. Returns null
     * if connection fails.
     *
     * @see Database#getConnection()
     */
    public Statement getStatement() {
        Connection cnt = this.getConnection();
        if (cnt == null)
            return null;
        else
            try {
                return cnt.createStatement();
            } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
    }

    /**
     * Returns a new statement for the current connection. If the connection does
     * not exist or is invalid, it will create a new connection. Returns null
     * if connection fails.
     *
     * @param sql The partial sql query.
     * @see Database#getConnection()
     */
    public PreparedStatement getPreparedStatement(String sql) {
        Connection cnt = this.getConnection();
        if (cnt == null)
            return null;
        else
            try {
                return cnt.prepareStatement(sql);
            } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
    }

    /**
     * Checks whether the current connection is active. Method may be expensive.
     * 
     * @return True if the connection is active. 
     */
    private boolean isActiveConnection() {
        Statement s = null;
        ResultSet rs = null;
        try {
            if (this.conn == null || this.conn.isClosed()) {
                return false;
            } else {
                s = conn.createStatement();
                // Oracle should use SELECT 1 FROM Dual
                rs = s.executeQuery("SELECT 1");
                return rs.next();
            }
        } catch (SQLException ex) {
            // This is not exceptional; this is supposed to happen.
            return false;
        } finally {
            try { if (s != null)  s.close();  } catch (SQLException ex) { }
            try { if (rs != null) rs.close(); } catch (SQLException ex) { }
        }
    }

    /**
     * Creates a new connection to the database.
     * 
     * @return True if connection succeeded.
     */
    private boolean createConnection() {
        try {
            this.conn = DriverManager.getConnection(this.getConnectionUrl());
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE,
                    "Could not create link to database.", ex);
            try { if (this.conn != null) this.conn.close(); } catch (SQLException exx) { }
        }
        return false;
    }
    
    /**
     * Returns the JDBC Url to the database.
     */
    private String getConnectionUrl() {
    	if (this.connectionUrl == null) {
    		this.connectionUrl = Config.getValueFromConfigFile("DATABASE_URL");
    		if (this.connectionUrl == null) {
    			Logger.getLogger(Database.class.getName()).log(Level.SEVERE,
                        "Failed to retrieve DATABASE_URL setting from config file.");
    		}
    	}
    	return this.connectionUrl;
    }
}
