import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.util.List.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
// For logging
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Window extends JFrame
{
    private DefaultListModel<String> searchResults;
    private java.util.List<String> activeDownloads = new java.util.LinkedList<>();
    private final static Logger LOGGER = Logger.getLogger(Window.class.getName());
    private NewsFilter filter;

    public Window(final NewsFilter filter)
    {
        // Basic setup 
        super("News Headlines");
        super.setSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.filter = filter;

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

        addCancelListener(cancelButton);
        addUpdateListener(updateButton);
    }

    private void addCancelListener(JButton cancelButton)
    {
        // When the "Clear results" button is pressed...
        cancelButton.addActionListener(new ActionListener()
        {   
            @Override public void actionPerformed(ActionEvent e)
            {
                //searchResults.clear();
                System.out.println("Cancel button pressed");
                filter.cancel();
                // LOGGER.log(info, "Cancel pressed");
            }
        });
    }   

    private void addUpdateListener(JButton updateButton)
    {
        // When the "Clear results" button is pressed...
        updateButton.addActionListener(new ActionListener()
        {   
            @Override public void actionPerformed(ActionEvent e)
            {
                //searchResults.clear();
                //Force and update
                System.out.println("Update button pressed");

                filter.update();//( copy );
                // progress();
                // LOGGER.log(info, "Cancel pressed");
            }
        });
    }

    public void progress()
    {
        Container pane = getContentPane();
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        pane.add(bar, BorderLayout.SOUTH);
    }

    public void runningTasks(String plugin)
    {
        SwingUtilities.invokeLater( () -> {
            if(!this.activeDownloads.contains(plugin))
            {
                this.activeDownloads.add(plugin);
                System.out.println("Running: " + plugin );
            }
        });
    }

    public void finishedTasks(String plugin)
    {   
        SwingUtilities.invokeLater( () -> {
            if(this.activeDownloads.contains(plugin))
            {
                this.activeDownloads.remove(plugin);
                System.out.println("Finishing: " + plugin );
            }
        });
        
    }

    public void update(java.util.List<Headline> updateList)
    {
        System.out.println("RECEIVING UPDATES" + updateList.size() );
        SwingUtilities.invokeLater( () -> {
            if(updateList.size() > 0)
            {
                this.searchResults.clear();
                
                updateList.stream()
                          .sorted((x,y) -> (int)(x.getTime() - y.getTime()) )
                          .forEach((x) -> {  this.searchResults.addElement(x.toString());
                        });
            }


        });
    }
}