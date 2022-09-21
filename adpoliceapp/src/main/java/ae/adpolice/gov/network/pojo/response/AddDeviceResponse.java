package ae.adpolice.gov.network.pojo.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddDeviceResponse {

    @SerializedName("resultCodes")
    @Expose
    private ResultCodes resultCodes;
    @SerializedName("result")
    @Expose
    private AddDeviceResult result;

    public ResultCodes getResultCodes() {
        return resultCodes;
    }

    public void setResultCodes(ResultCodes resultCodes) {
        this.resultCodes = resultCodes;
    }

    public AddDeviceResult getResult() {
        return result;
    }

    public void setResult(AddDeviceResult result) {
        this.result = result;
    }
}
