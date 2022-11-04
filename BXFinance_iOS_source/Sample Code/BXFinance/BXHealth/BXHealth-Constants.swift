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
    
}

struct Product {
    
    static let productName: String = Bundle.main.infoDictionary?["CFBundleName"] as? String ?? "BXHealth"
    
}
