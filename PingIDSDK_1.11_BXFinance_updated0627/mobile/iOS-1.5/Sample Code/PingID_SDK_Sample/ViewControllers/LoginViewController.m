//
//  LoginViewController.m
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 3/23/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import "LoginViewController.h"
#import "CommunicationManager.h"
#import "LocationServices.h"
#import "HelperMethods.h"
#import "AuthViewController.h"
#import "HomeViewController.h"
#import "OneTimePasscodeViewController.h"
#import "QRScannerViewController.h"
#import "UserSelectionViewController.h"
#import <AVFoundation/AVFoundation.h>

typedef void (^PostRegistrationCallback)(OIDServiceConfiguration *configuration,
                                         OIDRegistrationResponse *registrationResponse);

@interface LoginViewController ()

@property (strong, nonatomic)   CommunicationManager    *commManager;
@property (strong, nonatomic)   LocationServices        *locationServices;
@property (strong, nonatomic)   NSString                *currentSumStr;
@property (strong, nonatomic)   NSDictionary            *currentSessionInfo;
@property (strong, nonatomic)   NSDictionary            *selectedUsernameForQrAuth;

@end

@implementation LoginViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    

    //[HelperMethods addGradientTo:self.view];
    
    self.logoImageView.image = [UIImage imageNamed:kLogoImageName];
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    NSLocale* formatterLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_GB"];
    [formatter setLocale:formatterLocale];
    [formatter setDateFormat:@"YYYY"];
    self.versionLabelOutlet.text = [NSString stringWithFormat:kModernoCopyRight,[formatter stringFromDate:[NSDate date]],[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"]];
    self.activityIndicator.alpha = 0;
    self.commManager = [[CommunicationManager alloc]init]; // Used to communicate with the customer server
    
    self.locationServices = [[LocationServices alloc]init];
    
    CGColorRef colorRef = [UIColor colorWithRed:0.74 green:0.83 blue:0.87 alpha:1.0].CGColor;
    self.usernameTextField.layer.cornerRadius   = 5.0f;
    self.usernameTextField.layer.masksToBounds  = YES;
    self.usernameTextField.layer.borderColor    = colorRef;
    self.usernameTextField.layer.borderWidth    = 1.0f;
    
    self.passwordTextField.layer.cornerRadius   = 5.0f;
    self.passwordTextField.layer.masksToBounds  = YES;
    self.passwordTextField.layer.borderColor    = colorRef;
    self.passwordTextField.layer.borderWidth    = 1.0f;
    
    self.loginButtonOutlet.enabled = NO;
    self.loginButtonOutlet.alpha = 0.6;
    self.loginButtonOutlet.backgroundColor = [Constants approveColor];
    self.loginPingFedButtonOutlt.backgroundColor = [Constants pingFedColor];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [PingID setPingIDDelegate:self];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if([PingID isDeviceTrusted])
    {
        self.scanOutlt.hidden = NO;
    }
}

-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    self.passwordTextField.text = @"";
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Buttons

- (IBAction)loginButton:(UIButton *)sender
{
    [self.view endEditing:YES];
    
    NSError *clientError = nil;
    self.currentPayload = [PingID generatePayload:&clientError];
    if(self.currentPayload && clientError == nil)
    {
        [self authenticate];
    }
}

- (IBAction)loginWithPingFederate:(UIButton *)sender
{
    NSError *clientError = nil;
    self.currentPayload = [PingID generatePayload:&clientError];
    if(self.currentPayload && clientError == nil)
    {
        [self authenticateWithPingFederate];
    }    
}

#pragma mark - Auth

// Authenticate user via the customer server

- (void)authenticate
{
    NSDictionary *params = @{kAuthType:kOnline,
                             kUser:self.usernameTextField.text,
                             kPassword:self.passwordTextField.text,
                             kPingPayload:self.currentPayload,
                             kAppIdKey:kAppID,
                             kOperation:kAuthUser};
    
    __weak typeof(self) weakSelf = self;
    
    [self showActivityIndicator:YES];
    
    [self.commManager authenticate:params completionBlock:^(NSDictionary *response, NSError *error)
     {
         __strong typeof(self) strongSelf = weakSelf;
         
         if(response && !error)
         {
             NSInteger status = [[response objectForKey:kStatus] integerValue];
             
             switch (status)
             {
                 case STATUS_OK:
                 case STATUS_ONE_TIME_PASSCODE:
                 case STATUS_DEVICE_BYPASSED:
                     [strongSelf handleAuthResponse:response];
                     break;
                 case STATUS_AUTHENTICATION_DENIED:
                     [self showAlertWithMessage:NSLocalizedString(@"auth_denied", nil)];
                     [strongSelf showActivityIndicator:NO];
                     break;
                 default:
                     [strongSelf showActivityIndicator:NO];
                     break;
             }
         }
         else
         {
             [strongSelf showActivityIndicator:NO];
             NSLog(@"Error: %@",[NSString stringWithFormat:@"%@",[error description]]);
         }
     }];
}

// Handle the customer server response and set the PingID payload

- (void)handleAuthResponse:(NSDictionary *)response
{
    NSString *pingIdPayload = [response valueForKey:kPingPayload];
    self.currentSumStr = [response objectForKey:kSum];
    
    if(!pingIdPayload || [pingIdPayload isEqualToString:@""]) // Sign in without MFA
    {
        [self showActivityIndicator:NO];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self performSegueWithIdentifier:kHomeSegue sender:response];
        });
    }
    else
    {
        [PingID setServerPayload:pingIdPayload completionBlock:^(PIDFlowState flowState,NSArray *availableTrustLevels, NSError *error)
         {
             if(!error)
             {
                 switch (flowState)
                 {
                     case PIDFlowStateTrustLevels:
                     {
                         UIAlertController *alertController =   [UIAlertController
                                                                 alertControllerWithTitle:NSLocalizedString(@"trusted_title",nil)
                                                                 message:NSLocalizedString(@"primary_message",nil)
                                                                 preferredStyle:UIAlertControllerStyleAlert];
                         
                         UIAlertAction *ok = [UIAlertAction
                                              actionWithTitle:NSLocalizedString(@"approve",nil)
                                              style:UIAlertActionStyleDefault
                                              handler:^(UIAlertAction * action)
                                              {
                                                  [alertController dismissViewControllerAnimated:YES completion:nil];
                                                  [self setAnswers:PIDTrustLevelPrimary];
                                                  
                                              }];
                         UIAlertAction *cancel = [UIAlertAction
                                                  actionWithTitle:NSLocalizedString(@"deny",nil)
                                                  style:UIAlertActionStyleDefault
                                                  handler:^(UIAlertAction * action)
                                                  {
                                                      [self showActivityIndicator:NO];
                                                      [alertController dismissViewControllerAnimated:YES completion:nil];
                                                      dispatch_async(dispatch_get_main_queue(), ^{
                                                          [self performSegueWithIdentifier:kHomeSegue sender:response];
                                                      });
                                                      
                                                  }];
                         
                         [alertController addAction:ok];
                         [alertController addAction:cancel];
                         
                         dispatch_async(dispatch_get_main_queue(), ^{
                             [self presentViewController:alertController animated:YES completion:nil];
                         });
                     }
                         break;
                         
                     case PIDFlowStateDeviceIsTrusted:
                     {
                         NSLog(@"PIDFlowStateDeviceIsTrusted");
                         
                         dispatch_async(dispatch_get_main_queue(), ^{
                             [self performSegueWithIdentifier:kHomeSegue sender:response];
                             [self showActivityIndicator:NO];
                         });
                     }
                         break;
                         
                     case PIDFlowStateOneTimePasscode:
                     {
                         NSLog(@"PIDFlowStateOneTimePasscode");

                         PIDUserSelectionObject *selection = [[PIDUserSelectionObject alloc]initWithAction:PIDActionTypeNone trustLevel:PIDTrustLevelTrusted userName:nil];
                         NSError *error = nil;
                         self.currentPayload = [PingID updateExistingPayloadWithUserSelection:selection error:&error];
                         [self showEnterOneTimePasscode:[response valueForKey:kAuthSessionID]];
                     }
                         break;

                     case PIDFlowStateDone:
                     {
                         NSLog(@"PIDFlowStateDone");
                         
                         dispatch_async(dispatch_get_main_queue(), ^{
                             [self performSegueWithIdentifier:kHomeSegue sender:response];
                             [self showActivityIndicator:NO];
                         });
                     }
                     default:
                         break;
                 }
             }
             else
             {
                 [self showActivityIndicator:NO];
                 NSLog(@"%@",[NSString stringWithFormat:@"Error: %@",[error description]]);
             }
         }];

    }
}

