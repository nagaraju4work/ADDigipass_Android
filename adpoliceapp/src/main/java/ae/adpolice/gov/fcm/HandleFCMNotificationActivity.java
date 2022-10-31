package ae.adpolice.gov.fcm;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.vasco.digipass.sdk.DigipassSDK;
import com.vasco.digipass.sdk.DigipassSDKConstants;
import com.vasco.digipass.sdk.DigipassSDKReturnCodes;
import com.vasco.digipass.sdk.models.SecureChannelMessage;
import com.vasco.digipass.sdk.responses.DigipassPropertiesResponse;
import com.vasco.digipass.sdk.responses.GenerationResponse;
import com.vasco.digipass.sdk.responses.SecureChannelDecryptionResponse;
import com.vasco.digipass.sdk.responses.SecureChannelGenerateResponse;
import com.vasco.digipass.sdk.responses.SecureChannelParseResponse;
import com.vasco.digipass.sdk.utils.biometricsensor.BiometricSensorSDK;
import com.vasco.digipass.sdk.utils.biometricsensor.BiometricSensorSDKException;
import com.vasco.digipass.sdk.utils.biometricsensor.BiometricSensorSDKParams;
import com.vasco.digipass.sdk.utils.biometricsensor.BiometricSensorSDKScanListener;
import com.vasco.digipass.sdk.utils.notification.client.NotificationSDKClient;
import com.vasco.digipass.sdk.utils.notification.client.exceptions.NotificationSDKClientException;
import com.vasco.digipass.sdk.utils.utilities.UtilitiesSDK;

