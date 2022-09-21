package ae.adpolice.gov.users;



import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ae.adpolice.gov.users.pojo.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user ORDER BY last_modified DESC")
    List<User> getAll();

    @Query("SELECT * FROM user ORDER BY last_modified DESC LIMIT 1")
    User getCurrentUser();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Delete
    void delete(User user);

    @Query("UPDATE user SET dynamic_vector_key = :dynamicVector WHERE user_id=:userId")
    void updateDynamicVector(String userId,String dynamicVector);

    @Query("UPDATE user SET dynamic_vector_pin_key = :dynamicVectorPin WHERE user_id=:userId")
    void updateDynamicVectorPin(String userId,String dynamicVectorPin);

    @Query("UPDATE user SET authenticate_choice = :authenticateChoice WHERE user_id=:userId")
    void updateAuthenticateChoice(String userId,boolean authenticateChoice);

    @Update
    void update(User user);
}
