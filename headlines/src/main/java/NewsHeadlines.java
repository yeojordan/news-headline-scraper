import javax.swing.SwingUtilities;

public class NewsHeadlines
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run()
            {
                NewsFilter filter = new NewsFilter();
                NewsController controller = new NewsController(args);
                controller.setFilter(filter);
                Window window = new Window(filter);
                
                filter.setUI(window);
                filter.setController(controller);
                window.setVisible(true);
            }
        });
    }
}