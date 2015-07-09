package net.cryptea.nfc.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by keyo on 09/07/15.
 */
public class NfcDBHelper {

    private NfcDBConnector nfcDBConnector;

    public NfcDBHelper(Context context) {

        nfcDBConnector = new NfcDBConnector(context);
    }


    public void write(NfcObject tag) {
        // Gets the data repository in write mode
        SQLiteDatabase db = nfcDBConnector.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(NfcDBConnector.NfcEntry.COLUMN_NAME_ID, tag.getId());
        values.put(NfcDBConnector.NfcEntry.COLUMN_NAME_PAYLOAD, tag.getPayload());
        values.put(NfcDBConnector.NfcEntry.COLUMN_NAME_TYPES, tag.getTypesAsString());
        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                NfcDBConnector.NfcEntry.TABLE_NAME,
                "null",
                values);
    }


    public void search(NfcObject tag) {

    }

    public List<NfcObject> getAllCards() {
        return null;
    }


    public boolean tagExists(NfcObject tag) {

        return false;
    }

}
