package ae.adpolice.gov;


import android.content.Context;
import android.util.Base64;

import com.vasco.digipass.sdk.utils.devicebinding.DeviceBinding;
import com.vasco.digipass.sdk.utils.devicebinding.DeviceBindingSDKException;
import com.vasco.digipass.sdk.utils.wbc.WBCSDK;
import com.vasco.digipass.sdk.utils.wbc.WBCSDKConstants;
import com.vasco.digipass.sdk.utils.wbc.WBCSDKException;
import com.vasco.digipass.sdk.utils.wbc.WBCSDKTables;

import ae.adpolice.gov.utils.Crashlytics;
import ae.adpolice.gov.utils.Utils;
import ae.adpolice.gov.wbc.tables.WBCSDKTablesImpl;

public class Constants {
    public static final String BIOMETRIC_STATUS = "bioMetricStatus";
    public static final String CLIENT_SERVER_TIME_SHIFT_KEY = "clientServerTimeShift";
    public static final String PIN_KEY = "pin";
    public static final String CREDENTIALS_KEY = "credentials";
    public static final String NOTIFICATION_CONTENT_KEY = "notificationContent";

    // Salts used to diversify the protection mechanisms for sensitive features.
    // TODO: Paste here two different random strings of 64 hexadecimal characters.
    //  TODO:   public static final String SALT_STORAGE = "4F63F620263C91CF32998AA54D57DEDF333CD4D7F9D1DB362731DACFD1F14A21";
    //  TODO:   public static final String SALT_DIGIPASS = "EE738691D84DFF18FB373E9D846C4575F42073A2A1AD5B0890DCD871BFFE526C";

    public static String getAuthorization() {
        return "Basic " + Base64.encodeToString((BuildConfig.API_KEY + ":").getBytes(), Base64.NO_WRAP);
        //return "Basic RkE1RjU5MUZCRkQ2NDYwNTFEQkU5NEUxOERBMkQ3RjQzOUMxQ0U2MEM0RTE2QjQyMUEzQkNEQkUzNTI4NTc1Mzo=";
    }

    public static String getDevicePlatformFingerprintForDigipass(Context context) {
        try {
            return DeviceBinding.createDeviceBinding(context, DeviceBinding.FingerprintType.ANDROID_ID).fingerprint(Constants.SALT_DIGIPASS);
        } catch (DeviceBindingSDKException e) {
            Crashlytics.logException(e);
        }
        return "";
    }

    public static boolean isSaltStorageEmpty() {
        return SALT_STORAGE.isEmpty();
    }

    public static String getDevicePlatformFingerprintForStorage(Context context) {
        try {
            return DeviceBinding.createDeviceBinding(context, DeviceBinding.FingerprintType.ANDROID_ID).fingerprint(Constants.SALT_STORAGE);
        } catch (DeviceBindingSDKException e) {
            Crashlytics.logException(e);
        }
        return "";
    }

    //underscores can't be used for Secure Storage keys
    public static String getStaticVectorKey(String userId) {
        return userId + "staticvector";
    }

    //underscores can't be used for Secure Storage keys
    public static String getDynamicVectorPinKey(String userId) {
        return userId + "dvpin";
    }

    //underscores can't be used for Secure Storage keys
    public static String getDynamicVectorBioKey(String userId) {
        return userId + "dvbio";
    }


    // WBC Code

    private static final WBCSDKTables tablesToEncrypt = new WBCSDKTablesImpl();

    private static final String X_SALT_STORAGE = BuildConfig.SECURESTORAGE_SALT_KEY;
    private static final String X_SALT_DIGIPASS = BuildConfig.DIGIPASS_SALT_KEY;

    private static final String DP_IV = BuildConfig.DIGIPASS_IV;
    private static final String SS_IV = BuildConfig.SECURESTORAGE_IV;

    private static String SALT_STORAGE = ""; // ONLY KNOWN AT RUNTIME
    private static String SALT_DIGIPASS = "";


    // FOR PERFORMANCE, DO IT ONLY ONCE, WHEN THE APPLICATION STARTS
    public static void initializeSalts() {
        SALT_DIGIPASS = encryptAndFormat(DP_IV, X_SALT_DIGIPASS);
        SALT_STORAGE = encryptAndFormat(SS_IV, X_SALT_STORAGE);
        Utils.Log("Digipass Salt", SALT_DIGIPASS);
        Utils.Log("Secure Storage Salt", SALT_STORAGE);
    }

    private static String encryptAndFormat(String IV, String toEncrypt) {
        // 1. ENCRYPT
        byte[] B_IV = hexaToBytes(IV);
        byte[] B_TO_ENCRYPT = hexaToBytes(toEncrypt);
        byte[] B_ENCRYPTED = null;
        try {
            B_ENCRYPTED = WBCSDK.encrypt(WBCSDKConstants.CRYPTO_MECHANISM_AES,
                    WBCSDKConstants.CRYPTO_MODE_CTR, Constants.tablesToEncrypt, B_IV, B_TO_ENCRYPT);
        } catch (WBCSDKException e) {
            Utils.Log("WBC ENCRYPT", "ERROR:" + e.getLocalizedMessage() + e.getErrorCode());
            // something went wrong
            Crashlytics.logException(e);
        }

        return bytesToHexa(B_ENCRYPTED);
    }

    public static String bytesToHexa(byte[] array) {
        if (array == null) {
            return null;
        } else {
            StringBuilder var2 = new StringBuilder();

            for (byte b : array) {
                int var4 = b & 255;
                String var1 = Integer.toHexString(var4);
                if (var1.length() == 1) {
                    var2.append('0');
                }
                var2.append(var1);
            }

            return var2.toString().toUpperCase();
        }
    }

    public static int hexaToBytes(String str, byte[] buffer, int offset) {
        int var7 = str.length() / 2;

        for(int var8 = 0; var8 < var7; ++var8) {
            char var3 = str.charAt(var8 * 2);
            byte var5 = charToByte(var3);
            char var4 = str.charAt(var8 * 2 + 1);
            byte var6 = charToByte(var4);
            buffer[var8 + offset] = (byte)((var5 << 4) + var6);
        }

        return var7;
    }

    public static byte[] hexaToBytes(String str) {
        if (str == null) {
            return null;
        } else {
            byte[] var1 = new byte[str.length() / 2];
            hexaToBytes(str, var1, 0);
            return var1;
        }
    }

    private static byte charToByte(char var0) {
        byte var1;
        if (var0 >= 'a') {
            var1 = (byte)(var0 - 97 + 10);
        } else if (var0 >= 'A') {
            var1 = (byte)(var0 - 65 + 10);
        } else {
            var1 = (byte)(var0 - 48);
        }

        return var1;
    }

    public static int getIterationNumber() {
        return 5555;
    }
}
