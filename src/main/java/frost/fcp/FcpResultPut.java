/*
  FcpResultsPut.java / Frost
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
package frost.fcp;

public class FcpResultPut {

    public static final int Success      = 1;
    public static final int KeyCollision = 2;
    public static final int Error        = 3;
    public static final int Retry        = 4;
    public static final int NoConnection = 5;
    
    public static final FcpResultPut ERROR_RESULT = new FcpResultPut(Error);
    public static final FcpResultPut NO_CONNECTION_RESULT = new FcpResultPut(NoConnection);
    
    private int resultVal;
    private String chkKey;
    
    private int returnCode = -1;
    private String codeDescription = null;
    private boolean isFatal = false;

    public FcpResultPut(int result) {
        this(result, null);
    }

    public FcpResultPut(int result, int rc, String cd, boolean fatal) {
        this(result, null);
        returnCode = rc;
        codeDescription = cd;
        isFatal = fatal;
    }

    public FcpResultPut(int result, String chk) {
        resultVal = result;
        chkKey = chk;
    }

    public boolean isSuccess() {
        return (resultVal == Success);
    }
    public boolean isKeyCollision() {
        return (resultVal == KeyCollision);
    }
    public boolean isError() {
        return (resultVal == Error) || (resultVal == NoConnection);
    }
    public boolean isRetry() {
        return (resultVal == Retry);
    }
    public boolean isNoConnection() {
        return (resultVal == NoConnection);
    }

    public String getChkKey() {
        return chkKey;
    }
    
    public boolean isFatal() {
        return isFatal;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getCodeDescription() {
        return codeDescription;
    }
}
