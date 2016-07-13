package org.blocks4j.reconf.client.full;

import java.io.Serializable;

public class JsonClass implements Serializable {

    private static final long serialVersionUID = 42L;

    private String field;
    private String[] fieldArray;

    public JsonClass() {

    }

    public String getField() {
        return this.field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String[] getFieldArray() {
        return this.fieldArray;
    }

    public void setFieldArray(String[] fieldArray) {
        this.fieldArray = fieldArray;
    }
}
