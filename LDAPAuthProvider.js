//Upload this JavaScript to EspressoLogic Stormpath Authentication Provider
function LDAPAuthProviderCreate () {

    var result = {};
    var configSetup = {
  		serverName  : '',
   		port  : '' ,
   		baseDN: '',
        keyLifetimeMinutes : 60
    };

    //FUNCTION this call must be made first to pass in the required LDAP configuration values
    result.configure = function configure(myConfig) {
		configSetup.serverName 	= myConfig.serverName || '';
   		configSetup.port  = myConfig.port || 389 ;
   		//configSetup.query  = myConfig.query || 'SELECT Id,RDN,SN,CN FROM [User] where userPrincipalName = ? ORDER BY RDN LIMIT 20';
   		configSetup.baseDN = myConfig.baseDN || 'OU=DevelopmentTesting,OU=ServiceAccounts,DC=company,DC=com';
        configSetup.keyLifetimeMinutes = myConfig.keyLifetimeMinutes || 60;
    };

    //NOTE: the function configure must be called first - this will validate the sql user account
    //FUNCTION AUTHENTICATE REQUIRES PAYLOAD {username : '', password : ''}
    result.authenticate = function authenticate(payload) {

        var roles = [];
        var errorMsg = null;
        var resetPasswordURL = null;
        var forgotPasswordURL = null;
        var customData = {};

        try {
         	var ldapAuthService = new com.espressologic.ldap.LDAPAuthService(payload.username,payload.password,configSetup.serverName,configSetup.port,configSetup.baseDN);

       		var loginAttempt = ldapAuthService.findLdapUser(payload.username);
       		var userExists = JSON.parse(loginAttempt);
	   if(Array.isArray(userExists) && userExists.length > 0){

            var groupNames = ldapAuthService.findUserLdapGroupNames();
            if (groupNames ) {
				var roleNames = JSON.parse(groupNames);
				customData = {"UserPrincipalName":  payload.username ,"Password": payload.password };
				for (var row in roleNames) {
					roles.push(roleNames[row].Role);
				}

		} else {
			errorMsg = loginAttempt;
		}
	  }

        }
        catch (e) {
                errorMsg = e.message;
        }

        var autResponse = {
            errorMessage: errorMsg,
            roleNames: roles,
            userIdentifier: payload.username,
            keyExpiration: new Date(+new Date() + (+configSetup.keyLifetimeMinutes) * 60 * 1000),
            userData: customData,
            lastLogin : {
                datetime: null,
                ipAddress : null
            }
        };
        return autResponse;
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

        var autResponse = {
            errorMessage: errorMsg,
            roleNames: roles
        };

        return autResponse;
    };
    //FUNCTION getLoginInfo is used to create the logon dialog - DO NOT CHANGE
    result.getLoginInfo = function getLoginInfo() {
        return {
            fields: [
                {
                    name: "username",
                    display: "User Email",
                    description: "Enter your Username as an email address",
                    type: "text",
                    length: 40,
                    helpURL: ""
                },
                {
                    name: "password",
                    display: "Password",
                    description: "Enter your Password",
                    type: "password",
                    length: 40,
                    helpURL: ""
                }
            ],
            links : [

            ]
        };
    };

    result.getConfigInfo = function getConfigInfo() {
        return {
            current : {
 				"serverName": configSetup.serverName,
  				"port": configSetup.port,
   				"baseDN": configSetup.baseDN,
   				"query": configSetup.query,
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
                    name: "port",
                    display: "Port",
                    type: "number",
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
