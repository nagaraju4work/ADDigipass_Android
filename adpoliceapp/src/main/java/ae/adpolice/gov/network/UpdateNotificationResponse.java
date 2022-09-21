package ae.adpolice.gov.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ae.adpolice.gov.network.pojo.response.ResultCodes;

public class UpdateNotificationResponse {
    @SerializedName("resultCodes")
    @Expose
    private ResultCodes resultCodes;

    public ResultCodes getResultCodes() {
        return resultCodes;
    }

    public void setResultCodes(ResultCodes resultCodes) {
        this.resultCodes = resultCodes;
    }
}
