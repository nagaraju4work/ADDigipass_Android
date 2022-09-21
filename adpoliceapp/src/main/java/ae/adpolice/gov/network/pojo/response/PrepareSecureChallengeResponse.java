package ae.adpolice.gov.network.pojo.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PrepareSecureChallengeResponse {

    @SerializedName("resultCodes")
    @Expose
    private ResultCodes resultCodes;

    @SerializedName("result")
    @Expose
    private ParseSecureChallengeResult result;

    public ResultCodes getResultCodes() {
        return resultCodes;
    }

    public void setResultCodes(ResultCodes resultCodes) {
        this.resultCodes = resultCodes;
    }

    public ParseSecureChallengeResult getResult() {
        return result;
    }

    public void setResult(ParseSecureChallengeResult result) {
        this.result = result;
    }
}
