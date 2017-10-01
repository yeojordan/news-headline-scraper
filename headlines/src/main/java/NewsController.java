import java.util.*;

public class NewsController
{
    private Window ui = null;
    private List<NewsPlugin> plugins; 

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
        System.out.println("Controller updating");
        for(NewsPlugin plugin : this.plugins)
        {
            plugin.update();
        }
    }

    /**  
     * For the UI cancel button to notify that update cancel has been requested from the user
    */
    public void cancel()
    {
        System.out.println("Controller cancelling");
    }
}