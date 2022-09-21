package ae.adpolice.gov.network.pojo.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthUserResponse {

    @SerializedName("resultCodes")
    @Expose
    private ResultCodes resultCodes;
    @SerializedName("result")
    @Expose
    private AuthUserResult result;

    public ResultCodes getResultCodes() {
        return resultCodes;
    }

    public void setResultCodes(ResultCodes resultCodes) {
        this.resultCodes = resultCodes;
    }

    public AuthUserResult getResult() {
        return result;
    }

    public void setResult(AuthUserResult result) {
        this.result = result;
    }
}
