import javax.swing.SwingUtilities;
import java.util.*;
import java.util.concurrent.*;

public class NewsController
{
    private Window ui = null;
    private List<NewsPlugin> plugins; 
    private LinkedBlockingQueue<String> queue;

    public NewsController(String[] pluginNames)
    {
        PluginLoader loader = new PluginLoader();
        try
        {   
            this.plugins = new LinkedList<>();
            for(String name : pluginNames)
            {
                NewsPlugin plugin = loader.loadPlugin(name);   
                plugin.setFrequency(30);
                this.plugins.add(plugin);
            }
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
    public void update()
    {
        ExecutorService ex = Executors.newFixedThreadPool(4);
        Future<List<String>> future = null;
        System.out.println("Controller updating");
        try
        {
            for(NewsPlugin plugin : this.plugins)
            {
                future = ex.submit(plugin);
            }

            updateUI(future.get());
        }
        catch(InterruptedException e){}
        catch(ExecutionException e){}
    }

    public void updateUI(List<String> list)
    {
        this.ui.updateList(list);
    }

    /**  
     * For the UI cancel button to notify that update cancel has been requested from the user
    */
    public void cancel()
    {
        System.out.println("Controller cancelling");
    }
}