// Sets the answer and pairs user

-(void)setAnswers:(PIDTrustLevel)trustLevel
{
    PIDUserSelectionObject *selection = [[PIDUserSelectionObject alloc]initWithAction:PIDActionTypeNone trustLevel:trustLevel userName:nil];

    [PingID setUserSelection:selection completionBlock:^(NSError * _Nullable error)
     {
         [self showActivityIndicator:NO];
         
         if(error)
         {
             NSLog(@"Error: %@",[error description]);
         }
         else
         {
             NSLog(@"Success");
             dispatch_async(dispatch_get_main_queue(), ^{
                 NSDictionary *sumDict = nil;
                 if(self.currentSumStr)
                 {
                     sumDict = @{kSum:self.currentSumStr};
                 }
                [self performSegueWithIdentifier:kHomeSegue sender:sumDict];
              });
         }
     }];
}

#pragma mark - Design

// Show activity indicator when trying to log in

-(void)showActivityIndicator:(BOOL)show
{
    dispatch_async(dispatch_get_main_queue(), ^{
        
        if(show)
        {
            [self.activityIndicator startAnimating];
            self.activityIndicator.alpha = 1;
            self.loginButtonOutlet.alpha = 0;
        }
        else
        {
            [self.activityIndicator stopAnimating];
            self.activityIndicator.alpha = 0;
            self.loginButtonOutlet.alpha = 1;
        }
        
    });
}

