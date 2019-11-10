package com.rnfloatingvideowidget;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ActivityEventListener;
import android.util.Log;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReadableNativeArray;
import com.facebook.react.bridge.WritableMap;
import android.app.Activity;
import android.provider.Settings;


public class FloatingVideoWidgetModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    private final int DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE = 2084;
    private Promise mPromise;
    private final String error  = "Permission was not granted";

    public FloatingVideoWidgetModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        this.reactContext.addActivityEventListener(mActivityEventListener);
    }

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            super.onActivityResult(activity, requestCode, resultCode, data);
            if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(activity.getApplicationContext())) {
                        // Permission Granted by Overlay
                        mPromise.resolve(true);
                    }
                    else {
                        mPromise.reject(new Throwable(error));
                    }
                } else {
                    mPromise.resolve(true);
                }

            }
        }
    };


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

     @ReactMethod
    public void requestOverlayPermission(Promise promise) {
        mPromise = promise;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this.reactContext)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.reactContext.getPackageName()));
            this.reactContext.startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE, null);

        } else {
            promise.resolve(true);
        }
    }



}
