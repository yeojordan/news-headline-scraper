import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.util.List.*;
import java.util.Map;
import java.util.HashMap;
// For logging
import java.util.logging.Logger;

public class Window extends JFrame
{
    private DefaultListModel<String> searchResults;
    private Map<Integer, String> websiteMap;
    private Map<Integer, Headline> headlines;

    private final static Logger LOGGER = Logger.getLogger(Window.class.getName());

    public Window(final NewsController controller)
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

        this.websiteMap = new HashMap<>();
        this.headlines = new HashMap<>();

        //Set up backend map
        controller.getWebsites().stream()
                                .forEach((x)-> this.websiteMap.put(x.hashCode(), x));        

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


        // When the "Clear results" button is pressed...
        cancelButton.addActionListener(new ActionListener()
        {   
            @Override public void actionPerformed(ActionEvent e)
            {
                //searchResults.clear();
                System.out.println("Cancel button pressed");
                controller.cancel();
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
                controller.update(headlines.keySet());
                // LOGGER.log(info, "Cancel pressed");
            }
        });

    }

    public void update(java.util.List<Integer> articleIDs, java.util.List<Headline> headlines)
    {
        System.out.println("UPDATING UI LIST");
        articleIDs.stream()
                  .forEach((x) -> this.headlines.remove(x));

        // Add new entries
        headlines.stream()
                 .forEach((x) -> this.headlines.put((this.websiteMap.get(x.getHash())
                                                     + x.getHeadline()).hashCode(), x));
        // Set article source
        headlines.stream()
        .forEach((x) -> this.headlines.get((this.websiteMap.get(x.getHash())
                                            + x.getHeadline()).hashCode()).
                                            setWebsite(this.websiteMap.get(x.getHash())));


        SwingUtilities.invokeLater(() -> {
            this.searchResults.removeAllElements();
            DefaultListModel<String> newList = new DefaultListModel<>();
            this.headlines.values().stream()
                                .sorted((x,y) -> (int)(x.getTime() - y.getTime()) )
                                .forEach((x) -> {  this.searchResults.addElement(x.toString());
                                    System.out.println(x.toString());
                                });

                                System.out.println("PRE " + newList.capacity());
        
            
        });

        // System.out.println("AFTER " + this.searchResults.capacity());
    }

    // public void add(java.util.List<String> list)
    // {
    //     SwingUtilities.invokeLater(()->{
    //         for(String str : list )
    //         {
    //             this.searchResults.addElement(str);
    //         }
    //     });
    // }
}