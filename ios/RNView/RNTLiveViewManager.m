//
//  LiveViewManager.m
//  rrsapp
//
//  Created by 陆卫新 on 2018/9/17.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#import "RNTLiveViewManager.h"
#import "LiveView.h"

@interface RNTLiveViewManager()
@end

@implementation RNTLiveViewManager
RCT_EXPORT_MODULE()
RCT_EXPORT_VIEW_PROPERTY(roomId, int)
RCT_EXPORT_VIEW_PROPERTY(width, int)
RCT_EXPORT_VIEW_PROPERTY(noVideo, BOOL)
RCT_EXPORT_VIEW_PROPERTY(hasListener, BOOL)
RCT_EXPORT_VIEW_PROPERTY(onStatusChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onJoinFailed, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onJoinSuccess, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onNewMessage, RCTBubblingEventBlock)

- (LiveView *)view
{
  LiveView *view = [[LiveView alloc] init];
  return view;
}
@end
