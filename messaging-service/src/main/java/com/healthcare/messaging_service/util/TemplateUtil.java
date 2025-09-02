package com.healthcare.messaging_service.util;

import java.util.Map;

public final class TemplateUtil {
    private TemplateUtil(){}
    public static String apply(String template, Map<String, String> vars){
        String out = template;
        for (var e : vars.entrySet()){
            out = out.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }
}
