package com.sony.scea.pipeline.tools.lair;

import static com.sony.scea.pipeline.tools.Globals.getLatest;
import static com.sony.scea.pipeline.tools.Globals.getNewest;
import static com.sony.scea.pipeline.tools.GlobalsArgs.*;
import static com.sony.scea.pipeline.tools.lair.LairConstants.getAssetValues;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.BootApp;

import com.sony.scea.pipeline.tools.Wrapper;

public class PrepLairTurntable extends BootApp
{

    MasterMgrClient client;
    PluginMgrClient plug;

    public PrepLairTurntable()
    {
	try
	{
	    PluginMgrClient.init();
	    client = new MasterMgrClient();
	    plug = PluginMgrClient.getInstance();
	} catch ( PipelineException ex )
	{
	    ex.printStackTrace();
	}
    }

    @Override
    public void run(String[] args)
    {
	try
	{
	    TreeMap<String, LinkedList<String>> parsedArgs = argParser(args);
	    if ( !checkArgsAndSetParams(parsedArgs) )
	    {
		return;
	    }
	} catch ( PipelineException ex )
	{
	    System.err.println("There was a problem reading the arguments.\n"
		    + ex.getMessage());
	    printHelp();
	    return;
	}

	for (String turntableName : turntables)
	{
	    logLine("Doing turntable: " + turntableName);
	    for (String passName : passes)
	    {
		logLine("\tDoing pass: " + passName);
		for (LairAsset as : assets)
		{
		    Wrapper w;
		    try
		    {
			logLine("\t\tDoing asset: " + as.assetName);
			LairTurntable tt = new LairTurntable(as, turntableName, passName);
			w = new Wrapper(user, view, toolset, client);

			logLine("\t\tFreezing all the textures");
			getLatest(w, as.texGroup, over, froz);

			logLine("\t\tChecking out the final model scene");
			getNewest(w, as.finalScene, keep, pFroz);
			getNewest(w, as.lr_finalScene, keep, pFroz);

			logLine("\t\tChecking out the shader scene.");
			getNewest(w, as.shdScene, keep, pFroz);

			logLine("\t\tChecking out the turntable scene.");
			getNewest(w, tt.turntableScene, keep, pFroz);

			logLine("\t\tChecking out the overRide scene.");
			getNewest(w, tt.ttCamOverMI, keep, pFroz);

			logLine("\t\tChecking out the options scene.");
			getNewest(w, tt.ttOptionsMI, keep, pFroz);

			logLine("\t\tChecking out the camera MI node.");
			getNewest(w, tt.ttCamMI, keep, pFroz);

			logLine("\t\tChecking out the light MI node.");
			getNewest(w, tt.ttLightMI, keep, pFroz);

			logLine("\t\tChecking out the shade MI node.");
			getNewest(w, tt.ttShadeMI, keep, pFroz);

			logLine("\t\tChecking out the geo MI node.");
			getNewest(w, tt.ttGeoMI, keep, pFroz);

			logLine("\t\tChecking out the images node.");
			getNewest(w, tt.ttImages, keep, pFroz);

			logLine("\t\tFixing the turntable node");
			{
			    NodeMod mod = client.getWorkingVersion(user, view,
				tt.turntableScene);
			    if ( toolset != null )
				mod.setToolset(toolset);
			    client.modifyProperties(user, view, mod);
			}

			logLine("\t\tFixing the camera override node");
			{
			    NodeMod mod = client.getWorkingVersion(user, view,
				tt.ttCamOverMI);
			    if ( toolset != null )
				mod.setToolset(toolset);
			    BaseAction act = mod.getAction();
			    act.setSingleParamValue("ImageWidth", 1280);
			    act.setSingleParamValue("ImageHeight", 720);
			    act.setSingleParamValue("AspectRatio", 1.777);
			    act.setSingleParamValue("Aperture", 1.41732);
			    act.setSingleParamValue("OverrideFocal", false);
			    act.setSingleParamValue("OverrideClipping", false);
			    mod.setAction(act);
			    client.modifyProperties(user, view, mod);
			}

			logLine("\t\tFixing the camera node");
			{
			    NodeMod mod = client.getWorkingVersion(user, view, tt.ttCamMI);
			    if ( toolset != null )
				mod.setToolset(toolset);
			    client.modifyProperties(user, view, mod);
			}

			logLine("\t\tFixing the light node");
			{
			    NodeMod mod = client
				.getWorkingVersion(user, view, tt.ttLightMI);
			    if ( toolset != null )
				mod.setToolset(toolset);
			    client.modifyProperties(user, view, mod);
			}

			logLine("\t\tFixing the geo node");
			{
			    NodeMod mod = client.getWorkingVersion(user, view, tt.ttGeoMI);
			    if ( toolset != null )
				mod.setToolset(toolset);
			    BaseAction act = mod.getAction();
			    act.setSingleParamValue("CustomText", true);
			    mod.setAction(act);
			    client.modifyProperties(user, view, mod);
			}

			logLine("\t\tFixing the shader node");
			{
			    NodeMod mod = client
				.getWorkingVersion(user, view, tt.ttShadeMI);
			    if ( toolset != null )
				mod.setToolset(toolset);
			    BaseAction act = mod.getAction();
			    BaseAction act2 = LairConstants.actionMayaMiShader();
			    act2.setSingleParamValues(act);
			    mod.setAction(act2);
			    JobReqs req = mod.getJobRequirements();
			    req.addSelectionKey("MentalRay");
			    mod.setJobRequirements(req);
			    mod.setExecutionMethod(ExecutionMethod.Parallel);
			    mod.setBatchSize(100);
			    client.modifyProperties(user, view, mod);
			}

			logLine("\t\tFixing the images node");
			{
			    NodeMod mod = client.getWorkingVersion(user, view, tt.ttImages);
			    if ( toolset != null )
				mod.setToolset(toolset);
			    BaseAction act = mod.getAction();
			    act.setSingleParamValue("TexturePath", "$WORKING");
			    mod.setAction(act);
			    JobReqs req = mod.getJobRequirements();
			    req.addSelectionKey("MentalRay");
			    mod.setJobRequirements(req);
			    mod.setExecutionMethod(ExecutionMethod.Parallel);
			    mod.setBatchSize(5);
			    client.modifyProperties(user, view, mod);
			}

			logLine("All Done.  Remember to set your export set on the mod node "
				+ "if you want to export a custom part of the body");
		    } catch ( PipelineException e )
		    {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    }
	}
    }

    private boolean checkArgsAndSetParams(TreeMap<String, LinkedList<String>> parsedArgs)
    {
	if ( parsedArgs.containsKey("help") )
	{
	    printHelp();
	    return false;
	} else if ( parsedArgs.containsKey("version") )
	{
	    printVersion();
	    return false;
	}

	try
	{
	    user = getStringValue("user", true, parsedArgs);
	    view = getStringValue("view", true, parsedArgs);
	    toolset = getStringValue("toolset", false, parsedArgs);
	    turntables = getStringValues("tt", true, parsedArgs);
	    passes = getStringValues("pass", true, parsedArgs);
	    assets = getAssetValues("asset", true, parsedArgs);
	    {
		Boolean temp = getBooleanValue("verbose", false, parsedArgs);
		if ( temp == null )
		    verbose = false;
		else
		    verbose = temp;
	    }

	} catch ( PipelineException ex )
	{
	    System.err.println(ex.getMessage());
	    printHelp();
	    return false;
	}

	if ( parsedArgs.size() > 0 )
	{
	    printExtraArgs(parsedArgs);
	    return false;
	}
	return true;
    }

    private static void printHelp()
    {
	System.err.println("Use:  ./buildLairTurntable <flags>");
	System.err.println("Flag List:");
	System.err.println("\t--help");
	System.err.println("\t\tPrint this message.");
	System.err.println("\t--verbose=<boolean>");
	System.err.println("\t\tprint out lots of info.");
	System.err.println("\t\tIf you do not use this flag, it will default to false.");
	System.err.println("\t--user=<value>");
	System.err.println("\t\tThe user whose working area "
		+ "you want to prep the tree in.");
	System.err.println("\t--view=<value>");
	System.err.println("\t\tThe working area you want to prep the tree in.");
	System.err.println("\t--toolset=<value>");
	System.err.println("\t\tOptional flag.  Setting this will cause "
		+ "Pipeline to set all preped nodes to the specified toolset.");
	System.err.println("\t\tThis provides an easy way to upgrade the toolsets"
		+ "on lots of nodes at once.");
	System.err.println("\t--pass=<value>");
	System.err.println("\t\tWhich render passes should be preped.");
	System.err.println("\t\t\tUse a separate flag for each pass.");
	System.err.println("\t--tt=<value>");
	System.err.println("\t\tWhich turntable setups should be preped.");
	System.err.println("\t\t\tUse a separate flag for each setup.");
	System.err.println("\t--asset=<value,value>");
	System.err.println("\t\tWhich assets should be preped, "
		+ "followed by the type of the asset..");
	System.err.println("\t\t\tUse a separate flag for each asset.");
	System.err.println("\t\tValid asset types:");
	System.err.println("\t\t\tcharacter");
	System.err.println("\t\t\tprop");
	System.err.println("\t\t\tset");
    }

    private void log(String s)
    {
	if ( verbose )
	    System.err.print(s);
    }

    private void logLine(String s)
    {
	if ( verbose )
	    System.err.println(s);
    }

    private static void printVersion()
    {
	System.err.println("Lair Turntable Prep Tool version 1.0");
    }

    private boolean verbose;
    private String user;
    private String view;
    private String toolset;
    private ArrayList<String> passes;
    private ArrayList<String> turntables;
    private ArrayList<LairAsset> assets;

    public static final LinkPolicy REF = LinkPolicy.Reference;
    public static final LinkRelationship LINKALL = LinkRelationship.All;
    public static final LinkRelationship LINKONE = LinkRelationship.OneToOne;
    public static final LinkPolicy DEP = LinkPolicy.Dependency;
    public static final CheckOutMode over = CheckOutMode.OverwriteAll;
    public static final CheckOutMode keep = CheckOutMode.KeepModified;
    public static final CheckOutMethod modi = CheckOutMethod.Modifiable;
    public static final CheckOutMethod froz = CheckOutMethod.AllFrozen;
    public static final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;
    public static final CheckOutMethod pFroz = CheckOutMethod.PreserveFrozen;
}
