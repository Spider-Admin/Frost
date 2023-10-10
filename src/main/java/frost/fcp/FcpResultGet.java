/*
  FcpResultsGet.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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

/**
 * This class is a utility class to provide a datatype for results
 * returned from an FCP operation.
 */
public class FcpResultGet {

    private final boolean isSuccess;

    private int returnCode = -1;
    private String codeDescription = null;
    private String redirectURI = null;
    private boolean isFatal = false;

    public static FcpResultGet RESULT_FAILED = new FcpResultGet(false);

    public FcpResultGet(final boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public FcpResultGet(final boolean isSuccess, final int rc, final String cd, final boolean fatal, final String redirectUri) {
        this.isSuccess = isSuccess;
        returnCode = rc;
        codeDescription = cd;
        isFatal = fatal;
        redirectURI = redirectUri;
    }

    public boolean isFatal() {
        return isFatal;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getCodeDescription() {
        return codeDescription;
    }

    public String getRedirectURI() {
        return redirectURI;
    }
}
