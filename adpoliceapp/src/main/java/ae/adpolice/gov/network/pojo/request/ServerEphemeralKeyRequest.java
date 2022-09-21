package ae.adpolice.gov.network.pojo.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerEphemeralKeyRequest {
    @SerializedName("registrationIdentifier")
    @Expose
    private String registrationIdentifier;
    @SerializedName("clientEphemeralPublicKey")
    @Expose
    private String clientEphemeralPublicKey;

    public String getRegistrationIdentifier() {
        return registrationIdentifier;
    }

    public void setRegistrationIdentifier(String registrationIdentifier) {
        this.registrationIdentifier = registrationIdentifier;
    }

    public String getClientEphemeralPublicKey() {
        return clientEphemeralPublicKey;
    }

    public void setClientEphemeralPublicKey(String clientEphemeralPublicKey) {
        this.clientEphemeralPublicKey = clientEphemeralPublicKey;
    }
}
