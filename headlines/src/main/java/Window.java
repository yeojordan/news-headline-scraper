import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
// import java.util.List.*;
// import java.util.Map;
// import java.util.HashMap;
// import java.util.Set;
// For logging
import java.util.logging.Logger;
// import java.util.stream.Collectors;

public class Window extends JFrame
{
    private DefaultListModel<String> searchResults;
    private DefaultListModel<String> running;
    private java.util.List<String> activeDownloads = new java.util.LinkedList<>();
    private final static Logger LOGGER = Logger.getLogger(Window.class.getName());
    private NewsFilter filter;
    private JLabel clockTime;

    public Window(final NewsFilter filter)
    {
        // Basic setup 
        super("News Headlines");
        super.setSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.filter = filter;

        JPanel clock = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.clockTime = new JLabel("",SwingConstants.CENTER);

        // Top Panel        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));     
        

        // Buttons
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");
        
        // Clock
        clock.add(new JLabel("Current Time",SwingConstants.CENTER));
        
        
        
        JPanel scroll = new JPanel(new BorderLayout());

        searchResults = new DefaultListModel<>();        
        JScrollPane resultsList = new JScrollPane(new JList<String>(searchResults));

        running = new DefaultListModel<>();
        JScrollPane runningList = new JScrollPane( new JList<String>(running));
        scroll.add(new JLabel("News Headlines will be listed below", SwingConstants.CENTER), BorderLayout.NORTH);
        scroll.add(resultsList, BorderLayout.CENTER);
        scroll.add(runningList, BorderLayout.SOUTH);
        
        JPanel auxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        auxPanel.add(updateButton);
        auxPanel.add(cancelButton);

        Container contentPane = getContentPane();
        
        contentPane.setLayout(new BorderLayout());
        // contentPane.add(searchPanel, BorderLayout.NORTH);
        contentPane.add(this.clockTime, BorderLayout.NORTH);
        // contentPane.add(resultsList, BorderLayout.CENTER);   
        contentPane.add(scroll, BorderLayout.CENTER);   
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
            if(!this.running.contains(plugin))
            {
                // this.activeDownloads.add(plugin);
                this.running.addElement(plugin);
                System.out.println("Running: " + plugin );
            }
        });
    }

    public void finishedTasks(String plugin)
    {   
        SwingUtilities.invokeLater( () -> {
            if(this.running.contains(plugin))
            {
                // this.activeDownloads.remove(plugin);
                this.running.removeElement(plugin);
                System.out.println("Finishing: " + plugin );
            }
        });
    }

    /**
     * To receive the updated list of news headlines
     */
    public void update(java.util.List<Headline> updateList)
    {
        SwingUtilities.invokeLater( () -> {
            if(updateList.size() > 0)
            {
                // Clearing the entire list, rather than finding entries to delete
                // Faster to clear and add all than to find and delete, due to the usage of a DefaultListModel
                this.searchResults.clear();

                // Sort all headlines based on download time. 
                updateList.stream()
                        //   .sorted((x,y) -> (int)(x.getTime() - y.getTime()) )
                          .forEach((x) -> {  this.searchResults.addElement(x.toString());
                        });
            }
        });
    }

    public void updateTime(String time)
    {
        SwingUtilities.invokeLater( () -> {
            this.clockTime.setText(time);
        });
    }
}