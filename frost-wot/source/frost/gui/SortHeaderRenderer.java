package frost.gui;

/*
=====================================================================

  SortHeaderRenderer.java

  Created by Claude Duguay

=====================================================================
*/

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

public class SortHeaderRenderer extends DefaultTableCellRenderer
{
    static class SortArrowIcon implements Icon
    {
        public static final int NONE = 0;
        public static final int DECENDING = 1;
        public static final int ASCENDING = 2;

        protected int direction;
        protected int width = 8;
        protected int height = 8;

        public SortArrowIcon(int direction)
        {
            this.direction = direction;
        }

        public int getIconWidth()
        {
            return width;
        }

        public int getIconHeight()
        {
            return height;
        }

        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            Color bg = c.getBackground();
            Color light = bg.brighter();
            Color shade = bg.darker();

            int w = width;
            int h = height;
            int m = w / 2;
            if( direction == ASCENDING )
            {
                g.setColor(shade);
                g.drawLine(x, y, x + w, y);
                g.drawLine(x, y, x + m, y + h);
                g.setColor(light);
                g.drawLine(x + w, y, x + m, y + h);
            }
            if( direction == DECENDING )
            {
                g.setColor(shade);
                g.drawLine(x + m, y, x, y + h);
                g.setColor(light);
                g.drawLine(x, y + h, x + w, y + h);
                g.drawLine(x + m, y, x + w, y + h);
            }
        }
    }


    public static Icon NONSORTED = new SortArrowIcon(SortArrowIcon.NONE);
    public static Icon ASCENDING = new SortArrowIcon(SortArrowIcon.ASCENDING);
    public static Icon DECENDING = new SortArrowIcon(SortArrowIcon.DECENDING);

    public SortHeaderRenderer()
    {
        setHorizontalTextPosition(LEFT);
        setHorizontalAlignment(CENTER);
    }

    public Component getTableCellRendererComponent(
                      JTable table, Object value, boolean isSelected,
                      boolean hasFocus, int row, int col)
    {
        int index = -1;
        int modelIndex = -1;
        boolean ascending = true;
        if( table instanceof SortedTable )
        {
            SortedTable sortTable = (SortedTable)table;
            index = sortTable.getSortedColumnIndex();
            ascending = sortTable.isSortedColumnAscending();
            TableColumnModel colModel = table.getColumnModel();
            modelIndex = colModel.getColumn(col).getModelIndex();
        }
        if( table != null )
        {
            JTableHeader header = table.getTableHeader();
            if( header != null )
            {
                setForeground(header.getForeground());
                setBackground(header.getBackground());
                setFont(header.getFont());
            }
        }
        Icon icon = ascending ? ASCENDING : DECENDING;
        setIcon(modelIndex == index ? icon : NONSORTED);
        setText((value == null) ? "" : value.toString());
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        return this;
    }
}

