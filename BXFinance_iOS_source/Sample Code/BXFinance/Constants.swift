//
//  Constants.swift
//  BXFinance
//
//  Created by Bhavya Chauhan on 11/4/22.
//  Copyright © 2022 Ping Identity. All rights reserved.
//

import Foundation

struct SegueName {
    static let Manual                   = "manual"
    static let OIDC                     = "oidc"
}


struct SDKFunctionality {
    static let SendLogs                 = ""
}

struct OIDCKey {
    static let MobilePayload            = "mobilePayload"
    static let Prompt                   = "prompt"
    static let LoginPrompt              = "login"
}

struct PairingMethodName {
    static let Manual                   = ""
    static let OIDC                     = " "
}

struct Local {
    
    static let Pair                             = "Would you like to pair this device with your \(Product.productName) account?"
    static let Authenticate                     = "Authorize this \(Product.productName) Request?"
    static let Error                            = "We encountered an error. Please contact \(Product.productName) Customer Service"

    static let DeviceIsPaired                   = "Device is paired successfully."
    static let Approve                          = "Approve"
    static let Deny                             = "Deny"
    static let Cancel                           = "Cancel"
    static let Success                          = "Success!"
    static let Ok                               = "OK"
    
    static func getMessageForError(_ error: NSError) -> String {
        switch error.code {
        case 10000: //internalError
            return "generic_error_message".localized
        case 10001: //deviceTokenIsMissing
            return "deviceTokenIsMissing_10001".localized
        case 10002: //unrecognizedRemoteNotification
            return "unrecognizedRemoteNotification_10002".localized
        case 10003: //serverError
            return "generic_error_message".localized
        case 10004: //noConnectivity
            return "noConnectivity_10004".localized
        case 10005: //pairingKey
            return "pairingKey_10005".localized
        case 10006: //bundleId
            return "bundleId_10006".localized
        case 10007: //pairingKeyDataCenterMismatch
            return "pairingKeyDataCenterMismatch_10007".localized
        case 10008: //deviceIsNotPaired
            return "deviceIsNotPaired_10008".localized
        case 10009: //pushConfirmationTimeout
            return "pushConfirmationTimeout_10009".localized
        case 10010: //passcodeNotValid
            return "passcodeNotValid_10010".localized
        case 10011: //authCodeInvalid
            return "authCodeInvalid_10011".localized
        case 10012: //failedPolicyRequirements
            return "failedPolicyRequirements_10012".localized
        case 10013: //pairingAlreadyRunning
            return "pairingAlreadyRunning_10013".localized
        default:
            return "generic_error_message".localized
        }
        
    }

}
