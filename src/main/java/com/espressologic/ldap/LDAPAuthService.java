package com.espressologic.ldap;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LDAPAuthService {

    private static Properties languages = new Properties();

    private String LDAPUser = "JDeBroker@company.com";
    private String LDAPPassword = "Password1";// "dIurOa8r";
    private String LDAPServer = "1server1";
    private int Port = 389;
    private String BaseDN = "OU=DevelopmentTesting,OU=ServiceAccounts,DC=company,DC=com";
    private String LDAPVersion = "3";
    private String LDAPAuthMechanism = "SIMPLE";
    private Properties prop = new Properties();

    public LDAPAuthService() {
	init();
    }

    public LDAPAuthService(String user, String pw, String server, int port,
	    String baseDN) {
	this.LDAPUser = user;
	this.LDAPPassword = pw;
	this.Port = port;
	this.LDAPServer = server;
	this.BaseDN = baseDN;
	init();
    }

    public static String getProperty(String input) {
	String lang = languages.getProperty(input);
	try {
	    return new String(lang.getBytes("ISO-8859-1"), "UTF-8");
	} catch (Exception e) {
	    return lang;
	}
    }

    public static void main(String[] args) throws Exception {
	LDAPAuthService ls = new LDAPAuthService();
	String json = ls.findLdapUser(ls.LDAPUser);
	System.out.println(json);
	System.out.println(ls.findUserLdapGroupNames());
	
	
    }

    private Connection getConnection() throws ClassNotFoundException,
	    SQLException {
	Class.forName("rssbus.jdbc.ldap.LDAPDriver");

	Connection conn  = DriverManager.getConnection("jdbc:ldap", prop);
	return conn;
    }

    /**
     * RSSBus has other properties - see documentation for full details.
     * /http://cdn.rssbus.com/help/RJ1/jdbc/Connection.htm
     * 
     * @return Properties
     */
    private Properties init() {
	prop.put("User", getLDAPUser());
	prop.put("Password", getLDAPPassword());
	prop.put("Server", getLDAPServer());
	prop.put("Port", String.valueOf(getPort()));
	prop.put("BaseDN", getBaseDN());
	prop.put("AuthMechanism", getLDAPAuthMechanism());
	prop.put("Verbosity", 4);
	return prop;
    }

    private void printTable() throws SQLException, ClassNotFoundException {
	System.out.println("-----------------------------------------");
	Connection conn =  getConnection();
	DatabaseMetaData metaData =conn.getMetaData();
	ResultSet set = metaData.getTables(null, null, "%", null);
	System.out.println(getProperty("tables"));
	while (set.next()) {
	    System.out.println(set.getString("TABLE_NAME"));
	}
	set.close();
	System.out.println("----------------------------------------");
	conn.close();
    }

    /**
     * return JSON Array
     * 
     * @param query
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public String executeQuery(String query) throws Exception {
	List<HashMap> list = execute(query);
	ObjectMapper mapper = new ObjectMapper();
	return mapper.writeValueAsString(list).toString();
    }

    public List<HashMap> execute(String query) throws ClassNotFoundException,
	    SQLException, Exception {
	Connection conn =  getConnection();
	Statement stat = null;
	ResultSet rs = null;
	HashMap<String, String> map = new HashMap<String, String>();

	List<HashMap> list = new ArrayList<HashMap>();
	try {
	    stat = conn.createStatement();
	    boolean ret = stat.execute(query);
	    if (ret) {
		rs = stat.getResultSet();
		while (rs.next()) {
		    map = new HashMap<String, String>();
		    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
			
			map.put(rs.getMetaData().getColumnName(i),
				rs.getString(i));
		    }
		  
		    list.add(map);
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw ex;
	} finally {
	    try {
		stat.close();
		rs.close();
		conn.close();
	    } catch (Exception ex) {
		;//TCB
	    }
	}
	return list;
    }

    public String getLDAPUser() {
	return LDAPUser;
    }
    
    public String findLdapUser(String userName) throws Exception{
	
	String query = "SELECT Id FROM [User] where UserPrincipalName = ? ORDER BY RDN LIMIT 1";
	query = query.replace("?", "'" + userName + "'");
	String json = executeQuery(query);
	return json;
    }

    public String findUserLdapGroupNames() throws Exception {
	List<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	// id=CN=G_BROKER,OU=DevelopmentTesting,OU=ServiceAccounts,DC=meridiancapital,DC=com
	String query = "Select Id from [Group] LIMIT 20";
	List<HashMap> result = execute(query);
	HashMap<String,String> map = new HashMap<String,String>();
	String id;
	for (int i = 0; i < result.size(); i++) {
	    id = (String) result.get(i).get("Id");
	    int endIdx = id.indexOf(",OU");
	    map.put("Role",id.substring(3, endIdx));
	    list.add(map);
	    map = new HashMap<String,String>();
	}
	ObjectMapper mapper = new ObjectMapper();
	return mapper.writeValueAsString(list).toString();
    }

    public void setLDAPUser(String lDAPUser) {
	LDAPUser = lDAPUser;
    }

    public String getLDAPPassword() {
	return LDAPPassword;
    }

    public void setLDAPPassword(String lDAPPassword) {
	LDAPPassword = lDAPPassword;
    }

    public String getLDAPServer() {
	return LDAPServer;
    }

    public void setLDAPServer(String lDAPServer) {
	LDAPServer = lDAPServer;
    }

    public int getPort() {
	return Port;
    }

    public void setPort(int port) {
	Port = port;
    }

    public String getBaseDN() {
	return BaseDN;
    }

    public void setBaseDN(String baseDN) {
	BaseDN = baseDN;
    }

    public String getLDAPVersion() {
	return LDAPVersion;
    }

    public void setLDAPVersion(String lDAPVersion) {
	LDAPVersion = lDAPVersion;
    }

    public String getLDAPAuthMechanism() {
	return LDAPAuthMechanism;
    }

    public void setLDAPAuthMechanism(String lDAPAuthMechanism) {
	LDAPAuthMechanism = lDAPAuthMechanism;
    }

}
