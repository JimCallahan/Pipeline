package us.temerity.pipeline.plugin.MayaRemoveReferenceTool.v2_0_9;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/**
 * Removes selected source models from the target scene as references in both Pipline and Maya
 */
public class MayaRemoveReferenceTool
  extends BaseTool
{
  public MayaRemoveReferenceTool() {
    super("MayaRemoveReference", new VersionID("2.0.9"), "Temerity",
      "Used to remove a reference from Maya and unlink it in Pipeline.");

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }

  /** 
   * Create and show graphical user interface components to collect information from the 
   * user to use as input in the next phase of execution for the tool. <P> 
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */  
  public String collectPhaseInput() throws PipelineException
  {
    int size = pSelected.keySet().size();

    if(pPrimary == null)
      throw new PipelineException("Must have a target node.");

    if(size < 2)
      throw new PipelineException("Must have at least two nodes selected");

    NodeStatus stat = pSelected.get(pPrimary);
    Set<String> allSources = stat.getSourceNames();
    for(String s : pSelected.keySet()) {
      if(s.equals(pPrimary))
	continue;
      if(!allSources.contains(s))
	throw new PipelineException("The selected node (" + s + ") is not a source node of"
	  + pPrimary);
    }

    return ":  Removing References";
  }

  /**
   * Perform one phase in the execution of the tool. <P> 
   *    
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *   Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException 
   *   If unable to sucessfully execute this phase of the tool.
   */ 
  public boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
    throws PipelineException
  {
    Set<String> nodes = new TreeSet<String>(pSelected.keySet());
    nodes.remove(pPrimary);
    NodeStatus targetStatus = pSelected.get(pPrimary);
    NodeID targetID = targetStatus.getNodeID();
    String user = targetID.getAuthor();
    String view = targetID.getView();

    NodeMod targetMod = targetStatus.getDetails().getWorkingVersion();

    File targetFile;
    {
      FileSeq fseq = targetMod.getPrimarySequence();
      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || (suffix == null)
	|| (!suffix.equals("ma") && !suffix.equals("mb")))
	throw new PipelineException("The target node (" + pPrimary
	  + ") must be a maya scene.");
      targetFile =
	new Path(PackageInfo.sProdPath, targetID.getWorkingParent() + "/" + fseq.getPath(0))
	  .toFile();
    }

    File script = null;

    PrintWriter out = null;
    try {
      script =
	File.createTempFile("RemoveReferenceTool.", ".mel", PackageInfo.sTempPath.toFile());
      FileCleaner.add(script);
      out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
    }
    catch(IOException ex) {
      throw new PipelineException(
	"Unable to create the temporary MEL script used to remove a "
	  + "reference from the Maya scene!");
    }

    NodeStatus referenceStatus;
    for(String sourceName : nodes) {
      referenceStatus = pSelected.get(sourceName);
      NodeMod referenceMod = referenceStatus.getDetails().getWorkingVersion();
      String refFileName;
      {
	FileSeq fseq = referenceMod.getPrimarySequence();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || (suffix == null)
	  || (!suffix.equals("ma") && !suffix.equals("mb")))
	  throw new PipelineException("The Selected Scene Node (" + referenceMod.getName()
	    + ") must be a Maya Scene!");
	Path p = new Path(sourceName);
	refFileName = "$WORKING" + p.getParent() + "/" + fseq.getFile(0);
      }
      out.println("file -rr \"" + refFileName + "\";");
    }

    out.println("file -save;");
    out.close();

    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-batch");
      args.add("-script");
      args.add(script.getPath());
      args.add("-file");
      args.add(targetFile.getAbsolutePath());

      TreeMap<String, String> env =
	mclient.getToolsetEnvironment(targetID.getAuthor(), targetID.getView(), targetMod
	  .getToolset(), PackageInfo.sOsType);

      Path wdir = new Path(PackageInfo.sProdPath.toOsString() + targetID.getWorkingParent());

      Map<String, String> nenv = env;
      String midefs = env.get("PIPELINE_MI_SHADER_PATH");
      if(midefs != null) {
	nenv = new TreeMap<String, String>(env);
	Path dpath = new Path(new Path(wdir, midefs));
	nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
      }

      String command = "maya";
      if(PackageInfo.sOsType.equals(OsType.Windows))
	command += ".exe";

      SubProcessLight proc =
	new SubProcessLight("RemoveReferenceTool", command, args, env, wdir.toFile());
      try {
	proc.start();
	proc.join();
	if(!proc.wasSuccessful()) {
	  throw new PipelineException(
	    "Did not correctly remove the reference due to a maya error.!\n\n"
	      + proc.getStdOut() + "\n\n" + proc.getStdErr());
	}
      }
      catch(InterruptedException ex) {
	throw new PipelineException(ex);
      }
    }
    catch(Exception ex) {
      throw new PipelineException(ex);
    }
    for(String sourceName : nodes) {
      mclient.unlink(user, view, targetMod.getName(), sourceName);
    }

    return false;
  }

  private static final long serialVersionUID = 427796884989831829L;
}
