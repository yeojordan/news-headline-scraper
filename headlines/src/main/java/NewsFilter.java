import java.util.concurrent.*;
import java.util.stream.*;
import java.util.*;
import java.util.logging.Logger;

public class NewsFilter
{
    private LinkedBlockingQueue<Headline> queue; // Blocking queue of all the headlines 
    // Map of all headlines contained in the user interface
    private Map<String, Map<String, Headline>> uiContents; // Key = URL, Value = Map [Key = Headline (String), Value = Headline (Object)] 
    // Map of all the headlines retrieved from the blocking queue. Updated as a plugin finishes
    private Map<String, Map<String, Headline>> retrieved; // 
    private List<String> running;           // List of all the currently running plugins
    private Object monitor = new Object();  // Monitor to synchronize on running plugins
    private Object cancelledMonitor = new Object(); // Monitor to synchronize on cancelled status
    private Window ui;                      // User interface
    private String finished;                // The name of the plugin that has just finished
    private Thread filter;                  // The thread for filtering
    private boolean cancelled;              // Let's the filter know if cancel has been called by the user 
    private NewsController controller;      // NewsController

    // Logger 
    private static Logger theLogger = Logger.getLogger(NewsFilter.class.getName());

    /**
     * Default constructor for the NewsFilter
     */
    public NewsFilter()
    {
        this.queue = new LinkedBlockingQueue<>();
        this.uiContents = new HashMap<>();
        this.retrieved = new HashMap<>();
        this.running = new LinkedList<>();
        this.cancelled = false;

        // Start the filter task
        filter = new Thread( ()-> filter() ); 
        filter.start();
        theLogger.info("NewsFilter Constructed and filter started");
    }

    /**
     * Add a headline to the blocking queue
     */
    public void addHeadline(Headline headline)
    {
        try
        {
            synchronized(this.monitor)
            {
                this.queue.put(headline);
            }
        }
        catch(InterruptedException e)
        {
            theLogger.warning("Unable to add headline to queue:\n" + headline.toString());
        }
    }

    /**
     * Peform the filtering process to determine the headlines to be 
     * added and removed for each news source that finishes
     */
    public void filter()
    {
        Map<String,Headline> currHeadlineMap;
        try
        {
            // Run while the thread has not been interrupted 
            while( !Thread.currentThread().isInterrupted() )
            {
                synchronized(this.monitor)
                {
                    this.monitor.wait(); // Wait for a plugin to finish
                    Headline retrievedHeadline;
                
                    while(this.queue.size() > 0) // While there are things to take
                    {
                        retrievedHeadline = this.queue.take(); //Take from the blocking queue
                        currHeadlineMap = this.retrieved.get(retrievedHeadline.getSource()); // Retrieve map associated with headline source
                        // Instantiate a new map if one does not exist yet
                        if ( currHeadlineMap == null)
                        {
                            currHeadlineMap = new HashMap<>();
                        }
                        // Add headline the map for that news plugin
                        currHeadlineMap.put(retrievedHeadline.getHeadline(), retrievedHeadline); 

                        // Insert the map for a plugin into the larger map of all headlines
                        this.retrieved.put(retrievedHeadline.getSource(), currHeadlineMap); 
                    }
                    
                    // Retrieve map associated with the plugin that has just finished
                    // Leaving all other headlines from other sources in the retrieved map
                    currHeadlineMap = this.retrieved.get(finished);
                    
                    // Retrieve updated list
                    List<Headline> update = updatedList(currHeadlineMap);

                    // Clear the headline map for a plugin
                    currHeadlineMap = new HashMap<>();
                    
                    // If the user has not signalled cancel prior to the filter finishing its task
                    synchronized( this.cancelledMonitor )
                    {
                        if( !cancelled )
                        {   
                            // Send the updated headline list to the UI
                            this.ui.finishedTasks(finished + " is running");
                            this.ui.update(update);
                            theLogger.info("Update sent. Plugin:" + finished);
                        }
                        else 
                        {
                            clear();
                            theLogger.info("Update cancelled");
                        }
                    }
                    
                }
                theLogger.info("Filter complete. Plugin:" + finished);
            }
        }
        catch(InterruptedException e)
        {
            System.out.println("Interrupted filtering");
        
            this.queue.clear(); // Empty queue
            this.retrieved = new HashMap<>(); // Reset all running plugins
        }
    }