- (IBAction)editingChanged:(UITextField *)sender
{
    if(sender.text.length > 0)
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.loginButtonOutlet.enabled = YES;
            self.loginButtonOutlet.alpha = 1;
        });
    }
    else
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.loginButtonOutlet.enabled = NO;
            self.loginButtonOutlet.alpha = 0.6;
        });
    }
}
#pragma mark - UITextField Delegate

-(BOOL) textFieldShouldReturn:(UITextField *)textField
{    
    [textField resignFirstResponder];
    return YES;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:kAuthSegue])
    {
        AuthViewController *vc = [segue destinationViewController];
        NSString *currentTimeout = [sender objectForKey:kCurrentTimeout];
        if(currentTimeout && ![currentTimeout isEqualToString:@""])
        {
            NSInteger timeout = [currentTimeout integerValue];
            vc.currentTimeout = timeout;
        }

        if(currentTimeout && ![currentTimeout isEqualToString:@""])
        {
            NSInteger timeout = [currentTimeout integerValue];
            vc.currentTimeout = timeout;
        }

        NSDictionary *knownUserDict = [sender objectForKey:kUser];
        if(self.selectedUsernameForQrAuth)
        {
            vc.selectedUsername = [NSDictionary dictionaryWithDictionary:self.selectedUsernameForQrAuth];;
            self.selectedUsernameForQrAuth = nil;
        }
        else if (knownUserDict)
        {
            vc.selectedUsername = [NSDictionary dictionaryWithDictionary:knownUserDict];;
        }
        
        NSError *error = nil;
        NSString *clientContext = [sender objectForKey:kClientContext];
        if(clientContext && ![clientContext isEqualToString:@""])
        {
            NSDictionary *someDict = [NSJSONSerialization JSONObjectWithData:[clientContext dataUsingEncoding:NSUTF8StringEncoding] options:0 error:&error];
            if(someDict && !error)
            {
                vc.contextDict = someDict;
            }                
        }
    }
    else if([[segue identifier] isEqualToString:kHomeSegue])
    {
        HomeViewController *vc = [segue destinationViewController];
        if(sender)
        {
            vc.currentSumStr = [sender objectForKey:kSum];
        }
    }
    else if([segue.identifier isEqualToString:kQRScanSegue])
    {
        [PingID setPingIDDelegate:self];
        QRScannerViewController *controller = (QRScannerViewController *)segue.destinationViewController;
        controller.callback = ^(NSString *qrString)
        {
            if(qrString)
            {
                NSURL *url = [NSURL URLWithString:qrString];
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
        };
    }
    else if ([segue.identifier isEqualToString:kQRUserSelectionSegue])
    {
        UserSelectionViewController *controller = (UserSelectionViewController *)segue.destinationViewController;
        controller.currentSessionInfo = [[NSDictionary alloc] initWithDictionary:sender];
        controller.didSelect = ^(NSDictionary *selectedUser)
        {
            if(selectedUser)
            {
                self.currentSessionInfo = [NSDictionary dictionaryWithDictionary:sender];
                NSString *tokenStatus = [self.currentSessionInfo objectForKey:kAuthenticationTokenStatus];
                
                if([tokenStatus isEqualToString:kQrStatusMobileUserSelection])
                {
                    PIDUserSelectionObject *selection = [[PIDUserSelectionObject alloc]initWithAction:PIDActionTypeApprove trustLevel:PIDTrustLevelNone userName:[selectedUser objectForKey:@"username"]];
                    [PingID setAuthenticationUserSelection:selection completionBlock:^(NSError * _Nullable error)
                     {
                         if(error)
                         {
                             NSLog(@"Error: %@",error.description);
                         }
                     }];
                }
                
                if([tokenStatus isEqualToString:kQrStatusMobileUserSelectionAndApproval])
                {
                    self.selectedUsernameForQrAuth = selectedUser;
                    [self performSegueWithIdentifier:kAuthSegue sender:self.currentSessionInfo];
                }
            }
        };
    }
}

