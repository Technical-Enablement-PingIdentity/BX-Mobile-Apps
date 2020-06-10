//
//  AppDelegate.m
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 3/30/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import "AppDelegate.h"
#import <PingID_SDK/PingID.h>
#import "AuthViewController.h"
#import <AppAuth/AppAuth.h>
#import <SafariServices/SafariServices.h>

@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // If the app doesn't have permissions for location or push notifications, a warning will be written to the console
    
    [PingID initWithAppID:kAppID supportedMfa:PIDSupportedMfaTypeAutomatic];
    
    [PingID setRootDetection:YES dataCenter:PIDDataCenterTypeNA withCompletionBlock:^(NSError * _Nullable error) {
        if (error) {
             NSLog(@"Error: %@",error.description);
        }
    }];
    
#if defined(DEBUG)

    [PingID setDebugMode:YES]; //Uses sandbox APNS environment
    
#endif
    
    [self registerPush];
    
    return YES;
}

#pragma mark - Push Notifications

//Called when a notification is delivered to a foreground app.
/*-(void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler{
    NSLog(@"User Info : %@",notification.request.content.userInfo);
    completionHandler(UNNotificationPresentationOptionNone);
}*/

//Called to let your app know which action was selected by the user for a given notification.
/*-(void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void(^)())completionHandler{
    NSLog(@"User Info : %@",response.notification.request.content.userInfo);
    completionHandler();
}*/

-(void)registerPush
{
    NSLog(@"Registering push");
    
    if(NSFoundationVersionNumber > NSFoundationVersionNumber_iOS_9_x_Max)
    {
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        center.delegate = self;
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge) completionHandler:^(BOOL granted, NSError * _Nullable error)
        {
            if(!error)
            {
                // Registering UNNotificationCategory more than once results in previous categories being overwritten. PingID provides the needed categories. The developer may add categories.
                NSMutableSet *categories = [PingID getPingIDRemoteNotificationsCategories];
                [[UNUserNotificationCenter currentNotificationCenter] setNotificationCategories:categories];
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[UIApplication sharedApplication] registerForRemoteNotifications];
                });
            }
        }];
    }
    else
    {
        // Registering UIUserNotificationSettings more than once results in previous settings being overwritten. PingID provides the needed categories. The developer may add categories.

        NSMutableSet *categories = [PingID getPingIDDeprecatedRemoteNotificationsCategories];
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationTypeSound categories:categories];

        dispatch_async(dispatch_get_main_queue(), ^{
            [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
            [[UIApplication sharedApplication] registerForRemoteNotifications];
        });
    }
}

-(void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    NSLog(@"didRegisterForRemoteNotificationsWithDeviceToken", YES);
    
    if (deviceToken)
    {
        NSString *devToken = [self stringWithDeviceToken:deviceToken];
        
        NSLog(@"%@", [NSString stringWithFormat:@"with Push Token: %@",devToken]);
    }
    
    [PingID setRemoteNotificationsDeviceToken:deviceToken];
}
    
- (NSString *)stringWithDeviceToken:(NSData *)deviceToken
{
    const char *data = [deviceToken bytes];
    NSMutableString *token = [NSMutableString string];
    
    for (NSUInteger i = 0; i < [deviceToken length]; i++) {
        [token appendFormat:@"%02.2hhx", data[i]];
    }
    
    return [token copy];
}
    
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    NSLog(@"didFailToRegisterForRemoteNotificationsWithError");
    NSLog(@"%@",[NSString stringWithFormat:@"error: %@",[error description]]);
}

