import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.table.TableCellRenderer;

public class ShowUIDefaults extends JFrame implements ActionListener
{
    JFrame frame;
    JTabbedPane tabbedPane;
    SampleRenderer sampleRenderer;

    public ShowUIDefaults(String title)
    {
        super(title);
        frame = this;

        sampleRenderer = new SampleRenderer();

        getContentPane().setLayout( new BorderLayout() );
        tabbedPane = getTabbedPane();
        getContentPane().add( tabbedPane );

        JPanel buttons = new JPanel();
        buttons.setLayout( new GridLayout( 1, 3) );
        getContentPane().add(buttons, BorderLayout.SOUTH);

        JButton metal = new JButton( "Metal" );
        metal.setActionCommand( "javax.swing.plaf.metal.MetalLookAndFeel" );
        metal.addActionListener( this );
        buttons.add(metal);

        JButton windows = new JButton( "Windows" );
        windows.setActionCommand( "com.sun.java.swing.plaf.windows.WindowsLookAndFeel" );
        windows.addActionListener( this );
        buttons.add(windows);

        JButton motif = new JButton( "Motif" );
        motif.setActionCommand( "com.sun.java.swing.plaf.motif.MotifLookAndFeel" );
        motif.addActionListener( this );
        buttons.add(motif);

    }

    public void actionPerformed(ActionEvent e)
    {
        try
        {
            UIManager.setLookAndFeel(e.getActionCommand());
        }
        catch (Exception e2)
        {
            System.out.println(e2);
        }

        getContentPane().remove(tabbedPane);
        tabbedPane = getTabbedPane();
        getContentPane().add( tabbedPane );
        SwingUtilities.updateComponentTreeUI(frame);
        frame.pack();
    }

    private JTabbedPane getTabbedPane()
    {
        Map components = new TreeMap();
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();

        //  Build of Map of attributes for each component

        for ( Enumeration enu = defaults.keys(); enu.hasMoreElements(); )
        {
            Object key = enu.nextElement();
            Object value = defaults.get( key );

            Map componentMap = getComponentMap( components, key.toString() );

            if ( componentMap != null )
                componentMap.put( key, value );
        }

        JTabbedPane pane = new JTabbedPane( SwingConstants.BOTTOM );
        pane.setPreferredSize( new Dimension( 800, 400 ) );
        addComponentTabs( pane, components );

        return pane;
    }

    private Map getComponentMap( Map components, String key )
    {
        if ( key.startsWith("class") | key.startsWith("javax") )
            return null;

        //  Component name is found before the first "."

        String componentName;

        int pos = key.indexOf( "." );

        if (pos == -1)
            if (key.endsWith( "UI" ) )
                componentName = key.substring( 0, key.length() - 2 );
            else
                componentName = "System Colors";
        else
            componentName = key.substring( 0, pos );

        //  Fix inconsistency

        if (componentName.equals("Checkbox"))
            componentName = "CheckBox";

        //  Get the Map for this particular component

        Object componentMap = components.get( componentName );

        if (componentMap == null)
        {
            componentMap = new TreeMap();
            components.put( componentName, componentMap );
        }

        return (Map)componentMap;
    }

    private void addComponentTabs(JTabbedPane pane, Map components)
    {
        String[ ] colName = {"Key", "Value", "Sample"};
        Set c = components.keySet();

        for (Iterator ci = c.iterator(); ci.hasNext();)
        {
            String component = (String)ci.next();
            Map attributes = (Map)components.get( component );

            Object[ ][ ] rowData = new Object[ attributes.size() ][ 3 ];
            int i = 0;

            Set a = attributes.keySet();

            for(Iterator ai = a.iterator(); ai.hasNext(); i++)

            {
                String attribute = (String)ai.next();
                rowData[ i ] [ 0 ] = attribute;
                Object o = attributes.get(attribute);

                if (o != null)
                {
                    rowData[ i ] [ 1 ] = o.toString();
                    rowData[ i ] [ 2 ] = o;
                }
                else
                {
                    rowData[ i ] [ 1 ] = "null";
                    rowData[ i ] [ 2 ] = "";
                }

            }

            JTable table = new JTable(rowData, colName);
            table.getColumnModel().getColumn(2).setCellRenderer( sampleRenderer );
            table.getColumnModel().getColumn(0).setPreferredWidth(250);
            table.getColumnModel().getColumn(1).setPreferredWidth(500);
            table.getColumnModel().getColumn(2).setPreferredWidth(50);

            pane.addTab( component, new JScrollPane( table ) );
        }
    }

    class SampleRenderer extends JLabel implements TableCellRenderer
    {
        public SampleRenderer()
        {
            super();
            setHorizontalAlignment( SwingConstants.CENTER );
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(JTable table,
                                                         Object sample,
                                                         boolean isSelected,
                                                         boolean hasFocus,
                                                         int row,
                                                         int column)
        {
            setBackground( null );
            setBorder( null );
            setIcon( null );
            setText( "" );

            if ( sample instanceof Color )
            {
                setBackground( (Color)sample );
            }
            else if ( sample instanceof Border )
            {
                setBorder( (Border)sample );
            }
            else if ( sample instanceof Font )
            {
                setText( "Sample" );
                setFont( (Font)sample );
            }
            else if ( sample instanceof Icon )
            {
                setIcon( (Icon)sample );
            }

            return this;
        }

        public void paint(Graphics g)
        {
            try
            {
                super.paint(g);
            }
            catch(Exception e)
            {
//              System.out.println(e);
//              System.out.println(e.getStackTrace()[0]);
            }
        }
    }

    public static void main(String[] args)
    {
        JFrame f = new ShowUIDefaults( "UI Defaults" );
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo( null );
        f.setVisible(true);
    }

}
