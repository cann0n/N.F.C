package net.cryptea.nfc;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
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


    public String readTag(Parcelable p) {

        Log.d("Read tag", "Start");
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;

        // Read the ID of the Tag an present it as Hex value.
        sb.append("Tag ID:").append(codecConverter.getHex(tag.getId())).append("\n");


        for (String tech : tag.getTechList()) {
            sb.append("\n").append("Technologies: ").append(tech);

            Log.d("Technologies: ", tech);

            if (tech.equals(MifareClassic.class.getName())) {
                MifareClassic mifareClassic = MifareClassic.get(tag);
                try {
                    mifareClassic.connect();
                    byte[] bytePayload = mifareClassic.readBlock(MifareClassic.BLOCK_SIZE);
                    sb.append("Payload: ").append(new String(bytePayload, Charset.forName("US-ASCII")));
                    mifareClassic.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (tech.equals(MifareUltralight.class.getName())) {
                MifareUltralight mifareUltralight = MifareUltralight.get(tag);

                try {
                    mifareUltralight.connect();
                    byte[] bytePayload = mifareUltralight.readPages(MifareUltralight.PAGE_SIZE);
                    sb.append("Payload: ").append(new String(bytePayload, Charset.forName("US-ASCII")));
                    mifareUltralight.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (tech.equals(NfcV.class.getName())) {

                NfcV nfcV = NfcV.get(tag);

                try {
                    nfcV.connect();

                    sb.append("\n\t").append("Max Transceive length: ").append(nfcV.getMaxTransceiveLength());
                    sb.append("\n\t").append("Response flags: ").append(nfcV.getResponseFlags());
                    sb.append("\n\t").append("DSF ID: ").append(nfcV.getDsfId());

                    nfcV.close();
                    Log.d("NcfV", sb.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (tech.equals(NdefFormatable.class.getName())) {


            } else {
                Log.d("Type", tech);
            }

        }




        Log.d("Dump: ", sb.toString());
        return sb.toString();

/*

        Log.d("Read tag", "Finished");


        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(codecConverter.getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(codecConverter.getDec(id)).append("\n");
        sb.append("ID (reversed): ").append(codecConverter.getReversed(id)).append("\n");

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
        Log.d("Tag dump: ", sb.toString());

        Log.d("Sump Tag data", "Finished");
        return sb.toString();
    }
*/


    }
}
