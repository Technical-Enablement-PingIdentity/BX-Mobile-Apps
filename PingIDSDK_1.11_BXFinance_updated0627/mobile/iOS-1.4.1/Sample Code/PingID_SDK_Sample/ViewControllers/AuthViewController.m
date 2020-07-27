//
//  AuthViewController.m
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 3/27/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import "AuthViewController.h"
#import "HelperMethods.h"

/*
 * This ViewController is used for MFA user authentication. It can be modified to prompt the user for a PIN-Code,
 * fingerprint or any other way to verify the user's identity
 */

@interface AuthViewController ()

@property (strong, nonatomic) NSTimer               *authTimer;
@property (strong, nonatomic) UIAlertController     *spinnerAlert;
@property (strong, nonatomic) UIAlertController     *authAlert;

@property (assign, nonatomic) BOOL                  isTransaction;
@property (assign, nonatomic) BOOL                  isAuthentication;

@end

@implementation AuthViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.logoImageView.image = [UIImage imageNamed:kLogoImageName];
    if(self.currentTimeout != 0)
    {
        self.currentTimeout = self.currentTimeout/1000;
    }
    else
    {
        self.currentTimeout = 40;
    }   
    
    self.approveOutlt.backgroundColor = [Constants approveColor];
    self.denyOutlt.backgroundColor = [Constants denyColor];
    
    if(self.contextDict && [self.contextDict count] > 0)
    {

        NSString *contextMessage = [self.contextDict objectForKey:kMessage];
        if([[self.contextDict objectForKey:kTransactionType] isEqualToString:kAuthentication])
        {
            //if auth
            [HelperMethods addGradientTo:self.view];
            self.contextTitleLbl.text = NSLocalizedString(@"sign_on_title", nil);
            self.contextUsernameLbl.text = contextMessage;
            self.isAuthentication = YES;
        }
        else if([[self.contextDict objectForKey:kTransactionType] isEqualToString:kSteUpAuthentication])
        {
            //if transfer
            [HelperMethods addGradientTo:self.view];
            self.contextTitleLbl.text = NSLocalizedString(@"transaction_title", nil);
            self.contextLbl.text = contextMessage;
            self.isTransaction = YES;
            self.transactionDetails.alpha = 1;
            self.signOnDetails.alpha   = 0;
        }
        else // QR Auth
        {
            self.denyOutlt.alpha = 0;
            self.approveOutlt.alpha = 0;
            self.contextTitleLbl.alpha = 0;
            self.signOnDetails.alpha = 0;
            self.logoImageView.alpha = 0;
            self.view.backgroundColor = [UIColor clearColor];
            
            NSString *nameForMessage = nil;
            NSString *firstName = [self.selectedUsername objectForKey:@"firstName"];
            if(firstName && ![firstName isEqualToString:@""])
            {
                nameForMessage = firstName;
            }
            else
            {
                nameForMessage = [self.selectedUsername objectForKey:@"username"];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                self.authAlert = [UIAlertController
                                  alertControllerWithTitle:@"Sign On"
                                  message:[NSString stringWithFormat:@"%@, %@",nameForMessage, [self.contextDict objectForKey:kMessage]]
                                  preferredStyle:UIAlertControllerStyleAlert];
                UIAlertAction *approve = [UIAlertAction
                                          actionWithTitle:kApproveCapital
                                          style:UIAlertActionStyleDefault
                                          handler:^(UIAlertAction * action)
                                          {
                                              [self approveButton:nil];
                                          }];
                UIAlertAction *deny = [UIAlertAction
                                       actionWithTitle:kDenyCapital
                                       style:UIAlertActionStyleDefault
                                       handler:^(UIAlertAction * action)
                                       {
                                           [self denyButton:nil];
                                       }];
                [self.authAlert addAction:approve];
                [self.authAlert addAction:deny];
                
                [self presentViewController:self.authAlert animated:YES completion:nil];
            });
        }
    }
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [PingID setPingIDDelegate:self];
    // The user has X seconds to respond to the authentication request.

    self.authTimer = [NSTimer scheduledTimerWithTimeInterval: self.currentTimeout
                                                        target: self
                                                      selector: @selector(authTimeout:)
                                                      userInfo: nil
                                                       repeats: NO];

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}


// Set the user action for this authentication (approve/deny)

