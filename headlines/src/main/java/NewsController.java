// import javax.swing.SwingUtilities;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class NewsController
{
    private Window ui = null;
    private Map<String, NewsPlugin> plugins;
    private ExecutorService exService;
    private ExecutorService exScheduled;
    private NewsFilter filter;
    
    private Map<String, Future<?>> scheduledFutures;
    private Map<String, Future<?>> updateFutures;
    private Map<String, Boolean> runningPlugins;
    // private Thread scheduler;

    public NewsController(String[] pluginLocations)
    {
        PluginLoader loader = new PluginLoader();
        this.plugins = new HashMap<>();
        this.scheduledFutures = new HashMap<>();
        this.updateFutures = new HashMap<>();
        this.runningPlugins = new HashMap<>();
        this.filter = new NewsFilter();

        this.plugins.values().stream()
                             .forEach( x -> {
                                 this.scheduledFutures.put(x.retrieveURL(), null);
                                 this.updateFutures.put(x.retrieveURL(), null);
                                });

        // Add each plugin to a map
        for(String pluginName : pluginLocations)
        {
            try
            {
                NewsPlugin plugin = loader.loadPlugin(pluginName);
                plugin.setFrequency(15);
                plugin.setController(this);
                this.plugins.put(plugin.retrieveURL(), plugin);
            }
            catch(ClassNotFoundException e)
            {}
        }

        // Create executor service for scheduled and update threads
        this.exScheduled = Executors.newScheduledThreadPool(4);
        this.exService = Executors.newFixedThreadPool(4);

        // Schedule each plugin and keep each Future in a map
        this.plugins.values().stream()
                             .forEach( x -> this.scheduledFutures.put(x.retrieveURL(), 
                                        ((ScheduledExecutorService)(this.exScheduled)).scheduleAtFixedRate(x, 10, x.getFreq() ,TimeUnit.MINUTES)) );
        // System.out.println( this.scheduledFutures.get("http://www.bbc.com/news").toString());
    }

    public void setUI(Window ui)
    {
        this.ui = ui;
        this.filter.setUI(ui);
    }

    public void updateAll()
    {
        // Find all the plugins not currently running and submit them
        // this.runningPlugins.keySet().stream() 
        //                             .filter(x -> this.runningPlugins.get(x) == false)
        //                             .forEach(x -> {
        //                                 this.updateFutures.put( x, this.exService.submit(this.plugins.get(x)));
        //                                 this.filter.running(x);
        //                             });
System.out.println("UPDATE ALL REQUEST RECEIVED");

// for(String x : this.plugins.keySet() )
// {
//     System.out.println("Checking scheduled: " + x);
//     if( !this.scheduledFutures.get(x).isCancelled() || this.scheduledFutures.get(x).isDone())
//     {
//         System.out.println("Checking updates: " + x);
//         // If forced update is also not running or null
//         if( this.updateFutures.get(x) == null || this.updateFutures.get(x).isDone() )
//         {
//             // Submit for execution
//             this.updateFutures.put(x, this.exService.submit(this.plugins.get(x)));
//             System.out.println("SUBMITTING: " + x);
//         }
//     }
// }
        this.plugins.keySet().stream()
                             .forEach( x -> {
                                 // If scheduled update is not running
                                 if( !this.scheduledFutures.get(x).isCancelled() || this.scheduledFutures.get(x).isDone() )
                                 {
                                     // If forced update is also not running or null
                                     if( this.updateFutures.get(x) == null || this.updateFutures.get(x).isDone() )
                                     {
                                         // Submit for execution
                                         this.updateFutures.put(x, this.exService.submit(this.plugins.get(x)));
                                         System.out.println("SUBMITTING: " + x);
                                     }
                                 }
                             });
    }

    public void cancelDownloads()
    {
        // Find all the plugins, currently running and interrupt them
        // this.runningPlugins.keySet().stream()
        //                             .filter(x -> this.runningPlugins.get(x) == true)
        //                             .forEach(x -> { 
        //                                 this.scheduledFutures.get(x).cancel(true); // Cancel a scheduled plugin
        //                                 this.updateFutures.get(x).cancel(true); // Cancel an update plugin
        //                             });

        this.plugins.keySet().stream()
                             .forEach( x -> {
                                 // If scheduled update is running
                                 
                                 if( !this.scheduledFutures.get(x).isDone() || !this.scheduledFutures.get(x).isCancelled() )
                                 {
                                    System.out.println("CHECKING SCHEDULED FOR CANCEL: " + x);    
                                    this.scheduledFutures.get(x).cancel(true); 
                                 }  
                                 // If forced update is also running or null
                                 
                                 if( this.updateFutures.get(x) != null )
                                 {
                                     System.out.println("CHECKING UPDATE FOR CANCEL: " + x);
                                     // Submit for execution
                                     if(!this.updateFutures.get(x).isDone())
                                     {
                                         this.updateFutures.get(x).cancel(true);
                                    }
                                 }
                             });
    }

    public void running(NewsPlugin running)
    {
        System.out.println("PLUGIN RUNNING: " + running.retrieveURL());
        this.runningPlugins.put(running.retrieveURL(), true);
        this.filter.running(running.retrieveURL()); // Let filter know which plugin is running
    }
    
    public void finishedRunning(NewsPlugin finRunning)
    {
        System.out.println("PLUGIN FINISHED: " + finRunning.retrieveURL());
        this.runningPlugins.put(finRunning.retrieveURL(), false);
        this.filter.finished(finRunning.retrieveURL()); 
    }

    public void submitHeadline(Headline headline)
    {
        this.filter.addHeadline(headline);
    }
}



































