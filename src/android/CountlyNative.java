package ly.count.android.sdk;

//import io.flutter.plugin.common.MethodChannel;
import ly.count.android.sdk.Countly;
import ly.count.android.sdk.DeviceId;
import ly.count.android.sdk.RemoteConfig;
import ly.count.android.sdk.CountlyStarRating;
//import ly.count.android.sdk.DeviceInfo;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.SharedPreferences;
import android.util.Log;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;

//Push Plugin
import android.os.Build;
import android.app.NotificationManager;
import android.app.NotificationChannel;
// import ly.count.android.sdk.messaging.CountlyPush;
import org.json.JSONObject;
// import com.google.firebase.iid.FirebaseInstanceId;
// import com.google.firebase.iid.InstanceIdResult;
// import com.google.android.gms.tasks.Task;
// import com.google.android.gms.tasks.OnCompleteListener;
// import com.google.firebase.FirebaseApp;


public class CountlyNative {

    private Countly.CountlyMessagingMode pushTokenTypeVariable = Countly.CountlyMessagingMode.PRODUCTION;
    private Context context;
    private Activity activity;
    private Boolean isDebug = false;
    private CountlyConfig config = new CountlyConfig();
    private static Callback notificationListener = null;
    private static String lastStoredNotification = null;
    private final Set<String> validConsentFeatureNames = new HashSet<String>(Arrays.asList(
            Countly.CountlyFeatureNames.sessions,
            Countly.CountlyFeatureNames.events,
            Countly.CountlyFeatureNames.views,
            Countly.CountlyFeatureNames.location,
            Countly.CountlyFeatureNames.crashes,
            Countly.CountlyFeatureNames.attribution,
            Countly.CountlyFeatureNames.users,
            Countly.CountlyFeatureNames.push,
            Countly.CountlyFeatureNames.starRating
    ));
    public CountlyNative(Activity _activity, Context _context){
        this.activity = _activity;
        this.context = _context;
    }
    public static void onNotification(Map<String, String>  notification){
        JSONObject json = new JSONObject(notification);
        String notificationString = json.toString();
        Log.i("Countly onNotification", notificationString);
        if(notificationListener != null){
            notificationListener.callback(notificationString);
        }else{
            lastStoredNotification = notificationString;
        }
    }
    public interface Callback {
        void callback(String result);
    }

