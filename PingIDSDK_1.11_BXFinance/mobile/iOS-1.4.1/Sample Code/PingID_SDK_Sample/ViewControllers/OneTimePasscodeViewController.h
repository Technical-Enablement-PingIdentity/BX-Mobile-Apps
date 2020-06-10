//
//  OneTimePasscodeViewController.h
//  PingID_SDK_Sample
//
//  Created by Segev Sherry on 3/12/18.
//  Copyright © 2018 Ping Identity. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface OneTimePasscodeViewController : UIViewController

@property (copy, nonatomic) void (^didDismiss)(NSString *oneTimePasscodeString);

@property (weak, nonatomic) IBOutlet UITextField    *oneTimePasscodeTextField;
@property (weak, nonatomic) IBOutlet UIButton       *enterOneTimePasscodeOutlet;
@property (weak, nonatomic) IBOutlet UILabel        *versionLabelOutlet;
@property (weak, nonatomic) IBOutlet UIImageView    *logoImageView;

- (IBAction)editingChanged:(UITextField *)sender;
- (IBAction)enterOneTimePasscodeButton:(UIButton *)sender;


@end
