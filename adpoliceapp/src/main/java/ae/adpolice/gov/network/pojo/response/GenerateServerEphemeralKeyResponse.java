package ae.adpolice.gov.network.pojo.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GenerateServerEphemeralKeyResponse {
    @SerializedName("resultCodes")
    @Expose
    private ResultCodes resultCodes;
    @SerializedName("result")
    @Expose
    private ServerEphemeralKey result;

    public ResultCodes getResultCodes() {
        return resultCodes;
    }

    public void setResultCodes(ResultCodes resultCodes) {
        this.resultCodes = resultCodes;
    }

    public ServerEphemeralKey getResult() {
        return result;
    }

    public void setResult(ServerEphemeralKey result) {
        this.result = result;
    }
}
