//
//  RNILiveTools.m
//  RNIlive
//
//  Created by 陆卫新 on 2018/10/9.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#import "RNILiveTools.h"
#import <React/RCTRootView.h>
#import <React/RCTLog.h>
#import <ILiveSDK/ILiveCoreHeader.h>

#define kUserAgentKey @"UserAgent"

@implementation RNILiveTools

-(instancetype)init{
    if (self) {
    }
    return self;
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(initILiveSDK: (int)appid) {
    [[ILiveSDK getInstance] initSdk:appid];
}

RCT_EXPORT_METHOD(initILiveSDKWithType: (int)appid accountType: (int)accountType) {
    [[ILiveSDK getInstance] initSdk:appid accountType: accountType];
}

RCT_EXPORT_METHOD(loginLive:(NSString *)uid sig:(NSString *)sig succ:(RCTResponseSenderBlock)succ failed:(RCTResponseSenderBlock)failed) {
    [[ILiveLoginManager getInstance] iLiveLogin:uid sig:sig succ:^{
        NSLog(@"登录成功！");
        if (succ) {
            succ(@[[NSNull null]]);
        }
    } failed:^(NSString *module, int errId, NSString *errMsg) {
        NSLog(@"errId:%d, errMsg:%@",errId, errMsg);
        if (failed) {
            failed(@[[NSNull null], module, [NSString stringWithFormat:@"%d", errId], errMsg]);
        }
    }];
}

RCT_EXPORT_METHOD(quitLiveRoom:(RCTResponseSenderBlock)sucess failed:(RCTResponseSenderBlock)failed) {
    [[ILiveRoomManager getInstance] quitRoom:^{
        NSLog(@"退出房间成功");
        sucess(@[[NSNull null]]);
    } failed:^(NSString *module, int errId, NSString *errMsg) {
        NSLog(@"退出房间失败 %d : %@", errId, errMsg);
        failed(@[[NSNull null], module, [NSString stringWithFormat:@"%d", errId], errMsg]);
    }];
}

// cameraType: 0前置， 1后置
RCT_EXPORT_METHOD(enableCamera:(int)cameraType enabled:(BOOL)enabled success:(RCTResponseSenderBlock)success failed:(RCTResponseSenderBlock)failed) {
    [[ILiveRoomManager getInstance] enableCamera:cameraType enable:enabled succ:^{
        NSLog(@"打开摄像头成功");
        success(@[[NSNull null]]);
    } failed:^(NSString *module, int errId, NSString *errMsg) {
        NSLog(@"打开摄像头失败");
        failed(@[[NSNull null]]);
    }];
}

RCT_EXPORT_METHOD(enableMic:(BOOL)enabled success:(RCTResponseSenderBlock)success failed:(RCTResponseSenderBlock)failed) {
    [[ILiveRoomManager getInstance] enableMic:enabled succ:^{
        NSLog(@"打开麦克风成功");
        success(@[[NSNull null]]);
    } failed:^(NSString *module, int errId, NSString *errMsg) {
        NSLog(@"打开麦克风失败");
        failed(@[[NSNull null]]);
    }];
}

RCT_EXPORT_METHOD(changeToVoice:(RCTResponseSenderBlock)success failed:(RCTResponseSenderBlock)failed) {
    [[ILiveRoomManager getInstance] cancelAllView:^{
        NSLog(@"取消成功");
        success(@[[NSNull null]]);
    } failed:^(NSString *module, int errId, NSString *errMsg) {
        NSLog(@"取消失败");
        failed(@[[NSNull null]]);
    }];
}
RCT_EXPORT_METHOD(enableSpeaker:(BOOL)enable success:(RCTResponseSenderBlock)success failed:(RCTResponseSenderBlock)failed) {
    [[ILiveRoomManager getInstance] enableSpeaker:enable succ:^{
        NSLog(@"取消成功");
        success(@[[NSNull null]]);
    } failed:^(NSString *module, int errId, NSString *errMsg) {
        NSLog(@"取消失败");
        failed(@[[NSNull null]]);
    }];
}

RCT_EXPORT_METHOD(setAudioMode:(int)mode) {
    [[ILiveRoomManager getInstance] setAudioMode:mode];
}
//
// override init need use main queue
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

@end
