import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
// For logging
import java.util.logging.Logger;

public class Window extends JFrame
{
    private DefaultListModel<String> searchResults;

    private final static Logger LOGGER = Logger.getLogger(Window.class.getName());

    public Window()//final Controller controller)
    {
        // Basic setup 
        super("News Headlines");
        super.setSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel clock = new JPanel(new FlowLayout());
        JLabel clockTime = new JLabel();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run()
            {
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run()
                    {
                        clockTime.setText(new Date().toString());
                    }    
                });
                
            }
        }, 0,250);


        // Top Panel        
        JPanel searchPanel = new JPanel(new FlowLayout());     
        searchPanel.add(new JLabel("News Headlines will be listed below"));

        // Buttons
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");
        
        // Clock
        
        clock.add(new JLabel("Current Time"));


        // searchPanel.add(searchPathBox);
        // searchPanel.add(new JLabel("Search text:"));
        // searchPanel.add(cancelButton);
        // searchPanel.add(updateButton);

        searchResults = new DefaultListModel<>();        
        JScrollPane resultsList = new JScrollPane(new JList<String>(searchResults));
        
        JPanel auxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // JButton clearButton = new JButton("Clear results");
        auxPanel.add(updateButton);
        auxPanel.add(cancelButton);

        Container contentPane = getContentPane();
        
        contentPane.setLayout(new BorderLayout());
        // contentPane.add(searchPanel, BorderLayout.NORTH);
        contentPane.add(clockTime, BorderLayout.NORTH);
        contentPane.add(resultsList, BorderLayout.CENTER);   
        contentPane.add(auxPanel, BorderLayout.SOUTH);
        // pack();


        // When the "Clear results" button is pressed...
        cancelButton.addActionListener(new ActionListener()
        {   
            @Override public void actionPerformed(ActionEvent e)
            {
                //searchResults.clear();
                System.out.println("Cancel button pressed");
                // LOGGER.log(info, "Cancel pressed");
            }
        });

        // When the "Clear results" button is pressed...
        updateButton.addActionListener(new ActionListener()
        {   
            @Override public void actionPerformed(ActionEvent e)
            {
                //searchResults.clear();
                //Force and update
                System.out.println("Update button pressed");
                // LOGGER.log(info, "Cancel pressed");
            }
        });

    }

}