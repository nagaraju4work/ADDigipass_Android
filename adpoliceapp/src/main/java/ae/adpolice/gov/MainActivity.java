package ae.adpolice.gov;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.vasco.digipass.sdk.DigipassSDK;
import com.vasco.digipass.sdk.DigipassSDKConstants;
import com.vasco.digipass.sdk.DigipassSDKReturnCodes;
import com.vasco.digipass.sdk.responses.DigipassPropertiesResponse;
import com.vasco.digipass.sdk.responses.GenerationResponse;
import com.vasco.digipass.sdk.responses.SecureChannelGenerateResponse;
import com.vasco.digipass.sdk.utils.biometricsensor.BiometricSensorSDK;
import com.vasco.digipass.sdk.utils.biometricsensor.BiometricSensorSDKErrorCodes;
import com.vasco.digipass.sdk.utils.biometricsensor.BiometricSensorSDKException;
import com.vasco.digipass.sdk.utils.biometricsensor.BiometricSensorSDKParams;
import com.vasco.digipass.sdk.utils.biometricsensor.BiometricSensorSDKScanListener;
import com.vasco.digipass.sdk.utils.geolocation.GeolocationSDK;
import com.vasco.digipass.sdk.utils.geolocation.GeolocationSDKException;
import com.vasco.digipass.sdk.utils.notification.client.NotificationSDKClient;
import com.vasco.digipass.sdk.utils.notification.client.exceptions.NotificationSDKClientException;
import com.vasco.digipass.sdk.utils.qrcodescanner.QRCodeScannerSDKActivity;
import com.vasco.digipass.sdk.utils.qrcodescanner.QRCodeScannerSDKConstants;
import com.vasco.digipass.sdk.utils.qrcodescanner.QRCodeScannerSDKErrorCodes;
import com.vasco.digipass.sdk.utils.qrcodescanner.QRCodeScannerSDKException;
import com.vasco.digipass.sdk.utils.utilities.UtilitiesSDK;
import com.vasco.dsapp.client.responses.SRPClientEphemeralKeyResponse;

import java.util.List;
import java.util.Objects;

