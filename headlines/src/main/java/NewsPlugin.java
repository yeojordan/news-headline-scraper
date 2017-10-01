/** 
 * NewsPlugin abstract class
 * Each plugin must extend NewsPlugin
 */
public abstract class NewsPlugin
{
    private int updateFreq;
    
    public NewsPlugin()//int updateFreq)
    {
        //this.updateFreq = updateFreq;
    }

    public abstract void update();

    public void setFrequency(int updateFrequncy)
    {
        this.updateFreq = updateFrequncy;
    }

    public int getFreq()
    {
        return this.updateFreq;
    }

}