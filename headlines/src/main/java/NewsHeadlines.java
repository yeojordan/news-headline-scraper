import model.*;
import controller.*;
import view.*;

import javax.swing.SwingUtilities;

public class NewsHeadlines
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater( () -> {
                NewsFilter filter = new NewsFilter();
                NewsController controller = new NewsController();
                controller.setFilter(filter);
                Window window = new Window(filter);
                Clock clock = new Clock(window);
                filter.setUI(window);
                filter.setController(controller);
                window.setVisible(true);
                controller.loadPlugins(args);
        });
    }
}