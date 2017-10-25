import java.util.*;
import java.text.*;


public class bbc extends NewsPlugin
{
    private String match = "<h3 class=\"gs-c-promo-heading__title";
    private String endMatch = "</h3>";
    private StringBuilder rawHTML;
    private boolean interrupted;
    private String url = "http://www.bbc.com/news";

    /**
     * Run method for runnable task
     */
    @Override
    public void run() throws IllegalArgumentException
    {
        try
        {
            // Update status of running plugin
            super.running(this);
            this.interrupted = true;

            // Retrieve HTML from website
            this.rawHTML = super.downloadHTML();

            // Retrieve the current time
            Date time = new Date();

            // Parse HTML to retrieve headlines, in HTML
            List<String> hTags = parse();

            // Iterate through all HTML tags with potential headlines
            for(String tag : hTags)
            {
                // Create headline from tag
                Headline temp = createHeadline(tag, time);

                // Ensure a valid headline was created
                if( temp != null )
                {
                    // Invoke super method to send each headline created to the controller
                    super.sendHeadline(temp);
                }
            }
            this.interrupted = false;

            // Update status to finished
            super.finished(this);
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException("Unable to create arstechnica headline", e);
        }
    }

    /**
     * Parse the HTML to create a headline 
     */
    public Headline createHeadline(String headlineTag, Date time)
    {
        Headline headline = null;
        String startMatch = ">";

        StringBuilder head = new StringBuilder(headlineTag);

        // Remove the URL from the sequence
        int urlIdx = head.indexOf(startMatch);
        head.delete(0, urlIdx + startMatch.length());

        // Remove extra tags, specific to the plugin
        head = removeExtraTags(head);

        // Retrieve the headline
        int headEndIdx = head.indexOf("</h3>");
        String headlineText = head.substring(0, headEndIdx);

        // Last check to ensure a false positive headline isn't created
        if(!headlineText.contains("<") && headlineText.length() > 0)
        {
            headlineText = headlineText.trim();
            headline = new Headline(headlineText, time, this.url);
        }
        return headline;
    }

    /**
     * Remove the extra tags within the raw headline string
     */
    public StringBuilder removeExtraTags(StringBuilder head)
    {   
        // Replace &#x27; with single quote
        String quote = "&#x27;";
        while( head.indexOf(quote) != -1 )
        {
            int idx = head.indexOf(quote);
            head.replace(idx, idx+quote.length(), "'");
        }

        // Replace &amp; with ampersand
        String amp = "&amp;";
        while( head.indexOf(amp) != -1 )
        {
            int idx = head.indexOf(amp);
            head.replace(idx, idx+amp.length(), "&");
        }
        return head;
    }

    public boolean interrupted() 
    {
        return this.interrupted;
    }

    /**
     * Parse the raw HTML to find the potential headlines 
     */
    public List<String> parse()
    {
        List<String> headlineTags = new LinkedList<>();
        int startIdx = 0;
        int endIdx = 0; 

        // Look for all tags that have HTML tags for the news source headline
        while(startIdx != -1 && endIdx != -1)
        {
            // Find start of the heading tag
            startIdx = this.rawHTML.indexOf(this.match);
            if (startIdx != -1)
            {    
                // Trim start and discard
                this.rawHTML.delete(0, startIdx); 
            }

            // Find end of the heading tag
            endIdx = this.rawHTML.indexOf(this.endMatch);
            if(endIdx != -1)
            {            
                // Retrieve headline tag from sequence
                String retrieved = this.rawHTML.substring(0, endIdx+this.endMatch.length());

                // Delete from the sequence
                this.rawHTML.delete(0, endIdx+this.endMatch.length()); 
                headlineTags.add(retrieved);
            }
        }
        
        return headlineTags;
    }

    // public void update()
    // {
    //     System.out.println("BBC updating: " + super.getFreq());
    // }

    /**
     * Set the update frequency for the plugin
     */
    public void setFrequency(int updateFrequency)
    {
        super.setFrequency(updateFrequency);
    }

    // public void setFactory(HeadlineFactory fact)
    // {
    //     this.fact = fact;
    // }

    /**
     * Retrieve the plugin's URL source
     */
    public String retrieveURL()
    {
        return this.url;
    }
    
    // public String getKey()
    // {
    //     return this.match;
    // }
    
    
}