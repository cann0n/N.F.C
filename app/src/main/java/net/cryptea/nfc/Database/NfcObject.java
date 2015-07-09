package net.cryptea.nfc.Database;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keyo on 09/07/15.
 */
public class NfcObject {

    private String id;
    private String db_id;
    private List<String> types = new ArrayList<>();
    private String payload;


    public void addType(String type) {
        types.add(type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDb_id() {
        return db_id;
    }

    public void setDb_id(String db_id) {
        this.db_id = db_id;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getTypesAsString() {
        StringBuffer sb = new StringBuffer();

        for (String type : types)
            sb.append(type);

        return sb.toString();
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
