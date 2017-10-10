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
    private Map<Integer, String> websiteMap = new HashMap<>();
    private Map<Integer, Headline> headlines = new HashMap<>();
    private java.util.List<String> activeDownloads = new java.util.LinkedList<>();
    private final static Logger LOGGER = Logger.getLogger(Window.class.getName());

    public Window(final NewsController controller)
    {
        // Basic setup 
        super("News Headlines");
        super.setSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel clock = new JPanel(new FlowLayout());
        JLabel clockTime = new JLabel();
        // Timer timer2 = new Timer();
        // timer2.scheduleAtFixedRate( new TimerTask(){
        //     @Override 
        //     public void run(){
        //         System.out.println("            " +headlines.keySet().size());
        //     }
        // }, 0, 1000);


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

        
        // this.headlines = new HashMap<>();

        //Set up backend map
        // controller.getWebsites().stream()
        //                         .forEach((x)-> this.websiteMap.put(x.hashCode(), x));        

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
                System.out.println("Cancel button pressed " +headlines.keySet().size());
                controller.cancelDownloads();
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
                System.out.println("Update button pressed :" +headlines.keySet().size());
                Set<Integer> copy = headlines.keySet().stream()
                                                      .collect(Collectors.toSet());
                controller.updateAll();//( copy );
                System.out.println("SIZE AFTER PASS " + headlines.keySet().size());
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

    // public void update(java.util.List<Integer> articleIDs, java.util.List<Headline> headlines)
    // {
    //     System.out.println("UPDATING UI LIST\nto delete: " + articleIDs.size() + "\nto add " + headlines.size());


    //     articleIDs.stream()
    //               .forEach((x) -> this.headlines.remove(x));

    //     // Add new entries
    //     headlines.stream()
    //              .forEach((x) -> this.headlines.put((this.websiteMap.get(x.getHash())
    //                                                  + x.getHeadline()).hashCode(), x));

    //     // Set article source
    //     headlines.stream()
    //              .forEach((x) -> this.headlines.get((this.websiteMap.get(x.getHash())
    //                                         + x.getHeadline()).hashCode()).
    //                                         setWebsite(this.websiteMap.get(x.getHash())));


    //     SwingUtilities.invokeLater(() -> {
    //         if( articleIDs.size() > 0)
    //             this.searchResults.removeAllElements(); // O(n)
            
    //         if( headlines.size() > 0)
    //             this.headlines.values().stream()
    //                                 .sorted((x,y) -> (int)(x.getTime() - y.getTime()) )
    //                                 // .filter((x) ->  this.searchResults.contains(x.toString()) == false ) // Only insert if it isn't in the list 
    //                                 .forEach((x) -> {  this.searchResults.addElement(x.toString());
    //                                     // System.out.println(x.toString());
    //                                 });
            

    //     });

    //     System.out.println("AFTER update" + this.headlines.size());
    // }

    public void runningTasks(String plugin)
    {
    
        // if(!this.activeDownloads.contains(plugin))
        // {
            this.activeDownloads.add(plugin);
            System.out.println("Running: " + plugin );
        // }

        // SwingUtilities.invokeLater( () -> {

        // });
    }

    public void finishedTasks(String plugin)
    {
        if(this.activeDownloads.contains(plugin))
        {
            this.activeDownloads.remove(plugin);
            System.out.println("Finishing: " + plugin );
        }
    }

    public void update(java.util.List<Headline> updateList)
    {
        System.out.println("RECEIVING UPDATES");
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