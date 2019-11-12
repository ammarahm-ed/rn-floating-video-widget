import {NativeModules, DeviceEventEmitter} from 'react-native';

const Widget = NativeModules.FloatingVideoWidget;


/**
 * Opens the floating video player and starts
 * playing the video.
 * @param {object} data The data object.
 * @param {object} data.video The video object should atleast have a property named "".
 * @param data.video.url The url of video to be played
 * @param {Array} data.videos List of video objects structured similar to data.video.
 * @param {number}  data.seek Seek value of video.
 * @param {number} data.index Index of the video in data.videos array.
 */

export function open(data) {
    if (!data || typeof data !== "object") throw new Error("data must be an object with atleast one key as video object");
    if (!data.video || typeof data.video !== "object" || !data.video.url) throw new Error("video must be an object with atleast one key 'uri: url to video");
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

/**
 * Close the floating video player
 */

export function close() {
    Widget.close();
}

/**
 * Request for draw_over_other_apps permission for android 6.0 and above.
 * @return {Promise}
 */

export async function requestOverlayPermission() {
    return await Widget.requestOverlayPermission();
}

/**
 * Play the video
 *
 */

export function play() {
    Widget.play();
}

/**
 * Pause the video
 *
 */
export function pause() {
    Widget.pause();
}

/**
 * Play previous video
 *
 */
export function prev() {
    Widget.prev();
}

/**
 * Play next video
 *
 */
export function next() {
    Widget.next();
}

/**
 * @event onError Called when an error occurs
 * @type {function}
 */

export function onError(callback) {

    if (!callback) throw new Error("Callback cannot be undefined");

    DeviceEventEmitter.addListener("onError", (event) => callback(event))

}

/**
 * @event onError Called when video is played.
 * @type {function}
 */
export function onPlay(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onPlay", (event) => callback(event))
}

/**
 * @event onPause Called when video is paused
 * @type {function}
 */
export function onPause(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onPause", (event) => callback(event))
}

/**
 * @event onNext Called when a you play the next video
 * @type {function}
 */
export function onNext(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onNext", (event) => callback(event))
}

/**
 * @event onPrev Called when you play the previous video
 * @type {function}
 */
export function onPrev(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onPrev", (event) => callback(event))
}

/**
 * @event onClose Called when the floating video has closed.
 * @type {function}
 */
export function onClose(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onClose", (event) => callback(event))
}

/**
 * @event onOpen  Called when a new video is played from react-native side or when the floating player is opened
 * @type {function}
 */
export function onOpen(callback) {
    if (!callback) throw new Error("Callback cannot be undefined");
    DeviceEventEmitter.addListener("onOpen", (event) => callback(event))
}

/**
 * Removes all listeners
 * Use this in your ComponenWillUnmount() function
 */

export function removeAllListeners() {


    DeviceEventEmitter.removeAllListeners();
}

