//
//  UserSelectionViewController.m
//  PingID_SDK_Sample
//
//  Created by Segev Sherry on 11/5/18.
//  Copyright © 2018 Ping Identity. All rights reserved.
//

#import "UserSelectionViewController.h"
#import "HelperMethods.h"
#import "UserTableViewCell.h"

#define CELL_HEIGHT 50

@interface UserSelectionViewController ()

@property (strong, nonatomic) NSMutableArray *users;

@end

@implementation UserSelectionViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [HelperMethods addGradientTo:self.view];
    self.logoImageView.image = [UIImage imageNamed:kLogoImageName];
    self.versionLabelOutlet.text = [NSString stringWithFormat:kModernoCopyRight,[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"]];
    self.contextMessageLbl.text = @"Select the user to sign on as:";
    NSMutableArray *allUsers = [[NSMutableArray alloc]initWithArray:[self.currentSessionInfo objectForKey:kUsers]];
    self.users = [[NSMutableArray alloc] init];
    
    for (NSDictionary *user in allUsers)
    {
        if([[user objectForKey:@"status"] isEqualToString:@"ACTIVE"])
        {
            [self.users addObject:user]; 
        }
    }

    self.usersTableView.layer.borderWidth = 1.0;
    self.usersTableView.layer.cornerRadius = 10.0f;
    self.usersTableView.layer.borderColor = [UIColor colorWithRed:0.80 green:0.80 blue:0.80 alpha:1.00].CGColor;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    NSInteger numberOfCellsToDisplay = 0;
    if([self.users count] > 3)
    {
        numberOfCellsToDisplay = 3;
    }
    else
    {
        numberOfCellsToDisplay = [self.users count];
    }
    
    CGRect frame = self.usersTableView.frame;
    frame.size.height = CELL_HEIGHT * numberOfCellsToDisplay;
    dispatch_async(dispatch_get_main_queue(), ^{
           self.usersTableView.frame = frame;
    });
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

# pragma mark - Table View

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{

    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.users count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    
    UserTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell"];
    UIView *bgColorView = [[UIView alloc] init];
    bgColorView.backgroundColor = [UIColor colorWithRed:0.92 green:0.96 blue:0.97 alpha:1.00];
    [cell setSelectedBackgroundView:bgColorView];
    
    cell.usernameLbl.text = nil;
    NSDictionary *singleUser = [self.users objectAtIndex:indexPath.row];
    if(![[singleUser objectForKey:@"firstName"] isEqualToString:@""])
    {
        cell.usernameLbl.text = [NSString stringWithFormat:@"%@ %@",[singleUser objectForKey:@"firstName"], [singleUser objectForKey:@"lastName"]];
    }
    else
    {
        cell.usernameLbl.text = [singleUser objectForKey:@"username"];
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self dismissViewControllerAnimated:YES completion:^{
            if (self.didSelect)
            {
                self.didSelect([self.users objectAtIndex:indexPath.row]);
            }
        }];
    });
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return CELL_HEIGHT;
}
@end
