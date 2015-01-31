// this small section is provided by the Espresso Logic server when running in the server.
// a small emulator is provided for testing locally.

out = java.lang.System.out;

var SysUtility = {

    authenticate : function authenticate(username,password,serverName,port,baseDN,query) {
        var ldapAuthService = new com.espressologic.ldap.LDAPAuthService(username,password,serverName,port,baseDN);
        var result = ldapAuthService.executeQuery(query);
        return result;
    }
};

// 1. load the class
load("LDAPAuthProvider.js");

// 2. configuration needed for testing
var configSetup = {
   serverName  : 'servername',
   port  : 389 ,
  // query : 'SELECT Id,RDN,SN,CN FROM [User] where UserPrincipalName = %%username%% ORDER BY RDN LIMIT 20',
   baseDN: 'OU=DevelopmentTesting,OU=ServiceAccounts,DC=company,DC=com',
   keyLifetimeMinutes : 60
};

// 3.this is how the server creates the security object
var ldapClient = LDAPAuthProviderCreate();
ldapClient.configure(configSetup);

var payload = {
    username: "JDeBroker@company.com",
    password: "Password1"

};

out.println("------------- testing sql authenticate with good payload with roles");
var result = ldapClient.authenticate(payload);
out.println(JSON.stringify(result, null, 2));
out.println("-------------");



var payload = {
    username: "Tband@company.com",
    password: "password"

};

out.println("------------- testing sql authenticate with good payload no roles");
var result = ldapClient.authenticate(payload);
out.println(JSON.stringify(result, null, 2));
out.println("-------------");


out.println("------------- testing sql authenticate with bad payload");
badPayload = {
    username: "DavidBAD",
    password: "Password$1"
};

result = ldapClient.authenticate(badPayload);
out.println(JSON.stringify(result, null, 2));
out.println("-------------");


var payload = {
    username: "tyler",
    password: "password1"
};


out.println("------------- testing getAllGroups");
result = ldapClient.getAllGroups();
out.println(JSON.stringify(result, null, 2));
out.println("-------------");

out.println("------------- testing getLoginInfo");

result = ldapClient.getLoginInfo(null);
out.println(JSON.stringify(result, null, 2));
out.println("First field is " + result.fields[0].name);
out.println("-------------");

out.println("------------- testing getConfigInfo");
result = ldapClient.getConfigInfo();
out.println(JSON.stringify(result, null, 2));
out.println("First config prop is " + result.fields[0].name);
out.println("-------------");
