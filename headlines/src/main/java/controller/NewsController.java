package controller;

import model.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;



public class NewsController
{
    private Map<String, NewsPlugin> plugins; // Map of all the plugins loaded from the command line
    private ExecutorService exService;  // Executor service for updates to run
    private ExecutorService exScheduled; // Executor service for periodic updates to run
    private NewsFilter filter; // Reference to the NewsFilter
    
    // Map of each scheduled plugin's future, that is created upon submission to the respective executor service
    private Map<String, Future<?>> scheduledFutures; 
    // Map of each update plugin's future, that is created upon submission to the respective executor service
    private Map<String, Future<?>> updateFutures;

    // Constants 
    private final String pluginPath = "/build/classes/main/";
    private final int NUM_THREADS = 13;
    
    // Logger 
    private static Logger theLogger = Logger.getLogger(NewsController.class.getName());

    /**
     * Default constructor for the NewsController
     */
    public NewsController()
    {
        // Instantiate class fields
        this.plugins = new HashMap<>();
        this.scheduledFutures = new HashMap<>();
        this.updateFutures = new HashMap<>();
        this.plugins.values().stream()
                             .forEach( x -> {
                                 this.scheduledFutures.put(x.retrieveURL(), null);
                                 this.updateFutures.put(x.retrieveURL(), null);
                                });
        theLogger.info("NewsController Constructed");
    }

    /** 
     *  Load the plugins at runtime for the news sources to be retrieved from
     */
    public void loadPlugins(String[] pluginLocations)
    {
        PluginLoader loader = new PluginLoader();
        // Load plugins from the command line and store them in the appropriate map
        for(String pluginName : pluginLocations)
        {
            try
            {   
                // Append the necessary path to the plugin.
                pluginName = pluginName + this.pluginPath + pluginName + ".class";
                NewsPlugin plugin = loader.loadPlugin(pluginName);
                plugin.setFrequency(4);
                plugin.setController(this);
                // Insert instantiated plugin into map
                this.plugins.put(plugin.retrieveURL(), plugin);
            }
            catch(ClassNotFoundException e)
            {
                theLogger.warning(e.getMessage());
                this.alert(e.getMessage());
            }    
        }

        theLogger.info("Plugins Loaded");

        // Create executor service for scheduled and update threads
        this.exScheduled = Executors.newScheduledThreadPool(NUM_THREADS);
        this.exService = Executors.newFixedThreadPool(NUM_THREADS);

        theLogger.info("Executor Services created");

        // Schedule each plugin and keep each Future in a map
        scheduleUpdates();
    }

    /** 
     * Schedule all plugins to run simultaneously, if they are not already running.
     */
    public void updateAll()
    {
        this.plugins.keySet().stream()
                             .forEach( x -> {
                                 // If scheduled update is not running
                                 if( !this.scheduledFutures.get(x).isCancelled() || this.scheduledFutures.get(x).isDone() )
                                 {
                                     // If forced update is also not running or null
                                     if( this.updateFutures.get(x) == null || this.updateFutures.get(x).isDone() )
                                     {
                                        System.out.println("\n\n\n            UPDATE RUNNING: " + x );
                                         // Submit for execution
                                         this.updateFutures.put(x, this.exService.submit(this.plugins.get(x)));
                                         System.out.println("SUBMITTING: " + x);
                                     }
                                 }
                             });

        theLogger.info("All plugins scheduled for update");
    }

    /** 
     * Cancel all active downloads. 
     */
    public void cancelDownloads()
    {   
        // Cancel all running plugins
        this.plugins.keySet().stream()
                             .forEach( x -> {
                                 // If scheduled update is running
                                 if( !this.scheduledFutures.get(x).isDone() || !this.scheduledFutures.get(x).isCancelled() )
                                 { 
                                    this.scheduledFutures.get(x).cancel(true); 
                                 }  
                                 // If forced update is also running or null
                                 if( this.updateFutures.get(x) != null )
                                 {
                                     // Submit for execution if 
                                    if(!this.updateFutures.get(x).isDone())
                                    {
                                         this.updateFutures.get(x).cancel(true);
                                    }
                                 }
                             });

        theLogger.info("All active downloads cancelled");
        // Resubmit all periodic update plugins 
        resubmit();
    }

    /** 
     * Create a new thread pool for periodic updates and resubmit all period update plugins
     */
    public void resubmit()
    {
        // If the scheduled executor is still running, shut it down
        if(!this.exScheduled.isShutdown())
        {
            this.exScheduled.shutdownNow();
        }

        // Clear all Futures for the previously scheduled plugins
        this.scheduledFutures.clear();

        // Create a new thread pool
        this.exScheduled = Executors.newScheduledThreadPool(NUM_THREADS);        

        // Schedule each periodic update plugin and keep each Future in a map
        scheduleUpdates();

        theLogger.info("Perodic updates rescheduled");
    }

    /**
     * Schedule update plugin task
     */
    public void scheduleUpdates()
    {   
        // Schedule each plugin and keep each Future in a map
        // Periodic updates are wrapped in a lambda, to check there is not already a forced update running
        this.plugins.values().stream()
                             .forEach( x -> this.scheduledFutures.put(x.retrieveURL(), 
                                     ((ScheduledExecutorService)(this.exScheduled)).scheduleAtFixedRate( () -> {
                                         // Run periodic update for plugin, if not currently downloading
                                         if( updateFutures.get(x.retrieveURL()) == null ||  
                                             updateFutures.get(x.retrieveURL()).isDone() )
                                         {
                                             x.run(); 
                                         }
                                     }, x.getFreq()*1, x.getFreq()*3 ,TimeUnit.SECONDS)) );

        theLogger.info("Perodic updates scheduled");
    }


    /**
     * Update the NewsFilter with the currently running plugin
     */
    public void running(NewsPlugin running)
    {
        // Let filter know which plugin is running
        this.filter.running(running.retrieveURL()); 
        theLogger.info("Plugin running: " + running.retrieveURL());
    }

    /**
     * Update the NewsFilter with the a plugin that has finished running
     */
    public void finishedRunning(NewsPlugin finRunning)
    {
        // Let filter know which plugin is finished
        this.filter.finished(finRunning.retrieveURL()); 
        theLogger.info("Plugin finished: " + finRunning.retrieveURL());
    }

    /**
     * Send a newly created headline to the NewsFilter
     */
    public void submitHeadline(Headline headline)
    {
        this.filter.addHeadline(headline);
        // theLogger.info("Headline submitted");
    }

    /**
     * Notify the NewsFilter of any alerts that need to be sent to the UI
     */
    public void alert(String msg)
    {
        this.filter.alert(msg);
        theLogger.warning("Alert sent to user: " + msg);
    }

    /**
     *  Add a reference to the NewsFilter in the controller.
     */
    public void setFilter(NewsFilter filter)
    {
        this.filter = filter;
        theLogger.info("Set reference to NewsFilter");
    }
}