    public String init(JSONArray args){
        try {
            this.log("init", args);
            String serverUrl = args.getString(0);
            String appKey = args.getString(1);
            this.config.setContext(context);
            this.config.setServerURL((serverUrl));
            this.config.setAppKey(appKey);
            if (args.length() == 2) {
            } else if (args.length() == 3) {
                String yourDeviceID = args.getString(2);
                if(yourDeviceID.equals("TemporaryDeviceID")){
                    this.config.enableTemporaryDeviceIdMode();
                }else{
                    this.config.setDeviceId(yourDeviceID);
                }
            } else {
            }
            Countly.sharedInstance().init(this.config);
            Countly.sharedInstance().onStart(activity);
            return "initialized: success";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public void log(String method, JSONArray args){
        if(isDebug){
            Log.i("Countly Native", "Method: "+method);
            Log.i("Countly Native", "Arguments: "+args.toString());
        }
    }

    public String changeDeviceId(JSONArray args){
        try {
            this.log("changeDeviceId", args);
            String newDeviceID = args.getString(0);
            String onServerString = args.getString(1);
            if ("1".equals(onServerString)) {
                Countly.sharedInstance().changeDeviceId(newDeviceID);
            } else {
                Countly.sharedInstance().changeDeviceId(DeviceId.Type.DEVELOPER_SUPPLIED, newDeviceID);
            }
            return "changeDeviceId success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String setHttpPostForced(JSONArray args){
        try {
            this.log("setHttpPostForced", args);
            int isEnabled = Integer.parseInt(args.getString(0));
            if (isEnabled == 1) {
                this.config.setHttpPostForced(true);
            } else {
                this.config.setHttpPostForced(false);
            }
            return "setHttpPostForced";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String enableParameterTamperingProtection(JSONArray args){
        try {
            this.log("enableParameterTamperingProtection", args);
            String salt = args.getString(0);
            this.config.setParameterTamperingProtectionSalt(salt);
            return "setParameterTamperingProtectionSalt success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String setLocation(JSONArray args){
        try {
            this.log("setLocation", args);
            String latitude = args.getString(0);
            String longitude = args.getString(1);
            String latlng = (latitude + "," + longitude);
            Countly.sharedInstance().setLocation(null, null, latlng, null);
            return "setLocation success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String enableCrashReporting(JSONArray args){
        this.log("enableCrashReporting", args);
        this.config.enableCrashReporting();
        return "enableCrashReporting success!";
    }

    public String addCrashLog(JSONArray args){
        try {
            this.log("addCrashLog", args);
            String record = args.getString(0);
            Countly.sharedInstance().crashes().addCrashBreadcrumb(record);
            return "addCrashLog success";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }
    public String setCustomCrashSegments(JSONArray args){
        try {
            this.log("setCustomCrashSegments", args);
            HashMap<String, Object> segments = new HashMap<String, Object>();
            for (int i = 0, il = args.length(); i < il; i += 2) {
                segments.put(args.getString(i), args.getString(i + 1));
            }
            this.config.setCustomCrashSegment(segments);
            return "setCustomCrashSegments";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String logException(JSONArray args){
        try {
            this.log("logException", args);
            String exceptionString = args.getString(0);
            Exception exception = new Exception(exceptionString);
            Countly.sharedInstance().crashes().recordHandledException(exception);
            return "logException success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

   public String askForNotificationPermission(JSONArray args){
        this.log("askForNotificationPermission", args);
    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    //         String channelName = "Default Name";
    //         String channelDescription = "Default Description";
    //         NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
    //         if (notificationManager != null) {
    //             NotificationChannel channel = new NotificationChannel(CountlyPush.CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
    //             channel.setDescription(channelDescription);
    //             notificationManager.createNotificationChannel(channel);
    //         }
    //    }
    //    CountlyPush.init(activity.getApplication(), pushTokenTypeVariable);
    //    FirebaseApp.initializeApp(context);
    //    FirebaseInstanceId.getInstance().getInstanceId()
    //            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
    //                @Override
    //                public void onComplete(Task<InstanceIdResult> task) {
    //                    if (!task.isSuccessful()) {
    //                        Log.w("Tag", "getInstanceId failed", task.getException());
    //                        return;
    //                    }
    //                    String token = task.getResult().getToken();
    //                    CountlyPush.onTokenRefresh(token);
    //                }
    //            });
       return "askForNotificationPermission";
   }
    public String registerForNotification(JSONArray args, final Callback theCallback){
        notificationListener = theCallback;
        Log.i("CountlyNative", "registerForNotification theCallback");
        if(lastStoredNotification != null){
            theCallback.callback(lastStoredNotification);
            lastStoredNotification = null;
        }
        return "pushTokenType: success";
    }

    public String pushTokenType(JSONArray args){
        try {
            this.log("pushTokenType", args);
            String tokenType = args.getString(0);
            if("2".equals(tokenType)){
                pushTokenTypeVariable = Countly.CountlyMessagingMode.TEST;
            }else{
                pushTokenTypeVariable = Countly.CountlyMessagingMode.PRODUCTION;
            }
            return "pushTokenType: success";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String start(JSONArray args){
        this.log("start", args);
        Countly.sharedInstance().onStart(activity);
        return "started: success";
    }

    public String stop(JSONArray args){
        this.log("stop", args);
        Countly.sharedInstance().onStop();
        return "stoped: success";
    }

    public String halt(JSONArray args){
        this.log("halt", args);
        Countly.sharedInstance().halt();
        return "halt: success";
    }



    // public String eventSendThreshold(JSONArray args){
    //     try {
    //         int eventSendThreshold = Integer.parseInt(args.getString(0));
    //         Countly.sharedInstance().setEventQueueSizeToSend(eventSendThreshold);
    //         return "default";
    //     }catch (JSONException jsonException){
    //         return jsonException.toString();
    //     }
    // }



    public String startEvent(JSONArray args){
        try {
            this.log("startEvent", args);
            String startEvent = args.getString(0);
            Countly.sharedInstance().events().startEvent(startEvent);
            return "startEvent";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }
    public String cancelEvent(JSONArray args){
        try {
            this.log("cancelEvent", args);
            String cancelEvent = args.getString(0);
            Countly.sharedInstance().events().cancelEvent(cancelEvent);
            return "cancelEvent";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String endEvent(JSONArray args){
        try {
            this.log("endEvent", args);
            String key = args.getString(0);
            int count = Integer.parseInt(args.getString(1));
            float sum = Float.valueOf(args.getString(2)); // new Float(args.getString(2)).floatValue();
            HashMap<String, Object> segmentation = new HashMap<String, Object>();
            if (args.length() > 3) {
                for (int i = 3, il = args.length(); i < il; i += 2) {
                    segmentation.put(args.getString(i), args.getString(i + 1));
                }
            }
            Countly.sharedInstance().events().endEvent(key, segmentation, count, sum);
            return "endEvent for:" + key;
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String recordEvent(JSONArray args){
        try {
            this.log("recordEvent", args);
            String key = args.getString(0);
            int count = Integer.parseInt(args.getString(1));
            float sum = Float.valueOf(args.getString(2)); // new Float(args.getString(2)).floatValue();
            int duration = Integer.parseInt(args.getString(3));
            HashMap<String, Object> segmentation = new HashMap<String, Object>();
            if (args.length() > 4) {
                for (int i = 4, il = args.length(); i < il; i += 2) {
                    segmentation.put(args.getString(i), args.getString(i + 1));
                }
            }
            Countly.sharedInstance().events().recordEvent(key, segmentation, count, sum, duration);
            return "recordEvent for: " + key;
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String setLoggingEnabled(JSONArray args){
        try {
            this.log("setLoggingEnabled", args);
            String loggingEnable = args.getString(0);
            if (loggingEnable.equals("true")) {
                isDebug = true;
                this.config.setLoggingEnabled(true);
            } else {
                isDebug = false;
                this.config.setLoggingEnabled(false);
            }
            return "setLoggingEnabled success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String setuserdata(JSONArray args){
        try {
            // Bundle bundle = new Bundle();

            this.log("setuserdata", args);
            Map<String, String> bundle = new HashMap<String, String>();

            bundle.put("name", args.getString(0));
            bundle.put("username", args.getString(1));
            bundle.put("email", args.getString(2));
            bundle.put("organization", args.getString(3));
            bundle.put("phone", args.getString(4));
            bundle.put("picture", args.getString(5));
            bundle.put("picturePath", args.getString(6));
            bundle.put("gender", args.getString(7));
            bundle.put("byear", String.valueOf(args.getInt(8)));

            Countly.userData.setUserData(bundle);
            Countly.userData.save();
            return "setuserdata success";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String userData_setProperty(JSONArray args){
        try {
            this.log("userData_setProperty", args);
            String keyName = args.getString(0);
            String keyValue = args.getString(1);
            Countly.userData.setProperty(keyName, keyValue);
            Countly.userData.save();
            return "userData_setProperty success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String userData_increment(JSONArray args){
        try {
            this.log("userData_increment", args);
            String keyName = args.getString(0);
            Countly.userData.increment(keyName);
            Countly.userData.save();
            return "userData_increment: success";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }


    public String userData_incrementBy(JSONArray args){
        try {
            this.log("userData_incrementBy", args);
            String keyName = args.getString(0);
            int keyIncrement = Integer.parseInt(args.getString(1));
            Countly.userData.incrementBy(keyName, keyIncrement);
            Countly.userData.save();
            return "userData_incrementBy success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String userData_multiply(JSONArray args){
        try {
            this.log("userData_multiply", args);
            String keyName = args.getString(0);
            int multiplyValue = Integer.parseInt(args.getString(1));
            Countly.userData.multiply(keyName, multiplyValue);
            Countly.userData.save();
            return "userData_multiply success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String userData_saveMax(JSONArray args){
        try {
            this.log("userData_saveMax", args);
            String keyName = args.getString(0);
            int maxScore = Integer.parseInt(args.getString(1));
            Countly.userData.saveMax(keyName, maxScore);
            Countly.userData.save();
            return "userData_saveMax success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String userData_saveMin(JSONArray args){
        try {
            this.log("userData_saveMin", args);
            String keyName = args.getString(0);
            int minScore = Integer.parseInt(args.getString(1));
            Countly.userData.saveMin(keyName, minScore);
            Countly.userData.save();
            return "userData_saveMin success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String userData_setOnce(JSONArray args){
        try {
            this.log("userData_setOnce", args);
            String keyName = args.getString(0);
            String minScore = args.getString(1);
            Countly.userData.setOnce(keyName, minScore);
            Countly.userData.save();
            return "userData_setOnce success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String userData_pushUniqueValue(JSONArray args){
        try {
            this.log("userData_pushUniqueValue", args);
            String type = args.getString(0);
            String pushUniqueValue = args.getString(1);
            Countly.userData.pushUniqueValue(type, pushUniqueValue);
            Countly.userData.save();
            return "userData_pushUniqueValue success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String userData_pushValue(JSONArray args){
        try {
            this.log("userData_pushValue", args);
            String type = args.getString(0);
            String pushValue = args.getString(1);
            Countly.userData.pushValue(type, pushValue);
            Countly.userData.save();
            return "userData_pushValue success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String userData_pullValue(JSONArray args){
        try {
            this.log("userData_pullValue", args);
            String type = args.getString(0);
            String pullValue = args.getString(1);
            Countly.userData.pullValue(type, pullValue);
            Countly.userData.save();
            return "userData_pullValue success!";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String setRequiresConsent(JSONArray args){
        try {
            this.log("setRequiresConsent", args);
            String consentFlagString = args.getString(0);
            Boolean consentFlag = false;
            if (consentFlagString.equals("1")) {
                consentFlag = true;
            }
            this.config.setRequiresConsent(consentFlag);
            return "setRequiresConsent: Success";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String giveConsent(JSONArray args){
        try {
            this.log("giveConsent", args);
            String consent = null;
            List<String> features = new ArrayList<>();
            for (int i = 0; i < args.length(); i++) {
                consent = args.getString(i);
                if (consent.equals("sessions")) {
                    Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.sessions});
                }
                if (consent.equals("events")) {
                    Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.events});
                }
                if (consent.equals("views")) {
                    Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.views});
                }
                if (consent.equals("location")) {
                    Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.location});
                }
                if (consent.equals("crashes")) {
                    Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.crashes});
                }
                if (consent.equals("attribution")) {
                    Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.attribution});
                }
                if (consent.equals("users")) {
                    Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.users});
                }
                if (consent.equals("push")) {
                    Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.push});
                }
                if (consent.equals("starRating")) {
                    Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.starRating});
                }
            }
            return "giveConsent: Success";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String removeConsent(JSONArray args){
        try {
            this.log("removeConsent", args);
            String consent = null;
            List<String> features = new ArrayList<>();
            for (int i = 0; i < args.length(); i++) {
                consent = args.getString(i);
                if (consent.equals("sessions")) {
                    Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.sessions});
                }
                if (consent.equals("events")) {
                    Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.events});
                }
                if (consent.equals("views")) {
                    Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.views});
                }
                if (consent.equals("location")) {
                    Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.location});
                }
                if (consent.equals("crashes")) {
                    Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.crashes});
                }
                if (consent.equals("attribution")) {
                    Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.attribution});
                }
                if (consent.equals("users")) {
                    Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.users});
                }
                if (consent.equals("push")) {
                    Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.push});
                }
                if (consent.equals("starRating")) {
                    Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.starRating});
                }
            }
            return "removeConsent: Success";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String giveAllConsent(JSONArray args){
        this.log("giveAllConsent", args);
        Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.sessions});
        Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.events});
        Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.views});
        Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.location});
        Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.crashes});
        Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.attribution});
        Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.users});
        Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.push});
        Countly.sharedInstance().consent().giveConsent(new String[]{Countly.CountlyFeatureNames.starRating});
        return "giveAllConsent: Success";
    }

    public String removeAllConsent(JSONArray args){
        this.log("removeAllConsent", args);
        Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.sessions});
        Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.events});
        Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.views});
        Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.location});
        Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.crashes});
        Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.attribution});
        Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.users});
        Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.push});
        Countly.sharedInstance().consent().removeConsent(new String[]{Countly.CountlyFeatureNames.starRating});
        return "removeAllConsent: Success";
    }

    // public String sendRating(JSONArray args){
    //     try {
    //         String ratingString = args.getString(0);
    //         int rating = Integer.parseInt(ratingString);

    //         Map<String, String> segm = new HashMap<>();
    //         segm.put("platform", "android");
    //         // segm.put("app_version", DeviceInfo.getAppVersion(context));
    //         segm.put("rating", "" + rating);

    //         Countly.sharedInstance().recordEvent("[CLY]_star_rating", segm, 1);
    //         return "sendRating: " + ratingString;
    //     }catch (JSONException jsonException){
    //         return jsonException.toString();
    //     }
    // }

    public String recordView(JSONArray args){
        try {
            this.log("recordView", args);
            String viewName = args.getString(0);
            Countly.sharedInstance().views().recordView(viewName);
            return "View name sent: " + viewName;
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String setOptionalParametersForInitialization(JSONArray args){
        try {
            this.log("setOptionalParametersForInitialization", args);
            String city = args.getString(0);
            String country = args.getString(1);
            String latitude = args.getString(2);
            String longitude = args.getString(3);
            String ipAddress = args.getString(4);
            String latlng = latitude + "," + longitude;
            if(city.length() == 0){
                city = null;
            }
            if(country.length() == 0){
                country = null;
            }
            if(latitude.equals("0.00")){
                latitude = null;
            }
            if(longitude.equals("0.00")){
                longitude = null;
            }
            if(latitude == null && longitude == null){
                latlng = null;
            }
            if(ipAddress.equals("0.0.0.0")){
                ipAddress = null;
            }
            Countly.sharedInstance().setLocation(country, city, latlng, ipAddress);
            return "setOptionalParametersForInitialization sent.";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String setRemoteConfigAutomaticDownload(JSONArray args, final Callback theCallback){
        this.log("setRemoteConfigAutomaticDownload", args);
        this.config.setRemoteConfigAutomaticDownload(true, new RemoteConfig.RemoteConfigCallback() {
            @Override
            public void callback(String error) {
                if (error == null) {
                    theCallback.callback("Success");
                } else {
                    theCallback.callback("Error: " + error.toString());
                }
            }
        });
        return "setRemoteConfigAutomaticDownload: Success";
    }

    public String remoteConfigUpdate(JSONArray args ,final Callback theCallback){
        this.log("remoteConfigUpdate", args);
        Countly.sharedInstance().remoteConfig().update(new RemoteConfigCallback() {
            @Override
            public void callback(String error) {
                if (error == null) {
                    theCallback.callback("Success");
                } else {
                    theCallback.callback("Error: " + error.toString());
                }
            }
        });
        return "remoteConfigUpdate: success";
    }

    public String updateRemoteConfigForKeysOnly(JSONArray args, final Callback theCallback){
        try {
            this.log("updateRemoteConfigForKeysOnly", args);
            String[] keysOnly = new String[args.length()];
            for (int i = 0, il = args.length(); i < il; i++) {
                keysOnly[i] = args.getString(i);
                ;
            }
            Countly.sharedInstance().remoteConfig().updateForKeysOnly(keysOnly, new RemoteConfigCallback() {
                @Override
                public void callback(String error) {
                    if (error == null) {
                        theCallback.callback("Success");
                    } else {
                        theCallback.callback("Error: " + error.toString());
                    }
                }
            });
            return "updateRemoteConfigForKeysOnly: success";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String updateRemoteConfigExceptKeys(JSONArray args, final Callback theCallback){
        try {
            this.log("updateRemoteConfigExceptKeys", args);
            String[] exceptKeys = new String[args.length()];
            for (int i = 0, il = args.length(); i < il; i++) {
                exceptKeys[i] = args.getString(i);
            }
            Countly.sharedInstance().remoteConfig().updateExceptKeys(exceptKeys, new RemoteConfigCallback() {
                @Override
                public void callback(String error) {
                    if (error == null) {
                        theCallback.callback("Success");
                    } else {
                        theCallback.callback("Error: " + error.toString());
                    }
                }
            });
            return "updateRemoteConfigExceptKeys: Success";
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String remoteConfigClearValues(JSONArray args){
        this.log("remoteConfigClearValues", args);
        Countly.sharedInstance().remoteConfig().clearStoredValues();
        return "remoteConfigClearValues: success";
    }

    public String getRemoteConfigValueForKey(JSONArray args){
        try {
            this.log("getRemoteConfigValueForKey", args);
            String getRemoteConfigValueForKeyResult = Countly.sharedInstance().remoteConfig().getValueForKey(args.getString(0)).toString();
            return getRemoteConfigValueForKeyResult;
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
    }

    public String askForFeedback(JSONArray args, final Callback theCallback){
        try {
            this.log("askForFeedback", args);
            String widgetId = args.getString(0);
            String closeButtonText = args.getString(1);
            Countly.sharedInstance().ratings().showFeedbackPopup(widgetId, closeButtonText, activity, new FeedbackRatingCallback(){
                @Override
                public void callback(String error) {
                    if (error != null) {
                        theCallback.callback("Error: Encountered error while showing feedback dialog: [" + error + "]");
                    } else {
                        theCallback.callback("Feedback submitted.");
                    }
                }
            });
            return "askForFeedback: success";
        }catch (JSONException jsonException){
            theCallback.callback(jsonException.toString());
            return jsonException.toString();
        }
    }

    public String askForStarRating(JSONArray args){
        this.log("askForStarRating", args);
        Countly.sharedInstance().ratings().showStarRating(activity, null);
        return "askForStarRating success.";
    }

    public String sendPushToken(JSONArray args){
        try {
            this.log("sendPushToken", args);
            ConnectionQueue connectionQueue_ = Countly.sharedInstance().getConnectionQueue();
            String token = args.getString(0);
            int messagingMode = Integer.parseInt(args.getString(1));
            if(messagingMode == 0){
                connectionQueue_.tokenSession(token, Countly.CountlyMessagingMode.PRODUCTION);
            }else{
                connectionQueue_.tokenSession(token, Countly.CountlyMessagingMode.TEST);
            }
        }catch (JSONException jsonException){
            return jsonException.toString();
        }
        return "sendPushToken success.";
    }


}
