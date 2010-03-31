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
package mseries.ui;

import java.beans.*;

import mseries.Calendar.*;

/**
*   Bean Info class for MDateSelectorPanel. Not all attributes are editable in the
*   beanbox but most are.
*/
public class MDateEntryFieldBeanInfo extends SimpleBeanInfo
{
    Class mdsClass = MDateEntryField.class;

/*
    public Image getIcon(int kind)
    {
        Image image = null;

        if (kind == BeanInfo.ICON_COLOR_16x16)
        {
            image = loadImage("calicon16.gif");
        }
        else if (kind == BeanInfo.ICON_COLOR_32x32)
        {
            image = loadImage("calicon32.gif");
        }
        return image;
    }
*/

    public EventSetDescriptor[] getEventSetDescriptors()
    {
        EventSetDescriptor entered=null;
        EventSetDescriptor changed=null;
        try
        {
            String[] methods= {"fieldEntered", "fieldExited"};
            entered = new EventSetDescriptor(mdsClass,
                                            "mField",
                                            mseries.Calendar.MFieldListener.class,
                                            methods,
                                            "addMFieldListener",
                                            "removeMFieldListener");
            changed = new EventSetDescriptor(mdsClass,
                                           "mChange",
                                           mseries.ui.MChangeListener.class,
                                           "valueChanged");

        }
        catch (IntrospectionException e)
        {
        }
        EventSetDescriptor[] events = {entered, changed};
        return events;
    }

    public PropertyDescriptor[] getPropertyDescriptors()
    {
        try
        {
            PropertyDescriptor
                date = new PropertyDescriptor("value", mdsClass),
                format = new PropertyDescriptor("dateFormatter", mdsClass);
/*
                text = new PropertyDescriptor("text", mdsClass);
                foreground = new PropertyDescriptor("foreground", mdsClass),
                background = new PropertyDescriptor("background", mdsClass),
                db = new PropertyDescriptor("doubleBuffered", mdsClass),
                opaque = new PropertyDescriptor("opaque", mdsClass),
                autoscrolls = new PropertyDescriptor("autoscrolls", mdsClass),
                alx = new PropertyDescriptor("alignmentX", mdsClass),
                aly = new PropertyDescriptor("alignmentY", mdsClass),
                dgo = new PropertyDescriptor("debugGraphicsOptions", mdsClass),
                rfe = new PropertyDescriptor("requestFocusEnabled", mdsClass),
                ps = new PropertyDescriptor("preferredSize", mdsClass),
                maxS = new PropertyDescriptor("maximumSize", mdsClass),
                minS = new PropertyDescriptor("minimumSize", mdsClass),
                b = new PropertyDescriptor("border", mdsClass),
                max = new PropertyDescriptor("maximum", mdsClass),
                min = new PropertyDescriptor("minimum", mdsClass),
                tl = new PropertyDescriptor("textLocalizer", mdsClass),
                nfc = new PropertyDescriptor("nextFocusableComponent", mdsClass),
                fd = new PropertyDescriptor("firstDay", mdsClass),
                font = new PropertyDescriptor("font", mdsClass);


            ps.setHidden(true);
            maxS.setHidden(true);
            minS.setHidden(true);
            b.setHidden(true);
            db.setHidden(true);
            opaque.setHidden(true);
            autoscrolls.setHidden(true);
            alx.setHidden(true);
            aly.setHidden(true);
            dgo.setHidden(true);
            rfe.setHidden(true);
            nfc.setHidden(true);
*/

            date.setPropertyEditorClass(MDateValueEditor.class);
            format.setPropertyEditorClass(MDateFormatEditor.class);
/*
            max.setPropertyEditorClass(MDateValueEditor.class);
            min.setPropertyEditorClass(MDateValueEditor.class);
            fd.setPropertyEditorClass(MFirstDayEditor.class);
            tl.setPropertyEditorClass(MTextLocaliserEditor.class);
            min.setShortDescription("Minimum Value");
            max.setShortDescription("Maximum Value");
*/
            date.setShortDescription("Current Value");
            format.setShortDescription("Date Format");

            PropertyDescriptor[] pd = {date, format};
            return pd;
        }
        catch (IntrospectionException e)
        {
            System.out.println("Exception is "+e.getMessage());
            return super.getPropertyDescriptors();
        }
    }
}
