package com.rnfloatingvideowidget;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.bridge.WritableMap;

public class FloatingVideoWidgetModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    public FloatingVideoWidgetModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "FloatingVideoWidget";
    }


    @ReactMethod
    public void close() {
        Intent intent = new Intent("ACTION_CLOSE_WIDGET", null, reactContext, FloatingVideoWidgetShowService.class);
        reactContext.startService(intent);
    }

    @ReactMethod
    public void play() {
        Intent intent = new Intent("ACTION_PLAY", null, reactContext, FloatingVideoWidgetShowService.class);
        reactContext.startService(intent);
    }

    @ReactMethod
    public void pause() {
        Intent intent = new Intent("ACTION_PAUSE", null, reactContext, FloatingVideoWidgetShowService.class);
        reactContext.startService(intent);
    }

    @ReactMethod
    public void prev() {
        Intent intent = new Intent("ACTION_PREV", null, reactContext, FloatingVideoWidgetShowService.class);
        reactContext.startService(intent);
    }

    @ReactMethod
    public void next() {
        Intent intent = new Intent("ACTION_NEXT", null, reactContext, FloatingVideoWidgetShowService.class);
        reactContext.startService(intent);
    }


    @ReactMethod
    public void open(ReadableMap data) {
        Intent intent = new Intent("ACTION_SET_VIDEO", null, reactContext, FloatingVideoWidgetShowService.class);
        intent.putExtra("DATA", Arguments.toBundle(data));
        reactContext.startService(intent);
    }


}
