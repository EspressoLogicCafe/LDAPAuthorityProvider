// this small section is provided by the Espresso Logic server when running in the server.
// a small emulator is provided for testing locally.

out = java.lang.System.out;

var SysUtility = {

    authenticate : function authenticate(ldapAdServer,ldapSearchBase,username,password) {
        var ldapService = new com.espressologic.lib.ldap.LDAPService(ldapAdServer,ldapSearchBase);
        var result = ldapService.authenticate(username,password);
        return result;
    }
};

// load the class
load("LDAPSecurityProvider.js");

// configuration needed for testing
var configSetup = {
   ldapAdServer  : 'ldap://0.0.0.0:1389',
   ldapSearchBase  : "dc=ad,dc=my-domain,dc=com" ,
   ldapRoot : 'cn=root'
};

// this is how the server creates the security object
var ldapClient = LDAPSecurityProviderCreate();
ldapClient.configure(configSetup);

var payload = {
    username: "David",
    password: "Password$1"
};

out.println("------------- testing ldap authenticate with good payload");
var result = ldapClient.authenticate(payload);
out.println(JSON.stringify(result, null, 2));
out.println("-------------");


out.println("------------- testing ldap authenticate with bad payload");
badPayload = {
    username: "DavidBAD",
    password: "Password$1"
};

result = ldapClient.authenticate(badPayload);
out.println(JSON.stringify(result, null, 2));
out.println("-------------");


out.println("------------- testing getAllGroups");
result = ldapClient.getAllGroups();
out.println(JSON.stringify(result, null, 2));
out.println("-------------");

out.println("------------- testing getLoginInfo");
result = ldapClient.getLoginInfo(url);
out.println(JSON.stringify(result, null, 2));
out.println("First field is " + result.fields[0].name);
out.println("-------------");

out.println("------------- testing getConfigInfo");
result = ldapClient.getConfigInfo();
out.println(JSON.stringify(result, null, 2));
out.println("First config prop is " + result.fields[0].name);
out.println("-------------");
