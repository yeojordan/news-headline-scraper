// import javax.swing.SwingUtilities;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class NewsController
{
    private Window ui = null;
    private List<NewsPlugin> plugins; 
    private LinkedBlockingQueue<Headline> queue;
    private Map<Integer, String> websiteMap;
    private ExecutorService ex;
    private Future<?> poller;
    private boolean isRunning;

    public NewsController(String[] pluginNames)
    {   
        PluginLoader loader = new PluginLoader();
        this.websiteMap = new HashMap<>();
        this.queue = new LinkedBlockingQueue<>();
        this.isRunning = true;
        try
        {   
            this.plugins = new LinkedList<>();
            for(String name : pluginNames)
            {
                NewsPlugin plugin = loader.loadPlugin(name);   
                plugin.setFrequency(30);
                this.plugins.add(plugin);
            }
            //Set up backend map
            getWebsites().stream()
                         .forEach((x)-> this.websiteMap.put(x.hashCode(), x)); 


            this.ex = Executors.newScheduledThreadPool(this.websiteMap.size()+1);
            Runnable poll = new Runnable(){
                @Override
                public void run()
                {
                    while(isRunning)
                    {

                    }
                }
            };


        }
        catch(ClassNotFoundException e)
        {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Set the controller's reference to the UI
     */
    public void setUI(Window ui)
    {
        this.ui = ui;
    }

    /** 
     * For the UI update button to notify that an update has been requested from the user
     */
    public void update(Set<Integer> uiHeadlines)
    {
        this.poller = this.ex.submit( () -> {
        int threadPoolSize = 4;
        ExecutorService ex = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<List<Headline>>> future = new LinkedList<>();
        System.out.println("Controller updating");
        try
        {   
            for(NewsPlugin plugin : this.plugins)
            {
                System.out.println("STARTING PLUGIN");
                future.add(ex.submit(plugin));
            }
            
            System.out.println("INSERTING IN BLOCKING QUEUE");
            future.stream()
                    .forEach((x) -> {
                        try
                        {
                            x.get().stream()
                            .forEach((y) -> {
                                try
                                {
                                    this.queue.put(y);
                                }
                                catch(Exception e){}
                            });
                        }
                        catch(Exception e){}
                    });

            this.queue.put( new Headline("POISON_PILL", -1, -1) );
            // lines.
            // List<Headline> lines = future.get();
            // filter(uiHeadlines);

            System.out.println("UPDATING UI CALL");
            updateUI(uiHeadlines);
             // For testing 
            // lines.stream()
            //      .forEach(System.out::println);
                     
                 //(x)->System.out.println(x.toString()));

            
        }
        catch(InterruptedException e){}
        // catch(ExecutionException e){}
        });
        System.out.println("SUBMITTED UPDATE");
    }

    public void updateUI(Set<Integer> uiKeys)
    {
        List<Integer> remove;
        List<Headline> add;
        Map<Integer, Headline> newEntries = new HashMap<>();
        try
        {
            Headline curr = this.queue.take();
            
            System.out.println("STARTING RETRIEVE");
            while( !"POISON_PILL".equals(curr.getHeadline()) )
            {   
                // Create a map of the new entries u
                newEntries.put((this.websiteMap.get(curr.getHash()) + curr.getHeadline()).hashCode() , curr);
                curr = this.queue.take();
            }
            System.out.println("ENDING RETRIEVE");
            Set<Integer> newKeys = newEntries.keySet();
            Set<Integer> newKeys2 = newEntries.keySet();
            Set<Integer> oldKeys = new HashSet<>();
            uiKeys.stream()
                  .forEach((x) -> oldKeys.add(x));

            remove = filterOld(newKeys, uiKeys);
            add = filterNew(newKeys2, oldKeys, newEntries);

            // System.out.println("FINAL CALL TO UI UPDATE");
            // for(Headline head : add)
            // {
            //     System.out.println(head.toString());
            // }
            this.ui.update(remove, add);
        }
        catch(InterruptedException e)
        {

        }
        
    }

    public List<Integer> filterOld(Set<Integer> newKeys, Set<Integer> oldKeys)
    {
        oldKeys.removeAll(newKeys);
        System.out.println("OLD COUNT: " + oldKeys.size());
        return new LinkedList<Integer>(oldKeys);
    }

    public List<Headline> filterNew(Set<Integer> newKeys, Set<Integer> oldKeys, Map<Integer, Headline> headlines)
    {
        newKeys.removeAll(oldKeys);
        List<Headline> newHeads = new LinkedList<>();
        newKeys.stream()
                .filter( x -> { return headlines.containsKey(x) == true; } )
                .forEach( x -> {
                        newHeads.add(headlines.get(x));
                    });
        System.out.println("NEW COUNT: " + newHeads.size());
        return newHeads;
    }

    /**  
     * For the UI cancel button to notify that update cancel has been requested from the user
    */
    public void cancel()
    {
        System.out.println("Controller cancelling");
    }

    public List<String> getWebsites()
    {
        return this.plugins.stream()
                           .map(NewsPlugin::retrieveURL)
                           .collect(Collectors.toCollection(LinkedList::new));
    }
}