//
//  QRScannerViewController.h
//  Moderno
//
//  Created by Segev Sherry on 8/27/18.
//  Copyright © 2018 Segev Sherry. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface QRScannerViewController : UIViewController

@property (copy) void(^callback)(NSString *qrString);

@property (weak, nonatomic) IBOutlet UIButton *closeScannerOutlt;
@property (weak, nonatomic) IBOutlet UIView *invalidQrView;

- (IBAction)closeScannerBtn:(UIButton *)sender;

@end
