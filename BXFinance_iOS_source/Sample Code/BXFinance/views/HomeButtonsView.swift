//
//  HomeButtonsView.swift
//  BXFinance
//
//  Created by Bhavya Chauhan on 12/8/21.
//  Copyright © 2021 Ping Identity. All rights reserved.
//

import Foundation
import UIKit

class HomeButtonsView: UIView {
    
    @IBOutlet weak var button1: UIButton!
    @IBOutlet weak var button2: UIButton!
    @IBOutlet weak var button3: UIButton!
    @IBOutlet weak var button4: UIButton!
    @IBOutlet weak var button5: UIButton!
    @IBOutlet weak var button6: UIButton!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        self.setupView()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setupView()
    }
    
    func setupView() {
        if let bundle = Bundle.main.loadNibNamed("HomeButtonsView", owner: self, options: nil){
            let subView = bundle[0] as! UIView
            subView.frame = self.bounds
            subView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
            self.addSubview(subView)
            updateButtonLabels()
        }
    }
    
    func updateButtonLabels() {
        guard self.button1 != nil,
              self.button2 != nil,
              self.button3 != nil,
              self.button4 != nil,
              self.button5 != nil,
              self.button6 != nil else {
            print("nil buttons")
            return
        }
        
        self.button1.setTitle("button1".localized(in: "HomeButtonLabels"), for: .normal)
        self.button2.setTitle("button2".localized(in: "HomeButtonLabels"), for: .normal)
        self.button3.setTitle("button3".localized(in: "HomeButtonLabels"), for: .normal)
        self.button4.setTitle("button4".localized(in: "HomeButtonLabels"), for: .normal)
        self.button5.setTitle("button5".localized(in: "HomeButtonLabels"), for: .normal)
        self.button6.setTitle("button6".localized(in: "HomeButtonLabels"), for: .normal)
    }
    
}
