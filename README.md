# rn-floating-video-widget

React Native Module for **Floating/Popup** video player on Android.  

## Installation

    npm install rn-floating-video-Widget --save
or if you use yarn:

    yarn add rn-floating-video-widget

If you are using `react-native <= 0.59.0` you also need to run:

    react-native link rn-floating-video-widget

## FloatingVideo API
FloatingVideo API has been kept very simple and practical in use.
### Methods
|Name|arguments|Description|
|--|--|--|
| play |A video data object  | Initialise player and play video
| pause| |pause the video
|next||Play next video in playlist
|prev||Play previous video in playlist
|close||Close Floating video player

#### Video Data Object
|Name|Type|Required|Description
|--|--|--|--|
| video |Object  | yes|A video object must have a `url` property
| videos | Array|no |Array of the above video objects with a `url` property
| seek | number(ms) |no |seek video on load to this value
| index | number | no| index of `video`object in `videos` array.


### Event Listeners
All event listeners should have a callback function as an argument to handle the event.
|Name|Description|Data recieved from event
|--|--|--|
| onOpen |video is playing | `{type:"play",seek,index,url}`
| onPause|video is paused| `{type:"pause",seek,index,url}` 
|onNext|next video is playing| `{type:"next",seek,index,url}`
|onPrev|previous video is playing| `{type:"prev",seek,index,url}`
|onClose|floating video player has closed| `{type:"close",seek,index,url}`
|onError|Called when an error occured| `{type:"close",seek,index,url}`
