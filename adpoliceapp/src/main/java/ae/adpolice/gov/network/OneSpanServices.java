package ae.adpolice.gov.network;

import ae.adpolice.gov.network.pojo.request.AddDeviceRequest;
import ae.adpolice.gov.network.pojo.request.AuthUserRequest;
import ae.adpolice.gov.network.pojo.request.CancelAuthUserRequest;
import ae.adpolice.gov.network.pojo.request.GenerateActivateDataRequest;
import ae.adpolice.gov.network.pojo.request.PrepareSecureChallengeRequest;
import ae.adpolice.gov.network.pojo.request.ServerEphemeralKeyRequest;
import ae.adpolice.gov.network.pojo.request.ActivationRequest;
import ae.adpolice.gov.network.pojo.response.AddDeviceResponse;
import ae.adpolice.gov.network.pojo.response.AppActivationResponse;
import ae.adpolice.gov.network.pojo.response.AuthUserResponse;
import ae.adpolice.gov.network.pojo.response.CancelAuthUserResponse;
import ae.adpolice.gov.network.pojo.response.GenerateActivationDataResponse;
import ae.adpolice.gov.network.pojo.response.GenerateServerEphemeralKeyResponse;
import ae.adpolice.gov.network.pojo.response.PrepareSecureChallengeResponse;
import ae.adpolice.gov.network.pojo.response.ServerTimeResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface OneSpanServices {

    @POST("provisioning/DSAPPSRPGenerateEphemeralKey")
    Call<GenerateServerEphemeralKeyResponse> generateServerEphemeralKey(@Header("Authorization") String authorization,
                                                                        @Body ServerEphemeralKeyRequest serverEphemeralKeyRequest);

    @POST("provisioning/MdlAddDevice")
    Call<AddDeviceResponse> addDevice(@Header("Authorization") String authorization,
                                      @Body AddDeviceRequest addDeviceRequest);

    @POST("provisioning/MdlActivate")
    Call<AppActivationResponse> activateApp(@Header("Authorization") String authorization, @Body ActivationRequest activateRequestBody);

    @POST("provisioning/getServerTime")
    Call<ServerTimeResponse> getServerTime(@Header("Authorization") String authorization);

    @POST("provisioning/DSAPPSRPGenerateActivationData")
    Call<GenerateActivationDataResponse> generateActivationData(@Header("Authorization") String authorization,
                                                                @Body GenerateActivateDataRequest generateActivateDataRequest);
    @POST("notification/push/updateNotificationID")
    Call<UpdateNotificationResponse> updateNotificationID(@Header("Authorization") String authorization,
                                                                @Body UpdateNotificationRequest updateNotificationRequest);

    @POST("authentication/push/getPreparedSecureChallenge")
    Call<PrepareSecureChallengeResponse> getPreparedSecureChallenge(@Header("Authorization") String authorization,
                                                                    @Body PrepareSecureChallengeRequest updateNotificationRequest);
    @POST("authentication/push/cancelAuthUser")
    Call<CancelAuthUserResponse> cancelAuthUser(@Header("Authorization") String authorization,
                                                @Body CancelAuthUserRequest updateNotificationRequest);
    @POST("authentication/push/authUser")
    Call<AuthUserResponse> authUser(@Header("Authorization") String authorization,
                                    @Body AuthUserRequest updateNotificationRequest);
}
