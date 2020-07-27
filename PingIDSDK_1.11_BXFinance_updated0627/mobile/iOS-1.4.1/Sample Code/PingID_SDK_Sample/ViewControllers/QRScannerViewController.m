//
//  QRScannerViewController.m
//  InternalApp
//
//  Created by Segev Sherry on 8/27/18.
//  Copyright © 2018 Segev Sherry. All rights reserved.
//

#import "QRScannerViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <LocalAuthentication/LocalAuthentication.h>

@interface QRScannerViewController () <AVCaptureMetadataOutputObjectsDelegate>

@property (strong, nonatomic) AVCaptureSession              *session;
@property (strong, nonatomic) AVCaptureDevice               *device;
@property (strong, nonatomic) AVCaptureDeviceInput          *input;
@property (strong, nonatomic) AVCaptureMetadataOutput       *output;
@property (strong, nonatomic) AVCaptureVideoPreviewLayer    *prevLayer;
@property (strong, nonatomic) NSString                      *qrString;

@end

@implementation QRScannerViewController

- (void)viewDidLoad
{
    [super viewDidLoad];

    LAContext *context = [[LAContext alloc] init];
    NSError *contextError = nil;
    
    if ([context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&contextError])
    {
        [context evaluatePolicy:LAPolicyDeviceOwnerAuthentication
                localizedReason:@"Authenticate to continue with action" 
                          reply:^(BOOL success, NSError *error)
         {
             if (error)
             {
                 [self dismissViewControllerAnimated:YES completion:nil];
             }
             
             if (success)
             {
                 [self startCameraSeassion];
             }
         }];
    }
    else
    {
        [self startCameraSeassion];
    }

}

-(void)startCameraSeassion
{
    [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted)
     {
         if(granted)
         {
             dispatch_async(dispatch_get_main_queue(), ^{
                 NSLog(@"Granted access to %@", AVMediaTypeVideo);
                 _session = [[AVCaptureSession alloc] init];
                 _device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
                 NSError *error = nil;
                 
                 _input = [AVCaptureDeviceInput deviceInputWithDevice:_device error:&error];
                 if (_input) {
                     [_session addInput:_input];
                 } else {
                     NSLog(@"Error: %@", error);
                 }
                 
                 _output = [[AVCaptureMetadataOutput alloc] init];
                 [_output setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
                 [_session addOutput:_output];
                 
                 _output.metadataObjectTypes = [_output availableMetadataObjectTypes];
                 
                 _prevLayer = [AVCaptureVideoPreviewLayer layerWithSession:_session];
                 _prevLayer.frame = self.view.bounds;
                 _prevLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
                 
                 [self.view.layer addSublayer:_prevLayer];
                 [self.view bringSubviewToFront:_closeScannerOutlt];
                 
                 [_session startRunning];
             });
         } else {
             NSLog(@"Not granted access to %@", AVMediaTypeVideo);
         }
     }];
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection
{
    AVMetadataMachineReadableCodeObject *barCodeObject;
    NSString *detectionString = nil;
    
    for (AVMetadataObject *metadata in metadataObjects)
    {
        if ([metadata.type isEqualToString:AVMetadataObjectTypeQRCode])
        {
            barCodeObject = (AVMetadataMachineReadableCodeObject *)[_prevLayer transformedMetadataObjectForMetadataObject:(AVMetadataMachineReadableCodeObject *)metadata];
            detectionString = [(AVMetadataMachineReadableCodeObject *)metadata stringValue];
        }
        
        NSURL *url = [NSURL URLWithString:detectionString];
        
        if (detectionString != nil)
        {
            if([url.absoluteString containsString:kPingIDSDK])
            {
                self.qrString = detectionString;
                NSLog(@"QR String: %@",detectionString);
                _prevLayer.connection.enabled = NO;
                [_session stopRunning];
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    [self closeScannerBtn:nil];
                });
                break;
            }
            else
            {
                self.invalidQrView.hidden = NO;
                [self.view bringSubviewToFront:self.invalidQrView];
                [_session stopRunning];
                 [UIView animateWithDuration:0.4 animations:^() {self.invalidQrView.alpha = 1;}completion:^(BOOL finished)
                {
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        [UIView animateWithDuration:0.4 animations:^() {self.invalidQrView.alpha = 0;}completion:^(BOOL finished)
                        {
                            self.invalidQrView.hidden = YES;
                            [self startCameraSeassion];
                            
                        }];
                    });
                }];
                NSLog(@"PingID SDK Token wasn't found");
            }
        }
    }
}

-(void)stopSession
{
    [_session stopRunning];
    [_prevLayer removeFromSuperlayer];
    _prevLayer = nil;
    _session = nil;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)closeScannerBtn:(UIButton *)sender
{
    [self dismissViewControllerAnimated:YES completion:^{
        [self stopSession];
        if(self.qrString && ![self.qrString isEqualToString:@""])
        {
            self.callback(self.qrString);
        }
    }];
}

@end
