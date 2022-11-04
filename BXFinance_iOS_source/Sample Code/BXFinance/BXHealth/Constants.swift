//
//  Constants.swift
//  BXHealth
//
//  Created by Bhavya Chauhan on 12/8/21.
//  Copyright © 2021 Ping Identity. All rights reserved.
//

import Foundation

struct OIDC {
    /**
     The OIDC issuer from which the configuration will be discovered.
     For example: "https://auth.pingone.com/1e41d166-8012-4fa2-b755-15cd7c8a03de/as"
    */
    static let Issuer                  = "https://auth.pingone.com/6a96ada1-d5a2-40db-945d-9398dc873a84/as"
    /**
     The OAuth client ID.
     For example: "4a43c047-36f9-4d86-a53e-273a4d28d629"
    */
    static let ClientID                = "a9623025-8595-45ef-8acf-f83226cfbf5a"
    /**
     The OAuth redirect URI for the ClientID.
     For example: "pingonesdk://sample"
    */
    static let RedirectURI             = "pingonesdk://sample"
    
    static let LoginPrompt             = "login"
}

struct OIDCKey {
    static let MobilePayload            = "mobilePayload"
    static let Prompt                   = "prompt"
}

struct PairingMethodName {
    static let Manual                   = ""
    static let OIDC                     = " "
}

struct SDKFunctionality {
    static let SendLogs                 = ""
}

struct SegueName {
    static let Manual                   = "manual"
    static let OIDC                     = "oidc"
}

struct Local {
    static let DeviceIsPaired                   = "Device is paired successfully."
    static let Pair                             = "Would you like to pair this device with your BXHealth account?"
    static let Authenticate                     = "Authorize this BXHealth Request?"
    static let Approve                          = "Approve"
    static let Deny                             = "Deny"
    static let Cancel                           = "Cancel"
    static let Success                          = "Success!"
    static let Error                            = "We encountered an error. Please contact BXHealth Customer Service"
    static let Ok                               = "OK"
}

