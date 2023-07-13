//
//  HomeViewController.swift
//  SampleApp
//
//  Created by Ping Identity on 10/10/19.
//  Copyright © 2019 Ping Identity. All rights reserved.
//

import UIKit
import PingOneSDK
class HomeViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {

    @IBOutlet weak var actionTableView: UITableView!
    var actionsArray: Array<ActionItem>!
    
    @IBOutlet weak var versionOutlt: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.navigationController?.setNavigationBarHidden(true, animated: false)
        
        //let pairAction = ActionItem.init(actionName: PairingMethodName.Manual, segueID: SegueName.Manual, actionType: .segue)
        let oidcAction = ActionItem.init(actionName: PairingMethodName.OIDC, segueID: SegueName.OIDC, actionType: .segue)
        //let logsAction = ActionItem.init(actionName: SDKFunctionality.SendLogs, segueID: nil, actionType: .sendLogs)
        //actionsArray = [pairAction,oidcAction,logsAction]
        actionsArray = [oidcAction]
        if let version = getAppVersionAndBuild() {
            versionOutlt.text = version
        }
    }

    func getAppVersionAndBuild() -> String?{
        if let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
            if let appBuild = Bundle.main.infoDictionary?["CFBundleVersion"] as? String {
                return "v\(appVersion)(\(appBuild))"
            }
        }
        return nil
    }
    
    @IBAction func onGetStartedClicked(_ sender: UIButton) {
        guard let item = self.actionsArray.first,
              let segueId = item.segueID else {
            return
        }
        
        self.performSegue(withIdentifier: segueId, sender: nil)
    }
    
    //Table View
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    
        return actionsArray.count
    }
    

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

    var cell : UITableViewCell? = tableView.dequeueReusableCell(withIdentifier: "cell")
        if cell == nil {
            cell = UITableViewCell(style: UITableViewCell.CellStyle.value1, reuseIdentifier: "cell")
            
        }
        if self.actionsArray.count > 0 {
            cell?.textLabel!.text = self.actionsArray[indexPath.row].actionName
        }
        cell?.textLabel?.numberOfLines = 0

        return cell!
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {

        return 100.0
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath){
        let selectedActionItem = actionsArray[indexPath.row]
        switch selectedActionItem.type {
        case .segue:
            if let segueID = selectedActionItem.segueID {
              self.performSegue(withIdentifier: segueID, sender: nil)
            }
        case .sendLogs:
            PingOne.sendLogs { (supportId, error) in
                if let supportId = supportId{
                    Alert.generic(viewController: self, message:"Support ID: \(supportId)", error: nil)
                    print("Support ID:\(supportId)")
                }
                else if let error = error{
                    print(error.localizedDescription)
                }
            }
        }
    }
}
