[TOC] 
## 1.概述
提供云课堂基础SDK功能，包括推流，拉流等。为用户提供快速，简便的方法开展自己的实时互动课堂。

### 1.1 功能特性

| |  |  |
| --- | --- | --- |
| 功能特性 | 描述 | 备注 |
| 推流  |支持推流到服务器|		
| 拉流   |支持从服务器订阅流|
|获取流状态|支持获取流的状态(发报数、收报数、丢包数、延时)|
| 前后摄像头切换 | 支持手机前后摄像头切换 |  |
| 后台播放 | 支持直播退到后台只播放音频 |  |
| 支持https协议 | 支持接口https请求 |  |

### 1.2 阅读对象
本文档为技术文档，需要阅读者：
* 具备基本的Android开发能力
* 准备接入CC视频的基础SDK相关功能
* CC基础版本SDK，为了使用硬件媒体编解码器，建议API级别19以上。

## 2.开发准备

### 2.1 开发环境
* Android Studio : Android 开发IDE
* Android SDK : Android 官方SDK

### 2.2 混淆配置
ccclassroom-base.jar已经混淆过，如果需要对应用进行混淆，需要在混淆的配置文件增加如下代码，以防止SDK的二次混淆：
```
-keep public class com.bokecc.sskt.base.**{*;}
-keep public interface com.bokecc.sskt.base.**{*;}
-keep public class com.intel.webrtc.base.**{*;}
-keep public interface com.intel.webrtc.base.**{*;}
-keep public class com.intel.webrtc.conference.**{*;}
-keep public interface com.intel.webrtc.conference.**{*;}
-keep public class org.webrtc.**{*;}
-keep public interface org.webrtc.**{*;}
```

## 3.快速集成

注：快速集成主要提供的是推流和拉流的功能(核心功能)。

首先，需要下载最新版本的SDK，下载地址为：

### 3.1 导入jar
|            名称                            | 描述      |
| :--------------------------------------- | :------- | 
| ccclassroom-base.jar	 | CC基础SDK版本核心jar包	 | 

### 3.2 导入so
|            名称                            | 描述      |
| :--------------------------------------- | :------- | 
|libjingle_peerconnection.so|CC连麦依赖native库|

### 3.3 配置依赖库

修改 build.gradle，打开您的工程目录下的 build.gradle，确保已经添加了如下依赖：
```gradle
compile('io.socket:socket.io-client:0.8.3') {
        exclude group: 'org.json', module: 'json'
    }
compile 'com.squareup.okhttp3:okhttp:3.8.1'
compile files('libs/ccclassroom-base.jar')

```
### 3.4初始化渲染器以及布局控件
预览展示控件和订阅展示控件：
```java
<android.support.constraint.ConstraintLayout
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context="ccsskt.bokecc.base.example.MainActivity">

        <LinearLayout
            android:id="@+id/id_local_container"
            android:layout_width="0dp"
            android:layout_height="270dp"
            android:orientation="vertical"
            android:gravity="center"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintVertical_weight="1"/>

        <LinearLayout
            android:id="@+id/id_remote_mix_container"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:orientation="vertical"
            android:gravity="center"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toBottomOf="@id/id_local_container"
            app:layout_constraintVertical_weight="1"/>
<android.support.constraint.ConstraintLayout
```
初始化渲染器：
```java
 private void initRenderer() {
        mLocalRenderer = new CCSurfaceRenderer(this);
        mLocalRenderer.init(mAtlasClient.getEglBase().getEglBaseContext(), null);
        mLocalRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mLocalContainer.addView(mLocalRenderer);

        mRemoteMixRenderer = new CCSurfaceRenderer(this);
        mRemoteMixRenderer.init(mAtlasClient.getEglBase().getEglBaseContext(), null);
        mRemoteMixRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mRemoteMixContainer.addView(mRemoteMixRenderer);
    }
```
### 3.5 创建SDK实例

创建SDK实例：

```java
 mAtlasClient = new CCAtlasClient(this, Config.PUBLIC_KEY);
 mAtlasClient.addAtlasObserver(mClientObserver);
```
系统代理回调：

```java
  private CCAtlasClient.AtlasClientObserver mClientObserver = new CCAtlasClient.AtlasClientObserver() {
        @Override
        public void onServerDisconnected() {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        @Override
        public void onStreamAdded(CCStream stream) {
            if (stream.isRemoteIsLocal()) { // 不订阅自己的本地流
                return;
            }
            Log.e(TAG, "onStreamAdded: [ " + stream.getStreamId() + " ]");
            if (stream.getStreamType() == CCStream.REMOTE_MIX) {
                // 订阅
                mStream = stream;
            }
        }

        @Override
        public void onStreamRemoved(CCStream stream) {
            Log.e(TAG, "onStreamRemoved: [ " + stream.getStreamId() + " ]");
            if (stream.getStreamType() == CCStream.REMOTE_MIX) {
                mStream = null;
                mAtlasClient.unsubcribe(stream, null);
            }
        }

        @Override
        public void onStreamError(String streamid, String errorMsg) {

        }
    };
```