import ae.adpolice.gov.fragments.PinDialogFragment;
import ae.adpolice.gov.network.RetrofitClient;
import ae.adpolice.gov.network.UpdateNotificationRequest;
import ae.adpolice.gov.network.UpdateNotificationResponse;
import ae.adpolice.gov.network.pojo.response.ServerTimeResponse;
import ae.adpolice.gov.users.UserSession;
import ae.adpolice.gov.users.pojo.User;
import ae.adpolice.gov.utils.ActivationCallback;
import ae.adpolice.gov.utils.Crashlytics;
import ae.adpolice.gov.utils.DSAPPUserActivation;
import ae.adpolice.gov.utils.UIUtils;
import ae.adpolice.gov.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener, ActivationCallback {

    private static final String TAG = MainActivity.class.getName();
    private static final int ACCESS_CAMERA_REQUEST_CODE = 1;
    private static final int SECONDS = 30;

    private View layoutActivated, layoutNotActivated;
    private Dialog progressDialog;
    TextView tvOtp;
    ProgressBar progressBar;

    private Spinner userSpinner;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                // Start camera
                startCamera();
            } else {
                Toast.makeText(MainActivity.this, "The permission for accessing the camera is not set",
                        Toast.LENGTH_LONG).show();

            }
        }
    }

    private DrawerLayout drawer;
    private NavigationView navigationView;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.imageView5).setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("platform","Android");
            FirebaseAnalytics.getInstance(MainActivity.this).logEvent("Crash_button_clicked",b);
            throw new RuntimeException("Crashed!");
        });
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(SECONDS);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView tvHeaderTitle = navigationView.getHeaderView(0).findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(R.string.welcom_text);
        TextView tvSubHeaderTitle = navigationView.getHeaderView(0).findViewById(R.id.tvSubHeaderTitle);

        // We have 2 layouts, one when no user is activated, the other when a user is activated
        layoutActivated = findViewById(R.id.layout_activated);
        layoutNotActivated = findViewById(R.id.layout_not_activated);
        layoutActivated.setVisibility(View.INVISIBLE);
        layoutNotActivated.setVisibility(View.INVISIBLE);
        // Initialize spinner for selecting users
        userSpinner = navigationView.getHeaderView(0).findViewById(R.id.spinner_select_user);
        if (UserSession.getInstance(MainActivity.this).getCurrentUser() == null) {
            tvSubHeaderTitle.setText(getString(R.string.hi_guest));
            userSpinner.setVisibility(View.GONE);
            findViewById(R.id.ivDrawer).setVisibility(View.INVISIBLE);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            tvSubHeaderTitle.setText(getString(R.string.select_user));
            userSpinner.setVisibility(View.VISIBLE);
            findViewById(R.id.ivDrawer).setVisibility(View.VISIBLE);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                User selectedUSer = (User) adapterView.getItemAtPosition(position);
                if (selectedUSer != null) {
                    if (adapterView.getChildAt(0) != null)
                        ((TextView) adapterView.getChildAt(0)).setTextColor(0xFFFFFFFF);
                    UserSession.getInstance(MainActivity.this).setCurrentUser(selectedUSer);
                    resetMenuItems();
                    ((TextView) findViewById(R.id.tvUserName)).setText(getString(R.string.hello_user,
                            UserSession.getInstance(MainActivity.this).getCurrentUser().getUserId()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!UserSession.getInstance(MainActivity.this).getCurrentUser().isAuthenticateChoice()) {
                    FragmentManager fm = getSupportFragmentManager();
                    PinDialogFragment editNameDialog = new PinDialogFragment();
                    editNameDialog.setToValidate(true);
                    editNameDialog.setOnPinSetListener(new PinDialogFragment.OnPinSetListener() {
                        @Override
                        public void onPinSet(String pin) {
                            generateOTP(pin);
                        }

                        @Override
                        public void onPinNotMatched() {
                            //Not required
                        }

                        @Override
                        public void onPinSetFailed() {
                            Toast.makeText(MainActivity.this, "PIN_KEY Authentication Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    editNameDialog.setCancelable(false);
                    editNameDialog.show(fm, "fragment_validate");
                    return;
                }
                if (UserSession.getInstance(MainActivity.this).hasBiometricChanged()) {
                    FragmentManager fm = getSupportFragmentManager();
                    PinDialogFragment editNameDialog = new PinDialogFragment();
                    editNameDialog.setToValidate(true);
                    editNameDialog.setDescription(getString(R.string.new_fingerprint_enrolled_description));
                    editNameDialog.setOnPinSetListener(new PinDialogFragment.OnPinSetListener() {
                        @Override
                        public void onPinSet(String pin) {
                            UserSession.getInstance(MainActivity.this).setBiometricChanged(false);
                            onClick(v);
                        }

                        @Override
                        public void onPinNotMatched() {
                            //Not required
                        }

                        @Override
                        public void onPinSetFailed() {
                            Toast.makeText(MainActivity.this, "PIN_KEY Authentication Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    editNameDialog.setCancelable(false);
                    editNameDialog.show(fm, "fragment_validate");
                    return;
                }
                BiometricSensorSDKParams.Builder dialogParamsBuilder = new BiometricSensorSDKParams.Builder();
                try {
                    // Start the fingerprint authentication using dialog.
                    BiometricSensorSDK.verifyUserBiometry(new BiometricSensorSDKScanListener() {
                        @Override
                        public void onBiometryScanFailed(int i, String s) {

                        }

                        @Override
                        public void onBiometryScanError(int i, String s) {

                        }

                        @Override
                        public void onBiometryScanSucceeded() {
                            generateOTP("");
                        }

                        @Override
                        public void onBiometryScanCancelled() {

                        }

                        @Override
                        public void onBiometryNegativeButtonClicked() {

                        }
                    }, MainActivity.this, dialogParamsBuilder.create());
                } catch (BiometricSensorSDKException e) {
                    displayError(e);
                }

            }
        };
        tvOtp = findViewById(R.id.tvOTP);
        tvOtp.setOnClickListener(onClickListener);
        findViewById(R.id.ivRefresh).setOnClickListener(onClickListener);
        findViewById(R.id.ivDrawer).setOnClickListener(v -> {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        registerVascoSDK();
        resetMenuItems();
        RetrofitClient.getOneSpanServices().getServerTime(Constants.getAuthorization())
                .enqueue(new Callback<ServerTimeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ServerTimeResponse> call, @NonNull Response<ServerTimeResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                UserSession.getInstance(MainActivity.this)
                                        .setClientServerTimeShift(DigipassSDK.
                                                computeClientServerTimeShiftFromServerTime(response.body().getResult().getServerTime()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ServerTimeResponse> call, @NonNull Throwable t) {
                        Crashlytics.logException(t);
                    }
                });

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void resetMenuItems() {
        if (UserSession.getInstance(MainActivity.this).getCurrentUser() == null) {
            navigationView.getMenu().findItem(R.id.nav_deavtivate).setEnabled(false);
            navigationView.getMenu().findItem(R.id.nav_bio_sendsor).setEnabled(false);
            navigationView.getMenu().findItem(R.id.nav_change_pin).setEnabled(false);
            navigationView.getMenu().findItem(R.id.nav_pin).setEnabled(false);
            navigationView.getMenu().findItem(R.id.nav_encrol_face).setEnabled(false);
            navigationView.getMenu().findItem(R.id.nav_face_reco).setEnabled(false);
            navigationView.getMenu().findItem(R.id.nav_get_info).setEnabled(false);
        } else {
            navigationView.getMenu().findItem(R.id.nav_deavtivate).setEnabled(true);
            navigationView.getMenu().findItem(R.id.nav_bio_sendsor).setEnabled(true);
            navigationView.getMenu().findItem(R.id.nav_change_pin).setEnabled(true);
            navigationView.getMenu().findItem(R.id.nav_pin).setEnabled(true);
            navigationView.getMenu().findItem(R.id.nav_encrol_face).setEnabled(true);
            navigationView.getMenu().findItem(R.id.nav_face_reco).setEnabled(true);
            navigationView.getMenu().findItem(R.id.nav_get_info).setEnabled(true);
            if (UserSession.getInstance(MainActivity.this).getCurrentUser().isAuthenticateChoice()) {
                navigationView.getMenu().findItem(R.id.nav_pin).setChecked(false);
                try {
                    if (BiometricSensorSDK.isUserBiometrySupportedByPlatform(MainActivity.this) && BiometricSensorSDK.isUserBiometryUsable(MainActivity.this)) {
                        navigationView.getMenu().findItem(R.id.nav_bio_sendsor).setChecked(true);
                    }
                } catch (BiometricSensorSDKException e) {
                    Crashlytics.logException(e);
                    navigationView.getMenu().findItem(R.id.nav_pin).setChecked(true);
                    navigationView.getMenu().findItem(R.id.nav_bio_sendsor).setVisible(false);
                }
            } else {
                navigationView.getMenu().findItem(R.id.nav_pin).setChecked(true);
                try {
                    if (BiometricSensorSDK.isUserBiometrySupportedByPlatform(MainActivity.this) && BiometricSensorSDK.isUserBiometryUsable(MainActivity.this)) {
                        navigationView.getMenu().findItem(R.id.nav_bio_sendsor).setChecked(false);
                    }
                } catch (BiometricSensorSDKException e) {
                    Crashlytics.logException(e);
                    navigationView.getMenu().findItem(R.id.nav_bio_sendsor).setVisible(false);
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_actvate) {
            onStartActivationClicked(findViewById(R.id.ivScan));
        } else if (id == R.id.nav_deavtivate) {
            onDeleteUser();
        } else if (id == R.id.nav_bio_sendsor) {
            UserSession.getInstance(MainActivity.this).setAuthenticateChoice(true);
        } else if (id == R.id.nav_change_pin) {
            progressDialog = UIUtils.displayProgress(this, getString(R.string.dialog_progress_change_pwd));
        } else if (id == R.id.nav_pin) {
            UserSession.getInstance(MainActivity.this).setAuthenticateChoice(false);
        } else if (id == R.id.nav_get_info) {
            FragmentManager fm = getSupportFragmentManager();
            GetInfoFragment getInfoFragment = new GetInfoFragment();
            getInfoFragment.show(fm, "fragment_get_info");
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void registerVascoSDK() {
        FirebaseApp.initializeApp(MainActivity.this);
        final NotificationSDKClient.NotificationSDKClientListener listener = new NotificationSDKClient.NotificationSDKClientListener() {

            public void onRegistrationSuccess(final String vascoNotificationIdentifier) {
                if (UserSession.getInstance(MainActivity.this).getCurrentUser() == null) {
                    return;
                }
                byte[] dynamicVector = UserSession.getInstance(MainActivity.this).getDynamicVector();
                SecureChannelGenerateResponse secureChannelGenerateResponse = DigipassSDK.generateSecureChannelInformationMessage(
                        UserSession.getInstance(MainActivity.this).getStaticVector(UserSession.getInstance(MainActivity.this).getCurrentUser().getUserId()),
                        dynamicVector, vascoNotificationIdentifier,
                        DigipassSDKConstants.SECURE_CHANNEL_MESSAGE_PROTECTION_HMAC_AESCTR, Constants.getDevicePlatformFingerprintForDigipass(MainActivity.this));
                if (secureChannelGenerateResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
                    Toast.makeText(MainActivity.this, DigipassSDK.getMessageForReturnCode(secureChannelGenerateResponse.getReturnCode()), Toast.LENGTH_LONG).show();
                    return;
                }
                Utils.Log("details ", secureChannelGenerateResponse.getSecureChannelMessage().serialNumber);
                DigipassPropertiesResponse digipassPropertiesResponse = DigipassSDK.getDigipassProperties(
                        UserSession.getInstance(MainActivity.this).getStaticVector(UserSession.getInstance(MainActivity.this).getCurrentUser().getUserId()),
                        dynamicVector);
                if (digipassPropertiesResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
                    Toast.makeText(MainActivity.this, DigipassSDK.getMessageForReturnCode(digipassPropertiesResponse.getReturnCode()), Toast.LENGTH_LONG).show();
                    return;
                }
                UpdateNotificationRequest updateNotificationRequest = new UpdateNotificationRequest();
                updateNotificationRequest.setDigipassInstanceID(secureChannelGenerateResponse.getSecureChannelMessage().serialNumber + "-" + digipassPropertiesResponse.getSequenceNumber());
                updateNotificationRequest.setUserID(UserSession.getInstance(MainActivity.this).getCurrentUser().getUserId());
                updateNotificationRequest.setEncryptedNotificationID(secureChannelGenerateResponse.getSecureChannelMessage().rawData);
                updateNotificationRequest.setDomain(BuildConfig.DOMAIN);
                RetrofitClient.getOneSpanServices().updateNotificationID(Constants.getAuthorization(), updateNotificationRequest)
                        .enqueue(new Callback<UpdateNotificationResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<UpdateNotificationResponse> call, @NonNull Response<UpdateNotificationResponse> response) {
                                if (response.isSuccessful()) {
                                    if (response.body() != null) {
                                        if (response.body().getResultCodes().getReturnCodeEnum().equals("RET_SUCCESS")) {
                                            Toast.makeText(MainActivity.this, "Notification ID Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<UpdateNotificationResponse> call, @NonNull Throwable t) {
                                Crashlytics.logException(t);
                            }
                        });
            }

            public void onException(NotificationSDKClientException e) {
                final String errorMsg = "NotificationSDKClientException error code: " + e.getErrorCode();
                Utils.Log(TAG, errorMsg);
                Crashlytics.logException(e);
            }
        };
        // Register to Firebase Cloud Messaging
        // and retrieving the VASCO Notification Identifier that must be provided
        // to the NotificationSDKServer library to send a notification to this particular device.
        NotificationSDKClient.registerNotificationService(this, listener);

    }

    private void closeProgressDialogIfOpenned() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        findViewById(R.id.ivRefresh).setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.ivRefresh).setVisibility(View.VISIBLE);

        // No activated user, show corresponding text & button
        User activatedUser = UserSession.getInstance(MainActivity.this).getCurrentUser();
        if (activatedUser == null) {
            layoutActivated.setVisibility(View.GONE);
            layoutNotActivated.setVisibility(View.VISIBLE);
            return;
        }
        if (UserSession.getInstance(MainActivity.this).getUsers().size() != 0) {
            ((TextView) findViewById(R.id.tvUserName)).setText(getString(R.string.hello_user,
                    UserSession.getInstance(MainActivity.this).getCurrentUser().getUserId()));
        }
        // Update user selection spinner
        List<User> users = UserSession.getInstance(MainActivity.this).getUsers();
        ArrayAdapter<User> userAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, users);
        userSpinner.setAdapter(userAdapter);
        userSpinner.setSelection(((ArrayAdapter<User>) userSpinner.getAdapter()).getPosition(activatedUser));

        // We have an activated user, show proper layout
        layoutActivated.setVisibility(View.VISIBLE);
        layoutNotActivated.setVisibility(View.GONE);

        // Start registering for notifications
        //registerForNotifications();
    }


    private void startRegistration() {

        progressDialog = UIUtils.displayProgress(this, getString(R.string.dialog_progress_activating));
        DSAPPUserActivation.getInstance(MainActivity.this).setActivationCallback(this);
        // Example of use of validateSRPUserPasswordChecksum
        DSAPPUserActivation.getInstance(MainActivity.this).setIteration(0);
        DSAPPUserActivation.getInstance(MainActivity.this).validateSRPUserPasswordChecksum(Utils.getInstance().getActivationPasswords()[0]);

        // Example of use of generateSRPClientEphemeralKey
        SRPClientEphemeralKeyResponse srpClientEphemeralKeyResponse = DSAPPUserActivation.getInstance(MainActivity.this).generateSRPClientEphemeralKey();
        if (srpClientEphemeralKeyResponse != null) {
            // Example of use of generateSRPSessionKey
            DSAPPUserActivation.getInstance(MainActivity.this).generateSRPSessionKey(srpClientEphemeralKeyResponse, Utils.getInstance().getActivationPasswords()[0]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //orchestrator.getLifecycleObserver().activityPaused();
        if (!tvOtp.getText().toString().equals("")) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            progressBar.setProgress(100);
            tvOtp.setText("");
            findViewById(R.id.ivRefresh).setVisibility(View.VISIBLE);
            findViewById(R.id.ivDrawer).setVisibility(View.VISIBLE);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void onStartActivationClicked(View view) {
        try {
            if (!GeolocationSDK.isLocationServiceEnabled(MainActivity.this)) {
                buildAlertMessageNoGps();
                return;
            }
        } catch (GeolocationSDKException e) {
            Crashlytics.logException(e);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // Permission was already denied.
                // Display a clear explanation about why this permission is important for the app to work
                // Then, request the permission again
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission request");
                builder.setMessage("The requested permission is used to scan codes.");
                builder.setOnCancelListener(dialog -> {
                    // Request permission for the CAMERA permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            ACCESS_CAMERA_REQUEST_CODE);
                });
                builder.create().show();
            } else {
                // Request permission for the CAMERA permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        ACCESS_CAMERA_REQUEST_CODE);
            }
        } else {
            // Permission was already granted, start Camera
            startCamera();
        }

    }

    private void generateOTP(String pin) {
        Dialog otpDialog = UIUtils.displayProgress(MainActivity.this, "Generating OTP");
        GenerationResponse generateResponse;
        byte[] dynamicVector = UserSession.getInstance(MainActivity.this).getDynamicVector();
        generateResponse = DigipassSDK.generateResponseOnly(
                UserSession.getInstance(MainActivity.this).getStaticVector(UserSession.getInstance(MainActivity.this).getCurrentUser().getUserId()),
                dynamicVector, pin, UserSession.getInstance(MainActivity.this).getClientServerTimeShift(),
                DigipassSDKConstants.CRYPTO_APPLICATION_INDEX_APP_2, Constants.getDevicePlatformFingerprintForDigipass(MainActivity.this));
        UserSession.getInstance(MainActivity.this).setDynamicVector(generateResponse.getDynamicVector());
        if (generateResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
            Toast.makeText(this, "OTP Generation Failed. " + DigipassSDK.getMessageForReturnCode(generateResponse.getReturnCode()), Toast.LENGTH_SHORT).show();
            Utils.Log("The password generation has FAILED ", "[  generateResponse.getReturnCode() : "
                    + DigipassSDK.getMessageForReturnCode(generateResponse.getReturnCode()) + " ]");
        } else {
            Utils.Log("OTP generated: ", "The password generation has SUCCEEDED");
            Utils.Log("OTP generated: ", "" + generateResponse.getResponse());
            closeProgressDialogIfOpenned();
            tvOtp.setText(generateResponse.getResponse());
            findViewById(R.id.ivRefresh).setVisibility(View.INVISIBLE);
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            countDownTimer = new CountDownTimer(SECONDS * 1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    progressBar.setProgress((int) (millisUntilFinished / 1000));
                    findViewById(R.id.ivRefresh).setVisibility(View.INVISIBLE);
                    findViewById(R.id.ivDrawer).setVisibility(View.INVISIBLE);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }

                public void onFinish() {
                    progressBar.setProgress(100);
                    tvOtp.setText("");
                    findViewById(R.id.ivRefresh).setVisibility(View.VISIBLE);
                    findViewById(R.id.ivDrawer).setVisibility(View.VISIBLE);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }

            }.start();
        }
        UIUtils.hideProgress(otpDialog);
    }


    private void onDeleteUser() {

        DialogInterface.OnClickListener deleteListener = (dialog, id) -> onDeleteUserInternal();

        UIUtils.displayAlertWithAction(MainActivity.this,
                getString(R.string.confirm_delete_user_title),
                getString(R.string.confirm_delete_user_message) + " " + UserSession.getInstance(MainActivity.this).getCurrentUser() + "?",
                getString(R.string.confirm_delete_user_yes),
                getString(R.string.confirm_delete_user_no),
                deleteListener,
                null);
    }

    private void onDeleteUserInternal() {
        // Delete current user
        // OrchestrationUser orchestrationUser = new OrchestrationUser(storage.getCurrentUser());
        UserSession.getInstance(MainActivity.this).removeUser();

        // Back on activation page if there are no more users
        List<User> users = UserSession.getInstance(MainActivity.this).getUsers();
        if (users.size() == 0) {
            layoutActivated.setVisibility(View.GONE);
            layoutNotActivated.setVisibility(View.VISIBLE);
            UserSession.getInstance(MainActivity.this).setCurrentUser(null);
        }
//        // Back to calling activity
        setResult(Activity.RESULT_OK);
        Intent i = new Intent(MainActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void startCamera() {
        // Instantiate intent to start scanning activity
        Intent intent = new Intent(this, QRCodeScannerSDKActivity.class);

        // We want a vibration feedback after scanning
        // Note that the vibration feedback is activated by default
        intent.putExtra(QRCodeScannerSDKConstants.EXTRA_VIBRATE, true);

        // indicate which sort of image we want to scan
        intent.putExtra(QRCodeScannerSDKConstants.EXTRA_CODE_TYPE,
                QRCodeScannerSDKConstants.QR_CODE
                        + QRCodeScannerSDKConstants.CRONTO_CODE);

        // Enable the scanner overlay to facilitate scanning.
        intent.putExtra(QRCodeScannerSDKConstants.EXTRA_SCANNER_OVERLAY, true);

        // Launch QR Code Scanner activity
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Display the result
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {

            case RESULT_OK:


                // The result is returned as an extra
                String result = data.getStringExtra(QRCodeScannerSDKConstants.OUTPUT_RESULT);

                int codeType = data.getIntExtra(QRCodeScannerSDKConstants.OUTPUT_CODE_TYPE, 0);

                // Convert the result
                if (codeType == QRCodeScannerSDKConstants.CRONTO_CODE) {
                    // we have scan a cronto code => convert the hexa string to string
                    byte[] tmp = UtilitiesSDK.hexaToBytes(result);

                    String credentials = new String(tmp);
                    String[] values = credentials.split(Utils.STRING_SPLIT_CHARACTER);
                    if (values.length <= 1) {
                        values = credentials.split(Utils.STRING_ALTERNATE_SPLIT_CHARACTER);
                        if (values.length <= 1) {
                            Toast.makeText(this, "Registration process is failed due to wrong Cronto input code.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Utils.getInstance().putStringInSecureCache(Constants.CREDENTIALS_KEY, credentials);
                    startRegistration();

                } else if (codeType == QRCodeScannerSDKConstants.QR_CODE) {
                    // we have scan a QR code => display directly the result

                    Toast.makeText(MainActivity.this, "You have scanned a QR Code image",
                            Toast.LENGTH_LONG).show();

                }

                break;

            case RESULT_CANCELED:


                // Scanning has been cancelled
                Toast.makeText(MainActivity.this, "Scanning cancelled",
                        Toast.LENGTH_LONG).show();

                break;

            case QRCodeScannerSDKConstants.RESULT_ERROR:


                // Get returned exception
                QRCodeScannerSDKException exception = (QRCodeScannerSDKException) data
                        .getSerializableExtra(QRCodeScannerSDKConstants.OUTPUT_EXCEPTION);

                // Show error message
                switch (exception.getErrorCode()) {
                    case QRCodeScannerSDKErrorCodes.CAMERA_NOT_AVAILABLE:
                        Toast.makeText(MainActivity.this, "Camera is not available on this device",
                                Toast.LENGTH_LONG).show();
                        break;
                    case QRCodeScannerSDKErrorCodes.PERMISSION_DENIED:
                        Toast.makeText(MainActivity.this, "The permission for accessing the camera is not set",
                                Toast.LENGTH_LONG).show();
                        break;
                    case QRCodeScannerSDKErrorCodes.NATIVE_LIBRARY_NOT_LOADED:
                        Toast.makeText(MainActivity.this, "The native library cannot be loaded",
                                Toast.LENGTH_LONG).show();
                        break;
                    case QRCodeScannerSDKErrorCodes.INTERNAL_ERROR:
                        Toast.makeText(MainActivity.this, "An internal error occurred during QRCode scanning: "
                                        + Objects.requireNonNull(exception.getCause()).getMessage(),
                                Toast.LENGTH_LONG).show();
                        break;

                }
                break;
        }

    }



    private CountDownTimer countDownTimer;

    private void displayError(BiometricSensorSDKException e) {

        Crashlytics.logException(e);
        // Display error
        switch (e.getErrorCode()) {

            case BiometricSensorSDKErrorCodes.INTERNAL_ERROR:
                Utils.Log(TAG, "Internal error " + e.getLocalizedMessage());
                Toast.makeText(MainActivity.this, getResources().getString(R.string.internal_error), Toast.LENGTH_LONG).show();
                break;

            case BiometricSensorSDKErrorCodes.ACTIVITY_NULL:
                Utils.Log(TAG, "The activity is null" + e.getLocalizedMessage());
                Toast.makeText(MainActivity.this, getResources().getString(R.string.activity_null), Toast.LENGTH_LONG).show();
                break;

            case BiometricSensorSDKErrorCodes.LISTENER_NULL:
                Utils.Log(TAG, "The biometric listener is null" + e.getLocalizedMessage());
                Toast.makeText(MainActivity.this, getResources().getString(R.string.biometric_listener_null),
                        Toast.LENGTH_LONG).show();
                break;

            case BiometricSensorSDKErrorCodes.NO_BIOMETRY_ENROLLED:
                Utils.Log(TAG, "Fingerprint is not supported by the device or there is no registered fingerprint " + e.getLocalizedMessage());
                Toast.makeText(MainActivity.this,
                        getResources().getString(R.string.fingerprint_not_usable),
                        Toast.LENGTH_LONG).show();
                break;
            default:
                Utils.Log(TAG, "Unknown error " + e.getLocalizedMessage());
                Toast.makeText(MainActivity.this,
                        getResources().getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                break;
        }
    }


    @Override
    public void onActivationSuccess() {
        // Show progress dialog & start activation
        if (Utils.getInstance().getActivationPasswords().length > 1 && DSAPPUserActivation.getInstance(MainActivity.this).getIteration() == 0) {
            // Example of use of validateSRPUserPasswordChecksum
            DSAPPUserActivation.getInstance(MainActivity.this).setIteration(1);
            DSAPPUserActivation.getInstance(MainActivity.this).validateSRPUserPasswordChecksum(Utils.getInstance().getActivationPasswords()[1]);

            // use of generateSRPClientEphemeralKey
            SRPClientEphemeralKeyResponse srpClientEphemeralKeyResponse1 = DSAPPUserActivation.getInstance(MainActivity.this).generateSRPClientEphemeralKey();
            if (srpClientEphemeralKeyResponse1 != null) {
                // use of generateSRPSessionKey
                DSAPPUserActivation.getInstance(MainActivity.this).generateSRPSessionKey(srpClientEphemeralKeyResponse1, Utils.getInstance().getActivationPasswords()[1]);
            }
            return;
        }
        if (DSAPPUserActivation.getInstance(MainActivity.this).getIteration() == (Utils.getInstance().getRegistrationIdentifiers().length - 1)) {
            UIUtils.hideProgress(progressDialog);
            setResult(RESULT_OK);
            Intent i = new Intent(MainActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

    }

    @Override
    public void onActivationFailure(String message) {
        UIUtils.hideProgress(progressDialog);
        Utils.Log("OnActivationFailure mainactivity", "Failed due to " + message);
        Toast.makeText(this, "Activation Failed. " + message, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        Intent i = new Intent(MainActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
