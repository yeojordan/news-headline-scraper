import java.lang.reflect.Constructor;
import java.nio.file.*;
/**
* Loads a NewsPlugin subclass by filename (irrespective of the CLASSPATH).
*/

public class PluginLoader extends ClassLoader
{
    public NewsPlugin loadPlugin(String fname) throws ClassNotFoundException
    {
        try
        {
            System.out.println(fname);
            byte[] classData = Files.readAllBytes(Paths.get(fname));
            Class<?> cls = defineClass(null, classData, 0, classData.length);
            // Constructor<?> constr = cls.getConstructor(Integer.class);
            // return (NewsPlugin)constr.newInstance(30);
            return (NewsPlugin)cls.newInstance();
        }
        catch(Exception e)
        {
            // Slightly naughty, but there's a huge range of potential exceptions.
            throw new ClassNotFoundException(
            String.format("Could not load '%s': %s", fname, e.getMessage()),e);
        }
        
    }
}