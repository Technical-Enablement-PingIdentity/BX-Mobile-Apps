//
//  Constants.h
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 3/30/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

extern NSString *const kCustomerUrl;

extern NSString *const kAuthType;
extern NSString *const kUser;
extern NSString *const kPassword;
extern NSString *const kAppIdKey;
extern NSString *const kAppID;
extern NSString *const kPingPayload;
extern NSString *const kStatus;

extern NSString *const kApprove;
extern NSString *const kDeny;
extern NSString *const kApproveCapital;
extern NSString *const kDenyCapital;
extern NSString *const kCancel;

extern NSString *const kOnline;
extern NSString *const kOperation;
extern NSString *const kAuthUser;
extern NSString *const kAuthUserOffline;

extern NSString *const kHomeSegue;
extern NSString *const kAuthSegue;


extern NSString *const kPayload;
extern NSString *const kOTP;

extern NSString *const kSessionID;
extern NSString *const kAuthSessionID;
extern NSString *const kTransactionType;
extern NSString *const kAuthentication;
extern NSString *const kQrAuthentication;
extern NSString *const kSteUpAuthentication;
extern NSString *const kMessage;
extern NSString *const kSum;
extern NSString *const kClientContext;
extern NSString *const kCurrentTimeout;
extern NSString *const kLogoImageName;
extern NSString *const kModernoCopyRight;
extern NSString *const kPingIDSDK;

// QR
extern NSString *const kQRScanSegue;
extern NSString *const kQRUserSelectionSegue;
extern NSString *const kAuthenticationToken;

// QR Token
extern NSString *const kClientContext;
extern NSString *const kAuthenticationTokenStatus;
extern NSString *const kUsers;

//QR Status
extern NSString *const kQrStatusClaimed;
extern NSString *const kQrStatusPendingUserApproval;
extern NSString *const kQrStatusMobileUserSelection;
extern NSString *const kQrStatusMobileUserSelectionAndApproval;
extern NSString *const kQrStatusWebUserSelection;
extern NSString *const kQrStatusPendingPushVerification;
extern NSString *const kQrStatusCanceled;
extern NSString *const kQrStatusDenied;

typedef NS_ENUM(NSInteger, STATUS_CODES)
{
    STATUS_OK                                               = 0,
    STATUS_FAILED                                           = 1000,
    STATUS_AUTHENTICATION_DENIED                            = 1014,
    STATUS_ONE_TIME_PASSCODE                                = 1015,
    STATUS_INVALID_OTP                                      = 1025,
    STATUS_DEVICE_BYPASSED                                  = 1026
};

#pragma mark - Ping Federate

extern NSString *const kIssuer;
extern NSString *const kClientID;
extern NSString *const kRedirectURI;

@interface Constants : NSObject

+(UIColor *)approveColor;
+(UIColor *)pingFedColor;
+(UIColor *)denyColor;
+(UIColor *)gradientTopColor;
+(UIColor *)gradientBottomColor;

@end