创建本地流：

```java
  private void createLocalStream() {
        LocalStreamConfig config = new LocalStreamConfig.LocalStreamConfigBuilder().build();
        isFront = config.cameraType == LocalStreamConfig.CAMERA_FRONT;
        try {
            mLocalStream = mAtlasClient.createLocalStream(config);
        } catch (StreamException e) {
            showToast(e.getMessage());
        }
    }
```

### 3.6 加入直播间和直播间开始结束的接口

加入直播间的接口：
```java
mAtlasClient.join(sessionid, userAccount, new CCAtlasCallBack<CCBaseBean>() {
            @Override
            public void onSuccess(CCBaseBean ccBaseBean) {
                dismissProgress();
                showToast("join room success");
            }

            @Override
            public void onFailure(int errCode, String errMsg) {
                dismissProgress();
                showToast(errMsg);
            }
        });
```
开始直播：

```java
@OnClick(R.id.id_start)
    void start() {
        mAtlasClient.startLive(new CCAtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("start live success");
            }

            @Override
            public void onFailure(int errCode, String errMsg) {
                showToast("start live failed [ " + errMsg + " ]");
            }
        });
    }
```
结束直播：

```java
@OnClick(R.id.id_stop)
    void stop() {
        mAtlasClient.stopLive(new CCAtlasCallBack<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("stop live success");
            }

            @Override
            public void onFailure(int errCode, String errMsg) {
                showToast("stop live failed [ " + errMsg + " ]");
            }
        });
```
### 3.7 推流调用接口

下面是推流端代码：
```java
 mAtlasClient.publish(new CCAtlasCallBack<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    isPublish = true;
                    dismissProgress();
                    showToast("publish success");
                    mPublishBtn.setSelected(!mPublishBtn.isSelected());
                    mPublishBtn.setText(mPublishBtn.isSelected() ? "停止发布" : "发布");
                }

                @Override
                public void onFailure(int errCode, String errMsg) {
                    dismissProgress();
                    showToast("publish failed [ " + errMsg + " ]");
                }
            });
```
### 3.8 取消推流接口调用

下面取消推流接口调用代码：
```java
    mAtlasClient.unpublish(new CCAtlasCallBack<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    isPublish = false;
                    dismissProgress();
                    showToast("unpublish success");
                    mPublishBtn.setSelected(!mPublishBtn.isSelected());
                    mPublishBtn.setText(mPublishBtn.isSelected() ? "停止发布" : "发布");
                }

                @Override
                public void onFailure(int errCode, String errMsg) {
                    dismissProgress();
                    showToast("unpublish failed [ " + errMsg + " ]");
                }
            });
```

### 3.9 订阅流接口调用

下面代码是订阅流接口：
```java
 mAtlasClient.subscribe(mStream, new CCAtlasCallBack<CCStream>() {
             @Override
            public void onSuccess(CCStream stream) {
               dismissProgress();
               showToast("subscribe success");
               mSubscribeBtn.setSelected(!mSubscribeBtn.isSelected());
                        mSubscribeBtn.setText(mSubscribeBtn.isSelected() ? "取消订阅" : "订阅");
                        try {
                            mStream.attach(mRemoteMixRenderer);
                        } catch (StreamException ignored) {
                        }
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        dismissProgress();
                        showToast("subscribe failed [ " + errMsg + " ]");
                    }
                });
```

### 3.10 取消订阅流接口

下面是取消订阅流接口代码：
```java
mAtlasClient.unsubcribe(mStream, new CCAtlasCallBack<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgress();
                        showToast("unsubscribe success");
                        mSubscribeBtn.setSelected(!mSubscribeBtn.isSelected());
                        mSubscribeBtn.setText(mSubscribeBtn.isSelected() ? "取消订阅" : "订阅");
                        try {
                            mStream.detach(mRemoteMixRenderer);
                        } catch (StreamException ignored) {
                        } finally {
                            mRemoteMixRenderer.cleanFrame();
                        }
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        dismissProgress();
                        showToast("unsubscribe failed [ " + errMsg + " ]");
                    }
                });
```

### 3.11 切换摄像头

下面是切换摄像头代码：
```java
 @OnClick(R.id.id_switch)
    void switchCamera() {
        mAtlasClient.switchCamera(new CCAtlasCallBack<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                isFront = aBoolean;
                mLocalRenderer.setMirror(isFront);
            }

            @Override
            public void onFailure(int errCode, String errMsg) {

            }
        });
    }
```

