// import javax.swing.SwingUtilities;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class NewsController
{
    private Map<String, NewsPlugin> plugins;
    private ExecutorService exService;
    private ExecutorService exScheduled;
    private NewsFilter filter;
    
    private Map<String, Future<?>> scheduledFutures;
    private Map<String, Future<?>> updateFutures;
    
    private final String pluginPath = "/build/classes/main/";
    private final int NUM_THREADS = 4;

    public NewsController(String[] pluginLocations)
    {
        PluginLoader loader = new PluginLoader();
        this.plugins = new HashMap<>();
        this.scheduledFutures = new HashMap<>();
        this.updateFutures = new HashMap<>();

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
                pluginName = pluginName + this.pluginPath + pluginName + ".class";
                NewsPlugin plugin = loader.loadPlugin(pluginName);
                plugin.setFrequency(4);
                plugin.setController(this);
                this.plugins.put(plugin.retrieveURL(), plugin);
            }
            catch(ClassNotFoundException e)
            {}
        }

        // Create executor service for scheduled and update threads
        this.exScheduled = Executors.newScheduledThreadPool(NUM_THREADS);
        this.exService = Executors.newFixedThreadPool(NUM_THREADS);
System.out.println("SUBMITTING " + NUM_THREADS);
        // Schedule each plugin and keep each Future in a map
        this.plugins.values().stream()
                             .forEach( x -> this.scheduledFutures.put(x.retrieveURL(), 
                                        ((ScheduledExecutorService)(this.exScheduled)).scheduleAtFixedRate(x, x.getFreq()*60, x.getFreq()*60 ,TimeUnit.SECONDS)) );
    }

    public void setFilter(NewsFilter filter)
    {
        this.filter = filter;
    }

    public void updateAll()
    {
System.out.println("UPDATE ALL REQUEST RECEIVED");

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
        resubmit();
    }

    public void resubmit()
    {
        System.out.println("RESCHEDULING PLUGINS");
        // If the scheduled executor is still running, shut it down
        if(!this.exScheduled.isShutdown())
        {
            this.exScheduled.shutdownNow();
        }
        
        this.scheduledFutures.clear();
        this.exScheduled = Executors.newScheduledThreadPool(NUM_THREADS);        
        // Schedule each plugin and keep each Future in a map
        this.plugins.values().stream()
                             .forEach( x -> this.scheduledFutures.put(x.retrieveURL(), 
                                     ((ScheduledExecutorService)(this.exScheduled)).scheduleAtFixedRate(x, x.getFreq()*60, x.getFreq()*60 ,TimeUnit.SECONDS)) );



    }
    public void running(NewsPlugin running)
    {
        // Let filter know which plugin is running
        this.filter.running(running.retrieveURL()); 
    }
    
    public void finishedRunning(NewsPlugin finRunning)
    {
        // Let filter know which plugin is finished
        this.filter.finished(finRunning.retrieveURL()); 
    }

    public void submitHeadline(Headline headline)
    {
        this.filter.addHeadline(headline);
    }
}

