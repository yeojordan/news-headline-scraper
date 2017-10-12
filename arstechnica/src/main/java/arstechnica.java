import java.util.*;
import java.text.*;

// public class ArstechnicaPlugin extends NewsPlugin
public class arstechnica extends NewsPlugin
{
    private String match = "<h2>";
    private String endMatch = "</h2>";
    private StringBuilder rawHTML;
    private boolean interrupted;
    // private HeadlineFactory fact;
    private String url = "https://arstechnica.com";
    @Override
    public void run() 
    // throws IllegalArgumentException
    {
        // List<Headline> headlines = new LinkedList<>();
        try
        {
            super.running(this);
            this.interrupted = true;
            this.rawHTML = super.downloadHTML();

            long time = new Date().getTime();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

            // System.out.println("CURRENT DATE TIME" + new Date().toString());
            // System.out.println("CURRENT TIME" + format.format(new Date(tempTime)));

            List<String> hTags = parse();

            // int hash = this.retrieveURL().hashCode();
            for(String tag : hTags)
            {
                // Invoke super method to send each headline created to the controller
                super.sendHeadline( createHeadline(tag, time) );
            }
            this.interrupted = false;
            super.finished(this);
            

        }
        catch(InterruptedException e)
        {
            // throw new IllegalArgumentException("Unable to create arstechnica headline", e);
        }
        

    }

    public Headline createHeadline(String headlineTag, long time) throws InterruptedException
    {
        Headline headline;
        // String matcher;
        String urlMatch = "<a href=\"";
        String urlEndMatch ="\">";
        // matcher = this.pluginKeys.get(hash);

        StringBuilder head = new StringBuilder(headlineTag);
        int urlIdx = head.indexOf(urlMatch);
        
        // Replace <em> and </em> with single quotes
        String quote = "<em>";
        String endQuote = "</em>";
        while( head.indexOf(quote) != -1)
        {
            int idx = head.indexOf(quote);
            head.replace(idx, idx+quote.length(), "'");
            idx = head.indexOf(endQuote);
            head.replace(idx, idx+endQuote.length(), "'");
        }

        head.delete(0, urlIdx + urlMatch.length());
        int endURLIdx = head.indexOf(urlEndMatch);
        
        String source = head.substring(0, endURLIdx);
        
        System.out.println("SOURCE: " + source);
        head.delete(0, endURLIdx+urlEndMatch.length());
        int headEndIdx = head.indexOf("</a>");
        String headlineText = head.substring(0, headEndIdx);
        // System.out.println("HEADLINE: " + headlineText);

        headline = new Headline(headlineText, time, this.url.hashCode(), this.url);

        return headline;
    }

    public boolean interrupted() 
    {
        return this.interrupted;
    }

    public List<String> parse() throws InterruptedException
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
        System.out.println("Arstechnica updating: " + super.getFreq());
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
        return "https://arstechnica.com";
    }
    
    public String getKey()
    {
        return this.match;
    }
    
    
}