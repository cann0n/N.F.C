package net.cryptea.nfc;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import net.cryptea.nfc.Database.NfcDBConnector;
import net.cryptea.nfc.Database.NfcDBHelper;
import net.cryptea.nfc.Database.NfcObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    private AlertDialog mDialog;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private NdefMessage ndefMessage;
    private TextView textView;
    private NfcDBHelper nfcDBHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // DEVELOP Remove all DB
        getBaseContext().deleteDatabase(NfcDBConnector.DATABASE_NAME);

        mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            showMessage(R.string.error, R.string.no_nfc);
        } else if (!nfcAdapter.isEnabled()) {
            showMessage(R.string.warning, R.string.nfc_disabled);
            showWirelessSettingsDialog();
        }


        nfcDBHelper = new NfcDBHelper(getBaseContext());

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        ndefMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                "Message from NFC Reader :-)", Locale.ENGLISH, true)});

        getMenuInflater().inflate(R.menu.menu_main, menu);

        textView = (TextView) findViewById(R.id.textView);

        return true;
    }


    private void showMessage(int title, int message) {
        mDialog.setTitle(title);
        mDialog.setMessage(getText(message));
        mDialog.show();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("On new Intent", "Start");

        setIntent(intent);
        resolveIntent(intent);

        Log.d("One new Intent", "Finished");
    }

    @Override
    protected void onResume() {
        Log.d("On Resume", "Start");

        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                showWirelessSettingsDialog();
            }
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            nfcAdapter.enableForegroundNdefPush(this, ndefMessage);
        }

        resolveIntent(getIntent());
        Log.d("On Resume", "Finished");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showWirelessSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.nfc_disabled);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.create().show();
        return;
    }


    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }


    private void resolveIntent(Intent intent) {

        Log.d("Resolve Intent", "Start");


        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                NFCReader nfcReader = new NFCReader();

                NfcObject nfcO = nfcReader.readTag(tag);
                StringBuffer result = new StringBuffer();
                result.append("Tag ID: ").append(nfcO.getId()).append("\n");
                result.append("TagType: ").append(nfcO.getTypesAsString()).append("\n");
                result.append("Payload: ").append(nfcO.getPayload()).append("\n");
                ((TextView) findViewById(R.id.textView)).setText(result);


                if (nfcDBHelper != null) {
                    nfcDBHelper.write(nfcO);
                    Log.d("DB: ", "Good news!");
                } else {
                    nfcDBHelper = new NfcDBHelper(getBaseContext());
                    nfcDBHelper.write(nfcO);
                    Log.d("DB: ", "Good news!");
                }



              /*  byte[] payload = result.getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});


                for (NdefRecord re : msg.getRecords())
                    try {
                        Log.d("---> READ", readText(re));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }


                msgs = new NdefMessage[]{msg};*/
            }

        }

        Log.d("Resolve Intent", "Finished");
    }


    private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }
}
