import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.IOException;

import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.net.HttpURLConnection;
import java.net.*;

/** 
 * NewsPlugin abstract class
 * Each plugin must extend NewsPlugin
 */
public abstract class NewsPlugin implements Runnable
{
    // Classfields
    private int updateFreq;
    private NewsController controller;

    // Abstract methods
    public abstract String retrieveURL();
    public abstract boolean interrupted();
    
    /**
     * Set the reference to the controller
     */
    public void setController(NewsController controller)
    {
        this.controller = controller;
    }

    /**
     * Set the plugin's update frequency
     */
    public void setFrequency(int updateFrequncy)
    {
        this.updateFreq = updateFrequncy;
    }

    /**
     * Retrieve the update frequency for the plugin
     */
    public int getFreq()
    {
        return this.updateFreq;
    }

    /**
     * Subclass invokes when it has a headline to send
     */
    public void sendHeadline(Headline headline)
    {
        // Call controller's method to add a headline to a blocking queue
        this.controller.submitHeadline(headline);   
    }

    /**
     * Subclass to notify that it has finished
     */
    public void finished(NewsPlugin plugin)
    {
        this.controller.finishedRunning(plugin);
    }

    /**
     * Subclass to notify that it has begun running
     */
    public void running(NewsPlugin plugin)
    {
        this.controller.running(plugin);
    }

    /**
     * Download the HTML from a source URL
     */
    public StringBuilder downloadHTML()
    {
        StringBuilder parsedHTML = new StringBuilder();
        try
        {
            // Retrieve URL from the subclass
            URL url = new URL( retrieveURL() );

            // Set user agent
            HttpURLConnection con=(HttpURLConnection)(url.openConnection());
            System.setProperty("http.agent","");
            con.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

            try(ReadableByteChannel chan = Channels.newChannel(con.getInputStream()))
            {
                ByteBuffer buf = ByteBuffer.allocate(65536);
                byte[] array = buf.array();
                byte[] tempArray;
                int bytesRead = chan.read(buf);
                while(bytesRead != -1)
                {
                    // Trim extra bytes from array, that is unused
                    if( array.length > bytesRead)
                    {
                        tempArray = new byte[bytesRead];
                        tempArray = Arrays.copyOf(array, bytesRead);

                        // Convert bytes to string and add to stringbuilder
                        parsedHTML.append(new String(tempArray, "UTF-8"));
                    }
                    else
                    {
                        // Convert bytes to string and add to stringbuilder
                        parsedHTML.append(new String(array, "UTF-8"));
                    }

                    // Clear buffer and read again
                    buf.clear();
                    bytesRead = chan.read(buf);
                }
            }
            catch(ClosedByInterruptException e){}
            catch(UnknownHostException e)
            {
                notifyFail("No internet connection");
            }
            catch(IOException e){}
        }
        catch(Exception e){}

        return parsedHTML;
    }

    /**
     * Notify the controller of a failure occurring
     */
    public void notifyFail(String msg)
    {
        this.controller.alert(msg);
    }
}