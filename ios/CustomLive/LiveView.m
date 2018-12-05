 //
//  LiveView.m
//  rrsapp
//
//  Created by 陆卫新 on 2018/9/12.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#import "LiveView.h"
#import "NSObject+KJSerializer.h"

@interface LiveView()
@property(nonatomic, assign) BOOL myModel;
@end

@implementation LiveView

- (instancetype) init {
  if (self = [super init]) {
    NSLog(@"init");
  }
  return self;
}

- (void) dealloc {
  [[ILiveRoomManager getInstance] quitRoom:^{
    NSLog(@"退出房间成功");
  } failed:^(NSString *module, int errId, NSString *errMsg) {
    NSLog(@"退出房间失败 %d : %@", errId, errMsg);
  }];
}

- (void)setRoomId:(int)roomId {
  if (!roomId) {
    return;
  }
  _roomId = roomId;
  //
  //  // 该参数代表进房之后使用什么规格音视频参数，参数具体值为客户在腾讯云实时音视频控制台画面设定中配置的角色名（例如：默认角色名为user, 可设置controlRole = @"user"）
  ILiveRoomOption *option = [ILiveRoomOption defaultHostLiveOption];
  // 配置进房票据
  //  option.avOption.privateMapKey = @"privateMapKey";
  option.imOption.imSupport = YES;
  // 自动打开摄像头
  option.avOption.autoCamera = !self.noVideo;
  // 不自动打开mic
  option.avOption.autoMic = YES;
  // 设置房间内音视频监听
  option.memberStatusListener = self;
  // 设置房间中断事件监听
  option.roomDisconnectListener = self;

  option.controlRole = @"user";
  //  // 3. 调用加入房间接口，传入房间ID和房间配置对象
  [[ILiveRoomManager getInstance] joinRoom:_roomId option:option succ:^{
    // 加入房间成功，跳转到房间页
    NSLog(@"加入成功");
    [[[ILiveSDK getInstance] getTIMManager] setUserStatusListener:self];
    [[[ILiveSDK getInstance] getTIMManager] setGroupEventListener:self];
    [[[ILiveSDK getInstance] getTIMManager] setGroupAssistantListener:self];
    //  TIMUserDefinedStatusListener
    [[[ILiveSDK getInstance] getTIMManager] setUserDefinedStatusListener:self];
    [[[ILiveSDK getInstance] getTIMManager] setMessageListener:self];
    if (self.onJoinSuccess) {
      self.onJoinSuccess(@{ @"status": @"success" });
    }
  } failed:^(NSString *module, int errId, NSString *errMsg) {
    // 加入房间失败
    NSLog(@"加入房间失败 errId:%d errMsg:%@",errId, errMsg);
    if (self.onJoinFailed) {
      self.onJoinFailed(@{ @"status": @"failed", @"module": module, @"errId": @(errId), @"errMsg": errMsg });
    }
  }];
}

- (void)setFrame:(CGRect)frame {
  [super setFrame:frame];
}

- (void)setNoVideo:(BOOL)noVideo {
  _noVideo = noVideo;
  [self onCameraNumChange];
}

- (void)setHasListener:(BOOL)hasListener {
  _hasListener = hasListener;
  if (!_hasListener) {
    [[[ILiveSDK getInstance] getTIMManager] setUserStatusListener:nil];
    [[[ILiveSDK getInstance] getTIMManager] setGroupEventListener:nil];
    [[[ILiveSDK getInstance] getTIMManager] setGroupAssistantListener:nil];
    //  TIMUserDefinedStatusListener
    [[[ILiveSDK getInstance] getTIMManager] setUserDefinedStatusListener:nil];
    
    [[[ILiveSDK getInstance] getTIMManager] removeMessageListener:self];
  }
}

