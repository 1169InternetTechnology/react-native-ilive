//
//  LiveView.h
//  rrsapp
//
//  Created by 陆卫新 on 2018/9/12.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <ILiveSDK/ILiveCoreHeader.h>
#import <React/RCTComponent.h>
#import <ILiveSDK/ILiveSDK.h>
#import <ImSDK/ImSDK.h>

@interface LiveView : UIView <ILiveMemStatusListener, ILiveRoomDisconnectListener, TIMUserStatusListener, TIMGroupEventListener, TIMGroupAssistantListener, TIMUserDefinedStatusListener, TIMMessageListener>

@property (nonatomic, assign) int roomId;
@property (nonatomic, copy) RCTBubblingEventBlock onStatusChange;
@property (nonatomic, copy) RCTBubblingEventBlock onJoinSuccess;
@property (nonatomic, copy) RCTBubblingEventBlock onJoinFailed;
@property (nonatomic, copy) RCTBubblingEventBlock onNewMessage;
@property (nonatomic, assign) float width;
@property (nonatomic, assign) BOOL noVideo;
@property (nonatomic, assign) BOOL hasListener;
@end
