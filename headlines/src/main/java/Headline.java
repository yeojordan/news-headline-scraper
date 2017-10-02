import java.util.*;

public class Headline
{
    private String source;
    private String headline;
    private Date date;
    private String url;

    public Headline(String source, String headline, Date date, String url)
    {
        this.source = source;
        this.headline = headline;
        this.date = date;
        this.url = url;
    }

    public String toString()
    {
        return "Headline Model";
    }
}