package controller;

import view.*;
import java.text.*;
import java.util.*;

public class Clock
{   
    // Reference to the user interface
    private Window ui;
    // Thread for the clock to run
    private Thread clock;

    /**
     * Default constructor for Clock, that starts the clock thread
     */
    public Clock(Window ui)
    {
        this.ui = ui;
        this.clock = new Thread( () -> {
            try
            {
                while( !Thread.currentThread().isInterrupted() )
                {   
                    // Send updated time to user interface
                    this.ui.updateTime( getTime() );
                    Thread.sleep(150);
                }
            }
            catch(InterruptedException e){}
        });
        this.clock.start();
    }
    
    /**
     * Obtain the current time
     */
    public String getTime()
    {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a");
        return new String( format.format(new Date()));
    }
}