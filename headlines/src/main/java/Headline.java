import java.util.*;

public class Headline
{
    // private String source;
    private String headline;
    private int time;
    private int sourceHashCode;
    // private Date date;
    // private String url;

    public Headline(String headline, int time, int sourceHashCode)
    {
        this.headline = headline;
        this.time = time;
        this.sourceHashCode = sourceHashCode;
        // this.source = source;
        // this.headline = headline;
        // this.date = date;
        // this.url = url;
    }

    public String toString()
    {
        return new String(this.sourceHashCode + "    "  + this.headline + "    " + this.time );
    }
}