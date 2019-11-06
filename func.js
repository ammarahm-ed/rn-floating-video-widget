import { NativeModules,DeviceEventEmitter } from 'react-native';

const  Widget  = NativeModules.FloatingVideoWidget;

export function open(data) {
    let dataObject = data;
    if (!data || typeof data !== "object") throw new Error("data must be an object with atleast one key as video object");
    if (!data.video || typeof data.video !== "object" ||!data.video.uri) throw new Error("video must be an object with atleast one key 'uri: url to video");
    if (!data.videos) {
        let videos = [];
        videos.push(dataObject.video);
        dataObject.videos = videos;
    }
    if (!data.seek) {
        dataObject.seek = 0;
    }
    if (!data.index) {
        dataObject.index = 0;
    }
    Widget.open(dataObject);
}

export function onError(callback) {
    
    if (!callback) throw new Error("Callback cannot be undefined");

    DeviceEventEmitter.addListener("onError", (event) => callback(event))
    
}

export function onPlay(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");

    DeviceEventEmitter.addListener("onPlay", (event) => callback(event))
}

/* 
*
*@params callback
* a function that runs when video is paused.
*/
export function onPause(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");

    DeviceEventEmitter.addListener("onPause", (event) => callback(event))
}

export function onClose(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");

    DeviceEventEmitter.addListener("onClose", (event) => callback(event))
}

export function onOpen(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");

    DeviceEventEmitter.addListener("onClose", (event) => callback(event))
}

export function removeAllListeners() {
    DeviceEventEmitter.removeAllListeners();
}

