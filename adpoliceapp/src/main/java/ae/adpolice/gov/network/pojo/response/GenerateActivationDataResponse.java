package ae.adpolice.gov.network.pojo.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GenerateActivationDataResponse {
    @SerializedName("resultCodes")
    @Expose
    private ResultCodes resultCodes;
    @SerializedName("result")
    @Expose
    private ActivationData result;

    public ResultCodes getResultCodes() {
        return resultCodes;
    }

    public void setResultCodes(ResultCodes resultCodes) {
        this.resultCodes = resultCodes;
    }

    public ActivationData getResult() {
        return result;
    }

    public void setResult(ActivationData result) {
        this.result = result;
    }
}
