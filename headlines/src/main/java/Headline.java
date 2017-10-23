import java.util.*;
import java.text.*;

public class Headline
{
    // Classfields
    private String headline; // Headline text
    private long time; // Time of download
    private String source; // Source for headline

    /**
     * Constructor for headlines
     */
    public Headline(String headline, long time, String source)
    {
        this.headline = headline;
        this.time = time;
        this.source = source;
    }

    /**
     * Set the news headline's source
     */
    public void setWebsite(String source)
    {
        this.source = source;
    }

    /**
     * Retrieve the headline's source
     */
    public String getSource()
    {
        return this.source;
    }

    /**
     * Retrieve the headline text
     */
    public String getHeadline()
    {
        return this.headline;
    }

    /**
     * Retrieve the time downloaded
     */
    public long getTime()
    {
        return this.time;
    }

    /**
     * Format the headline for output
     */
    public String toString()
    {
        // Create data 
        Date date = new Date(this.time);
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd hh:mma");

        return new String(this.source + ":    "  + this.headline + "     (" + format.format(date) + ")" );
    }
}