package ae.adpolice.gov.utils;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.vasco.digipass.sdk.DigipassSDK;
import com.vasco.digipass.sdk.DigipassSDKConstants;
import com.vasco.digipass.sdk.DigipassSDKReturnCodes;
import com.vasco.digipass.sdk.responses.ActivationResponse;
import com.vasco.digipass.sdk.responses.GenerationResponse;
import com.vasco.digipass.sdk.responses.MultiDeviceLicenseActivationResponse;
import com.vasco.digipass.sdk.responses.SecureChannelParseResponse;
import com.vasco.dsapp.client.DSAPPClient;
import com.vasco.dsapp.client.exceptions.DSAPPException;
import com.vasco.dsapp.client.responses.SRPClientEphemeralKeyResponse;
import com.vasco.dsapp.client.responses.SRPSessionKeyResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import ae.adpolice.gov.Constants;
import ae.adpolice.gov.fragments.PinDialogFragment;
import ae.adpolice.gov.network.RetrofitClient;
import ae.adpolice.gov.network.pojo.request.ActivationRequest;
import ae.adpolice.gov.network.pojo.request.AddDeviceRequest;
import ae.adpolice.gov.network.pojo.request.GenerateActivateDataRequest;
import ae.adpolice.gov.network.pojo.request.ServerEphemeralKeyRequest;
import ae.adpolice.gov.network.pojo.response.AddDeviceResponse;
import ae.adpolice.gov.network.pojo.response.AppActivationResponse;
import ae.adpolice.gov.network.pojo.response.GenerateActivationDataResponse;
import ae.adpolice.gov.network.pojo.response.GenerateServerEphemeralKeyResponse;
import ae.adpolice.gov.users.DatabaseClient;
import ae.adpolice.gov.users.UserSession;
import ae.adpolice.gov.users.pojo.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DSAPPUserActivation {

    private static DSAPPUserActivation instance;
    private static FragmentActivity mContext;

    private DSAPPUserActivation() {

    }

    private ActivationCallback activationCallback;

    public void setActivationCallback(ActivationCallback activationCallback) {
        this.activationCallback = activationCallback;
    }

    public static DSAPPUserActivation getInstance(FragmentActivity context) {
        mContext = context;
        if (instance == null) {
            instance = new DSAPPUserActivation();
        }
        return instance;
    }

    private int iteration = 0;

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    private String getUserId() {
        return Utils.getInstance().getUserId();
    }

    private String[] getRegistrationIdentifier() {
        return Utils.getInstance().getRegistrationIdentifiers();
    }

    /*--------------------------------------------------------------------------------
     *  DSAPP Client - Demonstrating how to call the SDK methods with demo parameters.
     ----------------------------------------------------------------------------------*/

    /**
     * Example of use of {DSAPPClient.validateSRPUserPasswordChecksum}
     *
     * @param password password to validate Password checksum
     */
    public void validateSRPUserPasswordChecksum(String password) {

        // The validateSRPUserPasswordChecksum method of DSAPP
        // validates the checksum contained in the Activation Password.
        try {
            // Tries to validates the checksum contained in the activation password.
            Utils.Log("DSAPPUserActivation", "Sample validateSRPUserPasswordChecksum:");
            DSAPPClient.validateSRPUserPasswordChecksum(password);
            Utils.Log("DSAPPUserActivation", "SRP User password checksum successfully validated with validateSRPUserPasswordChecksum.\n\n");
        } catch (DSAPPException e) {
            Crashlytics.logException(e);
            Utils.Log("DSAPPUserActivation", "An error has occurred with validateSRPUserPasswordChecksum:");
            Utils.Log("DSAPPUserActivation", "Error code: " + e.getErrorCode());
            Utils.Log("DSAPPUserActivation", "Message:    " + e.getMessage() + "\n\n");
            if (activationCallback != null) {
                activationCallback.onActivationFailure("An error has occurred with validateSRPUserPasswordChecksum.");
            }
        }
    }

    /**
     * Example of use of {DSAPPClient.generateSRPClientEphemeralKey}
     */
    public SRPClientEphemeralKeyResponse generateSRPClientEphemeralKey() {
        // The generateSRPClientEphemeralKey method of DSAPP
        // generates Public and Private Client Ephemeral keys.
        //
        // Generates a SRPClientEphemeralKeyResponse containing:
        // - Client ephemeral private key
        // - Client ephemeral public key

        try {
            // Tries to generate SRP client ephemeral keys.
            Utils.Log("DSAPPUserActivation", "Sample generateSRPClientEphemeralKey");
            SRPClientEphemeralKeyResponse clientEphemeralKeyResponse = DSAPPClient.generateSRPClientEphemeralKey();
            clientEphemeralKeyResponse.getClientEphemeralPrivateKey();
            clientEphemeralKeyResponse.getClientEphemeralPublicKey();

            final String clientEphemeralPrivateKey = clientEphemeralKeyResponse.getClientEphemeralPrivateKey();
            final String clientEphemeralPublicKey = clientEphemeralKeyResponse.getClientEphemeralPublicKey();

            Utils.Log("DSAPPUserActivation", "The SRPClientEphemeralKeyResponse has well been generated:");
            Utils.Log("DSAPPUserActivation", "\tClient ephemeral private key: " + clientEphemeralPrivateKey);
            Utils.Log("DSAPPUserActivation", "\tClient ephemeral public key:  " + clientEphemeralPublicKey + "\n\n");
            return clientEphemeralKeyResponse;
        } catch (DSAPPException e) {
            Utils.Log("DSAPPUserActivation", "An error has occurred with generateSRPClientEphemeralKey:");
            Utils.Log("DSAPPUserActivation", "Error code: " + e.getErrorCode());
            Utils.Log("DSAPPUserActivation", "Message: " + e.getMessage() + "\n\n");
            if (activationCallback != null) {
                activationCallback.onActivationFailure("An error has occurred with generateSRPClientEphemeralKey.");
            }
        }
        return null;
    }

    /**
     * Example of use of {DSAPPClient.generateSRPSessionKey}
     */
    public void generateSRPSessionKey(final SRPClientEphemeralKeyResponse srpClientEphemeralKeyResponse, final String password) {

        // The generateSRPSessionKey method of DSAPP
        // generates Client Session Key, and Client Evidence Message
        //
        // Generates a SRPSessionKeyResponse containing:
        // - Client session key
        // - Client evidence message

        final String clientEphemeralPublicKey = srpClientEphemeralKeyResponse.getClientEphemeralPublicKey();

        final String clientEphemeralPrivateKey = srpClientEphemeralKeyResponse.getClientEphemeralPrivateKey();
        ServerEphemeralKeyRequest serverEphemeralKeyRequest = new ServerEphemeralKeyRequest();
        serverEphemeralKeyRequest.setClientEphemeralPublicKey(clientEphemeralPublicKey);
        serverEphemeralKeyRequest.setRegistrationIdentifier(getInstance(mContext).getRegistrationIdentifier()[getIteration()]);
        RetrofitClient.getOneSpanServices()
                .generateServerEphemeralKey(Constants.getAuthorization(), serverEphemeralKeyRequest)
                .enqueue(new Callback<GenerateServerEphemeralKeyResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenerateServerEphemeralKeyResponse> call, @NonNull Response<GenerateServerEphemeralKeyResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            if (response.code() == 401) {
                                activationCallback.onActivationFailure("Unauthorized Request");
                                return;
                            }
                            activationCallback.onActivationFailure("GenerateServerEphemeralKeyResponse Body is Empty");
                            return;
                        }
                        if (response.body().getResult() == null) {
                            if (response.code() == 401) {
                                activationCallback.onActivationFailure("Unauthorized Request");
                                return;
                            }
                            activationCallback.onActivationFailure("GenerateServerEphemeralKeyResponse Body is Empty");
                            return;
                        }
                        final String serverEphemeralPublicKey = response.body().getResult().getServerEphemeralPublicKey();
                        final String salt = response.body().getResult().getSalt();
                        Utils.Log("DSAPPUserActivation", "Sample generateSRPSessionKey");
                        SRPSessionKeyResponse clientSessionKeyResponse;
                        try {
                            clientSessionKeyResponse = DSAPPClient.generateSRPSessionKey(
                                    clientEphemeralPublicKey, clientEphemeralPrivateKey, serverEphemeralPublicKey, getInstance(mContext).getRegistrationIdentifier()[getIteration()], password,
                                    salt);
                            final String clientSessionKey = clientSessionKeyResponse.getSessionKey();
                            final String clientEvidenceMessage = clientSessionKeyResponse.getClientEvidenceMessage();
                            Utils.Log("DSAPPUserActivation", "The SRPSessionKeyResponse has well been generated:");
                            Utils.Log("DSAPPUserActivation", "\tClient session key:      " + clientSessionKey);
                            Utils.Log("DSAPPUserActivation", "\tClient evidence message: " + clientEvidenceMessage + "\n\n");
                            // Example of use of verifySRPServerEvidenceMessage
                            verifySRPServerEvidenceMessage(srpClientEphemeralKeyResponse, clientSessionKeyResponse);

                        } catch (DSAPPException e) {
                            Utils.Log("DSAPPUserActivation", "An error has occurred with generateSRPSessionKey:");
                            Utils.Log("DSAPPUserActivation", "Error code: " + e.getErrorCode());
                            Utils.Log("DSAPPUserActivation", "Message:    " + e.getMessage() + "\n\n");
                            Crashlytics.logException(e);
                            if (activationCallback != null) {
                                activationCallback.onActivationFailure("An error has occurred with generateSRPSessionKey.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GenerateServerEphemeralKeyResponse> call, @NonNull Throwable t) {
                        Crashlytics.logException(t);
                        if (activationCallback != null) {
                            activationCallback.onActivationFailure("GenerateServerEphemeralKeyResponse Failed");
                        }
                    }
                });
    }

    /**
     * Example of use of {DSAPPClient.verifySRPServerEvidenceMessage}
     *
     */
    private void verifySRPServerEvidenceMessage(SRPClientEphemeralKeyResponse srpClientEphemeralKeyResponse, final SRPSessionKeyResponse srpSessionKeyResponse) {
        // The verifySRPServerEvidenceMessage method of DSAPP
        // verifies the server evidence message.

        //refer to 3.6.1 for GenerateActivateData
//2nd api call
        final String clientEphemeralPublicKey = srpClientEphemeralKeyResponse.getClientEphemeralPublicKey();
        final String clientEvidenceMessage = srpSessionKeyResponse.getClientEvidenceMessage();
        final GenerateActivateDataRequest generateActivateDataRequest = new GenerateActivateDataRequest();
        generateActivateDataRequest.setRegistrationIdentifier(getInstance(mContext).getRegistrationIdentifier()[getIteration()]);
        generateActivateDataRequest.setClientEvidenceMessage(srpSessionKeyResponse.getClientEvidenceMessage());
        RetrofitClient.getOneSpanServices().generateActivationData(Constants.getAuthorization(), generateActivateDataRequest)
                .enqueue(new Callback<GenerateActivationDataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenerateActivationDataResponse> call, @NonNull Response<GenerateActivationDataResponse> response) {
                        if (!response.isSuccessful()) {
                            if (activationCallback != null) {
                                try {
                                    assert response.errorBody() != null;
                                    activationCallback.onActivationFailure("Response failed " + response.errorBody().string());
                                } catch (IOException e) {
                                    Crashlytics.logException(e);
                                }
                            }
                            return;
                        }
                        GenerateActivationDataResponse generateActivationDataResponse = response.body();
                        if (generateActivationDataResponse == null) {
                            if (activationCallback != null) {
                                activationCallback.onActivationFailure("GenerateActivationDataResponse is Empty");
                            }
                            return;
                        }
                        final String serverEvidenceMessage = generateActivationDataResponse.getResult().getServerEvidenceMessage();//"C387FAE7D696B2B8E73F981A740F0E02BD1419A618C0CC5C09C1A8AAA567A25D";
                        final String sessionKey = srpSessionKeyResponse.getSessionKey();//"A3F850A19C034A190E9C68110102172E2FCC12B78BC6C696FE68CC8B30605333";

                        // Tries to verify the Server Evidence Message
                        Utils.Log("DSAPPUserActivation", "Sample verifySRPServerEvidenceMessage");
                        try {
                            DSAPPClient.verifySRPServerEvidenceMessage(clientEphemeralPublicKey, clientEvidenceMessage,
                                    serverEvidenceMessage, sessionKey);
                            Utils.Log("DSAPPUserActivation", "SRP Server Evidence Message successfully verified with verifySRPServerEvidenceMessage.\n\n");
                            // Example of use of decryptSRPData
                            decryptSRPData(srpSessionKeyResponse, generateActivationDataResponse);
                        } catch (DSAPPException e) {
                            if (activationCallback != null) {
                                activationCallback.onActivationFailure("An error has occurred with verifySRPServerEvidenceMessage.");
                            }
                            Crashlytics.logException(e);
                            Utils.Log("DSAPPUserActivation", "An error has occurred with verifySRPServerEvidenceMessage:");
                            Utils.Log("DSAPPUserActivation", "Error code: " + e.getErrorCode());
                            Utils.Log("DSAPPUserActivation", "Message:    " + e.getMessage() + "\n\n");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GenerateActivationDataResponse> call, @NonNull Throwable t) {
                        if (activationCallback != null) {
                            activationCallback.onActivationFailure("An error has occurred with GenerateActivationDataResponse");
                        }
                        Crashlytics.logException(t);
                    }
                });

    }

    /**
     * Example of use of {DSAPPClient.decryptSRPData}
     *
     */
    private void decryptSRPData(SRPSessionKeyResponse srpSessionKeyResponse, final GenerateActivationDataResponse generateActivationDataResponse) {

        // The decryptSRPData method of DSAPP
        // decrypts the activation data.

        try {
            final String sessionKey = srpSessionKeyResponse.getSessionKey();//"A3F850A19C034A190E9C68110102172E2FCC12B78BC6C696FE68CC8B30605333";
            // In this sample, we use an encrypted License Activation Message as demo value.
            final String encryptedData = generateActivationDataResponse.getResult().getEncryptedLicenseActivationMessage();//"5AE94BF4119D3DF32967C3D5B0AFBD328AE60CF0C0CCE18074FA16D013A3C7C5E59F50416D64DEC3ED166933EEBFCFC97376360045C17EECC1106D21BB01A8F8086860ED450375CE3AADBBC745EFA6E87E966BBBACCEB234ADD739543F277628D24874AC36027C6B13AD826D";
            final String encryptionCounter = generateActivationDataResponse.getResult().getEncryptedCounter();//"0FC645CEF184D753";
            final String mac = generateActivationDataResponse.getResult().getMac();//"338AD38214B6EADE749A119DDE5198F002CB78E13E59950659A3AC9212607C6E";
            //refer 3.6
            // Tries to decrypt the Encrypted Activation Data.
            Utils.Log("DSAPPUserActivation", "Sample decryptSRPData");
            byte[] decryptedData = DSAPPClient.decryptSRPData(sessionKey, encryptedData, encryptionCounter, mac);

            Utils.Log("DSAPPUserActivation", "Activation data successfully decrypted");
            Utils.Log("DSAPPUserActivation", "\tDecrypted data: " + new String(decryptedData, StandardCharsets.UTF_8) + "\n\n");
            String decryptedDataString = new String(decryptedData, StandardCharsets.UTF_8);
            final SecureChannelParseResponse secureChannelParseResponse = DigipassSDK.parseSecureChannelMessage(decryptedDataString);
            if (secureChannelParseResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
                if (activationCallback != null) {
                    activationCallback.onActivationFailure(DigipassSDK.getMessageForReturnCode(secureChannelParseResponse.getReturnCode()));
                }
                return;
            }
            final MultiDeviceLicenseActivationResponse multiDeviceLicenseActivationResponse = DigipassSDK.multiDeviceActivateLicense(secureChannelParseResponse.getMessage(),
                    null, Constants.getDevicePlatformFingerprintForDigipass(mContext), DigipassSDKConstants.JAILBREAK_STATUS_NA,
                    UserSession.getInstance(mContext).getClientServerTimeShift());
            //device binding sdk to provide platform fingerprint
            //client Time Shift do api call serverTime in Digipass sdk
            if (multiDeviceLicenseActivationResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
                if (activationCallback != null) {
                    activationCallback.onActivationFailure(DigipassSDK.getMessageForReturnCode(multiDeviceLicenseActivationResponse.getReturnCode()));
                }
                return;
            }
            Utils.Log("DSAPPUserActivation", multiDeviceLicenseActivationResponse.getDeviceCode() + " ");
            AddDeviceRequest addDeviceRequest = new AddDeviceRequest();
            addDeviceRequest.setDeviceCode(multiDeviceLicenseActivationResponse.getDeviceCode());
            addDeviceRequest.setRegistrationIdentifier(getInstance(mContext).getRegistrationIdentifier()[getIteration()]);
            RetrofitClient.getOneSpanServices().addDevice(Constants.getAuthorization(), addDeviceRequest)
                    .enqueue(new Callback<AddDeviceResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<AddDeviceResponse> call, @NonNull Response<AddDeviceResponse> response) {
                            if (!response.isSuccessful()) {
                                try {
                                    assert response.errorBody() != null;
                                    activationCallback.onActivationFailure("Response Failed. " + response.errorBody().string());
                                } catch (IOException e) {
                                    Crashlytics.logException(e);
                                }
                                return;
                            }
                            //send to Server MDL add device
                            //s
                            assert response.body() != null;
                            final SecureChannelParseResponse secureChannelParseResponse1 = DigipassSDK
                                    .parseSecureChannelMessage(response.body().getResult().getInstanceActivationMessage());
                            if (secureChannelParseResponse1.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
                                if (activationCallback != null) {
                                    activationCallback.onActivationFailure(DigipassSDK.getMessageForReturnCode(secureChannelParseResponse1.getReturnCode()));
                                }
                                return;
                            }
                            if (getIteration() == 0) {
                                FragmentManager fm = mContext.getSupportFragmentManager();
                                PinDialogFragment editNameDialog = new PinDialogFragment();
                                editNameDialog.setOnPinSetListener(new PinDialogFragment.OnPinSetListener() {
                                    @Override
                                    public void onPinSet(String pin) {
                                        continueActivation(mContext, secureChannelParseResponse1, multiDeviceLicenseActivationResponse, pin);
                                        Toast.makeText(mContext, "Pin set successfully ", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onPinNotMatched() {
                                        Toast.makeText(mContext, "Pin not set. Please try again.", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onPinSetFailed() {
                                        Toast.makeText(mContext, "Pin strength is weak. Please set a strong pin.", Toast.LENGTH_SHORT).show();
                                        if (activationCallback != null) {
                                            activationCallback.onActivationFailure("Pin not Set");
                                        }
                                    }
                                });
                                editNameDialog.setCancelable(false);
                                editNameDialog.show(fm, "fragment_edit_name");
                            } else {
                                continueActivation(mContext, secureChannelParseResponse1, multiDeviceLicenseActivationResponse, "");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<AddDeviceResponse> call, @NonNull Throwable t) {
                            Crashlytics.logException(t);
                            if (activationCallback != null) {
                                activationCallback.onActivationFailure("Add device response is failed.");
                            }
                        }
                    });


        } catch (DSAPPException e) {
            Crashlytics.logException(e);
            if (activationCallback != null) {
                activationCallback.onActivationFailure("An error has occurred with decryptSRPData.");
            }
            Utils.Log("DSAPPUserActivation", "An error has occurred with decryptSRPData:");
            Utils.Log("DSAPPUserActivation", "Error code: " + e.getErrorCode());
            Utils.Log("DSAPPUserActivation", "Message:    " + e.getMessage() + "\n\n");
        }
    }


    private void continueActivation(final FragmentActivity mContext, final SecureChannelParseResponse secureChannelParseResponse1,
                                    final MultiDeviceLicenseActivationResponse multiDeviceLicenseActivationResponse,
                                    final String pin) {
        ActivationResponse activationResponse;
        activationResponse = DigipassSDK.multiDeviceActivateInstance(multiDeviceLicenseActivationResponse.getStaticVector(),
                multiDeviceLicenseActivationResponse.getDynamicVector(), secureChannelParseResponse1.getMessage(),
                pin, Constants.getDevicePlatformFingerprintForDigipass(mContext));
        //device binding sdk to provide platform fingerprint
        //above password should be the pin setup and send password
        //finger print will be a complete new flow and send password empty string. if finger print is available do
        //double activation.
        if (activationResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
            if (activationCallback != null) {
                activationCallback.onActivationFailure(DigipassSDK.getMessageForReturnCode(activationResponse.getReturnCode()));
            }
            return;
        }
        int cryptoAppIndex = DigipassSDKConstants.CRYPTO_APPLICATION_INDEX_APP_1;//pin.equals("")?DigipassSDKConstants.CRYPTO_APPLICATION_INDEX_APP_3:
        final GenerationResponse generationResponse = DigipassSDK.generateSignatureFromSecureChannelMessage(
                multiDeviceLicenseActivationResponse.getStaticVector(), activationResponse.getDynamicVector(),
                secureChannelParseResponse1.getMessage(), pin,
                UserSession.getInstance(mContext).getClientServerTimeShift(),
                cryptoAppIndex, Constants.getDevicePlatformFingerprintForDigipass(mContext));
        if (generationResponse.getReturnCode() != DigipassSDKReturnCodes.SUCCESS) {
            if (activationCallback != null) {
                activationCallback.onActivationFailure("GenerationResponse Signature Error ");
            }
            Utils.Log("Generate Sign failing", "Generation Response is " + DigipassSDK.getMessageForReturnCode(generationResponse.getReturnCode()) + " " + generationResponse.getReturnCode());
            return;
        }
        final ActivationRequest activationRequest = new ActivationRequest();
        activationRequest.setRegistrationIdentifier(getInstance(mContext).getRegistrationIdentifier()[getIteration()]);
        activationRequest.setSignature(generationResponse.getResponse());
        //TODO generation response has latest SV, DV use them.
        final ActivationResponse finalActivationResponse = activationResponse;
        RetrofitClient.getOneSpanServices().activateApp(Constants.getAuthorization(), activationRequest)
                .enqueue(new Callback<AppActivationResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AppActivationResponse> call, @NonNull Response<AppActivationResponse> response) {
                        //Store dynamic vector and static vector to secure storage
                        //store dynamic vector for each request into secure storage
                        // generationResponse.getDynamicVector()
                        // Example of use of verifySRPMAC
                        //demoVerifySRPMAC(sessionKey,generateActivationDataResponse.getResult().getMac());
                        // Store activated user identity in the local preferences
                        if (!response.isSuccessful()) {
                            if (activationCallback != null) {
                                try {
                                    activationCallback.onActivationFailure("Response Error " + (response.errorBody() != null ? response.errorBody().string() : ""));
                                } catch (IOException e) {
                                    Crashlytics.logException(e);
                                }
                            }
                            Utils.Log("Activation Request", "Failed");
                            return;
                        }
                        if (response.body() == null) {
                            if (activationCallback != null) {
                                try {
                                    activationCallback.onActivationFailure("Response Error " + (response.errorBody() != null ? response.errorBody().string() : ""));
                                } catch (IOException e) {
                                    Crashlytics.logException(e);
                                }
                            }
                            Utils.Log("Activation Request", "Failed");
                            return;
                        }
                        if (response.body().getResultCodes().getReturnCode() != 0) {
                            if (activationCallback != null) {
                                activationCallback.onActivationFailure(DigipassSDK.getMessageForReturnCode(response.body().getResultCodes().getReturnCode()));
                            }
                            Utils.Log("Activation Request", "Failed " + response.body().getResultCodes().getReturnCode() + " " + DigipassSDK.getMessageForReturnCode(response.body().getResultCodes().getReturnCode()));
                            return;
                        }
                        if (UserSession.getInstance(mContext).getCurrentUser() == null) {
                            Utils.Log("DSAPPUserActivation", "inserting user null");
                            User u = new User();
                            u.setUserId(getUserId());
                            UserSession.getInstance(mContext).setDynamicVectorPin(getUserId(), generationResponse.getDynamicVector());
                            UserSession.getInstance(mContext).setStaticVector(getUserId(), finalActivationResponse.getStaticVector());
                            u.setStaticVectorKey(Constants.getStaticVectorKey(getUserId()));
                            u.setDynamicVectorPinKey(Constants.getDynamicVectorPinKey(getUserId()));
                            u.setLastModified(System.currentTimeMillis());
                            u.setAuthenticateChoice(getIteration() == 1);
                            DatabaseClient.getInstance(mContext).getAppDatabase().userDao().insert(u);
                        } else {
                            if (UserSession.getInstance(mContext).getCurrentUser().getUserId().equals(getUserId())) {
                                Utils.Log("DSAPPUserActivation", "updating user");
                                User u = UserSession.getInstance(mContext).getCurrentUser();
                                u.setUserId(getUserId());
                                UserSession.getInstance(mContext).setDynamicVectorBio(getUserId(), generationResponse.getDynamicVector());
                                UserSession.getInstance(mContext).setStaticVector(getUserId(), finalActivationResponse.getStaticVector());
                                u.setStaticVectorKey(Constants.getStaticVectorKey(getUserId()));
                                u.setDynamicVectorKey(Constants.getDynamicVectorBioKey(getUserId()));
                                u.setLastModified(System.currentTimeMillis());
                                u.setAuthenticateChoice(getIteration() == 1);
                                DatabaseClient.getInstance(mContext).getAppDatabase().userDao().update(u);
                            } else {
                                Utils.Log("DSAPPUserActivation", "inserting user no null");
                                User u = new User();
                                u.setUserId(getUserId());
                                UserSession.getInstance(mContext).setDynamicVectorBio(getUserId(), generationResponse.getDynamicVector());
                                UserSession.getInstance(mContext).setStaticVector(getUserId(), finalActivationResponse.getStaticVector());
                                u.setStaticVectorKey(Constants.getStaticVectorKey(getUserId()));
                                u.setDynamicVectorKey(Constants.getDynamicVectorBioKey(getUserId()));
                                u.setLastModified(System.currentTimeMillis());
                                u.setAuthenticateChoice(getIteration() == 1);
                                DatabaseClient.getInstance(mContext).getAppDatabase().userDao().insert(u);
                            }
                        }
                        if (activationCallback != null) {
                            activationCallback.onActivationSuccess();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AppActivationResponse> call, @NonNull Throwable t) {
                        Crashlytics.logException(t);
                        if (activationCallback != null) {
                            activationCallback.onActivationFailure("Final Activation Failure");
                        }
                    }
                });
        //sent to Server MDL Activate v2
    }

}
