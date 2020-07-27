//
//  CommunicationManager.h
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 03/23/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import <Foundation/Foundation.h>

typedef void(^RequestCompleted)(NSDictionary *response, NSError *error);

@interface CommunicationManager : NSObject

-(void)postWithParams:(NSDictionary *)params completionBlock:(RequestCompleted)completionBlock;
-(void)authenticate:(NSDictionary *)params completionBlock:(RequestCompleted)completionBlock;
-(void)authenticateWithOneTimePasscode:(NSDictionary *)params completionBlock:(RequestCompleted)completionBlock;

@end
