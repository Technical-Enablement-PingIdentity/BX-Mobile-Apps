//
//  UserSelectionViewController.h
//  PingID_SDK_Sample
//
//  Created by Segev Sherry on 11/5/18.
//  Copyright © 2018 Ping Identity. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UserSelectionViewController : UIViewController

@property (copy, nonatomic) void (^didSelect)(NSDictionary *selectedUser);

@property (nonatomic, strong)        NSDictionary   *currentSessionInfo;

@property (weak, nonatomic) IBOutlet UILabel        *versionLabelOutlet;
@property (weak, nonatomic) IBOutlet UIImageView    *logoImageView;
@property (weak, nonatomic) IBOutlet UITableView    *usersTableView;
@property (weak, nonatomic) IBOutlet UILabel        *contextMessageLbl;

@end
