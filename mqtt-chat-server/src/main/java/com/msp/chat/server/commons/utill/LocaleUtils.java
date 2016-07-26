package com.msp.chat.server.commons.utill;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
public class LocaleUtils {
    private static ResourceBundle messages = null;
    private static Locale locale = null;

    private static void getLocale() {
        String[] localeArray;
        String localeProperty = BrokerConfig.getProperty("locale");
        if (localeProperty != null) {
            localeArray = localeProperty.split("_");
        }
        else {
            localeArray = new String[] {"", ""};
        }

        String language = localeArray[0];
        if (language == null) {
            language = "";
        }
        String country = "";
        if (localeArray.length == 2) {
            country = localeArray[1];
        }

        if (language.equals("") && country.equals("")) {
            locale = Locale.getDefault();
        }
        else {
            locale = new Locale(language, country);
        }
    }

    public static void init() {
        File file = new File("./resource");

        try {
            URL[] urls = {file.toURI().toURL()};
            ClassLoader loader = new URLClassLoader(urls);
            getLocale();
            messages= ResourceBundle.getBundle("message",locale, loader);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static String getLocalizedString(String key) {
        return messages.getString(key);
    }
}
