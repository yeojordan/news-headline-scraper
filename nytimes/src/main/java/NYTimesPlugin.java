import java.util.*;
import java.text.*;

public class NYTimesPlugin extends NewsPlugin
{
    private String match = "<h2 class=\"story-heading\">";//"<article class=\"story "; 
    private String endMatch = "</h2>";//"</article>";
    private String extraToIgnore = "i class=\"icon\"></i>";
    private String url = "https://www.nytimes.com";
    private StringBuilder rawHTML;
    private boolean interrupted;
    



    @Override
    public void run() throws IllegalArgumentException
    {
        // List<Headline> headlines = new LinkedList<>();
        try
        {
            super.running(this.url.hashCode());
            this.interrupted = true;
            this.rawHTML = super.downloadHTML();

            long time = new Date().getTime();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

            // System.out.println("CURRENT DATE TIME" + new Date().toString());
            // System.out.println("CURRENT TIME" + format.format(new Date(tempTime)));

            List<String> hTags = parse();
System.out.println("HTAG SIZE" + hTags.size());
            // int hash = this.retrieveURL().hashCode();
            int count = 0;
            for(String tag : hTags)
            {
count++;
System.out.println("count " + count);
                // Invoke super method to send each headline created to the controller
                Headline temp = createHeadline(tag, time);
                if( temp != null )
                {
                    super.sendHeadline(temp);
                }
                
            }
            this.interrupted = false;
            super.lastHeadline();
            
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException("Unable to create nytimes headline", e);
        }
        
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
                System.out.println("    " + retrieved);
                headlineTags.add(retrieved);
            }
        }
        
        System.out.println("            CONTAINS: " +headlineTags.stream()
                    .filter(x-> x.contains("<h2 class=\"story-heading\">") )
                    .count());

        return headlineTags;
    }

    public Headline createHeadline(String headlineTag, long time)
    {
        System.out.println("     "+headlineTag);
        Headline headline;
        // String matcher;
        String urlMatch = "<a href=\"";
        String urlEndMatch ="\">";
        // matcher = this.pluginKeys.get(hash);

        StringBuilder head = new StringBuilder(headlineTag);
        int urlIdx = head.indexOf(urlMatch);
        
        if( headlineTag.contains("<%=") )
        {
            return null;
        }
        // Remove if the headlineTag contains icon tag
        if( headlineTag.contains(extraToIgnore) )
        {
            // <h2 class="story-heading"><i class="icon"></i><a href="https://www.nytimes.com/2017/10/05/us/politics/tim-murphy-resigns-abortion-scandal.html">Conservative Congressman Resigns Amid Abortion Scandal</a> 
            int start = head.indexOf(extraToIgnore);
            head.delete(start, start + extraToIgnore.length());
        }
        String headlineText ="";
        if( headlineTag.contains(urlMatch) )
        {
            head.delete(0, urlIdx + urlMatch.length());
            int endURLIdx = head.indexOf(urlEndMatch);
            
            String source = head.substring(0, endURLIdx);
            
            // System.out.println("SOURCE: " + source);
            head.delete(0, endURLIdx+urlEndMatch.length());
            int headEndIdx = head.indexOf("</a>");
            headlineText = head.substring(0, headEndIdx);
            // System.out.println("HEADLINE: " + headlineText);
        }
        
        else
        {
            // int start = head.indexOf();
            return null;
        }
        headlineText = headlineText.trim();
        headline = new Headline(headlineText, time, this.url.hashCode(), this.url);
        
        System.out.println(headlineText);
        return headline;
    }


    public String getKey()
    {
        return this.match;
    }

    public void update()
    {
        System.out.println("NY Times updating: " + super.getFreq());
    }

    public void setFrequency(int updateFrequency)
    {
        super.setFrequency(updateFrequency);
    }

    public String retrieveURL()
    {
        return "https://www.nytimes.com";
    }
    
    public boolean interrupted()
    {
        return this.interrupted;
    }
}
