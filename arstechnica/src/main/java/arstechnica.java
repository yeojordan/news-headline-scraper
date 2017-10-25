import java.util.*;
import java.text.*;
import java.io.*;

public class arstechnica extends NewsPlugin
{
    private String match = "<h2>";
    private String endMatch = "</h2>";
    private StringBuilder rawHTML;
    private boolean interrupted;
    private String url = "https://arstechnica.com";

    /**
     * Run method for runnable task
     */
    @Override
    public void run()
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
            System.out.println(this.url + "     " + time.toString());
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
        catch(InterruptedException e)
        {}

    }

    /**
     * Parse the HTML to create a headline 
     */
    public Headline createHeadline(String headlineTag, Date time) throws InterruptedException
    {
        Headline headline = null;
        String urlMatch = "<a href=\"";
        String urlEndMatch ="\">";

        StringBuilder head = new StringBuilder(headlineTag);
        int urlIdx = head.indexOf(urlMatch);

        // Remove extra tags, specific to the plugin
        head = removeExtraTags(head);

        // Isolate the URL if possible, and remove it from the string
        head.delete(0, urlIdx + urlMatch.length());
        int endURLIdx = head.indexOf(urlEndMatch);        
        head.delete(0, endURLIdx+urlEndMatch.length());

        // Retrieve the headline
        int headEndIdx = head.indexOf("</a>");
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
        // Replace <em> and </em> with single quotes
        String quote = "<em>";
        String endQuote = "</em>";
        while( head.indexOf(quote) != -1)
        {
            // Replace the single quotes
            int idx = head.indexOf(quote);
            head.replace(idx, idx+quote.length(), "'");
            idx = head.indexOf(endQuote);
            head.replace(idx, idx+endQuote.length(), "'");
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
    public List<String> parse() throws InterruptedException
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
    //     System.out.println("Arstechnica updating: " + super.getFreq());
    // }

    /**
     * Set the update frequency for the plugin
     */
    public void setFrequency(int updateFrequency)
    {
        super.setFrequency(updateFrequency);
    }

    /**
     * Retrieve the plugin's URL source
     */
    public String retrieveURL()
    {
        return "https://arstechnica.com";
    }
    
    // public String getKey()
    // {
    //     return this.match;
    // }
}