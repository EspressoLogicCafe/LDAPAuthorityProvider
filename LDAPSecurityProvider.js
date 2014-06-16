//Upload this JavaScript to EspressoLogic Stormpath Authentication Provider
function stormpathSecurityProviderCreate() {

    var result = {};
    var configSetup = {
  		ldapServer  : '',
   		ldapSearchBase  : "" ,
        keyLifetimeMinutes : 60
    };

    //FUNCTION this call must be made first to pass in the required Stormpath configuration values
    result.configure = function configure(myConfig) {
		ldapServer 		: myConfig.ldapServer || '',
   		ldapSearchBase  : myConfig.ldapSearchBase || "" ,
        configSetup.keyLifetimeMinutes = myConfig.keyLifetimeMinutes || 60;
    };

    //NOTE: the function configure must be called first - this will validate the stormpath user account
    //FUNCTION AUTHENTICATE REQUIRES PAYLOAD {username : '', password : ''}
    result.authenticate = function authenticate(payload) {

        //helper function to return an named value pairs of customData (exlude reserved fields)
        var parseCustomData =  function parseCustomData(result, stringHREF) {
            for (var id in stringHREF) {
                if (!stringHREF.hasOwnProperty(id)) {
                    continue;
                }
                if (RESERVED_FIELDS_HREF.indexOf(id) != -1) {
                    continue;
                }
                if (stringHREF.hasOwnProperty('customData')) {
                    var customdata = stringHREF[id];
                    for (var key in customdata) {
                        result[key] = customdata[key];
                    }
                }
            }
        };

        var roles = [];
        var errorMsg = null;
        var resetPasswordURL = null;
        var forgotPasswordURL = null;
        var customDataHREF = {};
        var params = null;
        var settings = createSettings();



        try {
            //POST this JSON request to determine if username and password account is valid
            var loginAttempt = SysUtility.authenticate(configSetup.ldapServer,configSetup.ldapSearchBase,payload.userName,payload.password);

            var accountJSON = JSON.parse(loginAttempt);
            if (accountJSON.hasOwnProperty('status')) {
                 errorMsg = "LDAP: " + accountJSON.developerMessage;
            }
            else {
                //GET the account details and custom data - TO DO
                //var accountURL = accountJSON.account.href + '?expand=customData';
                //var accountHREF = SysUtility.restGet(accountURL, params, settings);
               // var account = JSON.parse(accountHREF);
                // only check find roles for Enabled Accounts
                if ('ENABLED' === account.status) {
                    resetPasswordURL = account.emailVerificationToken.href;
                    parseCustomData(customDataHREF, account.customData);
                    var groupsURL = account.href+'/groups?expand=customData';
                    //GET the groups customData for this account
                    var responseGroups = SysUtility.restGet(groupsURL, params, settings);
                    var groups = JSON.parse(responseGroups);
                    for (var i = 0; i < groups.items.length; i++) {
                        if('ENABLED' === groups.items[i].status) {
                            roles.push(groups.items[i].name);
                            var customdata = groups.items[i].customData;
                            parseCustomData(customDataHREF, customdata);
                        }
                    }
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
            resetPasswordURL: resetPasswordURL,
            forgotPasswordURL: forgotPasswordURL,
            userData: customDataHREF,
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
        var groupsURL = STORMPATH_BASE_URL + 'applications/' + configSetup.stormpathLoginID + '/groups';
        var params = null;
        var settings = createSettings();

        try {
            var groupsResponse = SysUtility.ldapService(groupsURL, params, settings);
            var groups = JSON.parse(groupsResponse);

            for (var i = 0; i < groups.items.length; i++) {
                if ('ENABLED' === groups.items[i].status) {
                    roles.push(groups.items[i].name);
                }
            }
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
                    display: "Username",
                    description: "Enter your Username",
                    type: "text",
                    length: 40,
                    helpURL: "http://docs.stormpath.com/java/quickstart/#authenticate-an-account"
                },
                {
                    name: "password",
                    display: "Password",
                    description: "Enter your Password",
                    type: "password",
                    length: 40,
                    helpURL: "http://docs.stormpath.com/java/quickstart/#authenticate-an-account"
                }
            ],
            links : [
                {
                    display: "Forgot password?",
                    href: "https://api.stormpath.com/forgotLogin"
                },
                {
                    display: "Forgot Tenant?",
                    href: "https://api.stormpath.com/forgotTenant"
                }
            ]
        };
    };

    result.getConfigInfo = function getConfigInfo() {
        return {
            current : {

                "keyLifetimeMinutes" : configSetup.keyLifetimeMinutes
            },
            fields : [
                {
                    name: "ldapServer",
                    display: "LDAP Server",
                    type: "text",
                    length: 40,
                    helpURL: ""
                },
                {
                    name: "ldapPort",
                    display: "LDAP Port",
                    type: "text",
                    length: 40,
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
