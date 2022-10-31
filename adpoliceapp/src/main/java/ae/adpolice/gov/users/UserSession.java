package ae.adpolice.gov.users;

import static ae.adpolice.gov.Constants.BIOMETRIC_STATUS;
import static ae.adpolice.gov.Constants.CLIENT_SERVER_TIME_SHIFT_KEY;
import static ae.adpolice.gov.Constants.getIterationNumber;

import android.annotation.SuppressLint;
import android.content.Context;

import com.vasco.digipass.sdk.utils.securestorage.SecureStorageSDK;
import com.vasco.digipass.sdk.utils.securestorage.SecureStorageSDKException;

import java.util.List;

import ae.adpolice.gov.Constants;
import ae.adpolice.gov.users.pojo.User;
import ae.adpolice.gov.utils.Crashlytics;


public class UserSession {

    private final Context mContext;
    private SecureStorageSDK secureStorage;
    @SuppressLint("StaticFieldLeak")
    private static UserSession instance = null;

    private UserSession(Context context) {
        this.mContext = context;
        init(context.getApplicationContext());
    }

    public void init(Context context) {
        try {
            if (Constants.isSaltStorageEmpty()) {
                Constants.initializeSalts();
            }
            secureStorage = SecureStorageSDK.init("digipass", Constants.getDevicePlatformFingerprintForStorage(context), getIterationNumber(), context);
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
    }

    //TODO permanent secure storage with getter and setter

    public static UserSession getInstance(Context mContext) {
        if (instance == null) {
            instance = new UserSession(mContext);
        }
        return instance;
    }

    public SecureStorageSDK getSecureStorage() {
        return secureStorage;
    }

    public byte[] getStaticVector(String userId) {
        try {
            return secureStorage.getBytes(Constants.getStaticVectorKey(userId));
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
        return null;
    }

    public byte[] getDynamicVectorPin(String userId) {
        try {
            return secureStorage.getBytes(Constants.getDynamicVectorPinKey(userId));
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
        return null;
    }

    public void setStaticVector(String userId, byte[] staticVector) {
        try {
            secureStorage.putBytes(Constants.getStaticVectorKey(userId), staticVector);
            secureStorage.write(Constants.getDevicePlatformFingerprintForStorage(mContext), Constants.getIterationNumber(), mContext);
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
    }

    public void setDynamicVectorPin(String userId, byte[] dynamicVector) {
        try {
            secureStorage.putBytes(Constants.getDynamicVectorPinKey(userId), dynamicVector);
            secureStorage.write(Constants.getDevicePlatformFingerprintForStorage(mContext), Constants.getIterationNumber(), mContext);
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
    }

    public void setDynamicVectorBio(String userId, byte[] dynamicVector) {
        try {
            secureStorage.putBytes(Constants.getDynamicVectorBioKey(userId), dynamicVector);
            secureStorage.write(Constants.getDevicePlatformFingerprintForStorage(mContext), Constants.getIterationNumber(), mContext);
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
    }

    public long getClientServerTimeShift() {
        try {
            return Long.parseLong(secureStorage.getString(CLIENT_SERVER_TIME_SHIFT_KEY));
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
        return 0;
    }

    public void setClientServerTimeShift(long clientServerTimeShift) {
        try {
            secureStorage.putString(Constants.CLIENT_SERVER_TIME_SHIFT_KEY, Long.toString(clientServerTimeShift));
            secureStorage.write(Constants.getDevicePlatformFingerprintForStorage(mContext), Constants.getIterationNumber(), mContext);
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
    }

    public boolean hasBiometricChanged() {
        try {
            if (!secureStorage.contains(BIOMETRIC_STATUS)) {
                return false;
            }
            return Boolean.parseBoolean(secureStorage.getString(BIOMETRIC_STATUS));
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
        return true;
    }

    public void setBiometricChanged(boolean biometricStatus) {
        try {
            secureStorage.putString(BIOMETRIC_STATUS, Boolean.toString(biometricStatus));
            secureStorage.write(Constants.getDevicePlatformFingerprintForStorage(mContext), Constants.getIterationNumber(), mContext);
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
    }

    public User getCurrentUser() {
        return DatabaseClient.getInstance(mContext).getAppDatabase().userDao().getCurrentUser();
    }

    public void setDynamicVector(byte[] dynamicVector) {
        if (getCurrentUser().isAuthenticateChoice()) {
            setDynamicVectorBio(getCurrentUser().getUserId(), dynamicVector);
        } else {
            setDynamicVectorPin(getCurrentUser().getUserId(), dynamicVector);
        }
    }

    public byte[] getDynamicVector() {
        try {
            if (getCurrentUser().isAuthenticateChoice()) {
                return secureStorage.getBytes(Constants.getDynamicVectorBioKey(getCurrentUser().getUserId()));
            } else {
                return secureStorage.getBytes(Constants.getDynamicVectorPinKey(getCurrentUser().getUserId()));
            }
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }
        return null;
    }


    public void setAuthenticateChoice(boolean authenticateChoice) {
        DatabaseClient.getInstance(mContext).getAppDatabase().userDao().updateAuthenticateChoice(getCurrentUser().getUserId(), authenticateChoice);
    }

    public void setCurrentUser(User user) {
        if (user == null) {
            return;
        }
        user.setLastModified(System.currentTimeMillis());
        DatabaseClient.getInstance(mContext).getAppDatabase().userDao().update(user);
    }

    public List<User> getUsers() {
        return DatabaseClient.getInstance(mContext).getAppDatabase().userDao().getAll();
    }

    public void removeUser() {

        try {
            secureStorage.remove(Constants.getDynamicVectorPinKey(getCurrentUser().getUserId()));
            secureStorage.remove(Constants.getDynamicVectorBioKey(getCurrentUser().getUserId()));
            secureStorage.remove(Constants.getStaticVectorKey(getCurrentUser().getUserId()));
            secureStorage.write(Constants.getDevicePlatformFingerprintForStorage(mContext), Constants.getIterationNumber(), mContext);
        } catch (SecureStorageSDKException e) {
            Crashlytics.logException(e);
        }

        DatabaseClient.getInstance(mContext).getAppDatabase().userDao().delete(getCurrentUser());
    }
}
