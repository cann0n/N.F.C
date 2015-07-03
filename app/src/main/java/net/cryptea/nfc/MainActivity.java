package net.cryptea.nfc;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    private AlertDialog mDialog;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private NdefMessage ndefMessage;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(nfcAdapter == null) {
            showMessage(R.string.error, R.string.no_nfc);
        }else if(!nfcAdapter.isEnabled()) {
            showMessage(R.string.warning, R.string.nfc_disabled);
            showWirelessSettingsDialog();
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        ndefMessage = new NdefMessage(new NdefRecord[] { newTextRecord(
                "Message from NFC Reader :-)", Locale.ENGLISH, true) });

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

        if(nfcAdapter != null){
            if(!nfcAdapter.isEnabled()){
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
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }

            StringBuffer sb = new StringBuffer();
            for (NdefMessage msg : msgs){
                sb.append(msg.toString());
            }
            Log.d("Resolve Intent SB: ", sb.toString());

        }

        Log.d("Resolve Intent", "Finished");
    }


    private String dumpTagData(Parcelable p) {
        Log.d("Sump Tag data", "Start");
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("ID (reversed): ").append(getReversed(id)).append("\n");

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {

                Log.d("NFC DUMP", "Its a Mifare Classic Card");
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                }

                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(MifareUltralight.class.getName())) {

                Log.d("NFC DUMP", "Its a Mifare ultra light card.");
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }
        Log.d("NFC DUMP", sb.toString());
        if(textView != null) {

        } else {
            Log.d("NFC DUMP", "textview is empty!");
            textView = (TextView) findViewById(R.id.textView);
        }

        if(textView!= null){
            Log.d("NFC DUMP", "BAMM");
            textView.setText(sb.toString());
        }

        Log.d("Sump Tag data", "Finished");
        return sb.toString();
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }
}
