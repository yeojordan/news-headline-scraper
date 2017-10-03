import java.util.*;
import java.text.*;

public class Headline
{
    // private String source;
    private String headline;
    private long time;
    private int sourceHashCode;
    private String source;
    // private Date date;
    // private String url;

    public Headline(String headline, long time, int sourceHashCode)
    {
        this.headline = headline;
        this.time = time;
        this.sourceHashCode = sourceHashCode;
        // this.source = source;
        // this.headline = headline;
        // this.date = date;
        // this.url = url;
    }

    public void setWebsite(String source)
    {
        this.source = source;
    }
    public int getHash()
    {
        return this.sourceHashCode;
    }

    public String getHeadline()
    {
        return this.headline;
    }

    public long getTime()
    {
        return this.time;
    }



    public String toString()
    {
        Date date = new Date(this.time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return new String(this.source + "    "  + this.headline + "     " + format.format(date) );
    }
}