- (void)setAuthenticationSelection:(PIDUserSelectionObject *)selection
{

    [PingID setAuthenticationUserSelection:selection completionBlock:^(NSError * _Nullable error)
     {
         dispatch_async(dispatch_get_main_queue(), ^{
                     [self.spinnerAlert dismissViewControllerAnimated:YES completion:nil];
             if(!error)
             {
                 if(selection.action == PIDActionTypeDeny)
                 {
                     if(self.isTransaction)
                     {
                         [self showAlertWithMessage:NSLocalizedString(@"transac_denied", nil)];
                     }
                     else
                     {
                         [self showAlertWithMessage:NSLocalizedString(@"auth_denied", nil)];
                     }
                     
                 }
                 else
                 {
                     if(self.isTransaction)
                     {
                         //[self showAlertWithMessage:NSLocalizedString(@"transac_success", nil)];
                         self.authStateImage.image = [UIImage imageNamed:@"authenticated-transferred"];
                         [UIView animateWithDuration:0.4 animations:^() {self.authStateView.alpha = 1;}completion:^(BOOL finished)
                          {
                              [self performSelector:@selector(dismissViewController) withObject:nil afterDelay:2];
                          }];
                     }
                     else if(self.isAuthentication)
                     {
                         //[self showAlertWithMessage:NSLocalizedString(@"auth_success", nil)];
                         [UIView animateWithDuration:0.4 animations:^() {self.authStateView.alpha = 1;}completion:^(BOOL finished)
                          {
                              [self performSelector:@selector(dismissViewController) withObject:nil afterDelay:2];
                          }];
                     }
                     else
                     {
                         dispatch_async(dispatch_get_main_queue(), ^{
                             self.authStateView.backgroundColor = [UIColor colorWithRed:0.20 green:0.89 blue:0.61 alpha:1.00];
                             self.authStateImage.image = [UIImage imageNamed:@"authenticated-scan"];
                             self.authStateImage.frame = CGRectMake(0, 0, 174, 174);
                             self.authStateImage.center = self.view.center;
                             
                             [UIView animateWithDuration:0.4 animations:^() {self.authStateView.alpha = 1;}completion:^(BOOL finished)
                              {
                                  [self performSelector:@selector(dismissViewController) withObject:nil afterDelay:2];
                              }];
                             
                         });
                     }
                 }
             }
             else
             {
                 NSLog(@"Error: %@",[error description]);
                 [self showAlertWithMessage:NSLocalizedString(@"error", nil)];
             }
         });
     }];
}

#pragma mark - Buttons

- (IBAction)approveButton:(UIButton *)sender
{
    [self showSpinner];
    [self.authTimer invalidate];
    self.authTimer = nil;
    
    NSString *selectedUsername = nil;
    if(self.selectedUsername)
    {
        selectedUsername = [self.selectedUsername objectForKey:@"username"];
    }
    
    PIDUserSelectionObject *selection = [[PIDUserSelectionObject alloc]initWithAction:PIDActionTypeApprove trustLevel:PIDTrustLevelNone userName:selectedUsername];
    [self setAuthenticationSelection:selection];
}

- (IBAction)denyButton:(UIButton *)sender
{
    [self showSpinner];
    [self.authTimer invalidate];
    self.authTimer = nil;
    
    NSString *selectedUsername = nil;
    if(self.selectedUsername)
    {
        selectedUsername = [self.selectedUsername objectForKey:@"username"];
    }
    
    PIDUserSelectionObject *selection = [[PIDUserSelectionObject alloc]initWithAction:PIDActionTypeDeny trustLevel:PIDTrustLevelNone userName:selectedUsername];
    [self setAuthenticationSelection:selection];
}

#pragma mark - TimeOut

// The authentication timed out

-(void)authTimeout:(NSTimer *)sender
{
    NSLog(@"Timeout");
    dispatch_async(dispatch_get_main_queue(), ^{
        self.authStateImage.image = [UIImage imageNamed:@"timedout"];
        if(self.authAlert)
        {
            [self.authAlert dismissViewControllerAnimated:NO completion:nil];
        }
        [UIView animateWithDuration:0.4 animations:^() {self.authStateView.alpha = 1;}completion:^(BOOL finished)
         {
             [self performSelector:@selector(dismissViewController) withObject:nil afterDelay:2];
         }];
 
    });
}

#pragma mark - PingID Delegate
-(void)authenticationTokenStatus:(NSDictionary *)sessionInfo error:(NSError * _Nullable)error
{
    if(!error)
    {
        NSLog(@"Authentication Token Status: %@",sessionInfo);
    }
    else
    {
        NSLog(@"Error: %@",error.description);
    }
}

#pragma mark - Design

-(void)dismissViewController
{
    dispatch_async(dispatch_get_main_queue(), ^{
           [self dismissViewControllerAnimated:YES completion:nil]; 
    });
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

-(void)showSpinner
{
    self.spinnerAlert = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"please_wait", nil)
                                                                   message:nil
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    spinner.frame = self.spinnerAlert.view.bounds;
    spinner.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight);
    spinner.color = [UIColor blackColor];
    [spinner startAnimating];
    [self.spinnerAlert.view addSubview:spinner];
    [self presentViewController:self.spinnerAlert animated:NO completion:nil];
}

@end
