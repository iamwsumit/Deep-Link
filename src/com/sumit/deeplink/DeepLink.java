package com.sumit.deeplink;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.OnNewIntentListener;
import com.google.appinventor.components.runtime.ReplForm;
import com.google.appinventor.components.runtime.util.JsonUtil;

public class DeepLink extends AndroidNonvisibleComponent implements Component {
    private static final String TAG = "DeepLink";
    private final Activity activity;
    private final boolean isDeepLink;
    private final String deepLinkUrl;

    public DeepLink(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();

        final Intent intent = activity.getIntent();
        if (intent != null) {
            isDeepLink = intent.hasExtra("deep_link_url");
            deepLinkUrl = isDeepLink ? intent.getStringExtra("deep_link_url") : "";
            intent.removeExtra("deep_link_url");
        } else {
            deepLinkUrl = "";
            isDeepLink = false;
        }

        form.registerForOnNewIntent(new OnNewIntentListener() {
            @Override
            public void onNewIntent(Intent intent) {
                if (intent.hasExtra("deep_link_url"))
                    DeepLinkActivityResumed(intent.getStringExtra("deep_link_url"));
            }
        });

    }

    private static SharedPreferences getPreference(Activity activity) {
        return activity.getSharedPreferences(TAG, 0);
    }

    @SimpleEvent
    public void DeepLinkActivityResumed(String url) {
        EventDispatcher.dispatchEvent(this, "DeepLinkActivityResumed", url);
    }

    @SimpleFunction
    public boolean IsDeepLink() {
        return isDeepLink;
    }

    @SimpleFunction
    public String GetDeepLinkUrl() {
        return deepLinkUrl;
    }

    @SimpleFunction
    public void RegisterActivity(String screenName, String startValue) {
        getPreference(activity).edit().putString("name", getScreenName(screenName)).apply();
        getPreference(activity).edit().putString("start", startValue).apply();
    }

    private String getScreenName(String screenName) {
        final String activityName = form.getClass().getName().replaceAll(form.getClass().getSimpleName(), screenName);
        Log.i(TAG, "getScreenName: " + activityName);
        return activityName;
    }

    public static class DeepLinkActivity extends Activity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            startActivity(getLaunchIntent());
            finish();
        }

        private Intent getLaunchIntent() {
            Intent intent = getDefaultIntent();
            try {
                final String screenName = getPreference(this).getString("name", "");
                Class klass = Class.forName(screenName);
                if (klass != null)
                    intent = new Intent(this, klass);
                else
                    Log.i(TAG, "getLaunchIntent: class is null so not setting the start value");
                Uri uri = getIntent().getData();
                if (uri != null) {
                    final String url = uri.toString();
                    intent.putExtra("deep_link_url", url);
                }
                intent.putExtra("APP_INVENTOR_START", JsonUtil.getJsonRepresentation(getPreference(this).getString("start", "")));

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } catch (Exception e) {
                Log.e(TAG, "getLaunchIntent: ", e);
            }

            return intent;
        }

        private Intent getDefaultIntent() {
            return getPackageManager().getLaunchIntentForPackage(getPackageName());
        }
    }
}