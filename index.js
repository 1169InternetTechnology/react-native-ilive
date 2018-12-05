import React from 'react';
import { requireNativeComponent, NativeModules } from 'react-native';

const {
  RNILiveTools: {
    initILiveSDK,
    initILiveSDKWithType,
    loginLive,
    enableCamera,
    enableMic,
    changeToVoice,
    enableSpeaker,
    setAudioMode,
    quitLiveRoom,
  }
} = NativeModules;
// requireNativeComponent 自动把'RNTMap'解析为'RNTMapManager'
class ILvieView extends React.Component {
  render() {
    return <RNTLiveView {...this.props} />;
  }
}

const RNTLiveView = requireNativeComponent('RNTLiveView', ILvieView);

export {
  initILiveSDK,
  initILiveSDKWithType,
  loginLive,
  enableCamera,
  enableMic,
  changeToVoice,
  enableSpeaker,
  setAudioMode,
  quitLiveRoom,
};
export default ILvieView;
