package ae.adpolice.gov.users.pojo;



import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "user")
public class User implements Serializable {

    @ColumnInfo(name = "user_id")
    @PrimaryKey
    @NonNull
    private String userId="";

    @ColumnInfo(name = "dynamic_vector_key")
    private String dynamicVectorKey;

    @ColumnInfo(name = "dynamic_vector_pin_key")
    private String dynamicVectorPinKey;

    @ColumnInfo(name = "static_vector_key")
    private String staticVectorKey;

    @ColumnInfo(name = "last_modified")
    private long lastModified;

    @ColumnInfo(name = "authenticate_choice")
    private boolean authenticateChoice; //false for Pin, true for BioMetric


    public boolean isAuthenticateChoice() {
        return authenticateChoice;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }


    public void setAuthenticateChoice(boolean authenticateChoice) {
        this.authenticateChoice = authenticateChoice;
    }


    public String getDynamicVectorKey() {
        return dynamicVectorKey;
    }

    public void setDynamicVectorKey(String dynamicVectorKey) {
        this.dynamicVectorKey = dynamicVectorKey;
    }

    public String getDynamicVectorPinKey() {
        return dynamicVectorPinKey;
    }

    public void setDynamicVectorPinKey(String dynamicVectorPinKey) {
        this.dynamicVectorPinKey = dynamicVectorPinKey;
    }

    public String getStaticVectorKey() {
        return staticVectorKey;
    }

    public void setStaticVectorKey(String staticVectorKey) {
        this.staticVectorKey = staticVectorKey;
    }

    @NonNull
    @Override
    public String toString() {
        return userId;
    }
}
