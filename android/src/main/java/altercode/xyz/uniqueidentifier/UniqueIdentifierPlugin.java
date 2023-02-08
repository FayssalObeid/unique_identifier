package altercode.xyz.uniqueidentifier;

import android.annotation.SuppressLint;
import android.content.Context;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.Objects;

import io.flutter.embedding.engine.plugins.FlutterPlugin;


public class UniqueIdentifierPlugin implements FlutterPlugin, MethodCallHandler {
    static private Context context;
    private MethodChannel channel;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "unique_identifier");
        context = flutterPluginBinding.getApplicationContext();
        channel.setMethodCallHandler(this);
    }


    @SuppressLint("HardwareIds")
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getUniqueIdentifier")) {
            String serialNumber = null;
            try {
                @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serialNumber = (String) get.invoke(c, "gsm.sn1");

                if (Objects.equals(serialNumber, ""))
                    // Samsung Galaxy S5 (SM-G900F) : 6.0.1
                    // Samsung Galaxy S6 (SM-G920F) : 7.0
                    // Samsung Galaxy Tab 4 (SM-T530) : 5.0.2
                    // (?) Samsung Galaxy Tab 2 (https://gist.github.com/jgold6/f46b1c049a1ee94fdb52)
                    serialNumber = (String) get.invoke(c, "ril.serialnumber");

                if (Objects.equals(serialNumber, ""))
                    // Archos 133 Oxygen : 6.0.1
                    // Google Nexus 5 : 6.0.1
                    // Hannspree HANNSPAD 13.3" TITAN 2 (HSG1351) : 5.1.1
                    // Honor 5C (NEM-L51) : 7.0
                    // Honor 5X (KIW-L21) : 6.0.1
                    // Huawei M2 (M2-801w) : 5.1.1
                    // (?) HTC Nexus One : 2.3.4 (https://gist.github.com/tetsu-koba/992373)
                    serialNumber = (String) get.invoke(c, "ro.serialno");

                if (Objects.equals(serialNumber, ""))
                    // (?) Samsung Galaxy Tab 3 (https://stackoverflow.com/a/27274950/1276306)
                    serialNumber = (String) get.invoke(c, "sys.serialnumber");

                if (Objects.equals(serialNumber, ""))
                    // Archos 133 Oxygen : 6.0.1
                    // Hannspree HANNSPAD 13.3" TITAN 2 (HSG1351) : 5.1.1
                    // Honor 9 Lite (LLD-L31) : 8.0
                    // Xiaomi Mi 8 (M1803E1A) : 8.1.0
                    serialNumber = Build.SERIAL;

                if (Objects.equals(serialNumber, "") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    serialNumber = Build.getSerial();
                }
                // If none of the methods above worked
                if (Objects.equals(serialNumber, Build.UNKNOWN) || Objects.equals(serialNumber, "")) {
                    serialNumber = Settings.Secure.getString(context.getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                }
            } catch (Exception e) {
                e.printStackTrace();
                serialNumber = null;
            }
            result.success(serialNumber);
        } else {
            result.notImplemented();
        }


    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }


}