-(void)showAlertWithMessage:(NSString *)message
{
    UIAlertController *alertController = [UIAlertController  alertControllerWithTitle:message message:nil  preferredStyle:UIAlertControllerStyleAlert];
    
    [alertController addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"ok", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction *action)
                                {
                                    [self dismissViewControllerAnimated:YES completion:nil];
                                }]];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self presentViewController:alertController animated:YES completion:nil];
    });
}

#pragma mark - One Time Passcode
- (void)showEnterOneTimePasscode:(NSString *)sessionId
{
    OneTimePasscodeViewController *oneTimePasscodeVc = [self.storyboard instantiateViewControllerWithIdentifier:@"OneTimePasscodeID"];
    oneTimePasscodeVc.didDismiss = ^(NSString *oneTimePasscodeString)
    {
        [self offlineAuthentication:sessionId withOtp:oneTimePasscodeString];
    };
    
    dispatch_async(dispatch_get_main_queue(), ^{
           [self presentViewController:oneTimePasscodeVc animated:YES completion:nil];
    });

}

- (void)offlineAuthentication:(NSString *)sessionId withOtp:(NSString *)otp
{
    NSDictionary *params = @{kSessionID:sessionId,
                             kOperation:kAuthUserOffline,
                             kOTP:otp,
                             kPingPayload:self.currentPayload};
    
    __weak typeof(self) weakSelf = self;
    
    [self showActivityIndicator:YES];
    
    [self.commManager authenticateWithOneTimePasscode:params completionBlock:^(NSDictionary *response, NSError *error)
     {
         __strong typeof(self) strongSelf = weakSelf;
         
         if(response && !error)
         {
             NSInteger status = [[response objectForKey:kStatus] integerValue];
             
             switch (status)
             {
                 case STATUS_OK:
                     [strongSelf handleOfflineAuthResponse:response];
                     break;
                 case STATUS_AUTHENTICATION_DENIED:
                     [strongSelf showActivityIndicator:NO];
                     [self showAlertWithMessage:NSLocalizedString(@"auth_denied", nil)];
                     break;
                 case STATUS_INVALID_OTP:
                 {
                     [strongSelf showActivityIndicator:NO];
                     dispatch_async(dispatch_get_main_queue(), ^{
                         [self performSegueWithIdentifier:kHomeSegue sender:response];
                     });
                 }
                     break;
                 default:
                     [strongSelf showActivityIndicator:NO];
                     break;
             }
         }
         else
         {
             [strongSelf showActivityIndicator:NO];
             NSLog(@"Error: %@",[NSString stringWithFormat:@"%@",[error description]]);
         }
     }];
}


