package ae.adpolice.gov;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.vasco.digipass.sdk.utils.geolocation.GeolocationSDK;
import com.vasco.digipass.sdk.utils.geolocation.GeolocationSDKArea;
import com.vasco.digipass.sdk.utils.geolocation.GeolocationSDKErrorCodes;
import com.vasco.digipass.sdk.utils.geolocation.GeolocationSDKException;
import com.vasco.digipass.sdk.utils.geolocation.GeolocationSDKLocation;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import ae.adpolice.gov.fragments.PinDialogFragment;
import ae.adpolice.gov.users.UserSession;
import ae.adpolice.gov.utils.UIUtils;
import ae.adpolice.gov.utils.Utils;
import io.fabric.sdk.android.Fabric;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        Fabric.with(this, new Crashlytics());
        Constants.initializeSalts();
        Utils.getInstance().initSecureCache(SplashActivity.this);
        initCreateKeys();
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected = false;
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            isConnected = networkInfo != null && networkInfo.isConnected();
        }
        if (UserSession.getInstance(SplashActivity.this).getCurrentUser() == null && !isConnected) {
            UIUtils.displayAlert(SplashActivity.this, "No Internet", "Internet connectivity not exists to activate the Device. Please try later.", "Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SplashActivity.this.finish();
                }
            });
            return;
        }
        try {
            if (!GeolocationSDK.isLocationServiceEnabled(SplashActivity.this)) {
                buildAlertMessageNoGps();
                return;
            }
        } catch (GeolocationSDKException e) {
            Crashlytics.logException(e);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Permission was already denied.
                // Display a clear explanation about why this permission is important for the app to work
                // Then, request the permission again
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission request");
                builder.setMessage("The requested permission is used to retrieve the device's position.");
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Request permission for the ACCESS_FINE_LOCATION permission
                        ActivityCompat.requestPermissions(SplashActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                ACCESS_FINE_LOCATION_REQUEST_CODE);
                    }
                });
                builder.create().show();
            } else {
                // Request permission for the ACCESS_FINE_LOCATION permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCESS_FINE_LOCATION_REQUEST_CODE);
            }
        } else {
            launchGeolocationThread();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    //GeoFencing code below

    private final String TAG = "GeolocationSDKSample";

    /**
     * Constant for retrieving the permission result
     */
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 1;

    /**
     * North hemisphere.
     */
    private static final GeolocationSDKArea NORTH_HEMISPHERE_AREA = new GeolocationSDKArea(0, -180, 90, 180);
   // private static final GeolocationSDKArea UAE_HEMISPHERE_AREA = new GeolocationSDKArea(24.295259, 54.235676, 24.610394, 54.283631);
    //    /**
    //     * South hemisphere.
    //     */
    //   private static final GeolocationSDKArea SOUTH_HEMISPHERE_AREA = new GeolocationSDKArea(-90, -180, 0, 180);

    /**
     * Used to prevent deadlock during location computation.
     * Set to 30 seconds for this sample.
     */
    private static final int TIMEOUT = 30;

    /**
     * Progress bar displayed while retrieving location.
     */
    private ProgressDialog progressDialog;

    /**
     * Alert dialog displayed if error occured.
     */
    private AlertDialog alertDialog;

    /**
     * Indicates if the getLocation method is running.
     */
    private boolean isGettingLocation = false;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Permission was granted.
                // Start geolocation.
                launchGeolocationThread();
            } else {
                // Permission was denied, act in consequence.
                // In our case, we display a toast message.
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
            return;
        }
    }

    private void launchGeolocationThread() {
        // Retrieves the location.
        // As the getLocation function is executing in an external thread, you should not call it twice.
        if (!isGettingLocation) {
            isGettingLocation = true;
            clearScreen();

            // Permission was already granted, start geolocation
            displayProgressBar();
            geolocationThread.start();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    /**
     * Clears screen.
     * Remove alert dialog, progress bar, and any text.
     */
    private void clearScreen() {
        closeAlertDialog();
        closeProgressBar();

    }

    /**
     * Displays a progress bar while retrieving location.
     */
    private void displayProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(SplashActivity.this, null,
                            "Retrieving location...", true, false);
                }
            }
        });
    }

    /**
     * Closes the progress bar.
     */
    private void closeProgressBar() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * Closes the alert dialog.
     */
    private void closeAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    /**
     * Process the error, and display the error message in an alert dialog.
     *
     * @param e The exception
     */
    private void processError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String errorText;
                Crashlytics.logException(e);
                if (e instanceof GeolocationSDKException) {
                    int errorCode = ((GeolocationSDKException) e).getErrorCode();
                    switch (errorCode) {
                        case GeolocationSDKErrorCodes.LOCATION_UNAVAILABLE:
                            Utils.Log(TAG, "Location unavailable");
                            errorText = "You must grant access to the geolocation service to use this application";
                            break;
                        case GeolocationSDKErrorCodes.INTERNAL_ERROR:
                            Utils.Log(TAG, "Internal error " + e.getLocalizedMessage());
                            errorText = "Internal error";
                            break;
                        case GeolocationSDKErrorCodes.PERMISSION_DENIED:
                            Utils.Log(TAG, "Permission denied");
                            errorText = "Permission denied";
                            break;
                        case GeolocationSDKErrorCodes.LOCATION_TIMEOUT:
                            Utils.Log(TAG, "Timeout");
                            errorText = "Timeout during geolocation";
                            break;
                        default:
                            Utils.Log(TAG, "Other error " + e.getLocalizedMessage());
                            errorText = "Error " + errorCode;
                            break;
                    }
                } else {
                    Utils.Log(TAG, "Unexpected error " + e.getLocalizedMessage());
                    errorText = e.getMessage();
                    Crashlytics.logException(e);
                }

                // Build the alert dialog that will display the error message.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashActivity.this);
                alertDialog = alertDialogBuilder.setTitle("Error").setMessage(errorText).setCancelable(true).create();
                alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Close the application if cancel is pressed on the alert dialog.
                        finish();
                    }
                });
                alertDialog.show();
            }
        });
    }

    /**
     * Display the result.
     *
     * @param location     The location to display
     * @param isAuthorized True if the location is in the North hemisphere
     */
    private void displayResult(final boolean isServiceAvailable, final GeolocationSDKLocation location,
                               final boolean isAuthorized) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isServiceAvailable) {
                    if (isAuthorized) {
                        if (initCipher(defaultCipher)) {
                            Intent i = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(i);
                            SplashActivity.this.finish();
                        } else {
                            PinDialogFragment pinDialogFragment = new PinDialogFragment();
                            pinDialogFragment.setToValidate(true);
                            pinDialogFragment.setDescription(getString(R.string.new_fingerprint_enrolled_description));
                            pinDialogFragment.setOnPinSetListener(new PinDialogFragment.OnPinSetListener() {
                                @Override
                                public void onPinSet(String pin) {
                                    try {
                                        mKeyStore.deleteEntry(DEFAULT_KEY_NAME);
                                    } catch (KeyStoreException e) {
                                        Crashlytics.logException(e);
                                    }
                                    createKey(DEFAULT_KEY_NAME, true);
                                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(i);
                                    SplashActivity.this.finish();
                                }

                                @Override
                                public void onPinNotMatched() {
                                    //TODO store the value in the secure storage
                                    UserSession.getInstance(SplashActivity.this).setBiometricChanged(true);
                                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(i);
                                    SplashActivity.this.finish();
                                }

                                @Override
                                public void onPinSetFailed() {
                                    UserSession.getInstance(SplashActivity.this).setBiometricChanged(true);
                                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(i);
                                    SplashActivity.this.finish();
                                }
                            });
                            pinDialogFragment.show(getSupportFragmentManager(), "New Fingerprint detected");
                        }
                    } else {
                        UIUtils.displayAlert(SplashActivity.this, "Unauthorized Zone",
                                "You are not in an authorized zone to use the " + getString(R.string.app_name) + ". Please try using in Authorized Area. Thank you", "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SplashActivity.this.finish();
                                    }
                                });
                    }
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashActivity.this);
                    alertDialogBuilder.setTitle("Location Not Found");
                    alertDialogBuilder.setMessage("Device Location still not found. Please restart the application or retry.");
                    alertDialogBuilder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            launchGeolocationThread();
                        }
                    });
                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SplashActivity.this.finish();
                        }
                    });
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.create().show();
                }
            }
        });
    }

    private Thread geolocationThread = new Thread(new Runnable() {

        @Override
        public void run() {
            GeolocationSDKLocation location = new GeolocationSDKLocation(0, 0, 0, 0);
            boolean isAuthorized = false;
            boolean isServiceEnabled = false;
            try {
                // Check service availablility
                if (isServiceEnabled = GeolocationSDK.isLocationServiceEnabled(SplashActivity.this)) {
                    // Get the geolocation
                    location = GeolocationSDK.getLocation(TIMEOUT, SplashActivity.this, 100);

                    // Authorized zone = North hemisphere
                    List<GeolocationSDKArea> areaList = new ArrayList<GeolocationSDKArea>();
                    areaList.add(NORTH_HEMISPHERE_AREA);

                    // Check if the location is in the authorized area
                    isAuthorized = GeolocationSDK.isLocationAuthorized(location, areaList);
                }
            } catch (final Exception e) {
                processError(e);
                Crashlytics.logException(e);
                return;
            } finally {
                closeProgressBar();
                isGettingLocation = false;
            }

            displayResult(isServiceEnabled, location, isAuthorized);

        }
    });


    //FingerPrint Invalidation
    private static final String DEFAULT_KEY_NAME = "digipass_key";

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;

    private void initCreateKeys() {

        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            Crashlytics.logException(e);
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }
        try {
            mKeyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            Crashlytics.logException(e);
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }
        try {
            defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Crashlytics.logException(e);
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }
        Key key = null;
        try {
            key = mKeyStore.getKey(DEFAULT_KEY_NAME, null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            Crashlytics.logException(e);
        }
        if (key == null)
            createKey(DEFAULT_KEY_NAME, true);
    }

    private Cipher defaultCipher;

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName                          the name of the key to be created
     * @param invalidatedByBiometricEnrollment if {@code false} is passed, the created key will not
     *                                         be invalidated even if a new fingerprint is enrolled.
     *                                         The default value is {@code true}, so passing
     *                                         {@code true} doesn't change the behavior
     *                                         (the key will be invalidated if a new fingerprint is
     *                                         enrolled.). Note that this parameter is only valid if
     *                                         the app works on Android N developer preview.
     */
    private void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            try {
                Key key = mKeyStore.getKey(DEFAULT_KEY_NAME, null);
                if (key != null) {
                    return;
                }
            } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
                Crashlytics.logException(e);
            }
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            Crashlytics.logException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the {@link Cipher} instance with the created key in the
     * {@link #createKey(String, boolean)} method.
     *
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    private boolean initCipher(Cipher cipher) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(SplashActivity.DEFAULT_KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            Crashlytics.logException(e);
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            Crashlytics.logException(e);
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }


}
