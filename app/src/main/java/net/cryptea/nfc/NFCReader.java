package net.cryptea.nfc;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by keyo on 08/07/15.
 * <p/>
 * <p/>
 * Documentation links;
 * https://developer.android.com/guide/topics/connectivity/nfc/index.html
 * https://github.com/nadam/nfc-reader
 */
public class NFCReader {

    CodecConverter codecConverter = new CodecConverter();
    private StringBuilder sb;


    public String readTag(Parcelable p) {

        Log.d("Read tag", "Start");
        sb = new StringBuilder();
        Tag tag = (Tag) p;

        // Read the ID of the Tag an present it as Hex value.
        sb.append("Tag ID:").append(codecConverter.getHex(tag.getId())).append("\n");


        for (String tech : tag.getTechList()) {
            sb.append("\n").append("Technologies: ").append(tech);

            Log.d("Technologies: ", tech);

            if (tech.equals(MifareClassic.class.getName())) {
                mifareClassic(tag);
            } else if (tech.equals(MifareUltralight.class.getName())) {
                mifareUltralight(tag);
            } else if (tech.equals(NfcV.class.getName())) {
                nfcV(tag);
            }  else if (tech.equals(NfcA.class.getName())) {
                nfcA(tag);
            } else if (tech.equals(NdefFormatable.class.getName())) {
                // Nothing todo here!
            } else {
                Log.d("Type", tech);
            }

        }


        Log.d("Dump: ", sb.toString());
        return sb.toString();
    }


    private void mifareClassic(Tag tag) {
        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            byte[] bytePayload = mifareClassic.readBlock(MifareClassic.BLOCK_SIZE);
            sb.append("Payload: ").append(new String(bytePayload, Charset.forName("US-ASCII")));
            mifareClassic.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void mifareUltralight(Tag tag) {
        MifareUltralight mifareUltralight = MifareUltralight.get(tag);

        try {
            mifareUltralight.connect();
            byte[] bytePayload = mifareUltralight.readPages(MifareUltralight.PAGE_SIZE);
            sb.append("Payload: ").append(new String(bytePayload, Charset.forName("US-ASCII")));
            mifareUltralight.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nfcV(Tag tag) {
        try {

            NfcV nfcV = NfcV.get(tag);

            nfcV.connect();

            sb.append("\n\t").append("Max Transceive length: ").append(nfcV.getMaxTransceiveLength());
            sb.append("\n\t").append("Response flags: ").append(nfcV.getResponseFlags());
            sb.append("\n\t").append("DSF ID: ").append(nfcV.getDsfId());

            nfcV.close();
            Log.d("NcfV", sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void nfcA(Tag tag){
        NfcA nfcA = NfcA.get(tag);
        try {
            nfcA.connect();
            Short s = nfcA.getSak();
            byte[] a = nfcA.getAtqa();
            sb.append("SAK: ").append(s).append("\n");
            sb.append("ATQA").append(codecConverter.getHex(a)).append("\n");
            nfcA.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
