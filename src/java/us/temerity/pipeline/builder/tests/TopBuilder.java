package us.temerity.pipeline.builder.tests;

import java.util.TreeSet;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.*;


public class TopBuilder
  extends BaseBuilder
{
  public TopBuilder() 
    throws PipelineException
  {
    super("TopBuilder", "The TopLevel Test Builder");
    
    {
      BuilderParam param = 
	new IntegerBuilderParam
	(aNumberOfChildren,
	 "Number of Child Builders to create",
	 1);
      addParam(param);
    }
    addSetupPass(new InformationLoop());
  }
  
  public void
  commandLineParams() 
    throws PipelineException
  {
    assignCommandLineParams();
  }
  
  @Override
  protected TreeSet<String> getNodesToCheckIn()
  {
    return null;
  }

  protected class
  InformationLoop
    extends SetupPass
  {
    private static final long serialVersionUID = -1721476046848333118L;

    public 
    InformationLoop()
    {
      super("InformationPass", "Information pass for the TopBuilder");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
	"Starting the validate phase in " + this.toString());
      validateBuiltInParams();
      pNumberOfChildren = getIntegerParamValue(new ParamMapping(aNumberOfChildren), 1);
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }

    @Override
    public void initPhase()
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
	"Starting the init phase in " + this.toString());
      for (int i = 0; i < pNumberOfChildren ; i ++) 
	addSubBuilder(new BabyBuilder("LittleBaby" + i));
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
	"Finished the init phase in the Information Pass.");
    }
  }
  
  public static final String aNumberOfChildren = "NumberOfChildren";
  private int pNumberOfChildren;
}
