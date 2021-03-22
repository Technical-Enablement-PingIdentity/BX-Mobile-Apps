//
//  AppDelegate.swift
//  SampleApp
//
//  Created by Ping Identity on 3/12/19.
//  Copyright © 2019 Ping Identity. All rights reserved.
//

import UIKit
import UserNotifications
import PingOne
import AppAuth
import CryptoTools // added from Verify

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    var window: UIWindow?
    
    // added from Verify
    var pnToken: Data?
        var notificationUserInfo: [AnyHashable: Any]? {
            didSet {
                NotificationCenter.default.post(name: NSNotification.Name(StorageManager.REMOTE_PUSH_RECEIVED_NOTIFICATION_CENTER_KEY), object: nil, userInfo: self.notificationUserInfo)
            }
        }
    
    var currentAuthorizationFlow: OIDExternalUserAgentSession?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            window!.overrideUserInterfaceStyle = .light
        }
        self.registerRemoteNotifications()
        
        // added from Verify
        self.notificationUserInfo = launchOptions?[.remoteNotification] as? [AnyHashable: Any]


        return true
    }
    
    func registerRemoteNotifications()
    {
        print("Registering remote notifications")
        
        let center  = UNUserNotificationCenter.current()
        center.delegate = self
        center.requestAuthorization(options: [.sound, .alert, .badge]) { (granted, error) in
            if error == nil
            {
                // Registering UNNotificationCategories more than once results in previous categories being overwritten. PingOne provides the needed categories. The developer may add categories.
                UNUserNotificationCenter.current().setNotificationCategories(PingOne.getUNNotificationCategories())
                DispatchQueue.main.async {
                    UIApplication.shared.registerForRemoteNotifications()
                }
            }
        }
    }
    
    func application(_ application: UIApplication,
                     didFailToRegisterForRemoteNotificationsWithError error: Error)
    {
        print(error.localizedDescription)
    }
    
    
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        
        // added from Verify
        print("didRegisterForRemoteNotificationsWithDeviceToken: \(deviceToken.hexDescription)")
                self.pnToken = deviceToken
        
        let deviceTokenString = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        print("Device Token: \(deviceTokenString)")

        var deviceTokenType : PingOne.APNSDeviceTokenType = .production
        #if DEBUG
        deviceTokenType = .sandbox
        #endif
        
        PingOne.setDeviceToken(deviceToken, type: deviceTokenType) { (error) in
            if let error = error{
                print(error.localizedDescription)
            }
        }
    }

    
    

    // added from Verify
            public class func registerForAPNS() {
                DispatchQueue.main.async {
                    let center = UNUserNotificationCenter.current()
                    center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
                        if let error = error {
                            print("Error: User denied permission for push notifications. \(error.localizedDescription)")
                            
                            let alert = UIAlertController(title: "Notifications Disabled".localized, message: "You must enable notifications for your application to be able to submit data for verification.", preferredStyle: .alert)
                            alert.addAction(UIAlertAction(title: "Cancel".localized, style: UIAlertAction.Style.cancel, handler: nil))
                            alert.addAction(UIAlertAction(title: "Go to Settings".localized, style: UIAlertAction.Style.default, handler: { (_) in
                                if let settingsUrl = URL(string: UIApplication.openSettingsURLString) {
                                    UIApplication.shared.open(settingsUrl, options: [:], completionHandler: nil)
                                }
                            }))
                            alert.show()
                        }
                        DispatchQueue.main.async {
                            UIApplication.shared.registerForRemoteNotifications()
                        }
                    }
                    
                }
            }
    
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void)
    {
        print("didReceive")
        
        PingOne.processRemoteNotificationAction(response.actionIdentifier, authenticationMethod: "user", forRemoteNotification: response.notification.request.content.userInfo) { (notificationObject, error) in
            
            if let error = error{
                print("Error: \(String(describing: error))")
                if error.code == ErrorCode.unrecognizedRemoteNotification.rawValue{
                    //Do something else with remote notification.
                }
            }
            else if let notificationObject = notificationObject{ //User pressed the actual banner, instead of an action.
                self.displayNotificationViewAlert(notificationObject)
            }
            completionHandler()
        }
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void)
    {
        print("didReceiveRemoteNotification userinfo: \(userInfo)")
        
        PingOne.processRemoteNotification(userInfo) { (notificationObject, error) in
            if let error = error{
                print("Error: \(String(describing: error))")
                if error.code == ErrorCode.unrecognizedRemoteNotification.rawValue{
                    //Unrecognized remote notification.
                    completionHandler(UIBackgroundFetchResult.noData)
                    self.notificationUserInfo = userInfo

                }
            }
            else if let notificationObject = notificationObject{
                switch(notificationObject.notificationType){
                case .authentication:
                    self.displayNotificationViewAlert(notificationObject)
                    completionHandler(UIBackgroundFetchResult.newData)

                default:
                    print("Error: \(String(describing: error))")
                    completionHandler(UIBackgroundFetchResult.noData)
                }
            }
            else{
                completionHandler(UIBackgroundFetchResult.noData)
            }
        }
    }
    
    func displayNotificationViewAlert(_ notificationObject: NotificationObject){
        DispatchQueue.main.async {
            
            var displayOnVc: UIViewController
            guard let rootVc = self.window?.rootViewController else{
                return
            }
            
            if rootVc.presentedViewController != nil{
                displayOnVc = rootVc.presentedViewController!
            }
            else{
                displayOnVc = rootVc
            }
            
            Alert.approveDeny(viewController: displayOnVc, title: Local.Authenticate) { (approved) in
                if let approved = approved{
                    if(approved){
                        notificationObject.approve(withAuthenticationMethod: "user", completionHandler: { (error) in
                            if error != nil
                            {
                                Alert.generic(viewController: displayOnVc, message: nil, error: error)
                            }
                        })
                    }
                    else{
                        notificationObject.deny(completionHandler: { (error) in
                            if error != nil
                            {
                                Alert.generic(viewController: displayOnVc, message: nil, error: error)
                            }
                        })
                    }
                }
            }
        }
    }
    
    //OIDC
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {

        if let authorizationFlow = self.currentAuthorizationFlow, authorizationFlow.resumeExternalUserAgentFlow(with: url) {
            self.currentAuthorizationFlow = nil
            return true
        }

        return false
    }
    
    func applicationWillResignActive(_ application: UIApplication) {
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
    }

    func applicationWillTerminate(_ application: UIApplication) {
    }
}
