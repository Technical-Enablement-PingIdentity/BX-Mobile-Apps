Pod::Spec.new do |s|
  s.name              = "ShoLib"
  s.version           = "1.0.36"
  s.summary           = "ShoCard Library for Swift."
  s.license           = " SHOCARD CONFIDENTIAL
 __________________
 (C) COPYRIGHT 2017 ShoCard, Inc. All Rights Reserved.
 NOTICE: All information contained herein is the property of ShoCard, Inc.
 The intellectual and technical concepts contained herein are proprietary to
 ShoCard, Inc., and may be covered by U.S. and Foreign Patents, patents
 in process, and are protected by trade secret or copyright law.
 Dissemination or reproduction of this material is strictly forbidden unless
 prior written permission is obtained from ShoCard, Inc."
  s.description       = "ShoCard Library for Swift."
  s.homepage          = "https://gitlab.corp.pingidentity.com/shocard/sholib_swift.git"
  s.author            = { "ShoCard Build" => "build@shocard.com" }
  s.social_media_url  = "http://twitter.com/getShoCard"
  s.platform          = :ios, "9.0"
  s.source            = { :git => "https://gitlab.corp.pingidentity.com/shocard/sholib_swift.git" }
  s.source_files      = 'ShoLib/*.{swift}','ShoLib/Generated/*.{swift}','ShoLib/customviews/**/*.{swift}','ShoLib/utils/**/*.{swift}','ShoLib/livefaceverification/**/*.{swift}','ShoLib/idcapture/**/*.{swift}'
  s.resources = "ShoLib/**/*.{xib,storyboard,xcassets,strings}"
  s.requires_arc      = true
end