    /**
     * Clear all running tasks and headlines retrieved but not sent to the UI
     */
    public void clear()
    {
        // Clear headlines retrieved
        for( String running : this.running)
        {
            // Clear all retrieved headlines
            this.retrieved.put(running, new HashMap<>());
            this.ui.finishedTasks(running + " is running");
        }
        // Clear list of running tasks
        this.running.clear();

        // Reset cancelled flag
        synchronized(this.cancelledMonitor)
        {
            this.cancelled = false;
        }

        theLogger.info("Clearing filter due to user cancel");
    }

    /**
     * Determine the latest version of headline from a finished source
     */
    public List<Headline> updatedList(Map<String, Headline> updateMap) throws InterruptedException
    {
        Map<String, Headline> uiSourceMap = this.uiContents.get(finished);
        List<Headline> updated;// = new LinkedList<>();
        final List<Headline> temp = new LinkedList<>();
        if (updateMap != null )
        {
            // Compare to UI contents
            // If new headline is in original map, take original value
            updateMap.keySet().stream()
                              .forEach( (x) -> {
                                  if( uiSourceMap.containsKey(x) )
                                  {
                                      temp.add( uiSourceMap.get(x) );
                                  }
                                  else
                                  {
                                      temp.add( updateMap.get(x) );
                                  }
                              });
        }                          
        updated = new LinkedList<>(temp);
        // Update the uiMap
        uiSourceMap.clear();

        for(Headline head : updated)
        {
            uiSourceMap.put(head.getHeadline(), head);
        }

        this.uiContents.put(finished, uiSourceMap);

        updated.clear();

        // Create a list from the entire UIMap
        // The following method(s) create a stream of all the maps within the uiContents
        // Then utilises flatMap() to put all the collections into a single stream
        // Then sortes the Headlines based on download time
        // Finally the sorted stream is added to a list               
        updated = this.uiContents.values()
                                 .stream() 
                                 .flatMap(x -> x.values().stream())    
                                 .sorted(Comparator.comparing(Headline::getTime))
                                 .collect(Collectors.toCollection(LinkedList::new));

        theLogger.info("Updated headlines list created");
        return updated;
    }

    /**
     * Update the user interface with the currently running plugins
     */
    public void running(String plugin)
    {
        synchronized(this.monitor)
        {
            this.running.add(plugin);
            this.uiContents.putIfAbsent(plugin, new HashMap<>());
            this.ui.runningTasks(plugin + " is running");    
        }
    }

    /**
     * Update the user interface with the currently finished plugins
     */
    public void finished(String plugin)
    {
        synchronized(this.monitor)
        {
            if ( this.running.remove(plugin) )
            {   
                this.finished = plugin; // Updated shared resource
            }
            this.monitor.notify(); // Notify filter to start
        }
    }

    /**
     * Set the filter's reference to the user interface
     */
    public void setUI(Window ui)
    {
        this.ui = ui;
        theLogger.info("Set reference to Window (UI)");
    }

    /**
     * Set the filter's reference to the controller
     */
    public void setController(NewsController controller)
    {
        this.controller = controller;
        theLogger.info("Set reference to NewsController");
    }
    
    /**
     * Notify the controller that a request to download from all sources occurred
     */
    public void update()
    {
        this.controller.updateAll();
        theLogger.info("Update request recieved from UI");
    }

    /**
     * Update the cancelled field 
     */
    public void cancel()
    {
        this.controller.cancelDownloads();

        // Synchronised to avoid a race condition
        synchronized(this.cancelledMonitor)
        {
            // Set the cancelled field to true
            this.cancelled = true;
        }
        theLogger.info("Cancel request recieved from UI");
    }

    /**
     *  Send an alert to the user interface
     */
    public void alert(String msg)
    {
        this.ui.alert(msg);
        theLogger.info("Alert sent to UI");
    }
}