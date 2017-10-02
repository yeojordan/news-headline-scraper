import java.util.*;

public class ArstechnicaPlugin extends NewsPlugin
{
    private String match = "<h2>";
    private String endMatch = "</h2>";
    private StringBuilder rawHTML;

    @Override
    public List<String> call()
    {
        this.rawHTML = super.downloadHTML();
        return parse();
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
                // System.out.println("TRIM to discard:\n\n" + temp.toString());    
            }

            endIdx = this.rawHTML.indexOf(this.endMatch);
            if(endIdx != -1)
            {            
                String retrieved = this.rawHTML.substring(0, endIdx+5);
                this.rawHTML.delete(0, endIdx+5); //stop sequence.length()
                headlineTags.add(retrieved);
                System.out.println("RETRIEVED:\n\n" + retrieved + "\nEND RETRIEVED");
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

    public String retrieveURL()
    {
        return "https://arstechnica.com";
    }
    

    
}