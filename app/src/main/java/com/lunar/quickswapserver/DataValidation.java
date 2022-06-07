package com.lunar.quickswapserver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataValidation {


    public static boolean phoneValidator(String phone) {
        if (phone.matches("0[0-9]{9}")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean number(String numberr) {
        String n = "^[0-9]{1,7}$";
        Pattern pat = Pattern.compile(n);
        Matcher match = pat.matcher(numberr);
        if (match.matches()) {
            return true;
        } else {
            return false;
        }

    }
}
