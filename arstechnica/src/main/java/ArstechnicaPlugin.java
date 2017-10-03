import java.util.*;

public class ArstechnicaPlugin extends NewsPlugin
{
    private String match = "<h2>";
    private String endMatch = "</h2>";
    private StringBuilder rawHTML;
    // private HeadlineFactory fact;
    private String url = "https://arstechnica.com";
    @Override
    public List<Headline> call() throws IllegalArgumentException
    {
        List<Headline> headlines = new LinkedList<>();
        try
        {
            this.rawHTML = super.downloadHTML();
            int time = (int)(new Date().getTime()/1000);

            List<String> hTags = parse();
            
            int hash = this.retrieveURL().hashCode();
            for(String tag : hTags)
            {
                headlines.add( createHeadline(tag, time) );//this.fact.create( hash, tag ) );
            }
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException("Unable to create arstechnica headline", e);
        }

        return headlines;
        
        

    }

    public Headline createHeadline(String headlineTag, int time)
    {
        Headline headline;
        // String matcher;
        String urlMatch = "<a href=\"";
        String urlEndMatch ="\">";
        // matcher = this.pluginKeys.get(hash);

        StringBuilder head = new StringBuilder(headlineTag);
        int urlIdx = head.indexOf(urlMatch);
        


        head.delete(0, urlIdx + urlMatch.length());
        int endURLIdx = head.indexOf(urlEndMatch);
        
        String source = head.substring(0, endURLIdx);
        
        System.out.println("SOURCE: " + source);
        head.delete(0, endURLIdx+urlEndMatch.length());
        int headEndIdx = head.indexOf("</a>");
        String headlineText = head.substring(0, headEndIdx);
        System.out.println("HEADLINE: " + headlineText);

        headline = new Headline(headlineText, time, this.url.hashCode());

        return headline;
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