import ae.adpolice.gov.Constants;
import ae.adpolice.gov.MainActivity;
import ae.adpolice.gov.R;
import ae.adpolice.gov.fragments.PinDialogFragment;
import ae.adpolice.gov.network.RetrofitClient;
import ae.adpolice.gov.network.pojo.request.AuthUserRequest;
import ae.adpolice.gov.network.pojo.request.CancelAuthUserRequest;
import ae.adpolice.gov.network.pojo.request.PrepareSecureChallengeRequest;
import ae.adpolice.gov.network.pojo.response.AuthUserResponse;
import ae.adpolice.gov.network.pojo.response.CancelAuthUserResponse;
import ae.adpolice.gov.network.pojo.response.PrepareSecureChallengeResponse;
import ae.adpolice.gov.users.UserSession;
import ae.adpolice.gov.utils.UIUtils;
import ae.adpolice.gov.utils.Utils;
import ae.adpolice.gov.utils.Crashlytics;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HandleFCMNotificationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = HandleFCMNotificationActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_fcmnotfication);
        findViewById(R.id.btnReject).setOnClickListener(this);
        findViewById(R.id.btnApproval).setOnClickListener(this);
        // Handle possible incoming notification
        displayNotificationContent(getIntent());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnReject) {
            askAuthentication("NOK");
        } else if (id == R.id.btnApproval) {
            askAuthentication("OK");
        }
    }

    private void askAuthentication(final String consent) {
        if (!UserSession.getInstance(HandleFCMNotificationActivity.this).getCurrentUser().isAuthenticateChoice()) {
            FragmentManager fm = getSupportFragmentManager();
            PinDialogFragment editNameDialog = new PinDialogFragment();
            editNameDialog.setToValidate(true);
            editNameDialog.setOnPinSetListener(new PinDialogFragment.OnPinSetListener() {
                @Override
                public void onPinSet(String pin) {
                    provideDecisionToServer(consent, pin);
                }

                @Override
                public void onPinNotMatched() {
                    //Not required
                }

                @Override
                public void onPinSetFailed() {
                    Toast.makeText(HandleFCMNotificationActivity.this, "PIN_KEY Authentication Failed", Toast.LENGTH_SHORT).show();
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
                    provideDecisionToServer(consent, "");
                }

                @Override
                public void onBiometryScanCancelled() {

                }

                @Override
                public void onBiometryNegativeButtonClicked() {

                }
            }, HandleFCMNotificationActivity.this, dialogParamsBuilder.create());
        } catch (BiometricSensorSDKException e) {
            Crashlytics.logException(e);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        // Stop the current biometric authentication and dismiss the prompt if one is launched.
        BiometricSensorSDK.stopUserBiometryVerification();
    }


    private void openMainScreen() {
        if (dialog != null)
            UIUtils.hideProgress(dialog);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        HandleFCMNotificationActivity.this.finish();
    }

    private Dialog dialog;

    private void provideDecisionToServer(final String yesNo, final String pin) {
        dialog = UIUtils.displayProgress(HandleFCMNotificationActivity.this, "Submitting response, Please wait");
        if (UserSession.getInstance(HandleFCMNotificationActivity.this).getSecureStorage() == null) {
            UserSession.getInstance(HandleFCMNotificationActivity.this).init(getApplicationContext());
        }
        final byte[] dynamicVector =
                UserSession.getInstance(HandleFCMNotificationActivity.this).getDynamicVector();
        SecureChannelParseResponse secureChannelParseResponse = DigipassSDK.parseSecureChannelMessage(Utils.getInstance().getStringFromSecureCache(Constants.NOTIFICATION_CONTENT_KEY));
        if (secureChannelParseResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
            Toast.makeText(this, "" + DigipassSDK.getMessageForReturnCode(secureChannelParseResponse.getReturnCode()), Toast.LENGTH_SHORT).show();
            openMainScreen();
            return;
        }
        final SecureChannelMessage secureChannelMessage = secureChannelParseResponse.getMessage();
        final SecureChannelDecryptionResponse secureChannelDecryptionResponse =
                DigipassSDK.decryptSecureChannelMessageBody(UserSession.getInstance(HandleFCMNotificationActivity.this)
                                .getStaticVector(UserSession.getInstance(HandleFCMNotificationActivity.this).getCurrentUser().getUserId()),
                        dynamicVector, secureChannelMessage, Constants.getDevicePlatformFingerprintForDigipass(HandleFCMNotificationActivity.this));
        if (secureChannelDecryptionResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
            Toast.makeText(this, "" + DigipassSDK.getMessageForReturnCode(secureChannelDecryptionResponse.getReturnCode()), Toast.LENGTH_SHORT).show();
            openMainScreen();
            return;
        }
        byte[] hexStringBytes = UtilitiesSDK.hexaToBytes(secureChannelDecryptionResponse.getMessageBody());
        String notificationString = new String(hexStringBytes);
        final String challengeKey = notificationString.split(";")[2];
        DigipassPropertiesResponse digipassPropertiesResponse = DigipassSDK.getDigipassProperties(
                UserSession.getInstance(HandleFCMNotificationActivity.this).getStaticVector(
                        UserSession.getInstance(HandleFCMNotificationActivity.this).getCurrentUser().getUserId()), dynamicVector);
        if (digipassPropertiesResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
            Toast.makeText(this, "" + DigipassSDK.getMessageForReturnCode(digipassPropertiesResponse.getReturnCode()), Toast.LENGTH_SHORT).show();
            openMainScreen();
            return;
        }
        digipassPropertiesResponse.getApplications()[DigipassSDKConstants.CRYPTO_APPLICATION_INDEX_APP_1 - 1].isTimeBased();
        PrepareSecureChallengeRequest prepareSecureChallengeRequest = new PrepareSecureChallengeRequest();
        final String serialNumber = digipassPropertiesResponse.getSerialNumber() + "-" + digipassPropertiesResponse.getSequenceNumber();
        prepareSecureChallengeRequest.setChallengeKey(challengeKey);
        prepareSecureChallengeRequest.setSerialNumber(serialNumber);
        RetrofitClient.getOneSpanServices().getPreparedSecureChallenge(Constants.getAuthorization(),
                prepareSecureChallengeRequest).enqueue(new Callback<PrepareSecureChallengeResponse>() {
            @Override
            public void onResponse(@NonNull Call<PrepareSecureChallengeResponse> call, @NonNull Response<PrepareSecureChallengeResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(HandleFCMNotificationActivity.this, "Notification Expired.", Toast.LENGTH_SHORT).show();
                    openMainScreen();
                    return;
                }
                if (response.body() == null) {
                    Toast.makeText(HandleFCMNotificationActivity.this, "Notification Expired.", Toast.LENGTH_SHORT).show();
                    openMainScreen();
                    return;
                }
                if (response.body().getResult() == null) {
                    Toast.makeText(HandleFCMNotificationActivity.this, "Notification Expired.", Toast.LENGTH_SHORT).show();
                    openMainScreen();
                    return;
                }
                SecureChannelParseResponse secureChannelParseResponse1 = DigipassSDK.parseSecureChannelMessage(response.body().getResult().getRequestMessage());
                if (secureChannelParseResponse1.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
                    Toast.makeText(HandleFCMNotificationActivity.this, "" + DigipassSDK.getMessageForReturnCode(secureChannelParseResponse1.getReturnCode()), Toast.LENGTH_SHORT).show();
                    openMainScreen();
                    return;
                }
                SecureChannelMessage secureChannelMessage1 = secureChannelParseResponse1.getMessage();
                SecureChannelDecryptionResponse secureChannelDecryptionResponse1 = DigipassSDK.decryptSecureChannelMessageBody(
                        UserSession.getInstance(HandleFCMNotificationActivity.this).getStaticVector(UserSession.getInstance(HandleFCMNotificationActivity.this).getCurrentUser().getUserId()),
                        dynamicVector, secureChannelMessage1, Constants.getDevicePlatformFingerprintForDigipass(HandleFCMNotificationActivity.this));
                if (secureChannelDecryptionResponse1.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
                    Toast.makeText(HandleFCMNotificationActivity.this, "" + DigipassSDK.getMessageForReturnCode(secureChannelDecryptionResponse1.getReturnCode()),
                            Toast.LENGTH_SHORT).show();
                    openMainScreen();
                    return;
                }
                byte[] hexStringBytes1 = UtilitiesSDK.hexaToBytes(secureChannelDecryptionResponse1.getMessageBody());
                String notificationString1 = new String(hexStringBytes1);
                String userId = notificationString1.split(";")[4];
                String domain = notificationString1.split(";")[5];
                byte[] staticVector = UserSession.getInstance(HandleFCMNotificationActivity.this).getStaticVector(UserSession.getInstance(HandleFCMNotificationActivity.this).getCurrentUser().getUserId());
                if (yesNo.equals("OK")) {
                    long clientShiftTime = UserSession.getInstance(HandleFCMNotificationActivity.this).getClientServerTimeShift();
                    GenerationResponse generationResponse = DigipassSDK.generateSignatureFromSecureChannelMessage(staticVector,
                            dynamicVector, secureChannelMessage1, pin, clientShiftTime, DigipassSDKConstants.CRYPTO_APPLICATION_INDEX_APP_1,
                            Constants.getDevicePlatformFingerprintForDigipass(HandleFCMNotificationActivity.this));
                    UserSession.getInstance(HandleFCMNotificationActivity.this).setDynamicVector(generationResponse.getDynamicVector());
                    if (generationResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
                        Utils.Log(TAG, "Generation Response is " + DigipassSDK.getMessageForReturnCode(generationResponse.getReturnCode()) + " " + generationResponse.getReturnCode());
                        Utils.Log(TAG, " didn't work");
                        openMainScreen();
                        return;
                    }
                    AuthUserRequest authUserRequest = new AuthUserRequest();
                    authUserRequest.setChallengeKey(challengeKey);
                    authUserRequest.setDomain(domain);
                    authUserRequest.setUserID(userId);
                    authUserRequest.setSignature(generationResponse.getResponse());
                    RetrofitClient.getOneSpanServices().authUser(Constants.getAuthorization(), authUserRequest).enqueue(new Callback<AuthUserResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<AuthUserResponse> call, @NonNull Response<AuthUserResponse> response) {
                            if (response.isSuccessful()) {
                                if ((response.body() != null ? response.body().getResultCodes().getReturnCode() : -101) == 0) {
                                    Toast.makeText(HandleFCMNotificationActivity.this, "Windows Logon Request has been Approved.", Toast.LENGTH_SHORT).show();
                                    openMainScreen();
                                }
                            } else {
                                Toast.makeText(HandleFCMNotificationActivity.this, "Windows Logon Request has not Approved.", Toast.LENGTH_SHORT).show();
                                openMainScreen();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<AuthUserResponse> call, @NonNull Throwable t) {
                            Crashlytics.logException(t);
                            openMainScreen();
                        }
                    });
                } else {
                    Utils.Log(TAG, "Pressed NOK " + yesNo);
                    SecureChannelGenerateResponse secureChannelGenerateResponse = DigipassSDK.generateSecureChannelInformationMessage(staticVector, dynamicVector,
                            UtilitiesSDK.bytesToHexa(challengeKey.getBytes()), DigipassSDKConstants.SECURE_CHANNEL_MESSAGE_PROTECTION_HMAC_AESCTR,
                            Constants.getDevicePlatformFingerprintForDigipass(HandleFCMNotificationActivity.this));
                    if (secureChannelDecryptionResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
                        Toast.makeText(HandleFCMNotificationActivity.this, "" + DigipassSDK.getMessageForReturnCode(secureChannelDecryptionResponse.getReturnCode()), Toast.LENGTH_SHORT).show();
                        openMainScreen();
                        return;
                    }
                    CancelAuthUserRequest cancelAuthUserRequest = new CancelAuthUserRequest();
                    cancelAuthUserRequest.setChallengeKey(secureChannelGenerateResponse.getSecureChannelMessage().rawData);
                    cancelAuthUserRequest.setSerialNumber(serialNumber);
                    RetrofitClient.getOneSpanServices().cancelAuthUser(Constants.getAuthorization(), cancelAuthUserRequest).enqueue(new Callback<CancelAuthUserResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<CancelAuthUserResponse> call, @NonNull Response<CancelAuthUserResponse> response) {
                            if (response.isSuccessful()) {
                                if (response.body() == null) {
                                    Toast.makeText(HandleFCMNotificationActivity.this, "Windows Logon Request reponse submission failed.", Toast.LENGTH_SHORT).show();
                                    openMainScreen();
                                    return;
                                }
                                if (response.body().getResultCodes().getReturnCode() == 0) {
                                    Toast.makeText(HandleFCMNotificationActivity.this, "Windows Logon Request has been cancelled successfully.", Toast.LENGTH_SHORT).show();
                                    openMainScreen();
                                }
                            } else {
                                openMainScreen();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<CancelAuthUserResponse> call, @NonNull Throwable t) {
                            Crashlytics.logException(t);
                            openMainScreen();
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrepareSecureChallengeResponse> call, @NonNull Throwable t) {
                Crashlytics.logException(t);
                openMainScreen();
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Display notification content if the intent is a notification
        displayNotificationContent(intent);
    }


    private void displayNotificationContent(Intent intent) {
        try {
            // Check if the intent is a notification
            if (NotificationSDKClient.isVASCONotification(intent)) {
                if (Utils.getInstance().getSecureCache() == null) {
                    Utils.getInstance().initSecureCache(HandleFCMNotificationActivity.this);
                }

                // Parse the notification and retrieve its content.
                Utils.getInstance().putStringInSecureCache(Constants.NOTIFICATION_CONTENT_KEY, NotificationSDKClient.parseVASCONotification(intent));
                Utils.Log(TAG, Utils.getInstance().getStringFromSecureCache(Constants.NOTIFICATION_CONTENT_KEY));
            }
        } catch (NotificationSDKClientException e) {
            Crashlytics.logException(e);
            String errorMsg = "NotificationSDKClientException error code: " + e.getErrorCode();
            Utils.Log(TAG, errorMsg);
        }
    }
}
