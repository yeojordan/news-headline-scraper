import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.*;

public class NewsFilter
{
    private LinkedBlockingQueue<Headline> queue;
    private Map<String, Map<String, Headline>> uiContents; // Key = URL, Value = Map [Key = Headline (String), Value = Headline (Object)] 
    private Map<String, Map<String, Headline>> retrieved;
    private List<String> running;
    private Object monitor = new Object();
    private Window ui;
    private String finished;
    private boolean programRunning;
    private Thread filter;
    private boolean cancelled;

    public NewsFilter()
    {
        this.queue = new LinkedBlockingQueue<>();
        this.uiContents = new HashMap<>();
        this.retrieved = new HashMap<>();
        this.running = new LinkedList<>();
        this.programRunning = true;
        this.cancelled = false;
        filter = new Thread(()->filter()); 
        filter.start();
    }

    public void addHeadline(Headline headline)
    {
        try
        {
            this.queue.put(headline);
        }
        catch(InterruptedException e)
        {
            System.out.println("Interrupted adding to filter");
        }
    }

    public void filter()
    {
        Map<String,Headline> currHeadlineMap;
        try
        {
            while( !Thread.currentThread().isInterrupted() )
            {
                synchronized(this.monitor)
                {
                    System.out.println("WAITING");
                    this.monitor.wait(); // Wait for a plugin to finish
System.out.println("FINISHED WAITING");
                    Headline retrievedHeadline;
                
                    // while( !retrievedHeadline.getHeadline().contains("POISON") ) // While a POISON object has not been retrieved
                    while(this.queue.size() > 0) // While there are things to take
                    {
                        System.out.println("TAKING FROM QUEUE");
                        retrievedHeadline = this.queue.take(); //Take from the blocking queue
                        System.out.println(retrievedHeadline.toString());
                        currHeadlineMap = this.retrieved.get(retrievedHeadline.getSource()); // Retrieve map associated with headline source
                        if ( currHeadlineMap == null)
                        {
                            currHeadlineMap = new HashMap<>();
                        }
                        currHeadlineMap.put(retrievedHeadline.getHeadline(), retrievedHeadline); // Add to source map 
                        this.retrieved.put(retrievedHeadline.getSource(), currHeadlineMap); // Put back in retrieved map
                    }
                    System.out.println("FINISHED TAKING");
                    // Retrieve map associated with the plugin that has just finished
                    // Leaving all other headlines from other sources in the 
                    currHeadlineMap = this.retrieved.get(finished);
                    

                    // Retrieve updated list
                    List<Headline> update = updatedList(currHeadlineMap);

                    currHeadlineMap = new HashMap<>();

                    // Clear retrievedMap for plugin
                    this.retrieved.put(finished, new HashMap<>());

                    // Send updates to UI
                    System.out.println("SENDING UPDATES");
                    this.ui.finishedTasks(finished);
                    if( !cancelled )
                    {
                        this.ui.update(update);
                    }
                }
            }
        }
        catch(InterruptedException e)
        {
            System.out.println("Interrupted filtering");
            this.queue.clear(); // Empty queue
            this.retrieved = new HashMap<>(); // Reset all running plugins
        }
    }

    public List<Headline> updatedList(Map<String, Headline> updateMap) throws InterruptedException
    {
        Map<String, Headline> uiSourceMap = this.uiContents.get(finished);
        List<Headline> updated = new LinkedList<>();
        
        // Compare to UI contents
        // If new headline is in original map, take original value
        /*updateMap.keySet().stream()
                          .filter(x -> uiSourceMap.containsKey(x))
                          .forEach(x -> updated.add(uiSourceMap.get(x))); */
                          if(updateMap != null){
        for( String key : updateMap.keySet() )
        {
            if( uiSourceMap.containsKey(key) )
            {
                updated.add( uiSourceMap.get(key));
            }
            else
            {
                updated.add( updateMap.get(key));
            }
        }
    }
        // Update the uiMap
        uiSourceMap.clear();
        for(Headline head : updated)
        {
            uiSourceMap.put(head.getHeadline(), head);
        }

        this.uiContents.put(finished, uiSourceMap);


        System.out.println("CAPACITY AFTER FILTER: " + updated.size());
        // If new headline does not exist in original, add to list
        /*uiSourceMap.keySet().stream() 
                            .filter(x -> !updateMap.containsKey(x))
                            .forEach(x -> updated.add(updateMap.get(x)));*/

        updated.clear();

        // Create a list from the entire UIMap
        this.uiContents.values().stream()
                                .forEach(x -> x.values().stream()
                                                        .forEach( y -> updated.add(y)));

        System.out.println("CAPACITY AFTER SECOND FILTER: " + updated.size());
        return updated;
    }


    public void running(String plugin)
    {
        synchronized(this.monitor)
        {
            System.out.println("ADDING TO RUNNING LIST (NF): " + plugin);
            this.running.add(plugin);
            this.uiContents.putIfAbsent(plugin, new HashMap<>());
            this.ui.runningTasks(plugin);    
            System.out.println("SENT RUNNING");
        }
        
        System.out.println("SENT RUNNING after sync");
    }

    public void finished(String plugin)
    {
        synchronized(this.monitor)
        {
            if ( this.running.remove(plugin) )
            {   
                System.out.println("REMOVING FROM RUNNING LIST (NF): " + plugin);
                this.finished = plugin; // Updated shared resource
            }
            this.monitor.notify(); // Notify filter to start
            System.out.println("NOTIFYING");
        }
    }

    public void setUI(Window ui)
    {
        this.ui = ui;
    }

    public void cancel()
    {
        // this.filter.interrupt();
        this.cancelled = true;
    }
}