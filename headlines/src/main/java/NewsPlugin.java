import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.IOException;
// import java.nio.ClosedByInterruptException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;

/** 
 * NewsPlugin abstract class
 * Each plugin must extend NewsPlugin
 */
public abstract class NewsPlugin implements Callable
{
    private int updateFreq;
    
    public NewsPlugin()//int updateFreq)
    {
        //this.updateFreq = updateFreq;
    }

    
    public abstract void update();

    public void setFrequency(int updateFrequncy)
    {
        this.updateFreq = updateFrequncy;
    }

    public int getFreq()
    {
        return this.updateFreq;
    }

    public abstract String retrieveURL();

    public StringBuilder downloadHTML()
    {
        StringBuilder parsedHTML = new StringBuilder();
        try
        {
            // URL url = new URL("https://arstechnica.com");
            // URL url = new URL("https://www.nytimes.com/");

            // URL url = new URL("http://www.bbc.com/news");
            URL url = new URL( retrieveURL() );

            try(ReadableByteChannel chan = Channels.newChannel(url.openStream()))
            {
                ByteBuffer buf = ByteBuffer.allocate(65536);
                byte[] array = buf.array();

                int bytesRead = chan.read(buf);
                while(bytesRead != -1)
                {
                    // Get data
                    // System.out.println(new String(array, "UTF-8"));
                    parsedHTML.append(new String(array, "UTF-8"));

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

        return parsedHTML;
    }

}