/*public class NewsController
{
    private Window ui = null;
    private List<NewsPlugin> plugins; 
    private LinkedBlockingQueue<Headline> queue; // Blocking queue for headlines
    private Map<Integer, String> websiteMap; // Key = Hash of source website, Value = source URL


    private ExecutorService ex; // Periodic plugin threads
    private ExecutorService exUpdate; // Update threads
    private Future<?> poller; // Controller thread
    // private boolean isRunning; // 
    private Map<String, Future<?>> pluginThreads; // Each periodic thread has an associated Future<> assigned to it (Key = URL)
    private Map<String, Future<?>> updateThreads; // Each update thread has an associated Future<> assigned to it (Key = URL)

    private Map<String, Map<Integer, Headline>> newsHeadlines; // Synchronize this too
    private Map<String, List<Headline>> cache; // Cache of headlines retrieved before plugin finished - synchronize this
    private Object monitor;

    public NewsController(String[] pluginNames)
    {   
        PluginLoader loader = new PluginLoader();
        this.websiteMap = new HashMap<>();
        this.pluginThreads = new HashMap<>();
        this.queue = new LinkedBlockingQueue<>();
        this.newsHeadlines = new HashMap<>();
        for( String in : this.newsHeadlines.keySet())
        {
            this.newsHeadlines.put(in, new HashMap<>() );
        }
        this.cache = new HashMap<>();
        monitor = new Object();
        // this.isRunning = true;
        // try
        // {   
            this.plugins = new LinkedList<>();
            for(String name : pluginNames)
            {
                try
                {
                    // Load the plugins from their origin
                    NewsPlugin plugin = loader.loadPlugin(name);   
                    plugin.setFrequency(30);
                    plugin.setController(this);
                    this.plugins.add(plugin);
                    // this.pluginThreads.put(plugin.retrieveURL(), null); // Create map of plugin threads, key = URL (source) 
                    // OR use stream below.
                }
                catch(ClassNotFoundException e)
                {
                    System.out.println("Attempting to load the next plugin");
                }
            }

            //Set up backend map
            getWebsites().stream()
                         .forEach((x)-> this.websiteMap.put(x.hashCode(), x)); 

            // Create map of plugin threads, key = URL (source)
            this.pluginThreads = new HashMap<>();
            
            getWebsites().stream()
                         .forEach(x -> this.pluginThreads.put(x, null) );
                                            //   .collect(Collectors.toMap(HashMap::new, 
                                            //             x->x,
                                            //             x->null));

            this.updateThreads = new HashMap<>(this.pluginThreads);

            // Create thread pool for all plugins + NewsController thread
            this.ex = Executors.newScheduledThreadPool(this.websiteMap.size()+1);

            // Create fixed thread pool for all plugins, to account for potential of all plugins updating at once
            this.exUpdate = Executors.newFixedThreadPool(this.websiteMap.size());

            // Schedule periodic plugins
            this.plugins.stream()
                        .forEach(x-> {
                            // Future<?> future = this.pluginThreads.get(x.retrieveURL());
                            // future = ((ScheduledExecutorService)(this.ex)).scheduleAtFixedRate(x, 0, x.getFreq(), TimeUnit.MINUTES );
                            this.pluginThreads.putIfAbsent(x.retrieveURL(), ((ScheduledExecutorService)(this.ex)).scheduleAtFixedRate(x, 0, x.getFreq(), TimeUnit.MINUTES ));
                        });
            
            // Task: To poll for the meed to schedule threads
            // Runnable poll = new Runnable(){
            //     @Override
            //     public void run()
            //     {
            //         while(isRunning)
            //         {

            //         }
            //     }
            // };


        // }
        // catch(ClassNotFoundException e)
        // {
        //     System.out.println(e.getMessage());
        // }
    }
*/
    /**
     * Set the controller's reference to the UI
     */
