//
//  AuthViewController.h
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 3/27/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import <UIKit/UIKit.h>
#import <PingID_SDK/PingID.h>

@interface AuthViewController : UIViewController <PingIDDelegate>

@property (strong, nonatomic)           NSDictionary    *selectedUsername;
@property (strong, nonatomic)           NSDictionary    *contextDict;

@property (weak, nonatomic) IBOutlet    UIImageView     *logoImageView;
@property (weak, nonatomic) IBOutlet    UILabel         *contextLbl;
@property (weak, nonatomic) IBOutlet    UILabel         *contextTitleLbl;
@property (weak, nonatomic) IBOutlet    UILabel         *contextUsernameLbl;
@property (weak, nonatomic) IBOutlet    UIView          *transactionDetails;
@property (weak, nonatomic) IBOutlet    UIView          *signOnDetails;
@property (weak, nonatomic) IBOutlet    UIView          *authStateView;
@property (weak, nonatomic) IBOutlet    UIImageView     *authStateImage;
@property (weak, nonatomic) IBOutlet    UIButton        *approveOutlt;
@property (weak, nonatomic) IBOutlet    UIButton        *denyOutlt;

@property (assign, nonatomic)           NSInteger       currentTimeout;


- (IBAction)approveButton   :(UIButton *)sender;
- (IBAction)denyButton      :(UIButton *)sender;

@end
