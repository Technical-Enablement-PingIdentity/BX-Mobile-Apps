//
//  LocationManager.m
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 2/23/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import "LocationServices.h"
#import <PingID_SDK/PingID.h>

@interface LocationServices()

@property (strong, nonatomic) CLLocationManager *manager;

@end

@implementation LocationServices

// Init CLLocationManager and ask for location services permission

-(id)init
{
    if(self = [super init])
    {
        self.manager = [[CLLocationManager alloc] init];
        self.manager.delegate = self;
        [self.manager requestWhenInUseAuthorization];
    }
    return self;
}

@end
