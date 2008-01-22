/*
  BoardUpdateInformation.java / Frost
  Copyright (C) 2008  Frost Project <jtcfrost.sourceforge.net>

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
package frost.boards;

public class BoardUpdateInformation {

    // -> show non-modal dialog, change displayed board when board tree selection changes
    //      ComboBox for Boardname (extra, Text = "boardname (updcount)")
    //      ComboBox for dateString

    // - maybe compute : indices per X minutes

    private final String dateString;
    private final long dateMillis;

    private long updateInformationLastUpdated = -1;

    private int triedIndices = 0;
    private int currentIndex = -1;
    private int maxIndex = -1;
    private int maxSuccessfulIndex = -1;

    private int countADNF = 0;    // ALL_DATA_NOT_FOUND
    private int countDNF = 0;     // DATA_NOT_FOUND
    private int countInvalid = 0; // invalid msgs
    private int countValid = 0;   // valid messages

    public BoardUpdateInformation(final String newDateString, final long newDateMillis) {
        dateString = newDateString;
        dateMillis = newDateMillis;
    }

    public long getUpdateInformationLastUpdated() {
        return updateInformationLastUpdated;
    }
    public void setUpdateInformationLastUpdated(final long updateInformationLastUpdated) {
        this.updateInformationLastUpdated = updateInformationLastUpdated;
    }
    public int getTriedIndices() {
        return triedIndices;
    }
    public void setTriedIndices(final int triedIndices) {
        this.triedIndices = triedIndices;
    }
    public int getCurrentIndex() {
        return currentIndex;
    }
    public void setCurrentIndex(final int currentIndex) {
        this.currentIndex = currentIndex;
    }
    public int getMaxIndex() {
        return maxIndex;
    }
    public void setMaxIndex(final int maxIndex) {
        this.maxIndex = maxIndex;
    }
    public int getMaxSuccessfulIndex() {
        return maxSuccessfulIndex;
    }
    public void setMaxSuccessfulIndex(final int maxSuccessfulIndex) {
        this.maxSuccessfulIndex = maxSuccessfulIndex;
    }
    public int getCountADNF() {
        return countADNF;
    }
    public void setCountADNF(final int countADNF) {
        this.countADNF = countADNF;
    }
    public int getCountDNF() {
        return countDNF;
    }
    public void setCountDNF(final int countDNF) {
        this.countDNF = countDNF;
    }
    public int getCountInvalid() {
        return countInvalid;
    }
    public void setCountInvalid(final int countInvalid) {
        this.countInvalid = countInvalid;
    }
    public int getCountValid() {
        return countValid;
    }
    public void setCountValid(final int countValid) {
        this.countValid = countValid;
    }
    public String getDateString() {
        return dateString;
    }
    public long getDateMillis() {
        return dateMillis;
    }
}