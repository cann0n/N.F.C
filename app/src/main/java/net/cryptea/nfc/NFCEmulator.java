package net.cryptea.nfc;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by hannesgraf on 07.10.15.
 */
public class NFCEmulator extends HostApduService {

    public NFCEmulator() {
        super();
        Log.d("NFCEmulator", "Starting Constructor");
    }

    @Override
    public byte[] processCommandApdu(byte[] bytes, Bundle bundle) {
        Log.d("NFCEmulator", "Finished " + new String(bytes));
        return new byte[0];
    }

    @Override
    public void onDeactivated(int i) {
        Log.d("NFCEmulator", "onDeactivated called");
    }
}
