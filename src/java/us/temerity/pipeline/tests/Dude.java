
import us.temerity.pipeline.*;

public class Dude 
  implements Glueable 
{
  private String myName;
  private int    myAge;

  public Dude()
  {
    myName = "Nobody";
    myAge  = 0;
  }

  public Dude(String name, int age) 
  {
    myName = name;
    myAge  = age;
  }
  
  public void toGlue(GlueEncoder ge) 
    throws GlueException
  {
    ge.encode("Name", myName);
    ge.encode("Age", myAge);
  }

  public void fromGlue(GlueDecoder gd) 
    throws GlueException
  {
    myName = (String)  gd.decode("Name");
    myAge  = (Integer) gd.decode("Age");
  }
}
