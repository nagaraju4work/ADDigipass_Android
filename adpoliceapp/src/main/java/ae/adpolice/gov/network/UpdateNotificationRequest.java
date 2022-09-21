package ae.adpolice.gov.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateNotificationRequest {
    @SerializedName("userID")
    @Expose
    private String userID;
    @SerializedName("domain")
    @Expose
    private String domain;
    @SerializedName("digipassInstanceID")
    @Expose
    private String digipassInstanceID;
    @SerializedName("encryptedNotificationID")
    @Expose
    private String encryptedNotificationID;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDigipassInstanceID() {
        return digipassInstanceID;
    }

    public void setDigipassInstanceID(String digipassInstanceID) {
        this.digipassInstanceID = digipassInstanceID;
    }

    public String getEncryptedNotificationID() {
        return encryptedNotificationID;
    }

    public void setEncryptedNotificationID(String encryptedNotificationID) {
        this.encryptedNotificationID = encryptedNotificationID;
    }

}
