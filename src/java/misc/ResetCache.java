import us.temerity.pipeline.*;

public class
ResetCache
{
  public static void 
  main(String args[])
    throws PipelineException
  {
    MasterMgrClient client = new MasterMgrClient();
    MasterControls controls = 
      new MasterControls(209715200L, null, null, null, 
                         5000L, 5000L, 5000L, 5000L, 
                         null, null, null, null); 
    client.setRuntimeControls(controls); 
  }
}
