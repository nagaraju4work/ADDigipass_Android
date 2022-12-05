package ae.adpolice.gov.utils;


import ae.adpolice.gov.BuildConfig;

public class Crashlytics {
    public static void logException(Exception e){
        if(BuildConfig.DEBUG){
            e.printStackTrace();
        }
    }

    public static void logException(Throwable t){
        if(BuildConfig.DEBUG){
            t.printStackTrace();
        }
    }
}