/*    public void setUI(Window ui)
    {
        this.ui = ui;
    } */

    /** 
     * For the UI update button to notify that an update has been requested from the user
     */
    /*public void update()//Set<Integer> uiHeadlines)
    {
        this.poller = this.ex.submit( () -> {
        // int threadPoolSize = 4;
        // ExecutorService ex = Executors.newFixedThreadPool(threadPoolSize);
        // List<Future<List<Headline>>> future = new LinkedList<>();
        System.out.println("Controller updating");
        // try
        // {   
            for(NewsPlugin plugin : this.plugins) // For each new plugin loaded
            {   
                Future<?> periodicFuture = this.pluginThreads.get(plugin.retrieveURL()); // Retrieve periodic thread Future

                Future<?> updateFuture = this.updateThreads.get(plugin.retrieveURL()); // Retrieve update thread Future
                
                // If the plugin doesn't have an associated Future or associated Future is not running, both periodic and update
                if( periodicFuture == null || periodicFuture.isDone() || updateFuture == null || updateFuture.isDone() ) 
                {
                    System.out.println("STARTING PLUGIN: " + plugin.retrieveURL());
                    // future.add(this.ex.submit(plugin));

                    updateFuture = this.exUpdate.submit(plugin);

                    this.updateThreads.put(plugin.retrieveURL(), updateFuture);
                }
                else
                {
                    System.out.println(plugin.retrieveURL() + " is already updating");
                }
                
            }


            System.out.println("FINISHED SUBMITTING TO EXECUTORS" + this.updateThreads.size());//values().stream()
            // .filter(x -> !x.isDone())
            // .count());

            for(Future<?> fut : this.updateThreads.values())
            {
                System.out.println(fut.isDone());
            }

            // System.out.println("INSERTING IN BLOCKING QUEUE");
            // future.stream()
            //         .forEach((x) -> {
            //             try
            //             {
            //                 x.get().stream()
            //                 .forEach((y) -> {
            //                     try
            //                     {
            //                         this.queue.put(y);
            //                     }
            //                     catch(Exception e){}
            //                 });
            //             }
            //             catch(Exception e){}
            //         });

            // Loop until all threads are done, then insert final poison object
            // while( this.updateThreads.values().stream()
            //                                   .filter(x -> !x.isDone())
            //                                   .count() > 0 );
            
                                            //   System.out.println("POISON INSERTED");
            // this.queue.put( new Headline("POISON_PILL", -1, -1, "") );


            // System.out.println("UPDATING UI CALL");
            // updateUI();//uiHeadlines);
             // For testing 
            // lines.stream()
            //      .forEach(System.out::println);
                     
                 //(x)->System.out.println(x.toString()));

            
        // }
        // catch(InterruptedException e){}
        // catch(ExecutionException e){}
        });



        System.out.println("SUBMITTED UPDATE");
    }

    public void updateUI()//Set<Integer> uiKeys)
    {
        List<Integer> remove;
        List<Integer> add;
        List<Headline> sendToUI = new LinkedList<>();
        Map<Integer, Headline> newEntries = new HashMap<>();
        Map<String, Map<Integer, Headline>> currentUIMap;
        try
        {
            System.out.println("CHECKING FOR INTERRUPTIONS " + this.plugins.stream()
            .anyMatch( x -> x.interrupted() ));
            if( this.plugins.stream()
                            .anyMatch( x -> !x.interrupted() ) )
            {
                Headline curr = this.queue.take();
                
                System.out.println("STARTING RETRIEVE");
                // while( !"POISON_PILL".equals(curr.getHeadline()) )
                while( !curr.getHeadline().contains("POISON") ) // Stop if a POISON object of any kind is found
                {   
                    System.out.println("TAKE FROM QUEUE: "+curr.toString());
                    // Create a map of the new entries 
                    newEntries.put((curr.getSource() + curr.getHeadline()).hashCode() , curr);
                    curr = this.queue.take();
                }
System.out.println(curr.getHeadline());
                StringBuilder builder = new StringBuilder(curr.getHeadline());
                int index = builder.indexOf("POISON");
                String finishedPlugin = builder.substring(0, index);
System.out.println("Finished Plugin name: " + finishedPlugin);
                // Retrieve current version of the UI's news headlines at the current moment
                synchronized(this.monitor)
                {
System.out.println("SYNCED");
                    currentUIMap = new HashMap<>(this.newsHeadlines); 
System.out.println("SYNCED      1");
                    // Add to cache, new headlines not related to finished plugin
                    newEntries.values().stream()
                                    .filter( x -> x.getSource().equals(finishedPlugin) )
                                    .forEach( x -> {
                                        System.out.println("source.... " + x.getSource());
                                        List<Headline> list = this.cache.get(x);
                                        list.add(x);
                                        this.cache.put(x.getSource(), list);
                                    });



    System.out.println("SYNCED      2");
    List<Headline> fromCache = null;
                    // Remove from cache the previous headlines related to the finished plugin
                    if( this.cache.containsKey(finishedPlugin))
                         fromCache = this.cache.get( finishedPlugin );

System.out.println(fromCache);                    

                    if(fromCache != null)
                    {

                         System.out.println("SYNCED      3");

                        System.out.println("SYNCED      4");                    
                        fromCache.stream()
                             .forEach( x -> newEntries.put((x.getSource() + x.getHeadline()).hashCode() , x));
                    }
System.out.println("END SYNCED");                             
                // }

System.out.println(currentUIMap);
                // At this point there is a map of new entries (just retrieve and from cache)


                System.out.println("ENDING RETRIEVE\nStarting filter");
                // Set<Integer> newKeys = newEntries.keySet();
                // Set<Integer> newKeys2 = newEntries.keySet();
                // Set<Integer> oldKeys = new HashSet<>();
                // uiKeys.stream()
                //       .forEach((x) -> oldKeys.add(x));
remove = new LinkedList<>();
System.out.println("currentUIMap.size" + currentUIMap.size() );
                if(currentUIMap.size() > 0) // If there are things to remove
                {
                    remove = filterOld(newEntries, currentUIMap, finishedPlugin);
                }
System.out.println("STARTING filterNew()\n    " + newEntries.size());
add = new LinkedList<>();
                if (newEntries.size() > 0 && currentUIMap.size() > 0) 
                {
                    add = filterNew(newEntries, currentUIMap, finishedPlugin);
                }
                else if (newEntries.size() > 0)
                {
                    for(Integer en : newEntries.keySet())
                    {
                        add.add(en);
                    }
                }
                


System.out.println("FINISHED filterNew()");
                // Remove from copy of UIMap
                if (remove.size() > 0){
                remove.stream()
                    .forEach(x -> {
                            Map<Integer,Headline> innerMap = currentUIMap.get(finishedPlugin);
                            innerMap.remove(x);
                            currentUIMap.put(finishedPlugin, innerMap);
                        });
                }
System.out.println("STARTING addtocopy of UIMap");

                if (add.size() > 0){
                // Add to copy of UIMap
                add.stream()
                .forEach(x -> {
                    Map<Integer,Headline> innerMap;
                    if( currentUIMap.containsKey(finishedPlugin) )
                    {
                       innerMap = currentUIMap.get(finishedPlugin);
                    }
                    else
                    {
                        innerMap = new HashMap<>();
                        
                    }
                    innerMap.put(x, newEntries.get(x));
                    currentUIMap.put(finishedPlugin, innerMap);
                });
            
System.out.println("STARTING retrieve headlines from UIMap");       
                // Retrieve Headlines to add to UI
                add.stream()
                .forEach( x -> sendToUI.add(currentUIMap.get(finishedPlugin).get(x)));
            }
    System.out.println("STARTING SYNC 2");
                // synchronized(this.monitor)
                // {
                    this.newsHeadlines = new HashMap<>(currentUIMap);
                }

                System.out.println("Updated Actual\n    " + this.newsHeadlines);
                System.out.println("END copy\n    "  +currentUIMap);
        System.out.println("UPDATE UI\n    remove: " + remove + "\n    add" + sendToUI);
                // Pass remove and add details to UI
                this.ui.update(remove, sendToUI);
            }
        }
        catch(InterruptedException e)
        {

        }
        
    }

    public List<Integer> filterOld(Map<Integer, Headline> newEntries, Map<String, Map<Integer, Headline>> currentUIMap, String finishedPlugin)
    {
        // Find the old keys not in the new entries, thus need to be removed
        List<Integer> toRemove = currentUIMap.get(finishedPlugin).keySet().stream()
                                                                          .filter(x-> !newEntries.containsKey(x))
                                                                          .collect(Collectors.toCollection(LinkedList::new));

        return toRemove;
    }

    public List<Integer> filterNew(Map<Integer, Headline> newEntries, Map<String, Map<Integer, Headline>> currentUIMap, String finishedPlugin)
    {
        // newKeys.removeAll(oldKeys);
        // List<Headline> newHeads = new LinkedList<>();
        // newKeys.stream()
        //         .filter( x -> headlines.containsKey(x) )
        //         .forEach( x -> {
        //                 newHeads.add(headlines.get(x));
        //             });
        // System.out.println("NEW COUNT: " + newHeads.size());
        // return newHeads;

        List<Integer> toAdd = newEntries.keySet().stream()
                                                 .filter(x-> !currentUIMap.get(finishedPlugin).containsKey(x))
                                                 .collect(Collectors.toCollection(LinkedList::new));

        return toAdd;
    }

    public void submitHeadline(Headline headline) 
    {
        try
        {
            this.queue.put(headline);
        }
        catch(InterruptedException e)
        {
            System.out.println("Could not add headline to blocking queue");
        }
    }
*/
    /**  
     * For the UI cancel button to notify that update cancel has been requested from the user
    */
  /*  public void cancel()
    {
        System.out.println("Controller cancelling\nEmptying blocking queue");

        // try
        // {
            // Cancel running threads
            this.updateThreads.values().stream()
                                       .filter(x -> !x.isDone())
                                       .forEach(x -> x.cancel(true));

        //     // Clear the current blocking queue
        //     this.queue.clear();
        //     // Put in a poison object
        //     this.queue.put( new Headline("POISON_PILL", -1, -1) );
        // }
        // catch(InterruptedException e)
        // {
        //     System.out.println("Could not cancel update");
        // }
        
    }
*/
    /**
     * Retrieve a list of the sources (plugins) loaded into the controller
     */
/*    public List<String> getWebsites()
    {
        return this.plugins.stream()
                           .map(NewsPlugin::retrieveURL)
                           .collect(Collectors.toCollection(LinkedList::new));
    }*/

    /**
     * Update the UI with currently running tasks
     */
    /*public void running(int pluginCode)
    {
        this.ui.runningTasks(pluginCode);
    }
}*/