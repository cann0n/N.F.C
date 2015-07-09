package net.cryptea.nfc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by keyo on 09/07/15.
 */
public class NfcDBHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + nfcEntry.TABLE_NAME + " (" +
                    nfcEntry._ID + " INTEGER PRIMARY KEY," +
                    nfcEntry.COLUMN_NAME_IMAGE + TEXT_TYPE + COMMA_SEP +
                    nfcEntry.COLUMN_NAME_TYPES + TEXT_TYPE + COMMA_SEP +
                    nfcEntry.COLUMN_NAME_PAYLOAD + TEXT_TYPE + COMMA_SEP +

                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + nfcEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NfcDB.db";


    public NfcDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static abstract class nfcEntry implements BaseColumns {
        public static final String TABLE_NAME = "id";
        public static final String COLUMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_TYPES = "types";
        public static final String COLUMN_NAME_PAYLOAD = "payload";
    }

}
