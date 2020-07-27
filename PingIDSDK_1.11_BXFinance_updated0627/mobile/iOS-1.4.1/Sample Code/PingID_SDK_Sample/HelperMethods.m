//
//  HelperMethods.m
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 3/30/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import "HelperMethods.h"

@implementation HelperMethods

+(void)addGradientTo:(UIView *)view
{
    CAGradientLayer *gradient = [CAGradientLayer layer];
    gradient.frame = view.bounds;
    gradient.colors = @[(id)[Constants gradientTopColor].CGColor, (id)[Constants gradientBottomColor].CGColor];
    [view.layer insertSublayer:gradient atIndex:0];
}

@end
