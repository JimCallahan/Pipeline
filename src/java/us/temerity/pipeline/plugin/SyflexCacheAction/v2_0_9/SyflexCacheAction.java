// $Id: SyflexCacheAction.java,v 1.2 2008/01/20 06:42:17 jim Exp $

package us.temerity.pipeline.plugin.SyflexCacheAction.v2_0_9;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   S Y F L E X   C A C H E   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

public 
class SyflexCacheAction 
  extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public SyflexCacheAction()
  {
    super("SyflexCache", new VersionID("2.0.9"), "Temerity", 
          "Creates a syflex cache");

    {
      ActionParam param = 
        new LinkActionParam
        (aMayaScene,
         "The source Maya scene node.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new StringActionParam
        (aClothObject,
         "The name of the piece of cloth to be simulated.", 
         null);
      addSingleParam(param);
    }
  }

   
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
   * fulfill the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    Path sourceNodePath = null;
    NodeID nodeID = agenda.getNodeID();
    String cachePath;
    int cacheEnd ;
      
    {
      String name = (String) getSingleParamValue(aMayaScene);
      NodeID sNodeID = new NodeID(nodeID, name);
      FileSeq fseq = agenda.getPrimarySource(name);
      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || (suffix == null) || 
         !(suffix.equals("ma") || suffix.equals("mb"))) 
        throw new PipelineException
          ("The SyflexCacheAction Action requires that the Source Node" + 
           "must be a single Maya scene file."); 

      sourceNodePath = new Path(PackageInfo.sProdPath, 
                                sNodeID.getWorkingParent() + "/" + fseq.getPath(0));
    }
      
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      if (!fseq.hasFrameNumbers())
        throw new PipelineException("Cache files must have frame numbers");
      String suffix = fseq.getFilePattern().getSuffix();
      if(suffix != null)
        throw new PipelineException("Cache files cannot have a suffix");
      int padding = fseq.getFilePattern().getPadding();
      if (padding != 4)
        throw new PipelineException("Cache files must have a padding of 4");
      Path p = new Path(nodeID.getName());
      cachePath = ("$WORKING" + p.getParent() + "/" + 
                   fseq.getFilePattern().getPrefix() + "." );
      cacheEnd = fseq.getFrameRange().getEnd();
    }
      
    String clothName = (String) getSingleParamValue(aClothObject);
      
    File script = createTemp(agenda, 0755, "mel");
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
        
      /* load the animImportExport plugin */
      out.println("if (!`pluginInfo -q -l EnvPathConvert`)");
      out.write("     loadPlugin \"EnvPathConvert\";\n\n");

      out.println("string $childs[] = `listRelatives -shapes "+ clothName +"`;");
      out.println("string $child = $childs[0];");
      out.println("int $startFrame = `getAttr ($child + \".startFrame\")` -1;");
      out.println("currentTime $startFrame;");
      out.println("int $endFrame = " + cacheEnd + ";");
      out.println("setAttr ($child + \".active\") on;");
      out.println("string $envNode = `createNode envPathConvert`;");
      out.println("setAttr -type \"string\" ($envNode + \".envPath\") \""+ cachePath+"\";");
      out.println("string $expandedName = `getAttr ($child + \".cacheName\")`;");
      out.println("system (\"rm \" + $expandedName + \".*\");");
      out.println("connectAttr -f ($envNode + \".absPath\") ($child + \".cacheName\");");
      out.println("");
      out.println("int $frame;");
      out.println("for ($frame = $startFrame;  $frame <= $endFrame + 1; $frame++)");
      out.println("{");
      out.println("  float $sy_timer = `timerX`;");
      out.println("  currentTime -e $frame;");
      out.println("  string $shape[] = `listConnections -sh 1 ($child + \".outMesh\")`;");
      out.println("  int $place = 0;");
      out.println("  while (`nodeType $shape[$place]` != \"mesh\")");
      out.println("  {");
      out.println("    string $node = $shape[0];");
      out.println("    if (attributeExists(\"outputGeometry\", $node))");
      out.println("      $shape = `listConnections -sh 1 ($node+\".outputGeometry\")`;");
      out.println("    else");
      out.println("      $place++;");
      out.println("  }");
      out.println("  getAttr ($shape[$place]+\".boundingBoxMinX\");");
      out.println("  int $sy_roundTime = `timerX -st $sy_timer`;");
      out.println("  print (\"frame \" + $frame + \" took syflex \" + ($sy_roundTime) + " + 
                  "\" seconds.\\n\");");
      out.println("}");
        
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write temporary MEL script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
      
    /* create the process to run the action */
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-batch");
      args.add("-script");
      args.add(script.getPath());
      args.add("-file");
      args.add(sourceNodePath.toOsString());
        
      String program = "maya";
      if (PackageInfo.sOsType == OsType.Windows)
        program = (program + ".exe");
        
      /* added custom Mental Ray shader path to the environment */
      Map<String, String> env = agenda.getEnvironment();
      Map<String, String> nenv = env;
      String midefs = env.get("PIPELINE_MI_SHADER_PATH");
      if (midefs != null) {
        nenv = new TreeMap<String, String>(env);
        Path dpath = new Path(new Path(agenda.getWorkingDir()), midefs);
        nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
      }
        
      return new SubProcessHeavy
        (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
         program, args, nenv, agenda.getWorkingDir(), outFile, errFile);
    } 
    catch (Exception ex) {
      throw new PipelineException
        ("Unable to generate the SubProcess to perform this Action!\n"
         + ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5025322827447065085L;
   
  private static final String aMayaScene   = "MayaScene";
  private static final String aClothObject = "ClothObject";

}
