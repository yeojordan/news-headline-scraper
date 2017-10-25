package view;

import controller.*;
import model.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
// For logging
import java.util.logging.Logger;


public class Window extends JFrame
{
    private DefaultListModel<String> searchResults;
    private DefaultListModel<String> running;
    private final static Logger LOGGER = Logger.getLogger(Window.class.getName());
    private NewsFilter filter;
    private JLabel clockTime;
    private boolean visible; // Boolean for if the alert dialogue is visible

    public Window(final NewsFilter filter)
    {
        // Basic setup 
        super("News Headlines");
        super.setSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialise reference to NewsFilter
        this.filter = filter; 
        
        // Clock
        JPanel clock = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.clockTime = new JLabel("",SwingConstants.CENTER);
        clock.add(new JLabel("Current Time",SwingConstants.CENTER));
        
        // Scroll pane for results
        JPanel scroll = new JPanel(new BorderLayout());
        searchResults = new DefaultListModel<>();        
        JScrollPane resultsList = new JScrollPane(new JList<String>(searchResults));

        // Scroll pane for active downloads
        running = new DefaultListModel<>();
        JScrollPane runningList = new JScrollPane( new JList<String>(running));
        scroll.add(new JLabel("News Headlines will be listed below", SwingConstants.CENTER), BorderLayout.NORTH);
        scroll.add(resultsList, BorderLayout.CENTER);
        scroll.add(runningList, BorderLayout.SOUTH);

        // Buttons
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");

        JPanel auxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        auxPanel.add(updateButton);
        auxPanel.add(cancelButton);

        // Add structures to the window
        Container contentPane = getContentPane();
        
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.clockTime, BorderLayout.NORTH);
        contentPane.add(scroll, BorderLayout.CENTER);   
        contentPane.add(auxPanel, BorderLayout.SOUTH);

        // Set up event listeners
        addCancelListener(cancelButton);
        addUpdateListener(updateButton);
    }

    private void addCancelListener(JButton cancelButton)
    {
        // When the "Cancel" button is pressed
        cancelButton.addActionListener(new ActionListener()
        {   
            @Override public void actionPerformed(ActionEvent e)
            {
                running.clear();
                // Signal to the NewsFilter that an update has been requested by the user
                filter.cancel();
            }
        });
    }   

    private void addUpdateListener(JButton updateButton)
    {
        // When the "Update" button is pressed
        updateButton.addActionListener(new ActionListener()
        {   
            @Override public void actionPerformed(ActionEvent e)
            {
                // Signal to the NewsFilter that an update has been requested by the user
                filter.update();
            }
        });
    }

    /**
     * Update the list of running tasks
     * Add a plugin that is now running
     */
    public void runningTasks(String plugin)
    {
        SwingUtilities.invokeLater( () -> {
            if(!this.running.contains(plugin))
            {
                this.running.addElement(plugin);
            }
        });
    }

    /**
     * Update the list of running tasks
     * Remove a plugin that is no longer running
     */
    public void finishedTasks(String plugin)
    {   
        SwingUtilities.invokeLater( () -> {
            if(this.running.contains(plugin))
            {
                this.running.removeElement(plugin);
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
                          .forEach((x) -> {  
                              this.searchResults.addElement(x.toString());
                            });
            }
        });
    }

    /**
     * Update the time displayed on the user interface
     */
    public void updateTime(String time)
    {
        SwingUtilities.invokeLater( () -> {
            this.clockTime.setText(time);
        });
    }

    /**
     * Display an alert to the user
     */
    public void alert(String message)
    {   
        Container con = getContentPane();

        SwingUtilities.invokeLater( () -> {
            if( !this.visible )
            {
                JOptionPane.showMessageDialog(con,
                message,
                "Error Occurred",
                JOptionPane.WARNING_MESSAGE);
                this.visible = true;
            }
            this.visible = false;
        });
    }
}