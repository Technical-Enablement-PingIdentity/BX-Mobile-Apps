//
//  HomeViewController.h
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

@interface HomeViewController : UIViewController <PingIDDelegate>

@property (strong, nonatomic)           NSString    *currentSumStr;

@property (weak, nonatomic) IBOutlet    UILabel     *currentSumLbl;
@property (weak, nonatomic) IBOutlet    UIButton    *signOutOutlt;
@property (weak, nonatomic) IBOutlet    UILabel     *oneTimePasscodeTitleLbl;
@property (weak, nonatomic) IBOutlet    UILabel     *oneTimePasscodeLbl;
@property (weak, nonatomic) IBOutlet    UIButton    *oneTimePasscodeButtonOutlet;
@property (weak, nonatomic) IBOutlet    UILabel     *accountBalanceTitle;
@property (weak, nonatomic) IBOutlet    UIButton    *scanOutlt;
@property (weak, nonatomic) IBOutlet    UIView      *authStateView;

- (IBAction)getNextOneTimePasscode      :(UIButton *)sender;
- (IBAction)signOutButton               :(UIButton *)sender;

@end
