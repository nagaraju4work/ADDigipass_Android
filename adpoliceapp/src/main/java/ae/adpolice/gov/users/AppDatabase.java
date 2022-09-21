package ae.adpolice.gov.users;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import ae.adpolice.gov.users.pojo.User;

@Database(entities = {User.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}
