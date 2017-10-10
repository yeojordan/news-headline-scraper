import javax.swing.SwingUtilities;

public class NewsHeadlines
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run()
            {
                System.out.println("INITIALISING");
                NewsController controller = new NewsController(args);
                Window window = new Window(controller);
                controller.setUI(window);
                window.setVisible(true);
            }
        });
    }
}