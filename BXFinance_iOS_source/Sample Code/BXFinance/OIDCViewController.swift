//
//  OIDCViewController.swift
//  SampleApp
//
//  Created by Ping Identity on 10/10/19.
//  Copyright © 2019 Ping Identity. All rights reserved.
//

import UIKit
import AppAuth
import PingOneSDK
import PingOneVerify_iOS

class OIDCViewController: UIViewController {

    @IBOutlet weak var buttonsImageView: UIImageView!
    @IBOutlet weak var verifyButton: UIButton!
    
    var verifiedName: String? = nil
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.setNavigationBarHidden(true, animated: false)
        self.buttonsImageView.layer.cornerRadius = 20
        self.buttonsImageView.contentMode = .scaleAspectFit
        self.buttonsImageView.image = createButtonsImage() ?? UIImage(named: "menu-holder")
        self.verifyButton.isHidden = !(Bundle.main.infoDictionary?["isVerifyEnabled"] as? Bool ?? true)
    }
    
    func createButtonsImage() -> UIImage? {
        let frame = CGRect(x: 0, y: 0, width: 422, height: 520)
        let buttonsView = HomeButtonsView(frame: frame)
        buttonsView.layer.cornerRadius = 10
        UIGraphicsBeginImageContextWithOptions(buttonsView.bounds.size, false, 0.0)
        guard let context = UIGraphicsGetCurrentContext() else {
            return nil
        }
        buttonsView.layer.render(in: context)
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return image
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        guard let id = segue.identifier, id == "verify-segue" else {
            return
        }
        self.navigationController?.setNavigationBarHidden(false, animated: false)
    }
    
    @IBAction func pairDevice(_ sender: UIButton) {
        
        guard let issuer = URL(string: OIDC.Issuer) else {
            print("Error creating URL for : \(OIDC.Issuer)")
            return
        }
        OIDAuthorizationService.discoverConfiguration(forIssuer: issuer) { configuration, error in

            guard let config = configuration else {
                print("Error retrieving discovery document: \(error?.localizedDescription ?? "DEFAULT_ERROR")")
                return
            }

            print("Got configuration: \(config)")
            self.doAuthWithAutoCodeExchange(configuration: config, clientID: OIDC.ClientID, clientSecret: nil)
        }
    }
    
    @IBAction func LaunchVerify(_ sender: UIButton) {
        PingOneVerifyClient.Builder()
            .setListener(self)
            .setRootViewController(self)
            .startVerification { pingOneVerifyClient, clientBuilderError in
                if let clientBuilderError = clientBuilderError {
                    logerror(clientBuilderError.localizedDescription ?? "")
                    let alertController = UIAlertController(title: "Client Builder Error", message: clientBuilderError.localizedDescription, preferredStyle: .alert)
                    alertController.addAction(UIAlertAction(title: "Okay", style: .default))
                    if let presentedViewController = self.presentedViewController {
                        presentedViewController.dismiss(animated: true) {
                            self.present(alertController, animated: true)
                        }
                    } else {
                        self.present(alertController, animated: true)
                    }
                } else {
                    //Client object was initialized successfully and the SDK will return the results in callback methods
                }
            }
    }
    
    func doAuthWithAutoCodeExchange(configuration: OIDServiceConfiguration, clientID: String, clientSecret: String?) {

        guard let redirectURI = URL(string: OIDC.RedirectURI) else {
            print("Error creating URL for : \(OIDC.RedirectURI)")
            return
        }

        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
            print("Error accessing AppDelegate")
            return
        }

        do {
            let payload = try PingOne.generateMobilePayload()
            
            // builds authentication request
            let request = OIDAuthorizationRequest(configuration: configuration,
                                                  clientId: clientID,
                                                  clientSecret: clientSecret,
                                                  scopes: [OIDScopeOpenID, OIDScopeProfile],
                                                  redirectURL: redirectURI,
                                                  responseType: OIDResponseTypeCode,
                                                  additionalParameters: [OIDCKey.MobilePayload: payload, OIDCKey.Prompt: OIDCKey.LoginPrompt])

            // performs authentication request
            print("Initiating authorization request with scope: \(request.scope ?? "DEFAULT_SCOPE")")

            appDelegate.currentAuthorizationFlow = OIDAuthState.authState(byPresenting: request, presenting: self) { authState, error in

                if let authState = authState {
                    print("Got authorization tokens. Access token: \(authState.lastTokenResponse?.accessToken ?? "DEFAULT_TOKEN")")
                    //Call PingOne SDK with the idToken
                    self.processIdToken(authState.lastTokenResponse?.idToken ?? "DEFAULT_TOKEN")
                } else {
                    print("Authorization error: \(error?.localizedDescription ?? "DEFAULT_ERROR")")
                }
            }
        } catch let error {
            print(error)
        }
    }

    func processIdToken(_ idToken: String){
        PingOne.processIdToken(idToken) { (pairingObject, error) in
            if let pairingObject = pairingObject{
                self.displayNotificationViewAlert(pairingObject)
            }
            else if let error = error{
                print(error.localizedDescription)
            }
        }
    }
    
    func displayNotificationViewAlert(_ pairingObject: PairingObject){
        Alert.approveDeny(viewController: self, title: Local.Pair) { (approved) in
            if let approved = approved{
                if(approved){
                    pairingObject.approve(completionHandler: { (error) in
                        Alert.generic(viewController: self, message:Local.DeviceIsPaired, error: error)
                    })
                }
            }
        }
    }
}

extension OIDCViewController: DocumentSubmissionListener {
    func onDocumentSubmitted(response: DocumentSubmissionResponse) {
        print("The document status is \(String(describing: response.documentStatus))")
        print("The document submission status is \(String(describing: response.documentSubmissionStatus))")
            
        guard let documents = response.document else { return }
        for (key, value) in documents {
            print("\(key): \(value)")
        }
        
        if let verifiedFirstName = documents["firstName"],
           let verifiedLastName = documents["lastName"] {
            self.verifiedName = "\(verifiedFirstName.capitalized) \(verifiedLastName.capitalized)"
        }
    }
    
    func onSubmissionComplete(status: DocumentSubmissionStatus) {
        // present a basic alert to indicate completion
        let alertController = UIAlertController(title: "Document Submission Complete", message: "All documents\(self.verifiedName != nil ? " for " + self.verifiedName! : "") have been submitted and verification has successfully completed.", preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: "Okay", style: .default))
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { // need to wait for verify to close
            self.present(alertController, animated: true, completion: nil)
        }
    }
    
    func onSubmissionError(error: DocumentSubmissionError) {
        print(error.localizedDescription ?? "Unknown submission error occurred")
    }
}
