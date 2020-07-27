//
//  HostCommunication.m
//  Moderno by Ping Identity
//
//  Created by Ping Identity on 03/23/17.
//  Copyright © 2017 Ping Identity. All rights reserved.
//
// See LICENSE.txt for this sample’s licensing information and
// LICENSE_SDK.txt for the PingID SDK library licensing information.
//

#import "CommunicationManager.h"

@implementation CommunicationManager

-(void)postWithParams:(NSDictionary *)params completionBlock:(RequestCompleted)completionBlock
{
    NSError *error;
    
    NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:kCustomerUrl]
                                                           cachePolicy:NSURLRequestUseProtocolCachePolicy
                                                       timeoutInterval:30.0];
    
    [request addValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request addValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [request setHTTPMethod:@"POST"];
    
    NSData *postData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    
    [request setHTTPBody:postData];
    
    [[session dataTaskWithRequest:request
                completionHandler:^(NSData *data, NSURLResponse *response, NSError *error)
      {
          
          NSLog(@"%@", [NSString stringWithFormat:@"Response for %@",[params objectForKey:@"operation"]]);
          if(error)
          {
              NSLog(@"%@", [NSString stringWithFormat:@"There was en error: %@", [error description]]);
              completionBlock(nil,error);
          }
          else
          {
              NSError *errorJson;
              NSDictionary *responseDict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableLeaves error:&errorJson];
              if(responseDict)
              {
                  completionBlock(responseDict,nil);
              }
              else
              {
                  NSLog(@"Error: Response was empty");
                  completionBlock(nil,errorJson);
              }
          }
      }] resume];
}

-(void)authenticate:(NSDictionary *)params completionBlock:(RequestCompleted)completionBlock
{
    NSLog(@"%@", [NSString stringWithFormat:@"Calling auth with params: %@",params]);
    
    [self postWithParams:params completionBlock:^(NSDictionary *response, NSError *error)
    {
        if(error)
        {
            completionBlock(nil,error);
        }

        else
        {
            NSLog(@"%@",[NSString stringWithFormat:@"%@", response]);
            completionBlock(response,nil);
            
        }
    }];
}


-(void)authenticateWithOneTimePasscode:(NSDictionary *)params completionBlock:(RequestCompleted)completionBlock
{
    NSLog(@"%@", [NSString stringWithFormat:@"Calling auth with params: %@",params]);
    
    [self postWithParams:params completionBlock:^(NSDictionary *response, NSError *error)
     {
         if(error)
         {
             completionBlock(nil,error);
         }
         
         else
         {
             NSLog(@"%@",[NSString stringWithFormat:@"%@", response]);
             completionBlock(response,nil);
             
         }
     }];
}

@end
