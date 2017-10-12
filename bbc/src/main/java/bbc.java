import java.util.*;
import java.text.*;

// public class BBCPlugin extends NewsPlugin
public class bbc extends NewsPlugin
{
    private String match = "<h3 class=\"gs-c-promo-heading__title";
    private String endMatch = "</h3>";
    private StringBuilder rawHTML;
    private boolean interrupted;
    // private HeadlineFactory fact;
    private String url = "http://www.bbc.com/news";
    @Override
    public void run() throws IllegalArgumentException
    {
        // List<Headline> headlines = new LinkedList<>();
        try
        {
            super.running(this);
            this.interrupted = true;
            this.rawHTML = super.downloadHTML();

            long time = new Date().getTime();
            SimpleDateFormat format = new SimpleDateFormat("YY-MM-dd hh:mma");

            // System.out.println("CURRENT DATE TIME" + new Date().toString());
            // System.out.println("CURRENT TIME" + format.format(new Date(tempTime)));

            List<String> hTags = parse();

            // int hash = this.retrieveURL().hashCode();
            for(String tag : hTags)
            {
                // Invoke super method to send each headline created to the controller
                Headline temp = createHeadline(tag, time);
                if( temp != null )
                {
                    super.sendHeadline(temp);
                }
            }
            this.interrupted = false;
            super.finished(this);
            

        }
        catch(Exception e)
        {
            throw new IllegalArgumentException("Unable to create arstechnica headline", e);
        }
        

    }

    public Headline createHeadline(String headlineTag, long time)
    {
        Headline headline;
        // String matcher;
        String startMatch = ">";
        String urlEndMatch ="\">";
        // matcher = this.pluginKeys.get(hash);

        if( headlineTag.contains("</div>"))
        {
            return null;
        }
        StringBuilder head = new StringBuilder(headlineTag);
        int urlIdx = head.indexOf(startMatch);
        
        head.delete(0, urlIdx + startMatch.length());
        // int endURLIdx = head.indexOf(urlEndMatch);
        
        // String source = head.substring(0, endURLIdx);
        
        System.out.println("SOURCE: " + headlineTag);
        // head.delete(0, endURLIdx+urlEndMatch.length());

        // Replace &#x27; with single quote
        String quote = "&#x27;";
        while( head.indexOf(quote) != -1 )
        {
            int idx = head.indexOf(quote);
            head.replace(idx, idx+quote.length(), "'");
        }


        int headEndIdx = head.indexOf("</h3>");
        String headlineText = head.substring(0, headEndIdx);
        // System.out.println("HEADLINE: " + headlineText);

        headline = new Headline(headlineText, time, this.url.hashCode(), this.url);

        return headline;
    }

    public boolean interrupted() 
    {
        return this.interrupted;
    }

    public List<String> parse()
    {
        List<String> headlineTags = new LinkedList<>();
        int startIdx = 0;
        int endIdx = 0; 

        while(startIdx != -1 && endIdx != -1)
        {
            startIdx = this.rawHTML.indexOf(this.match);
            if (startIdx != -1)
            {    
                this.rawHTML.delete(0, startIdx); // Trim start and discard
            }

            endIdx = this.rawHTML.indexOf(this.endMatch);
            if(endIdx != -1)
            {            
                String retrieved = this.rawHTML.substring(0, endIdx+this.endMatch.length());
                this.rawHTML.delete(0, endIdx+5); //stop sequence.length()
                headlineTags.add(retrieved);
            }
        }
        
        return headlineTags;
    }

    public void update()
    {
        System.out.println("BBC updating: " + super.getFreq());
    }

    public void setFrequency(int updateFrequency)
    {
        super.setFrequency(updateFrequency);
    }

    // public void setFactory(HeadlineFactory fact)
    // {
    //     this.fact = fact;
    // }

    public String retrieveURL()
    {
        return this.url;
    }
    
    public String getKey()
    {
        return this.match;
    }
    
    
}