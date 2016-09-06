//Upload this JavaScript to LDAP Authentication Provider
out = java.lang.System.out;

function LDAPAuthProviderCreate () {

    var result = {};
    var configSetup = {
		ldapUser 			: '',
		ldapPassword 		: '',
  		serverName  		: '',
   		port  				: '' ,
   		baseDN				: '',
        keyLifetimeMinutes 	: 60
    };
    
    function getFixedTTL(){
      var now = new Date();
      var ttlHour = 6;//2 am. TODO: account for DST
      var ttlDate;
      
      if (now.getHours() < ttlHour)
    	ttlDate = new Date(now.getFullYear(), now.getMonth(), now.getDate(), ttlHour);
      else
    	ttlDate = new Date(now.getFullYear(), now.getMonth(), now.getDate()+1, ttlHour);
      
      out.println("now="+now+", ttlDate="+ttlDate);
      
      var keyLifetimeSeconds = (ttlDate.getTime() - now.getTime())/1000;
      return keyLifetimeSeconds;
    }

    //FUNCTION this call must be made first to pass in the required LDAP configuration values
    result.configure = function configure(myConfig) {
        configSetup.ldapUser 			= myConfig.ldapUser 	|| "adlookup@somecompany.com";
        configSetup.ldapPassword		= myConfig.ldapPassword	|| "somepassword=14";
		configSetup.serverName			= myConfig.serverName 	|| '172.16.10.21';
   		configSetup.port  				= myConfig.port 		|| "389" ;
   		configSetup.baseDN 				= myConfig.baseDN 		|| 'DC=somecomapny,DC=com';
   		configSetup.baseUrl             = myConfig.baseUrl      || 'UNSPECIFIED SERVER CONFIG';
        configSetup.keyLifetimeMinutes 	= myConfig.keyLifetimeMinutes || 720;//12 hours
    };

    //NOTE: the function configure must be called first - this will validate the user payload against LDAP service
    result.authenticate = function authenticate(payload) {

        var errorMsg 			= null;
    	var ou 					= "ROOT";
    	var cn 					= "ALL";
        var roles 				= [cn];
        var customData 			= {};
        var userInfo 			= {};
        var userName 			= payload.username;
        var sDomain 			= "@somecompany.com";
        var upn					= userName.concat(sDomain);
        var password			= payload.password;
        var keyLifetimeSeconds	= getFixedTTL();
        
        try {
        	
        	var LDAPService = Java.type("com.somecompany.ldap.LDAPService");
         	var ldapService = new LDAPService(configSetup.serverName,configSetup.port,configSetup.baseDN,configSetup.ldapUser,configSetup.ldapPassword);

         	errorMsg = ldapService.authenticateUser(upn,password);
         	if (errorMsg == null) {
         		
           		var displayName = ldapService.getUserDisplayName(upn);
           		var apiserver   = configSetup.baseUrl;
				
//				out.println("isUserInCnOu: upn="+upn+",cn="+cn+",ou="+ou);
				var authorized = ldapService.isUserInCnOu(upn, cn, ou);
//				out.println("isUserInCnOu: after");
				
				if (authorized) {
					var url = apiserver + "/rest/default/hsjag/v1/mssql%3AUsers?auth=demo_full:1&sysfilter=equal(userName:'"+userName+"')";
					var result = SysUtility.restGet(url, null, null);
					//out.println("result.length="+result.length+" -> "+result);
					authorized = result.length > 2;
					
					if (authorized) {
						userInfo = {
							"name" 		: ""+displayName,
							"apiserver"	: ""+apiserver
						};
						customData = {
							"UserPrincipalName" : upn,
							"Password" : password
						};
					}
				}
				if (!authorized)
					errorMsg = "You don't have access rights.";
         	} else 
         		errorMsg = ""+errorMsg;
        } catch (e) {
        	out.println(e.rhinoException.toString());
            errorMsg = "Invalid user name and/or password: " + e.message;
        }

        var authResponse = {
            errorMessage: errorMsg,
            roleNames: roles,
            userIdentifier: upn,
            keyLifetimeSeconds: keyLifetimeSeconds,
            userData: customData,
            userInfo: userInfo,
            lastLogin : {
                datetime: null,
                ipAddress : null
            }
        };
        out.println("authResponse: err="+errorMsg+" $ roles="+roles+" $ userId="+upn+" $ userData="+customData.UserPrincipalName/*+", "+customData.Password*/+" $ userInfo="+userInfo.name+"->"+userInfo.apiserver+" $ expr="+authResponse.keyLifetimeSeconds);
        return authResponse;
    };

    //FUNCTION getAllGroups is used to map all available groups for existing application - DO NOT CHANGE
    result.getAllGroups = function getAllGroups() {
        var roles = [];
        var errorMsg = null;

        try {
            //var groupsResponse = SysUtility.authenticate(configSetup.serverName,configSetup.serverPort,configSetup.databaseName,payload.username,payload.password,payload.roleQuery);
            //var groups = JSON.parse(groupsResponse);

            //for (var row in groups) {
			//	roles.push(groups[row].role);
			//}

        }
        catch(e) {
            errorMsg = e.message;
        }

        var authResponse = {
            errorMessage: errorMsg,
            roleNames: roles
        };

        return authResponse;
    };
    //FUNCTION getLoginInfo is used to create the logon dialog - DO NOT CHANGE
    result.getLoginInfo = function getLoginInfo() {
        return {
            fields: [
                {
                    name: "username",
                    display: "User Email",
                    description: "Enter your Username as an e-mail address",
                    type: "text",
                    length: 40,
                    helpURL: ""
                },
                {
                    name: "password",
                    display: "Password",
                    description: "Enter your password",
                    type: "password",
                    length: 40,
                    helpURL: ""
                }
            ],
            links : []
        };
    };

    //this function is called by the authenticaion provider and these values are stored in the server side admin database
    result.getConfigInfo = function getConfigInfo() {
        return {
            current : {
              "ldapUser" : configSetup.ldapUser,
              "ldapPassword": configSetup.ldapPassword,
 			  "serverName": configSetup.serverName,
  			  "port": configSetup.port,
   			  "baseDN": configSetup.baseDN,
              "keyLifetimeMinutes" : configSetup.keyLifetimeMinutes
            },
            fields : [
			{
			  name: "serverName",
			  display: "LDAP Server Name",
			  type: "text",
			  length: 40,
			  helpURL: ""
			},
			{
			  name: "ldapUser",
			  display: "LDAP Server User Name",
			  type: "text",
			  length: 40,
			  helpURL: ""
			},
			{
			  name: "ldapPassword",
			  display: "LDAP Server User Password",
			  type: "password",
			  length: 40,
			  helpURL: ""
			},
			{
			  name: "port",
			  display: "Port",
			  type: "text",
			  length: 10,
			  helpURL: ""
			},
			{
			  name: "baseDN",
			  display: "BaseDN",
			  type: "text",
			  length: 60,
			  helpURL: ""
			},
			{
			  name: "keyLifetimeMinutes",
			  display: "API Key Lifetime (Minutes)",
			  type: "number",
			  length: 8,
			  helpURL: "http://www.espressologic.com"
			}
            ],
            links: []
        };
    };

    return result;
}
