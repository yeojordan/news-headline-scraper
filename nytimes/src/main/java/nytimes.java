import model.*;
import java.util.*;
import java.text.*;

public class nytimes extends NewsPlugin
{
    private String match = "<h2 class=\"story-heading\">";
    private String endMatch = "</h2>";
    private String extraToIgnore = "i class=\"icon\"></i>";
    private String url = "https://www.nytimes.com";
    private StringBuilder rawHTML;
    private boolean interrupted;

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
            throw new IllegalArgumentException("Unable to create nytimes headline", e);
        }
        
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

    /**
     * Parse the HTML to create a headline 
     */
    public Headline createHeadline(String headlineTag, Date time)
    {
        Headline headline = null;
        String urlMatch = "<a href=\"";
        String urlEndMatch ="\">";

        StringBuilder head = new StringBuilder(headlineTag);
        int urlIdx = head.indexOf(urlMatch);

        // Check the headline tag does not contain css
        if( headlineTag.contains("<%=") )
        {
            return headline;
        }

        // Remove if the headlineTag contains icon tag
        if( headlineTag.contains(extraToIgnore) )
        {
            int start = head.indexOf(extraToIgnore);
            head.delete(start, start + extraToIgnore.length());
        }

        // Check the headline has a URL 
        String headlineText ="";
        if( headlineTag.contains(urlMatch) )
        {
            // Trim the start of irrelevant text
            head.delete(0, urlIdx + urlMatch.length());
            int endURLIdx = head.indexOf(urlEndMatch);
            
            // Remove the URL 
            head.delete(0, endURLIdx+urlEndMatch.length());

            // Remove extra tags, specific to the plugin
            removeExtraTags(head);
            int headEndIdx = head.indexOf("</a>");
            headlineText = head.substring(0, headEndIdx);
            
            // Last check to ensure a false positive headline isn't created
            if(!headlineText.contains("<") && headlineText.length() > 0)
            {
                headlineText = headlineText.trim();
                headline = new Headline(headlineText, time, this.url);
            }
        }
        return headline;
    }

    /**
     * Remove the extra tags within the raw headline string
     */
    public StringBuilder removeExtraTags(StringBuilder head)
    {
        // Replace &rsquo; with single quote
        String quote = "&rsquo;";
        while( head.indexOf(quote) != -1 )
        {
            int idx = head.indexOf(quote);
            head.replace(idx, idx+quote.length(), "'");
        }

        // Remove <br>
        String breakTag = "<br>";
        while( head.indexOf(breakTag) != -1 )
        {
            int idx = head.indexOf(breakTag);
            head.replace(idx, idx+breakTag.length(),"");
        }

        // Remove </br>
        String endBreakTag = "</br>";
        while( head.indexOf(endBreakTag) != -1 )
        {
            int idx = head.indexOf(endBreakTag);
            head.replace(idx, idx+endBreakTag.length(), "");
        }

        // Remove open and close span tag
        String span = "<span";
        String endSpan = "</span>";
        while( head.indexOf(span) != -1 )
        {
            int idx = head.indexOf(span);
            int end = head.indexOf(">", idx);
            head.delete(idx, end+1);
            end = head.indexOf(endSpan);
            head.delete(end, end+ endSpan.length());
        }

        return head;
    }

    // public String getKey()
    // {
    //     return this.match;
    // }

    // public void update()
    // {
    //     System.out.println("NY Times updating: " + super.getFreq());
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
        return "https://www.nytimes.com";
    }
    
    public boolean interrupted()
    {
        return this.interrupted;
    }
}

