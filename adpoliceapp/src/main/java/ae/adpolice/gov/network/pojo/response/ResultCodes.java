package ae.adpolice.gov.network.pojo.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultCodes {
    @SerializedName("returnCodeEnum")
    @Expose
    private String returnCodeEnum;
    @SerializedName("statusCodeEnum")
    @Expose
    private String statusCodeEnum;
    @SerializedName("returnCode")
    @Expose
    private Integer returnCode;
    @SerializedName("statusCode")
    @Expose
    private Integer statusCode;

    public String getReturnCodeEnum() {
        return returnCodeEnum;
    }

    public void setReturnCodeEnum(String returnCodeEnum) {
        this.returnCodeEnum = returnCodeEnum;
    }

    public String getStatusCodeEnum() {
        return statusCodeEnum;
    }

    public void setStatusCodeEnum(String statusCodeEnum) {
        this.statusCodeEnum = statusCodeEnum;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

}
