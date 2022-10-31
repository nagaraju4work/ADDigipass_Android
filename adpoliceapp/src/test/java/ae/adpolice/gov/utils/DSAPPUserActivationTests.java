package ae.adpolice.gov.utils;


import androidx.annotation.NonNull;

import com.vasco.dsapp.client.DSAPPClient;
import com.vasco.dsapp.client.exceptions.DSAPPException;
import com.vasco.dsapp.client.responses.SRPClientEphemeralKeyResponse;
import com.vasco.dsapp.client.responses.SRPSessionKeyResponse;

import org.junit.Test;

import ae.adpolice.gov.Constants;
import ae.adpolice.gov.network.RetrofitClient;
import ae.adpolice.gov.network.pojo.request.GenerateActivateDataRequest;
import ae.adpolice.gov.network.pojo.request.ServerEphemeralKeyRequest;
import ae.adpolice.gov.network.pojo.response.GenerateActivationDataResponse;
import ae.adpolice.gov.network.pojo.response.GenerateServerEphemeralKeyResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DSAPPUserActivationTests {

    @Test
    public void testDemoGenerateSRPClientEphemeralKey() throws DSAPPException {
        final SRPClientEphemeralKeyResponse clientEphemeralKeyResponse = DSAPPClient.generateSRPClientEphemeralKey();
        final String regid="Px7KcM2y";
        final String activationPassword = "sYv032l0";

        ServerEphemeralKeyRequest serverEphemeralKeyRequest=new ServerEphemeralKeyRequest();
        serverEphemeralKeyRequest.setClientEphemeralPublicKey(clientEphemeralKeyResponse.getClientEphemeralPublicKey());
        serverEphemeralKeyRequest.setRegistrationIdentifier(regid);

        RetrofitClient.getOneSpanServices()
                .generateServerEphemeralKey(Constants.getAuthorization(),serverEphemeralKeyRequest)
                .enqueue(new Callback<GenerateServerEphemeralKeyResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenerateServerEphemeralKeyResponse> call, @NonNull Response<GenerateServerEphemeralKeyResponse> response) {
                        try {
                            assert response.body() != null;
                            final SRPSessionKeyResponse clientSessionKeyResponse = DSAPPClient.generateSRPSessionKey(
                                        clientEphemeralKeyResponse.getClientEphemeralPublicKey(), clientEphemeralKeyResponse.getClientEphemeralPrivateKey(),
                                        response.body().getResult().getServerEphemeralPublicKey(), regid, activationPassword,
                                        response.body().getResult().getSalt());
                            GenerateActivateDataRequest generateActivateDataRequest = new GenerateActivateDataRequest();
                            generateActivateDataRequest.setRegistrationIdentifier(regid);
                            generateActivateDataRequest.setClientEvidenceMessage(clientSessionKeyResponse.getClientEvidenceMessage());
                            RetrofitClient.getOneSpanServices().generateActivationData(Constants.getAuthorization(),generateActivateDataRequest)
                                    .enqueue(new Callback<GenerateActivationDataResponse>() {
                                        @Override
                                        public void onResponse(@NonNull Call<GenerateActivationDataResponse> call, @NonNull Response<GenerateActivationDataResponse> response) {
                                            try {
                                                assert response.body() != null;
                                                DSAPPClient.verifySRPServerEvidenceMessage(clientEphemeralKeyResponse.getClientEphemeralPublicKey(), clientSessionKeyResponse.getClientEvidenceMessage(),
                                                        response.body().getResult().getServerEvidenceMessage(), clientSessionKeyResponse.getSessionKey());
                                                //decryptSRPData(srpSessionKeyResponse,response.body());
                                            } catch (DSAPPException e) {
                                                Crashlytics.logException(e);
                                                System.err.println("An error has occurred with verifySRPServerEvidenceMessage:");
                                                System.err.println("Error code: " + e.getErrorCode());
                                                System.err.println("Message:    " + e.getMessage() + "\n\n");
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<GenerateActivationDataResponse> call, @NonNull Throwable t) {
                                            Crashlytics.logException(t);
                                        }
                                    });

                        } catch (DSAPPException e) {
                            Crashlytics.logException(e);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GenerateServerEphemeralKeyResponse> call, @NonNull Throwable t) {
                        Crashlytics.logException(t);
                    }
                });

        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            Crashlytics.logException(e);
        }

    }
}
