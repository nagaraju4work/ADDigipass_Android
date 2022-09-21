package ae.adpolice.gov.network.pojo.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerTimeResponse {
    @SerializedName("resultCodes")
    @Expose
    private ResultCodes resultCodes;
    @SerializedName("result")
    @Expose
    private ServerTimeResult result;

    public ResultCodes getResultCodes() {
        return resultCodes;
    }

    public void setResultCodes(ResultCodes resultCodes) {
        this.resultCodes = resultCodes;
    }

    public ServerTimeResult getResult() {
        return result;
    }

    public void setResult(ServerTimeResult result) {
        this.result = result;
    }
}
