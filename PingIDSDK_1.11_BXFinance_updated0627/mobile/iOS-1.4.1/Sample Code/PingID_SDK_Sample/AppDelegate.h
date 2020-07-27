//
//  AppDelegate.h
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 3/30/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import <UIKit/UIKit.h>

@protocol OIDAuthorizationFlowSession;

@import UserNotifications;

@interface AppDelegate : UIResponder <UIApplicationDelegate, UNUserNotificationCenterDelegate>

@property (strong, nonatomic) UIWindow *window;

/*! @brief The authorization flow session which receives the return URL from \SFSafariViewController.
 @discussion We need to store this in the app delegate as it's that delegate which receives the
 incoming URL on UIApplicationDelegate.application:openURL:options:. This property will be
 nil, except when an authorization flow is in progress.
 */
@property(nonatomic, strong) id<OIDAuthorizationFlowSession> currentAuthorizationFlow;

@end

