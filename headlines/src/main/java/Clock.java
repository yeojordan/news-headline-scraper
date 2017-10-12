import java.text.*;
import java.util.*;

public class Clock
{
    private Window ui;
    private Thread clock;

    public Clock(Window ui)
    {
        System.out.println("Starting clock");
        this.ui = ui;
        this.clock = new Thread( () -> {
            try
            {
                while( !Thread.currentThread().isInterrupted() )
                {
                    this.ui.updateTime( getTime() );
                    Thread.sleep(150);;
                }
            }
            catch(InterruptedException e){}
        });
        this.clock.start();
    }

    public String getTime()
    {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a");
        return new String( format.format(new Date()));
    }
}