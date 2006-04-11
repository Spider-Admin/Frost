/*
  PropLanguage.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.util.gui.translation;

import java.text.*;
import java.util.*;

public class PropLanguage {
    
    public PropLanguage() {
        this(Locale.getDefault());
    }

    public PropLanguage(String locale) {
        this(getLocaleFromString(locale));
    }

    public PropLanguage(Locale l) {
        locale = l;
        rb = ResourceBundle.getBundle("res/language", locale);
    }

    private Locale locale = null;
    private ResourceBundle rb = null;

    private static Locale getLocaleFromString(String locale) {
        int pos = locale.indexOf("_");
        Locale l;
        if( pos == -1 ) {
            l = new Locale(locale);
        } else {
            String lang = locale.substring(0, pos);
            String rest = locale.substring(pos + 1);
            pos = locale.indexOf("_");
            if( pos == 0 ) {
                l = new Locale(lang, rest);
            } else {
                String country = locale.substring(0, pos);
                rest = locale.substring(pos + 1);
                l = new Locale(lang, country, rest);
            }
        }
        return l;
    }

    public static String get(String name) {
//        return FIWSystem.getInstance().getI18n().getString(name);
        return null;
    }

    public static String format(String format, Object param) {
        return format(format, new Object[] { param });
    }

    public static String format(String format, Object[] params) {
//        I18n i = FIWSystem.getInstance().getI18n();
//        String form = i.getString(format);
//        return new MessageFormat(form, i.getLocale()).format(params);
        return null;
    }

    public String getString(String name) {
        return rb.getString(name);
    }

    public Locale getLocale() {
        return locale;
    }
}
