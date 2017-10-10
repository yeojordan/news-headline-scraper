import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.IOException;
// import java.nio.ClosedByInterruptException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.net.HttpURLConnection;

/** 
 * NewsPlugin abstract class
 * Each plugin must extend NewsPlugin
 */
public abstract class NewsPlugin implements Runnable
{
    private int updateFreq;
    private NewsController controller;

    // Abstract methods
    public abstract String getKey();
    public abstract void update();
    public abstract String retrieveURL();
    public abstract boolean interrupted();
    
    public void setController(NewsController controller)
    {
        this.controller = controller;
    }

    public void setFrequency(int updateFrequncy)
    {
        this.updateFreq = updateFrequncy;
    }

    public int getFreq()
    {
        return this.updateFreq;
    }

    // Subclass invokes when it has a headline to send
    public void sendHeadline(Headline headline)
    {
        // Call controller's method to add a headline to a blocking queue
        this.controller.submitHeadline(headline);   
    }

    // protected void lastHeadline()
    // {
    //     this.controller.submitHeadline( new Headline( retrieveURL() + " POISON", -1, -1, " ") );
    //     this.controller.updateUI();
    // }

    public void finished(NewsPlugin plugin)
    {
        this.controller.finishedRunning(plugin);
    }
    
    public void running(NewsPlugin plugin)
    {
        this.controller.running(plugin);
    }

    public StringBuilder downloadHTML()
    {
        LinkedList<String> rawHTML = new LinkedList<>();
        StringBuilder parsedHTML = new StringBuilder();
        try
        {
            // URL url = new URL("https://arstechnica.com");
            // URL url = new URL("https://www.nytimes.com/");

            // URL url = new URL("http://www.bbc.com/news");
            URL url = new URL( retrieveURL() );

            // Set user agent
            HttpURLConnection con=(HttpURLConnection)(url.openConnection());
            System.setProperty("http.agent","");
            con.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
            // URL url = new URL("http://www.bbc.com/news");

            try(ReadableByteChannel chan = Channels.newChannel(con.getInputStream()))
            {
                ByteBuffer buf = ByteBuffer.allocate(65536);
                byte[] array = buf.array();
                byte[] tempArray;
                int bytesRead = chan.read(buf);
                while(bytesRead != -1)
                {
                    // Get data
                    // System.out.println(new String(array, "UTF-8"));
                    if( array.length > bytesRead)
                    {
                        tempArray = new byte[bytesRead];
                        tempArray = Arrays.copyOf(array, bytesRead);
                        // System.out.println(new String(tempArray, "UTF-8"));
                        parsedHTML.append(new String(tempArray, "UTF-8"));
                        rawHTML.add(new String(tempArray, "UTF-8"));
                    }
                    else
                    {
                        // Get data
                        // System.out.println(new String(array, "UTF-8"));
                        parsedHTML.append(new String(array, "UTF-8"));
                        rawHTML.add(new String(array, "UTF-8"));
                    }

                    // Clear buffer and read 
                    buf.clear();
                    bytesRead = chan.read(buf);
                }
            }
            catch(ClosedByInterruptException e){}
            catch(IOException e){}
        }
        catch(Exception e)
        {}
System.out.println("RAW HTML Size: " + rawHTML.size());
        return parsedHTML;
    }

}