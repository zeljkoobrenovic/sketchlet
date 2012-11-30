package com.sun.tools.internal.xjc.runtime;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ZeroOneBooleanAdapter extends XmlAdapter<String, Boolean> {
    public Boolean unmarshal(String v) {
        if (v == null) {
            return null;
        }
        return DatatypeConverter.parseBoolean(v);
    }

    public String marshal(Boolean v) {
        if (v == null) {
            return null;
        }
        if (v) {
            return "1";
        } else {
            return "0";
        }
    }
}
