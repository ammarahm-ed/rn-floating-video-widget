import { NativeModules,DeviceEventEmitter } from 'react-native';

const  Widget  = NativeModules.FloatingVideoWidget;

export function open(data) {

    if (!data || typeof data !== "object") throw new Error("data must be an object with atleast one key as video object");
    if (!data.video || typeof data.video !== "object" ||!data.video.videoURL) throw new Error("video must be an object with atleast one key 'uri: url to video");
    if (!data.hasOwnProperty("videos") || data.videos.length == 0) {
        let videos = [];
        videos.push(data.video);
        data.videos = videos;
    }
    if (!data.hasOwnProperty("seek")) {
        data.seek = 0;
    }

    if (!data.hasOwnProperty("index")) {
        data.index = 0;
    }
    Widget.open(data);

}


export function close() {
    Widget.close();
}

export async function requestOverlayPermission() {
   return await Widget.requestOverlayPermission();
}

export function play() {
    Widget.play();
}
export function pause() {
    Widget.pause();
}
export function prev() {
    Widget.prev();
}
export function next() {
    Widget.next();
}

export function onError(callback) {

    if (!callback) throw new Error("Callback cannot be undefined");

    DeviceEventEmitter.addListener("onError", (event) => callback(event))

}

export function removeOnError(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");

    DeviceEventEmitter.removeListener("onError", (event) => callback(event))
}

export function onPlay(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onPlay", (event) => callback(event))
}
export function removeOnPlay(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.removeListener("onPlay", (event) => callback(event))
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

export function removeOnPause(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.removeListener("onPause", (event) => callback(event))
}

export function onNext(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onNext", (event) => callback(event))
}
export function removeOnNext(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.removeListener("onNext", (event) => callback(event))
}

export function onPrev(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onPrev", (event) => callback(event))
}
export function removeOnPrev(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.removeListener("onPrev", (event) => callback(event))
}


export function onClose(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onClose", (event) => callback(event))
}
export function removeOnClose(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.removeListener("onClose", (event) => callback(event))
}

export function onOpen(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onOpen", (event) => callback(event))
}
export function removeOnOpen(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.removeListener("onOpen", (event) => callback(event))
}

export function removeAllListeners() {
    DeviceEventEmitter.removeAllListeners();
}

