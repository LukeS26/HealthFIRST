package HealthFIRST.dataschema;

import org.bson.Document;

public abstract class DataSchema {
    public abstract Document toDoc();

    public static Object fromDoc(Document doc) {
        return new Object();
    }
}
