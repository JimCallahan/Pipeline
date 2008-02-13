package com.sony.scea.pipeline.plugins.v1_0_0;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;

/**
 * Util tool to allow a hard-coded set of values to be applied to many nodes at once. <p>
 * This tool used to change render pass images nodes that were created with older versions
 * of the {@link BuildRenderTreeTool} to the newer values.  It will convert actions to
 * MayaMRayRender, and copy the value from PreRenderMEL to PreExportMEL.  It will also
 * set the job requirements to good values and make sure the Selection Keys are correct
 * for Lair renders.<P>
 * There is no error checking at all in the script.  So be careful.  If you run it, it will effect 
 * every node selected, no matter what it is named.
 * 
 * @author Jesse Clemens
 */
public class FixRenderNodesTool extends BaseTool
{
  public FixRenderNodesTool()
  {
    super("FixRenderNodes", new VersionID("1.0.0"), "SCEA",
      "Assigns a bunch of values to render nodes.");

    underDevelopment();
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  public synchronized String 
  collectPhaseInput() 
  throws PipelineException
  {
    return "So we beat on, boats against the current, borne back ceaselessly into the past.";
  }

  public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
  throws PipelineException
  {
    String toolset = mclient.getDefaultToolsetName();
    for (String nodeName : pSelected.keySet())
    {
      NodeStatus status = pSelected.get(nodeName);
      NodeID id = status.getNodeID();
      NodeMod mod = status.getDetails().getWorkingVersion();
      BaseAction act = mod.getAction();
      String mel = null;
      String mayaScene = (String) act.getSingleParamValue("MayaScene");
      String actName =act.getName(); 
      System.out.println(actName);
      if (actName.equals("MayaRender"))
	mel = (String) act.getSingleParamValue("PreRenderMEL");
      else if (actName.equals("MayaMRayRender"))
	  mel = (String) act.getSingleParamValue("PreExportMEL");
      System.out.println(mel);
      int batchSize = mod.getBatchSize();
      
      BaseAction newAct = PluginMgrClient.getInstance().newAction("MayaMRayRender", null, null);
      newAct.setSingleParamValue("MayaScene", mayaScene);
      newAct.setSingleParamValue("PreExportMEL", mel);
      newAct.setSingleParamValue("KeepTempFiles", false);
      mod.setAction(newAct);
      mod.setExecutionMethod(ExecutionMethod.Parallel);
      mod.setBatchSize(batchSize);
      TreeSet<String> select = new TreeSet<String>();
      TreeSet<String> lic = new TreeSet<String>();
      select.add("Layers");
      //select.add("Lighting");
      select.add("LinuxOnly");
      select.add("Lair");
      select.add("MentalRay");
      lic.add("MentalRay");
      JobReqs reqs = new JobReqs(50, 15, 4.5f, 1024, 1024, lic, select, null);
      reqs.setMinMemory(3221225472l);
      reqs.setMinDisk(536870912l);
      mod.setJobRequirements(reqs);
      mod.setToolset(toolset);
      mclient.modifyProperties(id.getAuthor(), id.getView(), mod);
    }
    return false;
  }
  private static final long serialVersionUID = -226439410721567277L;
}
