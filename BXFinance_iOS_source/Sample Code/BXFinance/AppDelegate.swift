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


@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    var window: UIWindow?
    
    // added from Verify
    var pnToken: Data?
    
    var currentAuthorizationFlow: OIDExternalUserAgentSession?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        if #available(iOS 13.0, *) {
            // Always adopt a light interface style.
            window!.overrideUserInterfaceStyle = .light
        }
        self.registerRemoteNotifications()

        if #available(iOS 15.0, *) {
            let navigationBarAppearance = UINavigationBarAppearance()
            navigationBarAppearance.backgroundColor = UIColor(named: "lib_nav_bar_color") ?? UIColor(netHex: 0x456058)
            navigationBarAppearance.shadowColor = nil
            navigationBarAppearance.titleTextAttributes = [.foregroundColor: UIColor.white]
            UINavigationBar.appearance().standardAppearance = navigationBarAppearance
            UINavigationBar.appearance().compactAppearance = navigationBarAppearance
            UINavigationBar.appearance().scrollEdgeAppearance = navigationBarAppearance
        } else {
            UINavigationBar.appearance().barTintColor = UIColor(named: "lib_nav_bar_color") ?? UIColor(netHex: 0x456058)
        }

        return true
    }
    
    func registerRemoteNotifications() {
        print("Registering remote notifications")
        
        let center  = UNUserNotificationCenter.current()
        center.delegate = self
        center.requestAuthorization(options: [.sound, .alert, .badge]) { (granted, error) in
            // This needs to happen whether they granted permissions or not or the
            // PingOne pairing with the device will fail when they "Add Device"
            if error == nil {
                // Registering UNNotificationCategories more than once results in previous categories being overwritten. PingOne provides the needed categories. The developer may add categories.
                UNUserNotificationCenter.current().setNotificationCategories(PingOne.getUNNotificationCategories())
                DispatchQueue.main.async {
                    UIApplication.shared.registerForRemoteNotifications()
                }
            }
            
            if !granted {
                print("Error: User denied permission for push notifications.")
                
                let alert = UIAlertController(title: "Notifications Disabled".localized, message: "If you do not wish to enable notifications you will need to be in the BXFinance App on your phone before you attempt to use it for MFA.", preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "Cancel".localized, style: UIAlertAction.Style.cancel, handler: nil))
                alert.addAction(UIAlertAction(title: "Go to Settings".localized, style: UIAlertAction.Style.default, handler: { (_) in
                    if let settingsUrl = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(settingsUrl, options: [:], completionHandler: nil)
                    }
                }))
                DispatchQueue.main.async {
                    self.window?.rootViewController?.present(alert, animated: true, completion: nil)
                }
            }
        }
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print(error.localizedDescription)
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        
        print("didRegisterForRemoteNotificationsWithDeviceToken: \(deviceToken)")
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
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        print("didReceive")
        
        PingOne.processRemoteNotificationAction(response.actionIdentifier, authenticationMethod: "user", forRemoteNotification: response.notification.request.content.userInfo) { (notificationObject, error) in
            
            if let error = error {
                print("Error: \(String(describing: error))")
                if error.code == ErrorCode.unrecognizedRemoteNotification.rawValue {
                    //Do something else with remote notification.
                }
            } else if let notificationObject = notificationObject { //User pressed the actual banner, instead of an action.
                self.displayNotificationViewAlert(notificationObject)
            }
            DispatchQueue.main.async {
                completionHandler()
            }
        }
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        print("didReceiveRemoteNotification userinfo: \(userInfo)")
        
        PingOne.processRemoteNotification(userInfo) { (notificationObject, error) in
            if let error = error{
                print("Error: \(String(describing: error))")
                if error.code == ErrorCode.unrecognizedRemoteNotification.rawValue{
                    //Unrecognized remote notification.
                    completionHandler(UIBackgroundFetchResult.noData)
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

