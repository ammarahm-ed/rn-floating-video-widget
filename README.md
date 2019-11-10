# rn-floating-video-widget
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-green?style=flat-square)](https://img.shields.io/badge/PRs-welcome-green?style=flat-square)


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
|open|A data object(see below)Initialise the player and play video|
| play || play/resume the video
| pause| |pause the video
|next||Play next video in playlist
|prev||Play previous video in playlist
|close||Close Floating video player
|requestOverlayPermission||Ask for `draw over other apps` permission

#### The Data Object
The video data object is a single object that can have the following properties

|Name|Type|Required|Description
|--|--|--|--|
| video |Object  | yes|A video object atlease should. have a `url` property. It can also include other properties such as `width` and `height` of the video. If `width` and `height` are provided, the floating video will maintain the aspect ratio according to video width and height. 
| videos | Array|no |Array of the above video objects
| seek | number(ms) |no |seek video on load to this value
| index | number | no| index of `video`object in `videos` array.


### Event Listeners
All event listeners should have a callback function as an argument to handle the event.

|Name|Description|Data recieved from event|
|--|--|--|
| onOpen |floating video is open and video is playing | `{type:"play",seek,index,url}`
| onPlay |video is playing | `{type:"play",seek,index,url}`
| onPause|video is paused| `{type:"pause",seek,index,url}` 
|onNext|next video is playing| `{type:"next",seek,index,url}`
|onPrev|previous video is playing| `{type:"prev",seek,index,url}`
|onClose|floating video player has closed| `{type:"close",seek,index,url}`
|onError|Called when an error occurred| `{type:"close",seek,index,url}`

Don't forget to call `FloatingVideo.removeAllListeners()` when component unmount.

## Usage
For complete usage, see the example project.

    import React from 'react';  
    import {  
      StyleSheet,  
      View,  
      TouchableOpacity,  
      ToastAndroid,  
      Text,  
    } from 'react-native';  
    import FloatingVideo from 'rn-floating-video-widget';  
      
    export default class App extends React.Component {  
    
      constructor(props) {  
      super(props);  
      this.state = {  
      floating: false,  
      granted: false,  
     };  
     // The Data Object
     this.data = {  
      video: {  
      url:  
      'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',  
     },  videos: [  
     {  url:  
      'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',  
     }, {  url:  
      'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4',  
     }, {  url:  
      'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',  
     }, ],  seek: 10,  
      index: 0,  
     }; }  
     
     
      componentDidMount() {  
    
      // Add event listeners
      
      FloatingVideo.onClose(data => console.log(data));  
      FloatingVideo.onOpen(data => console.log(data));  
      FloatingVideo.onPlay(data => console.log(data));  
      FloatingVideo.onPause(data => console.log(data));  
      FloatingVideo.onNext(data => console.log(data));  
      FloatingVideo.onPrev(data => console.log(data));  
      FloatingVideo.onError(data => console.log(data));  
     }  
     
     
      enterPipMode() { 
     
      FloatingVideo.requestOverlayPermission() 
     .then(() => {  
      this.setState({  
      floating: true,  
      granted: true,  
     });  
     FloatingVideo.open(this.data);  
     }) .catch(e => {  
      ToastAndroid.show(  
      'Please grant draw over other apps permission' + JSON.stringify(e),  
      800,  
     ); 
     }); 
     }  
      
      componentWillUnmount() {  
      FloatingVideo.removeAllListeners();  
     }  
     
      render() {  
      const floating = this.state.floating; 
      
      return (  
     <View style={styles.container}>  
     <TouchableOpacity  
      style={styles.start}  
      onPress={() => {  
      this.enterPipMode();  
     }}> 
     <Text  
      style={{  
      color: 'white',  
      fontSize: 20,  
     }}>  
     START  
     </Text>
     </TouchableOpacity>  
     </View>  
     ); }}  
      
    const styles = StyleSheet.create({  
      container: {  
      flex: 1,  
 
      alignItems: 'center',  
      paddingTop: 20,  
      backgroundColor: '#F5FCFF',  
     },  welcome: {  
      fontSize: 20,  
      textAlign: 'center',  
      margin: 10,  
     }, 
     start: {  
      width: '90%',  
      alignSelf: 'center',  
      padding: 15,  
      backgroundColor: 'red',  
      justifyContent: 'center',  
      alignItems: 'center',  
      elevation: 5,  
      borderRadius: 5,  
     },  
     button: {  
      alignSelf: 'center',  
      padding: 5,  
      borderWidth: 1,  
      borderColor: 'red',  
      backgroundColor: 'white',  
      justifyContent: 'center',  
      alignItems: 'center',  
      elevation: 5,  
      borderRadius: 5,  
     },});


## Todo List
 - [ ] Improve native code quality
 - [ ] Complete error handling
 - [ ] Use ExoPlayer w/o ExoMedia Library

### Thanks to developers of following libraries

 - [react-native-float-widget](https://github.com/thebylito/react-native-float-widget)
 - [react-native-draw-overlays](https://github.com/MaxToyberman/react-native-draw-overlay)

### Dependencies

 -  [ExoMedia Library Apache 2.0 Licence](https://github.com/brianwernick/ExoMedia)
 - [ExoPlayer Library Apache 2.0 Licence](https://github.com/google/ExoPlayer)

### MIT Licensed
