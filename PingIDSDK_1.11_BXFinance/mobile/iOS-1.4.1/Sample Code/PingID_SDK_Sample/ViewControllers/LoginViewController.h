//
//  LoginViewController.h
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 3/23/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import <UIKit/UIKit.h>
#import "AppDelegate.h"
#import <PingID_SDK/PingID.h>
#import <AppAuth/AppAuth.h>


@interface LoginViewController : UIViewController <PingIDDelegate>

@property (strong, nonatomic)        NSString                   *currentPayload;

@property (weak, nonatomic) IBOutlet UITextField                *usernameTextField;
@property (weak, nonatomic) IBOutlet UITextField                *passwordTextField;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView    *activityIndicator;
@property (weak, nonatomic) IBOutlet UIButton                   *loginButtonOutlet;
@property (weak, nonatomic) IBOutlet UIButton                   *loginPingFedButtonOutlt;
@property (weak, nonatomic) IBOutlet UIButton                   *scanOutlt;
@property (weak, nonatomic) IBOutlet UILabel                    *versionLabelOutlet;
@property (weak, nonatomic) IBOutlet UIImageView                *logoImageView;
@property (weak, nonatomic) IBOutlet UIView                     *authStateView;

- (IBAction)loginButton             :(UIButton *)sender;
- (IBAction)loginWithPingFederate   :(UIButton *)sender;
- (IBAction)editingChanged          :(UITextField *)sender;
- (void)postIDPAuthenticationStep;

@end