-(void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
{
    NSLog(@"didReceiveRemoteNotification");
    NSLog(@"userinfo: %@",userInfo);
    if ([PingID isRemoteNotificationFromPingID:userInfo])
    {
        [PingID handleRemoteNotification:userInfo completion:^(PIDRemoteNotificationType remoteNotificationType, NSArray * _Nullable availableTrustLevels, NSDictionary * _Nullable sessionInfo, NSError * _Nullable error)
         {
             if(!error)
             {
                 switch (remoteNotificationType)
                 {
                     case PIDRemoteNotificationTypeAuth:
                     {
                         if(availableTrustLevels)
                         {
                             NSLog(@"Trust levels are available");
                             
                             BOOL isPrimary = [availableTrustLevels containsObject: @(PIDTrustLevelPrimary)];
                             if(isPrimary)
                             {
                                 [self displayAuthAlertWithTitle:NSLocalizedString(@"trusted_title",nil) andMessage:NSLocalizedString(@"primary_message",nil) isPrimary:YES];
                             }
                             else
                             {
                                 [self displayAuthAlertWithTitle:NSLocalizedString(@"trusted_title",nil) andMessage:NSLocalizedString(@"trusted_message",nil) isPrimary:NO];
                             }
                         }
                         else
                         {
                             NSLog(@"Authenticate user");
                             NSLog(@"sessionInfo: %@",sessionInfo);
                             dispatch_async(dispatch_get_main_queue(), ^{
                                 UIViewController *visibleViewController = ((UINavigationController*)self.window.rootViewController).visibleViewController;
                                 if(![visibleViewController isKindOfClass:[AuthViewController class]] && ![visibleViewController isKindOfClass:[SFSafariViewController class]])
                                 {
                                     [visibleViewController performSegueWithIdentifier:kAuthSegue sender:sessionInfo];

                                 }
                             });
                         }
                     }
                         break;
                         
                         case PIDRemoteNotificationTypeCancel:
                     {
                         // This indicates that the authenticating request was canceled. You may take an action here or ignore it.
                     }
                         
                     default:
                         break;
                 }
             }
             else
             {
                 NSLog(@"Error: %@",[error description]);
             }
             completionHandler(UIBackgroundFetchResultNewData);
         }];        
    }
    else
    {
        completionHandler(UIBackgroundFetchResultNoData);
    }
}

-(void)application:(UIApplication *)application handleActionWithIdentifier:(NSString *)identifier forRemoteNotification:(NSDictionary *)userInfo completionHandler:(void (^)())completionHandler
{
    NSLog(@"handleActionWithIdentifier");
    
    if ([PingID isRemoteNotificationFromPingID:userInfo])
    {
        [PingID handleActionWithIdentifier:identifier forRemoteNotification:userInfo completionBlock:^(NSError *error)
         {
             if(error)
             {
                 NSLog(@"%@",[NSString stringWithFormat:@"Error: %@",[error description]]);
             }
             completionHandler();
         }];
    }
}

#pragma mark - Authentication

// Ask the user if the added device should be trusted

-(void)displayAuthAlertWithTitle:(NSString *)title andMessage:(NSString *)message isPrimary:(BOOL)primary
{
    UIViewController *visibleViewController = ((UINavigationController*)self.window.rootViewController).visibleViewController;
    
    UIAlertController *alertController = [UIAlertController  alertControllerWithTitle:title message:message  preferredStyle:UIAlertControllerStyleAlert];
    
    NSString *buttonName;
    
    if(primary)
    {
        buttonName = NSLocalizedString(@"approve",nil);
    }
    else
    {
        buttonName = NSLocalizedString(@"trust",nil);
    }
    
    [alertController addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"deny",nil) style:UIAlertActionStyleDestructive handler:^(UIAlertAction *action)
                                {
                                    PIDUserSelectionObject *selection = [[PIDUserSelectionObject alloc]initWithAction:PIDActionTypeDeny trustLevel:PIDTrustLevelNone userName:nil];
                                    [self setAuthenticationSelection:selection];
                                    [visibleViewController dismissViewControllerAnimated:YES completion:nil];
                                }]];
    
    [alertController addAction:[UIAlertAction actionWithTitle:buttonName style:UIAlertActionStyleDefault handler:^(UIAlertAction *action)
                                {
                                    PIDUserSelectionObject *selection = [[PIDUserSelectionObject alloc]initWithAction:PIDActionTypeNone trustLevel:primary?PIDTrustLevelPrimary:PIDTrustLevelTrusted userName:nil];
                                    [self setAuthenticationSelection:selection];
                                    [visibleViewController dismissViewControllerAnimated:YES completion:nil];
                                }]];
    

    dispatch_async(dispatch_get_main_queue(), ^{
        [visibleViewController presentViewController:alertController animated:YES completion:nil];
    });
}

// Deny request or set device as Primary or Trusted

- (void)setAuthenticationSelection:(PIDUserSelectionObject *)selection
{   
    [PingID setAuthenticationUserSelection:selection completionBlock:^(NSError * _Nullable error)
     {
         if(!error)
         {
             NSLog(@"Success");
         }
         else
         {
             NSLog(@"Error: %@",[error description]);
         }
     }];
}

#pragma mark - Ping Federate & QR Auth

/*! @brief Handles inbound URLs. Checks if the URL matches the redirect URI for a pending
 AppAuth authorization request.
 */
- (BOOL)application:(UIApplication *)app
            openURL:(NSURL *)url
            options:(NSDictionary<NSString *, id> *)options {
    // Sends the URL to the current authorization flow (if any) which will process it if it relates to
    // an authorization response.
    
    // Ping Federate
    if ([_currentAuthorizationFlow resumeAuthorizationFlowWithURL:url])
    {
        _currentAuthorizationFlow = nil;
        return YES;
    }

    // QR Auth
    if([PingID isDeviceTrusted])
    {
        if([url.absoluteString containsString:kPingIDSDK])
        {
            NSString *queryString = [url query];
            NSString *token = [queryString stringByReplacingOccurrencesOfString:kAuthenticationToken withString:@""];
            if(token)
            {
                //self.currentSessionInfo = nil;
                NSLog(@"Authentication Token: %@",token);
                [PingID validateAuthenticationToken:token];
            }
            else
            {
                NSLog(@"Token is missing");
            }
        }
        else
        {
            NSLog(@"PingID SDK Token wasn't found");
        }
    }
    
    return NO;
}

/*! @brief Forwards inbound URLs for iOS 8.x and below to @c application:openURL:options:.
 @discussion When you drop support for versions of iOS earlier than 9.0, you can delete this
 method. NB. this implementation doesn't forward the sourceApplication or annotations. If you
 need these, then you may want @c application:openURL:options to call this method instead.
 */
- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication
         annotation:(id)annotation {
    return [self application:application
                     openURL:url
                     options:@{}];
}

@end