- (void)viewPress {
  self.myModel = !self.myModel;
  [self onCameraNumChange];
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

#pragma mark - Custom Action
// 房间内上麦用户数量变化时调用，重新布局所有渲染视图，这里简单处理，从上到下等分布局
- (void)onCameraNumChange {
  // 获取当前所有渲染视图
  NSArray *allRenderViews = [[[ILiveRoomManager getInstance] getFrameDispatcher] getAllRenderViews];
  
  // 检测异常情况
  if (allRenderViews.count == 0) {
    return;
  }
  
  
  // 计算并设置每一个渲染视图的frame
  CGFloat viewWidth = self.bounds.size.width;
  CGFloat viewHeight = self.bounds.size.height;
  float scale = 0.25;
  CGRect frame1 = _noVideo ? CGRectZero : CGRectMake(16, 16, viewWidth * scale, viewHeight * scale);
  CGRect frame2 = _noVideo ? CGRectZero : CGRectMake(0, 0, viewWidth, viewHeight);
  
  if (allRenderViews.count > 1) {
    UIButton *viewBtn = [self viewWithTag:10001];
    if (!viewBtn) {
      viewBtn = [[UIButton alloc] initWithFrame:frame1];
      [viewBtn addTarget:self action:@selector(viewPress) forControlEvents:UIControlEventTouchDown];
      viewBtn.tag = 10001;
      [self addSubview:viewBtn];
      [self sendSubviewToBack:viewBtn];
    }
    
    UIView *myView = allRenderViews[1];
    UIView *otherView = allRenderViews[0];
    otherView.userInteractionEnabled = NO;
    myView.userInteractionEnabled = NO;
    if (self.myModel) {
      myView.frame = frame1;
      otherView.frame = frame2;
      [self sendSubviewToBack:myView];
      [self sendSubviewToBack:otherView];
    } else {
      otherView.frame = frame1;
      myView.frame = frame2;
      [self sendSubviewToBack:otherView];
      [self sendSubviewToBack:myView];
    }
    
  } else if (allRenderViews.count == 1) {
//    ((UIView *)allRenderViews[0]).frame = CGRectZero;
    ((UIView *)allRenderViews[0]).frame = frame2;
    ((UIView *)allRenderViews[0]).userInteractionEnabled = NO;
    [self sendSubviewToBack:allRenderViews[0]];
  }
}

#pragma mark - ILiveMemStatusListener
// 音视频事件回调
- (BOOL)onEndpointsUpdateInfo:(QAVUpdateEvent)event updateList:(NSArray *)endpoints {
//  TIMConversation * c2c_conversation = [[TIMManager sharedInstance] getConversation:TIM_GROUP receiver:@"iOS-001"];
  if (endpoints.count <= 0) {
    return NO;
  }
  if (self.onStatusChange) {
    self.onStatusChange(@{ @"status": @"onEndpointsUpdateInfo", @"params": @{@"eventType": @(event), @"endpoints": endpoints} });
  }
  
  for (QAVEndpoint *endpoint in endpoints) {
    switch (event) {
      case QAV_EVENT_ID_ENDPOINT_HAS_CAMERA_VIDEO:
      {
        /*
         创建并添加渲染视图，传入userID和渲染画面类型，这里传入 QAVVIDEO_SRC_TYPE_CAMERA（摄像头画面）,
         */
        ILiveFrameDispatcher *frameDispatcher = [[ILiveRoomManager getInstance] getFrameDispatcher];
        ILiveRenderView *renderView = [frameDispatcher addRenderAt:CGRectZero forIdentifier:endpoint.identifier srcType:QAVVIDEO_SRC_TYPE_CAMERA];
        [self addSubview:renderView];
        [self sendSubviewToBack:renderView];
        // 房间内上麦用户数量变化，重新布局渲染视图
        self.myModel = false;
        [self onCameraNumChange];
      }
        break;
      case QAV_EVENT_ID_ENDPOINT_NO_CAMERA_VIDEO:
      {
        // 移除渲染视图
        ILiveFrameDispatcher *frameDispatcher = [[ILiveRoomManager getInstance] getFrameDispatcher];
        ILiveRenderView *renderView = [frameDispatcher removeRenderViewFor:endpoint.identifier srcType:QAVVIDEO_SRC_TYPE_CAMERA];
        [renderView removeFromSuperview];
        // 房间内上麦用户数量变化，重新布局渲染视图
        [self onCameraNumChange];
      }
        break;
      default:
        break;
    }
  }
  return YES;
}

#pragma mark - ILiveRoomDisconnectListener
/**
 SDK主动退出房间提示。该回调方法表示SDK内部主动退出了房间。SDK内部会因为30s心跳包超时等原因主动退出房间，APP需要监听此退出房间事件并对该事件进行相应处理
 
 @param reason 退出房间的原因，具体值见返回码
 
 @return YES 执行成功
 */
- (BOOL)onRoomDisconnect:(int)reason {
  NSLog(@"房间异常退出：%d", reason);
  if (self.onStatusChange) {
    self.onStatusChange(@{ @"status": @"disconnect", @"reason": @(reason) });
  }
  return YES;
}

#pragma mark - IMUserStatusListener
- (void)onForceOffline {
  if (self.onStatusChange) {
    self.onStatusChange(@{ @"status": @"onForceOffline" });
  }
}

#pragma mark - TIMGroupEventListener
- (void)onMemberUpdate:(NSString*)group tipType:(TIMGroupTipsType)tipType members:(NSArray*)members {
  if (self.onStatusChange) {
    self.onStatusChange(@{
                          @"status": @"onMemberUpdate",
                          @"params": @{
                              @"tipType": @(tipType),
                              @"members": members,
                              @"membersStr": [NSString stringWithFormat: @"%@", members]
                              }
                          });
  }
}

#pragma TIMGroupAssistantListener
-(void) OnMemberUpdate:(NSString*)groupId membersInfo:(NSArray*)membersInfo {
  NSLog(@"OnMemberUpdate");
}

-(void) OnGroupUpdate:(TIMGroupInfo*)groupInfo {
  NSLog(@"OnGroupUpdate");
}

#pragma TIMUserDefinedStatusListener
- (void)onStatusChanged:(TIMUserDefinedStatus*)status {
  NSLog(@"onStatusChanged");
}

- (void)onNewMessage:(NSArray*) msgs {
  NSLog(@"onNewMessage");
  NSMutableArray * msgsArr = [NSMutableArray arrayWithCapacity:1];
  for (TIMMessage * msg in msgs) {
//    TIMConversation * conversation = [msg getConversation];
    for (int i = 0; i < [msg elemCount]; i++) {
      TIMElem * elem = [msg getElem:i];
      NSDictionary *dic = [elem getDictionary];
      [msgsArr addObject:dic];
    }
  }
  if (self.onNewMessage) {
    self.onNewMessage(@{ @"msgsArr": msgsArr});
  }
}

@end
