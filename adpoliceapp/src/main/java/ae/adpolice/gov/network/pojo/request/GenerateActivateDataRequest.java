package ae.adpolice.gov.network.pojo.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GenerateActivateDataRequest {
    @SerializedName("registrationIdentifier")
    @Expose
    private String registrationIdentifier;
    @SerializedName("clientEvidenceMessage")
    @Expose
    private String clientEvidenceMessage;

    public String getRegistrationIdentifier() {
        return registrationIdentifier;
    }

    public void setRegistrationIdentifier(String registrationIdentifier) {
        this.registrationIdentifier = registrationIdentifier;
    }

    public String getClientEvidenceMessage() {
        return clientEvidenceMessage;
    }

    public void setClientEvidenceMessage(String clientEvidenceMessage) {
        this.clientEvidenceMessage = clientEvidenceMessage;
    }
}
