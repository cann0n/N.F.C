package net.cryptea.nfc;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Parcelable;
import android.util.Log;

import net.cryptea.nfc.Database.NfcObject;

import java.io.IOException;
import java.nio.charset.Charset;

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
    private NfcObject nfcObject;
    private StringBuffer payload = new StringBuffer();


    public NfcObject readTag(Parcelable p) {

        Log.d("Read tag", "Start");

        nfcObject = new NfcObject();

        Tag tag = (Tag) p;

        // Read the ID of the Tag an present it as Hex value.
        nfcObject.setId(codecConverter.getHex(tag.getId()));


        for (String tech : tag.getTechList()) {
            Log.d("Technologies: ", tech);

            nfcObject.addType(tech);

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

        return nfcObject;
    }


    private void mifareClassic(Tag tag) {
        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            byte[] bytePayload = mifareClassic.readBlock(MifareClassic.BLOCK_SIZE);
            payload.append("Payload: ").append(new String(bytePayload, Charset.forName("US-ASCII")));
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
            payload.append("Payload: ").append(new String(bytePayload, Charset.forName("US-ASCII")));
            mifareUltralight.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nfcV(Tag tag) {
        try {

            NfcV nfcV = NfcV.get(tag);

            nfcV.connect();
            payload.append("\n\t").append("Max Transceive length: ").append(nfcV.getMaxTransceiveLength());
            payload.append("\n\t").append("Response flags: ").append(nfcV.getResponseFlags());
            payload.append("\n\t").append("DSF ID: ").append(nfcV.getDsfId());
            nfcV.close();

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
            payload.append("SAK: ").append(s).append("\n");
            payload.append("ATQA").append(codecConverter.getHex(a)).append("\n");
            nfcA.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
