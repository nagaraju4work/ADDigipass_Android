package ae.adpolice.gov.utils;


public interface ActivationCallback {

    void onActivationSuccess();

    void onActivationFailure(String error);

}
