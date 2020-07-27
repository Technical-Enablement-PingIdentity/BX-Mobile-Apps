//
//  HomeViewController.m
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 3/27/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import "HomeViewController.h"
#import "HelperMethods.h"
#import "AuthViewController.h"
#import "QRScannerViewController.h"
#import "UserSelectionViewController.h"

/*
 * This ViewController is an example for the logged in state.
 */

@interface HomeViewController ()

@property (strong, nonatomic)   NSDictionary            *currentSessionInfo;
@property (strong, nonatomic)   NSDictionary            *selectedUsernameForQrAuth;

@end

@implementation HomeViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    //[HelperMethods addGradientTo:self.view];
    
    if(self.currentSumStr && ![self.currentSumStr isEqualToString:@""])
    {
        self.currentSumLbl.text = self.currentSumStr;
    }
    
    [self.signOutOutlt setTitle:NSLocalizedString(@"sign_out", nil) forState:UIControlStateNormal];
    
    [PingID getNextRestrictiveOneTimePasscode:^(NSString * _Nonnull oneTimePasscode, PIDOneTimePasscodeStatus oneTimePasscodeStatus, NSError * _Nullable error) {
        
        switch (oneTimePasscodeStatus)
        {
            case PIDOneTimePasscodeOK:{
                dispatch_async(dispatch_get_main_queue(), ^{
                    self.oneTimePasscodeLbl.text = oneTimePasscode;
                });
                break;
            }
            case PIDOneTimePasscodeDeviceRooted:
            case PIDOneTimePasscodeCanNotCheckRootDetectionAtOffline:
            case PIDOneTimePasscodeUnsuccessesful:{
                NSLog(@"Error: %@",error.description);
                dispatch_async(dispatch_get_main_queue(), ^{
                    self.oneTimePasscodeTitleLbl.alpha      = 0;
                    self.oneTimePasscodeLbl.alpha           = 0;
                    self.oneTimePasscodeButtonOutlet.alpha  = 0;
                });
            }
                break;
            default:
                break;
        }
        
    }];
    
    

    [PingID getNextRestrictiveOneTimePasscode:^(NSString * _Nonnull oneTimePasscode, PIDOneTimePasscodeStatus oneTimePasscodeStatus, NSError * _Nullable error) {
        if (!error)
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                    self.oneTimePasscodeLbl.text = oneTimePasscode;
            });
            
        }
        else
        {
            self.oneTimePasscodeTitleLbl.alpha      = 0;
            self.oneTimePasscodeLbl.alpha           = 0;
            self.oneTimePasscodeButtonOutlet.alpha  = 0;
            
            NSLog(@"Error: %@",error.description);
        }
        
    }];

    self.oneTimePasscodeButtonOutlet.backgroundColor = [Constants denyColor];
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

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Buttons

- (IBAction)getNextOneTimePasscode:(UIButton *)sender
{
    
         [PingID getNextRestrictiveOneTimePasscode:^(NSString * _Nonnull oneTimePasscode, PIDOneTimePasscodeStatus oneTimePasscodeStatus, NSError * _Nullable error) {
           
                 switch (oneTimePasscodeStatus)
                 {
                     case PIDOneTimePasscodeOK:{
                         dispatch_async(dispatch_get_main_queue(), ^{
                             self.oneTimePasscodeLbl.text = oneTimePasscode;
                         });
                         break;
                     }
                     case PIDOneTimePasscodeDeviceRooted:
                     case PIDOneTimePasscodeCanNotCheckRootDetectionAtOffline:
                     case PIDOneTimePasscodeUnsuccessesful:
                         NSLog(@"Error: %@",error.description);
                         break;
                     default:
                         break;
                 }
            
        }];
    
    
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

        NSDictionary *knownUserDict = [sender objectForKey:kUser];
        if(self.selectedUsernameForQrAuth)
        {
            vc.selectedUsername = [NSDictionary dictionaryWithDictionary:self.selectedUsernameForQrAuth];
            self.selectedUsernameForQrAuth = nil;
        }
        else if (knownUserDict)
        {
            vc.selectedUsername = [NSDictionary dictionaryWithDictionary:knownUserDict];
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
    else if([segue.identifier isEqualToString:kQRScanSegue])
    {
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
        controller.currentSessionInfo = sender;
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

- (IBAction)signOutButton:(UIButton *)sender
{
    [self.navigationController popToRootViewControllerAnimated:YES];
}

#pragma mark - PingID Delegate

- (void)didRefreshOneTimePasscode:(nonnull NSString *)oneTimePasscode;
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if(oneTimePasscode)
        {
            self.oneTimePasscodeLbl.text = oneTimePasscode;
            self.oneTimePasscodeTitleLbl.alpha = 1;
            self.oneTimePasscodeLbl.alpha = 1;
        }
    });
    
}

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
