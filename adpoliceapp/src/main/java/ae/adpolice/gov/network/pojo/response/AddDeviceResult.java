package ae.adpolice.gov.network.pojo.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddDeviceResult {

    @SerializedName("instanceActivationMessage")
    @Expose
    private String instanceActivationMessage;

    public String getInstanceActivationMessage() {
        return instanceActivationMessage;
    }

    public void setInstanceActivationMessage(String instanceActivationMessage) {
        this.instanceActivationMessage = instanceActivationMessage;
    }
}