- (void)handleOfflineAuthResponse:(NSDictionary *)response
{
    NSString *pingIdPayload = [response valueForKey:kPingPayload];
    self.currentSumStr = [response objectForKey:kSum];
    
    if(!pingIdPayload || [pingIdPayload isEqualToString:@""]) // Sign in without MFA
    {
        [self showActivityIndicator:NO];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self performSegueWithIdentifier:kHomeSegue sender:response];
        });
    }
    else
    {
        [PingID setServerPayload:pingIdPayload completionBlock:^(PIDFlowState flowState,NSArray *availableTrustLevels, NSError *error)
         {
             if(!error)
             {
                 NSLog(@"One time passcose success");
                 [self showActivityIndicator:NO];
                 dispatch_async(dispatch_get_main_queue(), ^{
                     [self performSegueWithIdentifier:kHomeSegue sender:response];
                 });
                 
             }
             else
             {
                 [self showActivityIndicator:NO];
                 NSLog(@"%@",[NSString stringWithFormat:@"Error: %@",[error description]]);
             }
         }];
        
    }
}

#pragma mark - Auth with Ping Federate
-(void)authenticateWithPingFederate
{
    NSURL *issuer = [NSURL URLWithString:kIssuer];
    
    NSLog(@"Fetching configuration for issuer: %@", issuer);
    
    // discovers endpoints
    [OIDAuthorizationService discoverServiceConfigurationForIssuer:issuer
                                                        completion:^(OIDServiceConfiguration *_Nullable configuration, NSError *_Nullable error) {
                                                            if (!configuration) {
                                                                NSLog(@"Error retrieving discovery document: %@", [error localizedDescription]);
                                                                return;
                                                            }
                                                            
                                                            NSLog(@"Got configuration: %@", configuration);
                                                            
                                                            [self doAuthWithAutoCodeExchange:configuration clientID:kClientID clientSecret:nil];
                                                        }];
}


- (void)doAuthWithAutoCodeExchange:(OIDServiceConfiguration *)configuration
                          clientID:(NSString *)clientID clientSecret:(NSString *)clientSecret
{
    NSURL *redirectURI = [NSURL URLWithString:kRedirectURI];
    // builds authentication request
    OIDAuthorizationRequest *request =
    [[OIDAuthorizationRequest alloc] initWithConfiguration:configuration
                                                  clientId:clientID
                                              clientSecret:clientSecret
                                                    scopes:@[ OIDScopeOpenID, OIDScopeProfile ]
                                               redirectURL:redirectURI
                                              responseType:OIDResponseTypeCode
                                      additionalParameters:@{kPayload:self.currentPayload}];
    // performs authentication request
    AppDelegate *appDelegate = (AppDelegate *) [UIApplication sharedApplication].delegate;
    NSLog(@"Initiating authorization request with scope: %@", request.scope);
    
    appDelegate.currentAuthorizationFlow =
    [OIDAuthState authStateByPresentingAuthorizationRequest:request
                                   presentingViewController:self
                                                   callback:^(OIDAuthState *_Nullable authState, NSError *_Nullable error) {
                                                       if (authState) {
                                                           NSLog(@"Got authorization tokens. Access token: %@",
                                                                 authState.lastTokenResponse.accessToken);
                                                           [self postIDPAuthenticationStep];
                                                       } else {
                                                           NSLog(@"Authorization error: %@", [error localizedDescription]);
                                                       }
                                                   }];
}

