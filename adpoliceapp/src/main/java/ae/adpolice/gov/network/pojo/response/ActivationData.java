package ae.adpolice.gov.network.pojo.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ActivationData {

    @SerializedName("serverEvidenceMessage")
    @Expose
    private String serverEvidenceMessage;
    @SerializedName("encryptedLicenseActivationMessage")
    @Expose
    private String encryptedLicenseActivationMessage;
    @SerializedName("encryptedCounter")
    @Expose
    private String encryptedCounter;
    @SerializedName("mac")
    @Expose
    private String mac;

    public String getServerEvidenceMessage() {
        return serverEvidenceMessage;
    }

    public void setServerEvidenceMessage(String serverEvidenceMessage) {
        this.serverEvidenceMessage = serverEvidenceMessage;
    }

    public String getEncryptedLicenseActivationMessage() {
        return encryptedLicenseActivationMessage;
    }

    public void setEncryptedLicenseActivationMessage(String encryptedLicenseActivationMessage) {
        this.encryptedLicenseActivationMessage = encryptedLicenseActivationMessage;
    }

    public String getEncryptedCounter() {
        return encryptedCounter;
    }

    public void setEncryptedCounter(String encryptedCounter) {
        this.encryptedCounter = encryptedCounter;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }


}
