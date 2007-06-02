package us.temerity.pipeline.builder.tests;

import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;


public class BabyBuilder
  extends BaseBuilder
{
  public BabyBuilder
  (
    String name
  ) 
    throws PipelineException
  {
    super(name, "A Baby Builder");
    {
      BuilderParam param = 
	new BooleanBuilderParam
	(aMakeChild,
	 "Do I need to have a child", 
	 false);
      addParam(param);
    }
    {
      BuilderParam param = 
	new BooleanBuilderParam
	(aMakeLaterChild,
	 "Do I need to have a child", 
	 false);
      addParam(param);
    }
    {
      BuilderParam param = 
	new BooleanBuilderParam
	(aMakeThirdChild,
	 "Do I need to have a child", 
	 false);
      addParam(param);
    }
    addSetupPass(new FirstInfoPass());
    addSetupPass(new SecondInfoPass());
    addSetupPass(new ThirdInfoPass());
    addConstuctPass(new ConstructPass("LaLaLa", "Builds this shit!"));
    {
      PassLayoutGroup layout = new PassLayoutGroup("TopLayout", "The whole layout");
      {
	LayoutGroup group = new LayoutGroup(true);
	group.addEntry(aUtilContext);
	group.addEntry(aActionOnExistance);
	group.addEntry(aReleaseOnError);
	group.addEntry(aMakeChild);
	layout.addPass("FirstPass", group);
      }
      {
	LayoutGroup group = new LayoutGroup(true);
	group.addEntry(aMakeLaterChild);
	layout.addPass("SecondPass", group);
      }
      {
	LayoutGroup group = new LayoutGroup(true);
	group.addEntry(aMakeThirdChild);
	layout.addPass("ThirdPass", group);
      }
      setLayout(layout);
    }
  }
  
  @Override
  protected TreeSet<String> getNodesToCheckIn()
  {
    return null;
  }
  
  protected class
  FirstInfoPass
    extends SetupPass
  {
    private static final long serialVersionUID = -7718660194307818767L;

    public 
    FirstInfoPass()
    {
      super("FirstInfoPass", "FirstInfoPass pass for the BabyBuilder");
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
      pMakeChild = getBooleanParamValue(new ParamMapping(aMakeChild));
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }

    @Override
    public void initPhase()
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
	"Starting the init phase in " + this.toString());
      if (pMakeChild)
	addSubBuilder(new BabyBuilder("FirstChild"));
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
	"Finished the init phase.");
    }
  }
  
  protected class
  SecondInfoPass
    extends SetupPass
  {
    private static final long serialVersionUID = 4851868407502246737L;

    public 
    SecondInfoPass()
    {
      super("SecondInfoPass", "SecondInfoPass pass for the BabyBuilder");
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
      pMakeLaterChild = getBooleanParamValue(new ParamMapping(aMakeLaterChild));
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }

    @Override
    public void initPhase()
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
	"Starting the init phase in " + this.toString());
      if (pMakeLaterChild)
	addSubBuilder(new BabyBuilder("SecondChild"));
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
	"Finished the init phase.");
    }
  }
  
  protected class
  ThirdInfoPass
    extends SetupPass
  {
    private static final long serialVersionUID = 396637392727520619L;

    public 
    ThirdInfoPass()
    {
      super("ThirdInfoPass", "ThirdInfoPass pass for the BabyBuilder");
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
      pMakeThirdChild = getBooleanParamValue(new ParamMapping(aMakeThirdChild));
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }

    @Override
    public void initPhase()
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
	"Starting the init phase in " + this.toString());
      if (pMakeThirdChild)
	addSubBuilder(new BabyBuilder("ThirdChild"));
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
	"Finished the init phase.");
    }
  }
  
  public static final String aMakeChild = "MakeChild";
  public static final String aMakeLaterChild = "MakeLaterChild";
  public static final String aMakeThirdChild = "MakeThirdChild";
  
  private boolean pMakeChild;
  private boolean pMakeLaterChild;
  private boolean pMakeThirdChild;
}
