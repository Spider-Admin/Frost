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
package frost.messaging.frost.boards;

import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import frost.MainFrame;
import frost.util.DateFun;
import frost.util.FormatterUtils;

public class BoardUpdateInformation {

    private final Board board;
    private final String dateString;
    private final long dateMillis;

    private int countTriedIndices = 0;
    private int currentIndex = -1;
    private int maxIndex = -1;
    private int maxSuccessfulIndex = -1;

    private int countADNF = 0;    // ALL_DATA_NOT_FOUND
    private int countDNF = 0;     // DATA_NOT_FOUND
    private int countInvalid = 0; // invalid msgs
    private int countValid = 0;   // valid messages

    private long nodeTime = 0;

    private int subsequentInvalidMsgs = 0; // subsequent countADNF + countInvalid

    // if false this board is not updated for this day. used in DoS detection
    private Boolean isBoardUpdateAllowed = null;

    public BoardUpdateInformation(final Board newBoard, final String newDateString, final long newDateMillis) {
        board = newBoard;
        dateString = newDateString;
        dateMillis = newDateMillis;
    }

    public int getCountTriedIndices() {
        return countTriedIndices;
    }
    public void incCountTriedIndices() {
        this.countTriedIndices++;
    }
    public int getCurrentIndex() {
        return currentIndex;
    }
    public void setCurrentIndex(final int currentIndex) {
        this.currentIndex = currentIndex;
        if( maxIndex < currentIndex ) {
            maxIndex = currentIndex;
        }
    }
    public int getMaxIndex() {
        return maxIndex;
    }
    public int getMaxSuccessfulIndex() {
        return maxSuccessfulIndex;
    }
    public void updateMaxSuccessfulIndex(final int lMaxSuccessfulIndex) {
        if( this.maxSuccessfulIndex < lMaxSuccessfulIndex ) {
            this.maxSuccessfulIndex = lMaxSuccessfulIndex;
        }
    }
    public int getCountADNF() {
        return countADNF;
    }
    public void incCountADNF() {
        this.countADNF++;
        incSubsequentInvalidMsgs();
    }
    public int getCountDNF() {
        return countDNF;
    }
    public void incCountDNF() {
        this.countDNF++;
    }
    public int getCountInvalid() {
        return countInvalid;
    }
    public void incCountInvalid() {
        this.countInvalid++;
        incSubsequentInvalidMsgs();
    }
    public int getCountValid() {
        return countValid;
    }
    public void incCountValid() {
        this.countValid++;
        resetSubsequentInvalidMsgs();
    }
    public String getDateString() {
        return dateString;
    }
    public long getDateMillis() {
        return dateMillis;
    }
    public Board getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return dateString;
    }

    public long getNodeTime() {
        return nodeTime;
    }

    public void addNodeTime(final long addNodeTime) {
        this.nodeTime += addNodeTime;
    }

    public String getInfoString() {
        final int dayCount = (int)(getNodeTime() / (1000L*60L*60L*24L));
        final StringBuilder sb = new StringBuilder();
        sb.append("Board: ").append(getBoard().getName()).append("\n");
        sb.append("Date : ").append(getDateString()).append("\n");
        sb.append("\n");
        sb.append("Informations for current session:").append("\n");
        sb.append("\n");
        sb.append("nodeTime: ").append(dayCount).append("d ").append(DateFun.FORMAT_TIME_PLAIN.print(nodeTime)).append("  (").append(FormatterUtils.formatFraction((nodeTime/1000L), getCountTriedIndices())).append(" s/req)\n");
        sb.append("\n");
        sb.append("countTriedIndices : ").append(getCountTriedIndices()).append("\n");
        sb.append("currentIndex      : ").append(getCurrentIndex()).append("\n");
        sb.append("maxIndex          : ").append(getMaxIndex()).append("\n");
        sb.append("maxSuccessfulIndex: ").append(getMaxSuccessfulIndex()).append("\n");
        sb.append("\n");
        sb.append("countADNF   : ").append(getCountADNF()).append("  (").append(FormatterUtils.formatPercent(getCountADNF(),getCountTriedIndices())).append("%)\n");
        sb.append("countDNF    : ").append(getCountDNF()).append("  (").append(FormatterUtils.formatPercent(getCountDNF(),getCountTriedIndices())).append("%)\n");
        sb.append("countInvalid: ").append(getCountInvalid()).append("  (").append(FormatterUtils.formatPercent(getCountInvalid(),getCountTriedIndices())).append("%)\n");
        sb.append("countValid  : ").append(getCountValid()).append("  (").append(FormatterUtils.formatPercent(getCountValid(),getCountTriedIndices())).append("%)\n");
        sb.append("subsequentFailures: ").append(getSubsequentInvalidMsgs()).append("\n");
        return sb.toString();
    }

    public static String getSummaryInfoString(final List<Board> boardList) {
        final LocalDate localDate = new LocalDate(DateTimeZone.UTC);
		final long dateMillis = localDate.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis();
        final String dirDateString = DateFun.FORMAT_DATE.print(localDate);

        long sumNodeTimeToday = 0;
        int sumCountTriedIndicesToday = 0;
        int sumCountADNFToday = 0;    // ALL_DATA_NOT_FOUND
        int sumCountDNFToday = 0;     // DATA_NOT_FOUND
        int sumCountInvalidToday = 0; // invalid msgs
        int sumCountValidToday = 0;   // valid messages

        long sumNodeTimeOverall = 0;
        int sumCountTriedIndicesOverall = 0;
        int sumCountADNFOverall = 0;    // ALL_DATA_NOT_FOUND
        int sumCountDNFOverall = 0;     // DATA_NOT_FOUND
        int sumCountInvalidOverall = 0; // invalid msgs
        int sumCountValidOverall = 0;   // valid messages

        for( final Board b : boardList ) {
            if( b.hasBoardUpdateInformations() ) {
                final List<BoardUpdateInformation> l = b.getBoardUpdateInformationList();
                for( final BoardUpdateInformation bui : l ) {
                    if( bui.getDateMillis() == dateMillis ) {
                        sumNodeTimeToday += bui.getNodeTime();
                        sumCountTriedIndicesToday += bui.getCountTriedIndices();
                        sumCountADNFToday += bui.getCountADNF();
                        sumCountDNFToday += bui.getCountDNF();
                        sumCountInvalidToday += bui.getCountInvalid();
                        sumCountValidToday += bui.getCountValid();
                    }
                    sumNodeTimeOverall += bui.getNodeTime();
                    sumCountTriedIndicesOverall += bui.getCountTriedIndices();
                    sumCountADNFOverall += bui.getCountADNF();
                    sumCountDNFOverall += bui.getCountDNF();
                    sumCountInvalidOverall += bui.getCountInvalid();
                    sumCountValidOverall += bui.getCountValid();
                }
            }
        }
        final int dayCountToday = (int)(sumNodeTimeToday / (1000L*60L*60L*24L));
        final int dayCountOverall = (int)(sumNodeTimeOverall / (1000L*60L*60L*24L));
        final String infoString = new StringBuilder()
            .append("Summary for current session:").append("\n")
            .append("\n")
            .append("*** Today (").append(dirDateString).append(") ***\n")
            .append("\n")
            .append("nodeTime: ").append(dayCountToday).append("d ").append(DateFun.FORMAT_TIME_PLAIN.print(sumNodeTimeToday)).append("  (").append(FormatterUtils.formatFraction((sumNodeTimeToday/1000L), sumCountTriedIndicesToday)).append(" s/req)\n")
            .append("countTriedIndices : ").append(sumCountTriedIndicesToday).append("\n")
            .append("countADNF   : ").append(sumCountADNFToday).append("  (").append(FormatterUtils.formatPercent(sumCountADNFToday,sumCountTriedIndicesToday)).append("%)\n")
            .append("countDNF    : ").append(sumCountDNFToday).append("  (").append(FormatterUtils.formatPercent(sumCountDNFToday,sumCountTriedIndicesToday)).append("%)\n")
            .append("countInvalid: ").append(sumCountInvalidToday).append("  (").append(FormatterUtils.formatPercent(sumCountInvalidToday,sumCountTriedIndicesToday)).append("%)\n")
            .append("countValid  : ").append(sumCountValidToday).append("  (").append(FormatterUtils.formatPercent(sumCountValidToday,sumCountTriedIndicesToday)).append("%)\n")
            .append("\n")
            .append("*** Overall ***\n")
            .append("\n")
            .append("nodeTime: ").append(dayCountOverall).append("d ").append(DateFun.FORMAT_TIME_PLAIN.print(sumNodeTimeOverall)).append("  (").append(FormatterUtils.formatFraction((sumNodeTimeOverall/1000L), sumCountTriedIndicesOverall)).append(" s/req)\n")
            .append("countTriedIndices: ").append(sumCountTriedIndicesOverall).append("\n")
            .append("countValid  : ").append(sumCountValidOverall).append("  (").append(FormatterUtils.formatPercent(sumCountValidOverall,sumCountTriedIndicesOverall)).append("%)\n")
            .append("countInvalid: ").append(sumCountInvalidOverall).append("  (").append(FormatterUtils.formatPercent(sumCountInvalidOverall,sumCountTriedIndicesOverall)).append("%)\n")
            .append("countADNF   : ").append(sumCountADNFOverall).append("  (").append(FormatterUtils.formatPercent(sumCountADNFOverall,sumCountTriedIndicesOverall)).append("%)\n")
            .append("countDNF    : ").append(sumCountDNFOverall).append("  (").append(FormatterUtils.formatPercent(sumCountDNFOverall,sumCountTriedIndicesOverall)).append("%)\n")
            .toString();

        return infoString;
    }

    /**
     * Check allowed state, and apply and promote it to gui.
     */
    public synchronized boolean updateBoardUpdateAllowedState() {
        this.isBoardUpdateAllowed = Boolean.valueOf( checkBoardUpdateAllowedState() );
        return this.isBoardUpdateAllowed.booleanValue();
    }

    /**
     * Check allowed state, but don't apply and promote it to gui.
     */
    public synchronized boolean checkBoardUpdateAllowedState() {
        if( MainFrame.getInstance().getMessagingTab().getTofTree().isStopBoardUpdatesWhenDOSed() == false ) {
            return true;
        } else {
            final int maxSubsequentFailuresAllowed = MainFrame.getInstance().getMessagingTab().getTofTree().getMaxInvalidMessagesPerDayThreshold();
            if( getSubsequentInvalidMsgs() > maxSubsequentFailuresAllowed ) {
                return false;
            } else {
                return true;
            }
        }
    }

    public synchronized boolean isBoardUpdateAllowed() {
        if( isBoardUpdateAllowed == null ) {
            updateBoardUpdateAllowedState();
        }
        return isBoardUpdateAllowed.booleanValue();
    }

    private int getSubsequentInvalidMsgs() {
        return subsequentInvalidMsgs;
    }
    public void resetSubsequentInvalidMsgs() {
        subsequentInvalidMsgs = 0;
    }
    private void incSubsequentInvalidMsgs() {
        subsequentInvalidMsgs++;
    }
}
