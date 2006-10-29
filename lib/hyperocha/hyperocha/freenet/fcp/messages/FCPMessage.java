/*
  FCPMessage.java / Frost
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
package hyperocha.freenet.fcp.messages;

public abstract class FCPMessage {

    // *** utilities ***
    
    protected boolean stringToBool(String s, boolean allowEmpty) throws MessageEvaluationException {
        if( s == null || s.length() == 0 ) {
            if( !allowEmpty ) {
                throw new MessageEvaluationException("Empty bool");
            } else {
                return false;
            }
        }
        return s.equalsIgnoreCase("true");
    }

    protected int stringToInt(String s, boolean allowEmpty) throws MessageEvaluationException {
        if( s == null || s.length() == 0 ) {
            if( !allowEmpty ) {
                throw new MessageEvaluationException("Empty int");
            } else {
                return 0;
            }
        }
        return Integer.parseInt(s);
    }

    protected String stringToString(String s, boolean allowEmpty) throws MessageEvaluationException {
        if( s == null || s.length() == 0 ) {
            if( !allowEmpty ) {
                throw new MessageEvaluationException("Empty string");
            } else {
                return null;
            }
        }
        return s;
    }
}
