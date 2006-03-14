/*
*   Copyright (c) 2000 Martin Newstead (mseries@brundell.fsnet.co.uk).  All Rights Reserved.
*
*   The author makes no representations or warranties about the suitability of the
*   software, either express or implied, including but not limited to the
*   implied warranties of merchantability, fitness for a particular
*   purpose, or non-infringement. The author shall not be liable for any damages
*   suffered by licensee as a result of using, modifying or distributing
*   this software or its derivatives.
*
*   The author requests that he be notified of any application, applet, or other binary that
*   makes use of this code and that some acknowedgement is given. Comments, questions and
*   requests for change will be welcomed.
*/
package mseries.Calendar;

import mseries.ui.MImagePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
*    GUI for the date selector (Calendar) popup
*/
public class MDateSelectorUI extends JDialog implements MMonthListener
{
    private JTextField date;
    private GridBagConstraints c = new GridBagConstraints();
    private JButton okButton;
    private JButton todayButton;
    private JButton cancelButton;
    private DateFormat df;
    private ResourceBundle rb;

    MImagePanel innerPanel;

    public MDateSelectorUI(JFrame parent, MDateSelectorPanel panel, MDateSelector controller,
                           ResourceBundle rb, DateFormat df, String imageFile)
    {

        super(parent, "MSeries Date Selector", true);

        setResizable(false);

        this.rb = rb;

        if (df == null)
        {
            this.df=new SimpleDateFormat(System.getProperty("MDateFormat", "d MMMMM yyyy"));
        }
        else
        {
            this.df=df;
        }
        innerPanel = new MImagePanel(new GridBagLayout());
        if (imageFile!=null)
            innerPanel.setImageFile(imageFile);

        innerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        date = new JTextField("Error"){
            public boolean isFocusable()
            {
                return false;
            }
        };
        date.setEditable(false);

        setColours(date);
        date.setHorizontalAlignment(SwingConstants.CENTER);

        c.insets=new Insets(0, 0, 4, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = GridBagConstraints.RELATIVE;

        innerPanel.add(date, c);
        c.gridheight = 5;
        c.gridy = 1;
        innerPanel.add(panel, c);

        c.gridheight = 1;
        c.gridwidth = 1;
        okButton = new JButton();
        okButton.setActionCommand("ok");
        //setColours(okButton);

        todayButton = new JButton();
        todayButton.setActionCommand("today");
        //setColours(todayButton);

        cancelButton = new JButton();
        cancelButton.setActionCommand("cancel");
        //setColours(cancelButton);

        setLabels();

        c.insets=new Insets(0, 4, 4, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor=GridBagConstraints.SOUTH;
        c.weightx = 0;
        c.gridx=1;
        c.gridy=1;
        innerPanel.add(okButton, c);

        c.anchor=GridBagConstraints.SOUTH;
        c.weightx = 0;
        c.gridx=1;
        c.gridy=2;
        innerPanel.add(cancelButton,c);

        c.anchor=GridBagConstraints.CENTER;
        c.weightx = 0;
        c.gridx=1;
        c.gridy=5;
        innerPanel.add(todayButton, c);

        okButton.addActionListener(controller);
        todayButton.addActionListener(controller);
        cancelButton.addActionListener(controller);


        innerPanel.registerKeyboardAction(controller, "cancel",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        panel.addMMonthListener(this);

        updateDate(panel.getDate());
        getContentPane().add(innerPanel);

        date.setOpaque(false);
        cancelButton.setOpaque(false);
        todayButton.setOpaque(false);
        okButton.setOpaque(false);
        getRootPane().setDefaultButton(okButton);
        pack();
        setSize(getMinimumSize());

    }

    public void setImageFile(String imageFile)
    {
        innerPanel.setImageFile(imageFile);
    }

    private void setLabels()
    {
        okButton.setText(getString("OK", "OK"));
        cancelButton.setText(getString("Cancel", "Cancel"));
        todayButton.setText(getString("Today", "Today"));
    }

    private String getString(String in, String def)
    {
        String ret;
        if (rb == null)
        {
            return def;
        }
        try
        {
            ret = rb.getString(in);
        }
        catch(MissingResourceException e)
        {
            ret = def;
        }
        return ret;
    }

    private void updateDate(Date date)
    {
        this.date.setText(df.format(date));
    }

    
    /** Reacts to all changes in the data model (MMonth) which is given
    *   by the event type.
    *   from mseries.utils.MMonthListener interface
    */
    public void dataChanged(MMonthEvent e)
    {
        Calendar date = e.getNewDate();
        switch(e.getType())
        {
            case MMonthEvent.NEW_DATE:
                updateDate(date.getTime());
                break;
            case MMonthEvent.NEW_MONTH:
                updateDate(date.getTime());
                break;
            case MMonthEvent.NEW_RB:
                setLabels();
                break;
            default:
                break;
        }
    }

    protected void setColours(Component c)
    {
        c.setBackground(UIManager.getColor("control"));
        c.setForeground(UIManager.getColor("Button.foreground"));
    }

}
/*
$Log$
Revision 1.1  2006-03-14 14:09:44  bback
new date chooser component

Revision 1.12  2003/10/04 09:42:24  martin
*** empty log message ***

Revision 1.11  2003/10/03 19:45:14  martin
*** empty log message ***

Revision 1.10  2003/03/26 23:29:48  martin
Changed email address

Revision 1.9  2003/03/24 19:45:07  martin
Latest 1.4 version

Revision 1.7  2003/03/11 22:35:14  martin
Upgraded to Java 1.4 on 11/03/03

Revision 1.6  2002/05/24 15:14:03  martin
Added setSize(..) to end of constructor as pack seems not to work on ceratin JVM in Linux

Revision 1.5  2002/03/03 10:07:31  martin
Removed "Use MDateFormat throughout" changes

Revision 1.4  2002/03/03 09:33:41  martin
Use MDateFormat throughout

Revision 1.3  2002/02/16 14:21:27  martin
Added Escape Key to dismiss the dialog

*/
