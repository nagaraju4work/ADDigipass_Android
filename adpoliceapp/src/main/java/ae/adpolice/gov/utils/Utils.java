package ae.adpolice.gov.utils;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vasco.digipass.sdk.utils.securestorage.SecureStorageSDK;
import com.vasco.digipass.sdk.utils.securestorage.SecureStorageSDKException;
import com.vasco.digipass.sdk.utils.utilities.UtilitiesSDK;

import java.util.Random;

import ae.adpolice.gov.BuildConfig;
import ae.adpolice.gov.Constants;

public class Utils {
    private SecureStorageSDK secureCache;
    private Random random;

    public static void Log(String tag, String msg) {
        if (BuildConfig.DEBUG) {
        }
    }

    private static Utils instance = null;

    private Utils() {
        random = new Random();
    }

    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    public SecureStorageSDK getSecureCache(){
        return secureCache;
    }

    // THE SECURE STORAGE NEEDS TO BE INITIALIZED TO CREATE THE CACHE FOR GET AND PUT
    public void initSecureCache(Context context) {

        // WE DON'T USE DEVICE BINDING HERE !
        byte[] tempDeviceFingerprint = new byte[64];
        random.nextBytes(tempDeviceFingerprint);

        try {
            secureCache = SecureStorageSDK.init("DIGIPASS_SECURE_CACHE",
                    UtilitiesSDK.bytesToHexa(tempDeviceFingerprint), // TEMP DNA OF THE DEVICE
                    Constants.getIterationNumber(), // SLOWING DOWN BRUTE-FORCING
                    context);
        } catch (SecureStorageSDKException e) {
            // IF UNEXPECTED RETURN CODE
            // 1 DISPLAY AN ERROR SCREEN TO THE USER
            // 2 LOG THE RETURN CODE ON THE SERVER SIDE
            Log("SECURE_STORAGE", "ERROR ON SECURE STORAGE INIT. CODE " + e.getErrorCode() + " MESSAGE " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    public String getStringFromSecureCache(String key) {
        try {
            return secureCache.getString(key);
        } catch (SecureStorageSDKException e) {
            // IF UNEXPECTED RETURN CODE
            // 1 DISPLAY AN ERROR SCREEN TO THE USER
            // 2 LOG THE RETURN CODE ON THE SERVER SIDE
            Log("SECURE_STORAGE", "ERROR ON SECURE STORAGE GET STRING. CODE" + e.getErrorCode() + " MESSAGE " + e.getMessage());
            Crashlytics.logException(e);
        }
        return "";
    }

    public void putStringInSecureCache(String key, String toPut) {
        try {
            secureCache.putString(key, toPut);
        } catch (SecureStorageSDKException e) {
            // IF UNEXPECTED RETURN CODE
            // 1 DISPLAY AN ERROR SCREEN TO THE USER
            // 2 LOG THE RETURN CODE ON THE SERVER SIDE
            Log("SECURE_STORAGE", "ERROR ON SECURE STORAGE PUT. CODE" + e.getErrorCode() + " MESSAGE " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    public boolean containsKey(String key) {
        try {
            return secureCache.contains(key);
        } catch (SecureStorageSDKException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeKey(String key) {
        try {
            if (secureCache.contains(key))
                secureCache.remove(key);
        } catch (SecureStorageSDKException e) {
            e.printStackTrace();
        }
    }

    public String[] getActivationPasswords() {
        String cred = getStringFromSecureCache(Constants.CREDENTIALS_KEY);
        String[] activationPasswords = new String[2];
        activationPasswords[0] = cred.split(",")[2];
        activationPasswords[1] = cred.split(",")[4];
        return activationPasswords;
    }

    public String[] getRegistrationIdentifiers() {
        String cred = getStringFromSecureCache(Constants.CREDENTIALS_KEY);
        String[] registrationIdentifiers = new String[2];
        registrationIdentifiers[0] = cred.split(",")[1];
        registrationIdentifiers[1] = cred.split(",")[3];
        return registrationIdentifiers;
    }

    String getUserId() {
        String cred = getStringFromSecureCache(Constants.CREDENTIALS_KEY);
        return cred.split(",")[0];
    }

}
