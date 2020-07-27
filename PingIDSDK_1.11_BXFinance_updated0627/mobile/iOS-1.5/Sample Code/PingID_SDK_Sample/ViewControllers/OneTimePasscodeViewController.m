//
//  OneTimePasscodeViewController.m
//  PingID_SDK_Sample
//
//  Created by Segev Sherry on 3/12/18.
//  Copyright © 2018 Ping Identity. All rights reserved.
//

#import "OneTimePasscodeViewController.h"
#import "HelperMethods.h"

@interface OneTimePasscodeViewController ()

@end

@implementation OneTimePasscodeViewController

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
    
    self.enterOneTimePasscodeOutlet.enabled = NO;
    self.enterOneTimePasscodeOutlet.alpha = 0.6;
    self.enterOneTimePasscodeOutlet.backgroundColor = [Constants approveColor];
}

- (IBAction)enterOneTimePasscodeButton:(UIButton *)sender
{
    dispatch_async(dispatch_get_main_queue(), ^{
       [self dismissViewControllerAnimated:YES completion:nil];
    });
    
    if (self.didDismiss)
    {
        self.didDismiss(self.oneTimePasscodeTextField.text);
    }
}

- (IBAction)editingChanged:(UITextField *)sender
{
    if(sender.text.length > 0)
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.enterOneTimePasscodeOutlet.enabled = YES;
            self.enterOneTimePasscodeOutlet.alpha = 1;
        });
    }
    else
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.enterOneTimePasscodeOutlet.enabled = NO;
            self.enterOneTimePasscodeOutlet.alpha = 0.6;
        });
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
