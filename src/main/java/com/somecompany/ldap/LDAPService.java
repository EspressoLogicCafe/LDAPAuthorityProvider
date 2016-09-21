package com.somecompany.ldap;

import java.net.InetAddress;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class LDAPService {

	
    private static final Logger log = LogManager.getLogger(LDAPService.class);
    private String ldapUri;
    private String ldapBaseDN;
    private String ldapUser;
    private String ldapPassword;

    public static void main(String args[]) {

    	String ldapPort = "389";
    	String ldapServer = "127.0.0.1";
    	String ldapBaseDN = "dc=somecompany,dc=com";
        String ldapUser = "adlookup@somecompany";
        String ldapPassword = "somepassword=14";
        
        String user;
        String password;
        if (args.length == 2) {
            user = args[0];
            password = args[1];
        } else {
          user = "xxxx@somecompany.com";
          password = "yyyyy";
        }
        
        LDAPService ldapService = new LDAPService();
        ldapService.init(ldapServer, ldapPort, ldapBaseDN, ldapUser, ldapPassword);

        String errMsg = ldapService.authenticateUser(user, password);
        if (errMsg != null) {
        	log.error(errMsg);
        	System.exit(-1);
        }
        
        String displayName = ldapService.getUserDisplayName(user);
        String hostName = ldapService.getHostName();
        log.debug("Display name for "+user+" = "+displayName + ". Host name = "+hostName);

        String ou = "SOMEOU";
        String cn = "ALL";
        log.debug(user+" isInCnOu cn="+cn+",ou="+ou+": Searching...");
        boolean inCnOu = ldapService.isUserInCnOu(user, cn, ou);
        log.debug(user+" isInCnOu cn="+cn+",ou="+ou+": "+inCnOu);

//        String groups = ldapService.getUserGroupNames(user);
//        log.debug(user+" belongs to: "+groups);
    }

	public LDAPService() {
	}
	
	public LDAPService(String ldapServer, String ldapPort, String ldapBaseDN, String ldapUser, String ldapPassword) {
		init(ldapServer, ldapPort, ldapBaseDN, ldapUser, ldapPassword);
	}
	
	public void init(String ldapServer, String ldapPort, String ldapBaseDN, String ldapUser, String ldapPassword) {
		this.ldapUri = "ldap://"+ldapServer+":"+ldapPort;
		this.ldapBaseDN = ldapBaseDN;
		this.ldapUser = ldapUser;
		this.ldapPassword = ldapPassword;
	}
	
	public String authenticateUser(String user, String password) {
	    Hashtable<String, Object> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUri);
            env.put(Context.SECURITY_PRINCIPAL, user);
            env.put(Context.SECURITY_CREDENTIALS, password);

            DirContext ctx = null;

        try {
            ctx = new InitialDirContext(env);
	        log.debug("Authentication succeeded for: "+user);
	        return null;

	    } catch (AuthenticationException e) {
	        return "Authentication failed: " + e.getMessage();
	    } catch (NamingException e) {
	        return "Failed to bind to LDAP and get account information: " + e;
	    } finally {
	    	close(ctx);
	    }
	}

	public String getUserDisplayName(String user) {
	    Hashtable<String, Object> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUri);
            env.put(Context.SECURITY_PRINCIPAL, ldapUser);
            env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

            String displayName = null;
            DirContext ctx = null;

          try {
            ctx = new InitialDirContext(env);

	        SearchControls controls = new SearchControls();
	        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

	        NamingEnumeration<SearchResult> renum = ctx.search(
	                ldapBaseDN, 
	                "(&(objectClass=user)(userPrincipalName=" + user + "))", 
	                controls);

	        if (!renum.hasMore()) {
	            log.error("Cannot locate user information for " + user);
	        } else {
		        SearchResult result = renum.next();
		        Attribute users = result.getAttributes().get("displayName");
		        if (users != null && users.size() > 0)  {
	                displayName = users.get(0).toString();
		        }
	        }

        } catch (Exception e) {
            log.error("Exception while getting displayName for "+user+": "+e.getMessage());
        } finally {
        	close(ctx);
        }

        return displayName;
	}
	
	public String getHostName() {
		try {
			//return InetAddress.getLocalHost().getHostName();
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (Exception e){
			return "";
		}
	}

	public boolean isUserInCnOu(String user, String cn, String ou) {
	    Hashtable<String, Object> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUri);
            env.put(Context.SECURITY_PRINCIPAL, ldapUser);
            env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

            boolean found = false;
            DirContext ctx = null;

        try {
            ctx = new InitialDirContext(env);

	        SearchControls controls = new SearchControls();
	        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

	        NamingEnumeration<SearchResult> renum = ctx.search(
	                ldapBaseDN, 
	                "(&(memberOf:1.2.840.113556.1.4.1941:=CN="+cn+",OU="+ou+","+ldapBaseDN+")(objectCategory=Person)(userPrincipalName=" + user + "))",
	                controls);

	        boolean hasMore;
	        try {
				hasMore = renum.hasMore();
			} catch (Exception e) {
				log.debug(user+" does NOT have CN="+cn+",OU="+ou+". "+e.getMessage());
				hasMore = false;
			}
	        
	        if (hasMore) {
		        SearchResult result = renum.next();
		        Attribute orgs = result.getAttributes().get("distinguishedName");
		        if (orgs != null && orgs.size() > 0)  {
		        	found = true;
		        }
	        	/* deep enumeration of groups: 
		        Attribute orgs = result.getAttributes().get("memberOf");
		        if (orgs != null)  {
		        	for (int i = 0; i < orgs.size(); i++) {
		        		String thisMem = orgs.get(i).toString();
		        		log.debug("memberOf: "+thisMem);
					}
		        }
		        */
	        }

        } catch (Exception e) {
            log.error("Exception determining if user "+user+" has CN="+cn+",OU="+ou+". "+e.getMessage());
        } finally {
        	close(ctx);
        }

        return found;
	}


	private void close(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			} catch(Exception e) {
			}
		}
	}

}
