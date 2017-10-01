public class ArstechnicaPlugin extends NewsPlugin
{
    // public ArstechnicaPlugin(int updateFreq)
    // {
    //     super(updateFreq);
    // }

    public void update()
    {
        System.out.println("Arstechnica updating: " + super.getFreq());
    }

    public void setFrequency(int updateFrequency)
    {
        super.setFrequency(updateFrequency);
    }

    
}