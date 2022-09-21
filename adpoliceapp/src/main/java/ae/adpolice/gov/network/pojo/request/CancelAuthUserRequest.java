package ae.adpolice.gov.network.pojo.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CancelAuthUserRequest {
    @SerializedName("serialNumber")
    @Expose
    private String serialNumber;
    @SerializedName("challengeKey")
    @Expose
    private String challengeKey;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getChallengeKey() {
        return challengeKey;
    }

    public void setChallengeKey(String challengeKey) {
        this.challengeKey = challengeKey;
    }
}
