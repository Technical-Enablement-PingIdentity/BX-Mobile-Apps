//
//  Constants.swift
//  SampleApp
//
//  Created by Ping Identity on 10/10/19.
//  Copyright © 2019 Ping Identity. All rights reserved.
//

import Foundation

struct OIDC {
    /**
     The OIDC issuer from which the configuration will be discovered.
     For example: "https://auth.pingone.com/1e41d166-8012-4fa2-b755-15cd7c8a03de/as"
    */
    static let Issuer                  = "https://auth.pingone.com/17161047-290f-4c88-b771-01adc4e81564/as"
    /**
     The OAuth client ID.
     For example: "4a43c047-36f9-4d86-a53e-273a4d28d629"
    */
    static let ClientID                = "028887be-5d57-4cb8-aeb9-874c4f202ae0"
    /**
     The OAuth redirect URI for the ClientID.
     For example: "pingonesdk://sample"
    */
    static let RedirectURI             = "pingonesdk://sample"

}

struct Product {
    
    static let productName: String = Bundle.main.infoDictionary?["CFBundleName"] as? String ?? "BXFinance"
    
}
