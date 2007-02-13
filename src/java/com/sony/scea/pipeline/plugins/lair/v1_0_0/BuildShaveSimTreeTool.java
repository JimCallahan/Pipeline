/**
 * 
 */
package com.sony.scea.pipeline.plugins.lair.v1_0_0;

import java.io.*;
import java.util.Set;

import us.temerity.pipeline.*;

/**
 * @author Jesse Clemens
 *
 */
public class BuildShaveSimTreeTool
  extends BaseTool
{
  public BuildShaveSimTreeTool()
  {
    super("BuildShaveTree", new VersionID("1.0.0"), "SCEA",
      "Builds a big old shave sim tree.");

    underDevelopment();
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    pPhase = 1;
  }
  
  public synchronized String collectPhaseInput() throws PipelineException
  {
    System.err.println("Collecting Phase " + pPhase);
    switch (pPhase)
    {
      case 1:
	return collectFirstPhaseInput();

      case 2:
	return collectSecondPhaseInput();
      default:
	throw new IllegalStateException();
    }
  }
  
  public synchronized String collectFirstPhaseInput() throws PipelineException
  {
    if ( pPrimary == null )
      throw new PipelineException("You need to have a node selected");
    if ( pSelected.size() != 1 )
      throw new PipelineException("You can only select one node.");

    if ( !pPrimary.matches(animPattern) )
      throw new PipelineException(
	"Please select an animation node before running this tool.");

    NodeStatus stat = pSelected.get(pPrimary);
    NodeID id = stat.getNodeID();
    pAuthor = id.getAuthor();
    pView = id.getView();
    
    hairCharNode = "/projects/lr/assets/character/" + hairChar + "/" + hairChar;
    String lowrezChar = hairCharNode + "_lr";
    
    NodeMod animMod = stat.getDetails().getWorkingVersion();
    Set<String> sources = animMod.getSourceNames();
    if (!sources.contains(lowrezChar))
      throw new PipelineException("The selected shot does not contain a character that needs a hair simulation");
    
    
    return null;
  }
  
  public synchronized String collectSecondPhaseInput() throws PipelineException
  {
    return null;
  }
  
  public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
  throws PipelineException
  {
    System.err.println("Execute Phase " + pPhase);
    switch (pPhase)
    {
      case 1:
	return executeFirstPhase(mclient, qclient);
      case 2:
	return executeSecondPhase(mclient, qclient);
      default:
	throw new IllegalStateException();
    }
  }
  
  private boolean executeFirstPhase(MasterMgrClient mclient, QueueMgrClient qclient)
  throws PipelineException
  {
    Path mayaScenePath = null;
    NodeID nodeID = null;
    NodeMod mod = null;
    {

      NodeStatus stat = pSelected.get(pPrimary);
      nodeID = stat.getNodeID();
      mod = stat.getDetails().getWorkingVersion();
      FileSeq fseq = mod.getPrimarySequence();
      mayaScenePath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/"
	+ fseq.getPath(0));
    }
    
    File script;
    File info;
    try
    {
      script = File.createTempFile("BuildShaveSimTreeTool-Gather.", ".mel",
	PackageInfo.sTempPath.toFile());
      FileCleaner.add(script);
    } catch ( IOException ex )
    {
      throw new PipelineException("Unable to create the temporary MEL script used "
	  + "to get the information!");
    }
    try
    {
      info = File.createTempFile("BuildShaveSimTreeTool-Info.", ".txt", PackageInfo.sTempPath
	.toFile());
      FileCleaner.add(info);
    } catch ( IOException ex )
    {
      throw new PipelineException(
	"Unable to create the temporary text file used to store the "
	    + "information collected from the Maya scene!");
    }
    
    // Writing the mel script.
    try
    {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));

      out.println("file -open -lnr \"" + fixPath(mayaScenePath.toString()) + "\"");
      out.println("{");
      out.println("$out = `fopen \"" + fixPath(info.getAbsolutePath()) + "\" \"w\"`;"); 
      out.println("      int $first = `findKeyframe -w \"first\" \"*\"`;");
      out.println("      int $end = `findKeyframe -w \"last\" \"*\"`;");
      out
	.println("      fprint($out, \"Range \" + $space + \" \" + $first + \" \" + $end + \"\\n\");");
      out.println("fclose $out;");
      out.println("}");
    } catch ( IOException e )
    {
      throw new PipelineException("Unable to write the temporary MEL script (" + script
	  + ") used to export the shaders");
    }
    
    
    
    pPhase++;
    return true;
  }
  
  private boolean executeSecondPhase(MasterMgrClient mclient, QueueMgrClient qclient)
  throws PipelineException
  {
    return false;
  }
  
  private static final String fixPath(String path)
  {
    return path.replaceAll("\\\\", "/");
  }
  
  private int pPhase;
  
  private String pAuthor;
  private String pView;
  
  
  
  private static String animPattern = ".*/production/.*/anim/.*_anim";
  private String hairChar = "kobakai2";
  private String hairCharNode = null;
  
  private static final long serialVersionUID = 3048552457639061141L;
  
  
}