## 4.功能使用

功能使用时相关的核心类：CCAtlasClient（小班课核心类）。

### 4.1 预览
预览是将初始化相机的流渲染出来：
* 预览方法void preview()；
* 渲染的方法mLocalStream.attach(mLocalRenderer);
### 4.2 开始直播
点击开始直播，成功以后，才会进行推流和拉流操作：
* 开始直播方法void start()
* 开始直播接口mAtlasClient.startLive(new CCAtlasCallBack<Void>())
* 回调实例new CCAtlasCallBack<Void>
### 4.3 结束直播
结束当前直播：
* 结束直播方法void stop()
* 结束直播接口mAtlasClient.stopLive(new CCAtlasCallBack<Void>())
* 回调实例new CCAtlasCallBack<Void>
### 4.4 推流/取消推流

推本地相机的流到atlas服务器：
* 推流的方法void publish()
* 推流接口mAtlasClient.publish(new CCAtlasCallBack<Void>())

取消推相机本地流到atlas服务器：
* 取消推流的方法void publish()
* 取消推流接口mAtlasClient.unpublish(new CCAtlasCallBack<Void>())
### 4.5 拉流/取消拉流

从atlas服务端拉流：
* 拉流方法 void subscribe() 
* 拉流接口 mAtlasClient.subscribe(mStream, new CCAtlasCallBack<CCStream>())

取消从atlas服务端拉流：
* 取消拉流方法 void subscribe() 
* 取消拉流接口 mAtlasClient.unsubcribe(mStream, new CCAtlasCallBack<Void>())
### 4.6 添加RTMP推流/取消RTMP推流

添加RTMP流到atlas服务端：
* 添加RTMP流方法 void pushRtmp() 
* 添加RTMP流接口 mAtlasClient.addExternalOutput(rtmp, new CCAtlasCallBack<Void>())

取消RTMP流到atlas服务端：

* 取消添加RTMP流方法 void pushRtmp()
* 取消添加RTMP流接口  mAtlasClient.removeExternalOutput(rtmp, new CCAtlasCallBack<Void>())

### 4.7 切换摄像头

切换摄像头，前置摄像头和后置摄像头：

* 切换摄像头方法 void switchCamera() 
* 切换摄像头接口 mAtlasClient.switchCamera(new CCAtlasCallBack<Boolean>())

### 4.8 开启本地视频/关闭本地视频

开启本地视频流，也就是相机采集的视频：

* 开启相机视频方法void disableLocalVideo()
* 开启相机视频的接口mLocalStream.enableVideo();

关闭本地视频，也就是关闭相机采集的视频：

* 关闭相机视频方法void disableLocalVideo()
* 关闭相机视频的接口 mLocalStream.disableVideo();

### 4.9 开启本地音频/关闭本地音频

开启本地视频流的音频，也就是相机采集的音频流：

* 开启本地音频方法 void disableLocalAduio()
* 开启本地音频的接口 mLocalStream.enableAudio();

关闭本地视频流的音频，也就是关闭相机采集的音频流：

* 关闭本地音频方法 void disableLocalAduio()
* 关闭本地音频的接口 mLocalStream.disableAudio();

### 4.10 开启远程视频/关闭远程视频

订阅流视频的开启：

* 开启远程视频方法void disableRemoteVideo() 
* 开启远程视频的接口mStream.enableVideo();

订阅流视频的关闭：

* 关闭远程视频方法void disableRemoteVideo() 
* 关闭远程视频的接口 mStream.disableVideo();

### 4.11 开启远程音频/关闭远程音频

订阅流音频的开启：

* 开启远程音频方法  void disableRemoteAudio() 
* 开启远程音频的接口  mStream.enableAudio();

订阅流音频的关闭：

* 关闭远程音频方法  void disableRemoteAudio() 
* 关闭远程音频的接口 mStream.disableAudio();

### 4.12 拍照

拍照功能：

* 拍照方法void takePic()
* 拍照接口mLocalRenderer.getBitmap(new CCSurfaceRenderer.OnShotCallback())

## 5.API查询
Doc目录打开index.html文件

## 6.Q&A
### 6.1 无法拉流

* 首先开始预览，开始直播
* 然后发布（推流），在去订阅流。

### 6.2 推荐权限声明
在AndroidManifest.xml声明权限：
```
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <uses-permission android:name="android.permission.READ_LOGS"/>
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.FLASHLIGHT"/>
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.WRITE_SETTINGS"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.VIBRATE"/>
    <uses-permission android:name="android.permission.WAKE_LOCK"/>

    <uses-feature android:name="android.hardware.camera"/>
    <uses-feature android:name="android.hardware.camera.autofocus"/>
    <uses-feature
        android:glEsVersion="0x00020000"
        android:required="true">
    </uses-feature>
```
