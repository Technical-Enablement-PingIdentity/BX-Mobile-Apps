//
//  String+utilities.swift
//  BXFinance
//
//  Created by Eric Anderson on 5/3/23.
//  Copyright © 2023 Ping Identity. All rights reserved.
//

import Foundation

extension String {
    func capitalizedCase() -> String {
        if (self == "") {
            return self
        }
        
        return prefix(1).uppercased() + self.lowercased().dropFirst()
    }
}
