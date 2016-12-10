package space_ship.xyz.epills;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sidorov Leonid on 26.11.2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    //информация о БД
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "epillsDB";

    //таблица с курсами
    public static final String TABLE_COURSES = "courses";

    public static final String KEY_COURSES_ID = "_id";
    public static final String KEY_COURSES_NAME = "name";
    public static final String KEY_COURSES_DATESTART = "datestart";
    public static final String KEY_COURSES_DATEEND = "dateend";
    public static final String KEY_COURSES_RECEPTIONSCOUNT = "receptionscount";

    //таблица с приёмами таблеток курсов
    public static final String TABLE_RECEPTIONS = "receptions";

    public static final String KEY_RECEPTIONS_ID = "_id";
    public static final String KEY_RECEPTIONS_COURSEID = "courseid";
    public static final String KEY_RECEPTIONS_TIME = "time";
    public static final String KEY_RECEPTIONS_COUNT = "count";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_COURSES + "(" +
                        KEY_COURSES_ID + " integer primary key," +
                        KEY_COURSES_NAME + " text," +
                        KEY_COURSES_DATESTART + " text," +
                        KEY_COURSES_DATEEND + " text," +
                        KEY_COURSES_RECEPTIONSCOUNT + " integer" +
                    ")"
        );

        db.execSQL("create table " + TABLE_RECEPTIONS + "(" +
                KEY_RECEPTIONS_ID + " integer primary key," +
                KEY_RECEPTIONS_COURSEID + " integer," +
                KEY_RECEPTIONS_TIME + " text," +
                KEY_RECEPTIONS_COUNT + " text" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_COURSES);
        db.execSQL("drop table if exists " + TABLE_RECEPTIONS);

        onCreate(db);
    }
}