-(void)postIDPAuthenticationStep
{
    [PingID postIDPAuthenticationStepWithDataCenter:PIDDataCenterTypeDefault completionBlock:^(PIDFlowState flowState, NSArray * _Nullable availableTrustLevels, NSError * _Nullable error) {
        if(!error)
        {
            switch (flowState)
            {
                case PIDFlowStateTrustLevels:
                {
                    NSLog(@"Trust levels are available");
                    
                    BOOL isPrimary = [availableTrustLevels containsObject: @(PIDTrustLevelPrimary)];
                    NSString *trustMessage = NSLocalizedString(@"trusted_message",nil);
                    if(isPrimary)
                    {
                        trustMessage = NSLocalizedString(@"primary_message",nil);
                    }

                    
                    UIAlertController *alertController =   [UIAlertController
                                                            alertControllerWithTitle:NSLocalizedString(@"trusted_title",nil)
                                                            message:trustMessage
                                                            preferredStyle:UIAlertControllerStyleAlert];
                    
                    UIAlertAction *ok = [UIAlertAction
                                         actionWithTitle:NSLocalizedString(@"approve",nil)
                                         style:UIAlertActionStyleDefault
                                         handler:^(UIAlertAction * action)
                                         {
                                             [alertController dismissViewControllerAnimated:YES completion:nil];
                                             if(isPrimary)
                                             {
                                                 [self setAnswers:PIDTrustLevelPrimary];
                                             }
                                             else
                                             {
                                                 [self setAnswers:PIDTrustLevelTrusted];
                                             }

                                             
                                         }];
                    UIAlertAction *cancel = [UIAlertAction
                                             actionWithTitle:NSLocalizedString(@"deny",nil)
                                             style:UIAlertActionStyleDefault
                                             handler:^(UIAlertAction * action)
                                             {
                                                 [self showActivityIndicator:NO];
                                                 [alertController dismissViewControllerAnimated:YES completion:nil];
                                                 dispatch_async(dispatch_get_main_queue(), ^{
                                                     [self performSegueWithIdentifier:kHomeSegue sender:nil];
                                                 });
                                                 
                                             }];
                    [alertController addAction:ok];                    
                    [alertController addAction:cancel];
                    
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self presentViewController:alertController animated:YES completion:nil];
                    });
                }
                    break;
                    
                case PIDFlowStateDeviceIsTrusted:
                {
                    NSLog(@"PIDFlowStateDeviceIsTrusted");
                    
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self performSegueWithIdentifier:kHomeSegue sender:nil]; 
                        [self showActivityIndicator:NO];
                    });
                }
                    break;
                    
                default:
                {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self performSegueWithIdentifier:kHomeSegue sender:nil];
                        [self showActivityIndicator:NO];
                    });
                }
                    break;
            }
        }
        else
        {
            [self showActivityIndicator:NO];
            NSLog(@"%@",[NSString stringWithFormat:@"Error: %@",[error description]]);
        }
    }];
}

#pragma mark - PingID Delegate
-(void)authenticationTokenStatus:(NSDictionary *)sessionInfo error:(NSError * _Nullable)error
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if(!error)
        {
            NSLog(@"Authentication Token Status: %@",sessionInfo);
            self.currentSessionInfo = [NSDictionary dictionaryWithDictionary:sessionInfo];
            NSString *tokenStatus = [self.currentSessionInfo objectForKey:kAuthenticationTokenStatus];
            
            if([tokenStatus isEqualToString:kQrStatusPendingUserApproval])
            {
                [self performSegueWithIdentifier:kAuthSegue sender:sessionInfo];
            }
            
            if([tokenStatus isEqualToString:kQrStatusMobileUserSelection] ||
               [tokenStatus isEqualToString:kQrStatusMobileUserSelectionAndApproval])
            {
                [self performSegueWithIdentifier:kQRUserSelectionSegue sender:self.currentSessionInfo];
            }
            if([tokenStatus isEqualToString:kQrStatusClaimed])
            {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [UIView animateWithDuration:0.4 animations:^() {self.authStateView.alpha = 1;}completion:^(BOOL finished)
                     {
                         dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                             [UIView animateWithDuration:0.4 animations:^() {self.authStateView.alpha = 0;}
                                              completion:nil];
                         });
                     }];
                });
            }
        }
        else
        {
            NSLog(@"Error: %@",error.description);
        }
    });
}

-(void)didUntrustDevice
{
    NSLog(@"Device was untrusted");
    dispatch_async(dispatch_get_main_queue(), ^{
       self.scanOutlt.hidden = YES;
    });
}

@end
