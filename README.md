# rn-floating-video-widget


React Native Module for **Floating/Popup** video player on Android.  

## Run the Example
To run the example app clone the project

    git clone https://github.com/ammarahm-ed/rn-floating-video-widget.git

      

   then run ` yarn or npm install` in the example folder and finally to run the example app:
       
   
    react-native run-android



## Installation

    npm install rn-floating-video-widget --save
or if you use yarn:

    yarn add rn-floating-video-widget

If you are using `react-native <= 0.59.0` you also need to run:

    react-native link rn-floating-video-widget

Add permission `SYSTEM_ALERT_WINDOW` to `AndroidManifest.xml`:

    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
Add the following `service` in `AndroidManifest.xml` inside `<application>` tag:

     <application>
        ...
        <service android:name="com.rnfloatingvideowidget.FloatingVideoWidgetShowService"  
        android:enabled="true"></service>
      ...
      </application>
    

## FloatingVideo API
FloatingVideo API has been kept very simple and practical in use.
### Methods
|Name|arguments|Description|
|--|--|--|
| play |A video data object(see below)  | Initialise player and play video
| pause| |pause the video
|next||Play next video in playlist
|prev||Play previous video in playlist
|close||Close Floating video player
|requestOverlayPermission||Ask for `draw over other apps` permission

#### Video Data Object
The video data object is a single object that can have the following properties

|Name|Type|Required|Description
|--|--|--|--|
| video |Object  | yes|A video object must have a `url` property
| videos | Array|no |Array of the above video objects with a `url` property
| seek | number(ms) |no |seek video on load to this value
| index | number | no| index of `video`object in `videos` array.


### Event Listeners
All event listeners should have a callback function as an argument to handle the event.

|Name|Description|Data recieved from event|
|--|--|--|
| onOpen |video is playing | `{type:"play",seek,index,url}`
| onPause|video is paused| `{type:"pause",seek,index,url}` 
|onNext|next video is playing| `{type:"next",seek,index,url}`
|onPrev|previous video is playing| `{type:"prev",seek,index,url}`
|onClose|floating video player has closed| `{type:"close",seek,index,url}`
|onError|Called when an error occurred| `{type:"close",seek,index,url}`

Don't forget to call `FloatingVideo.removeAllListeners()` when component unmount.

## Todo List
 - [ ] Improve native code quality
 - [ ] Complete error handling
 - [ ] Use ExoPlayer w/o ExoMedia Library

### Thanks to developers of following libraries

 - [react-native-float-widget](https://github.com/thebylito/react-native-float-widget)
 - [react-native-draw-overlays](https://github.com/MaxToyberman/react-native-draw-overlays)

### Dependencies

 -  [ExoMedia Library Apache 2.0 Licence](https://github.com/brianwernick/ExoMedia)
 - [ExoPlayer Library Apache 2.0 Licence](https://github.com/google/ExoPlayer)

### MIT Licensed
