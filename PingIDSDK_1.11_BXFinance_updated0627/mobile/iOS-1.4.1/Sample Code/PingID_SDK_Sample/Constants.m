//
//  Constants.m
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 3/30/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import "Constants.h"

/*****************************************************************************************************/

NSString *const kCustomerUrl            = <#CUSTOMER_URL#>;  //For example: @"http://www.example.com/pingidsdk"
NSString *const kAppID                  = <#APP_ID#>;                      //For example: @"c0a658e0-47dc-4cb4-80d7-1a59a6a8a621";

#pragma mark - Ping Federate

NSString *const kIssuer                 = <#ISSUER#>;
NSString *const kClientID               = <#CLIENT_ID#>;
NSString *const kRedirectURI            = <#REDIRECT_URL#>;

/*****************************************************************************************************/

// These constants shouldn't be changed

NSString *const kAuthType               = @"authType";
NSString *const kUser                   = @"user";
NSString *const kPassword               = @"password";
NSString *const kAppIdKey               = @"appId";
NSString *const kPingPayload            = @"pingIdPayload";
NSString *const kStatus                 = @"status";
NSString *const kApprove                = @"approve";
NSString *const kDeny                   = @"deny";
NSString *const kApproveCapital         = @"Approve";
NSString *const kDenyCapital            = @"Deny";
NSString *const kCancel                 = @"cancel";
NSString *const kOnline                 = @"online";
NSString *const kOperation              = @"operation";
NSString *const kAuthUser               = @"auth_user";
NSString *const kAuthUserOffline        = @"auth_offline_user";
NSString *const kHomeSegue              = @"HomeSegueID";
NSString *const kAuthSegue              = @"AuthSegueID";
NSString *const kPayload                = @"payload";
NSString *const kOTP                    = @"otp";

NSString *const kSessionID              = @"sessionId";
NSString *const kAuthSessionID          = @"authSessionID";
NSString *const kTransactionType        = @"transactionType";
NSString *const kAuthentication         = @"AUTHENTICATION";
NSString *const kQrAuthentication       = @"QRCODE_AUTHENTICATION";
NSString *const kSteUpAuthentication    = @"STEP_UP";
NSString *const kMessage                = @"msg";
NSString *const kSum                    = @"sum";
NSString *const kClientContext          = @"client_context";
NSString *const kCurrentTimeout         = @"current_timeout";
NSString *const kLogoImageName          = @"logo_moderno_mobile";
NSString *const kModernoCopyRight       = @"© 2017 Moderno\nv%@";
NSString *const kPingIDSDK              = @"pingidsdk";

//QR
NSString *const kQRScanSegue            = @"QRScanSegue";
NSString *const kQRUserSelectionSegue  =  @"QRUserSelectionSegue";
NSString *const kAuthenticationToken    = @"authentication_token=";

// QR Token
NSString *const kAuthenticationTokenStatus                      = @"authentication_token_status";
NSString *const kUsers                                          = @"users";

//QR Status
NSString *const kQrStatusClaimed                                = @"CLAIMED";
NSString *const kQrStatusPendingUserApproval                    = @"PENDING_USER_APPROVAL";
NSString *const kQrStatusMobileUserSelection                    = @"MOBILE_USER_SELECTION";
NSString *const kQrStatusMobileUserSelectionAndApproval         = @"MOBILE_USER_SELECTION_AND_APPROVAL";
NSString *const kQrStatusWebUserSelection                       = @"WEB_USER_SELECTION";
NSString *const kQrStatusPendingPushVerification                = @"PENDING_PUSH_VERIFICATION";
NSString *const kQrStatusCanceled                               = @"CANCELED";
NSString *const kQrStatusDenied                                 = @"DENIED";

@implementation Constants

+(UIColor *)approveColor
{
    return [UIColor colorWithRed:0.50 green:0.89 blue:0.67 alpha:1.00];
}

+(UIColor *)denyColor
{
    return [UIColor colorWithRed:0.44 green:0.63 blue:0.70 alpha:1.00];
}

+(UIColor *)pingFedColor
{
    return [UIColor colorWithRed:0.36 green:0.56 blue:0.64 alpha:1.00];
}

+(UIColor *)gradientTopColor
{
    return [UIColor whiteColor];
}

+(UIColor *)gradientBottomColor
{
    return [UIColor colorWithRed:0.92 green:1.00 blue:0.97 alpha:1.00];
}

@end
