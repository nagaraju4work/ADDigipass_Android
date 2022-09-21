package ae.adpolice.gov.network.pojo.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ActivationRequest {

    @SerializedName("registrationIdentifier")
    @Expose
    private String registrationIdentifier;
    @SerializedName("signature")
    @Expose
    private String signature;

    public String getRegistrationIdentifier() {
        return registrationIdentifier;
    }

    public void setRegistrationIdentifier(String registrationIdentifier) {
        this.registrationIdentifier = registrationIdentifier;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
