package ae.adpolice.gov.network.pojo.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerEphemeralKey {

    @SerializedName("serverEphemeralPublicKey")
    @Expose
    private String serverEphemeralPublicKey;
    @SerializedName("salt")
    @Expose
    private String salt;

    public String getServerEphemeralPublicKey() {
        return serverEphemeralPublicKey;
    }

    public void setServerEphemeralPublicKey(String serverEphemeralPublicKey) {
        this.serverEphemeralPublicKey = serverEphemeralPublicKey;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
