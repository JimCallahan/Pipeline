package us.temerity.pipeline.plugin.SyflexApplyCacheAction.v2_0_9;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

public class SyflexApplyCacheAction extends BaseAction
{
   public SyflexApplyCacheAction()
   {
      super("SyflexApplyCache", new VersionID("2.0.9"), "Temerity", 
		  "Applies one or more syflex caches to a scene.");

      {
		  ActionParam param = new LinkActionParam(
		     aMayaScene,
		     "The source Maya scene node.", 
		     null);
		  addSingleParam(param);
      }
      
      {
		  ActionParam param = 
		     new LinkActionParam
		     (aInitialMEL,
		        "The MEL script to evaluate after scene creation and before importing models.",
		        null);
		  addSingleParam(param);
      }

      {
		  ActionParam param = 
		     new LinkActionParam
		     (aCacheMEL,
		        "The MEL script to evaluate after loading the caches.", 
		        null);
		  addSingleParam(param);
      }
      
      {
		  ActionParam param = 
		     new LinkActionParam
		     (aFinalMEL,
		        "The MEL script to evaluate after saving the scene.", 
		        null);
		  addSingleParam(param);
      }

      {
		  LayoutGroup layout = new LayoutGroup(true);
		  layout.addEntry(aMayaScene);
		  layout.addSeparator();
		  layout.addEntry(aInitialMEL);
		  layout.addEntry(aCacheMEL);
		  layout.addEntry(aFinalMEL);

		  setSingleLayout(layout);
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
      Path initialMel = null;
      Path finalMel = null;
      boolean isAscii;
      Path finalScene;
      
      {
		  /* MEL script filenames */
		  initialMel = getMelPath(aInitialMEL, "Initial MEL", agenda);
		  finalMel = getMelPath(aFinalMEL, "Final MEL", agenda);
      }
      
      {
		  String name = (String) getSingleParamValue(aMayaScene);
		  NodeID sNodeID = new NodeID(nodeID, name);
		  FileSeq fseq = agenda.getPrimarySource(name); 
		  if (fseq == null)
		     throw new PipelineException("The value of the Maya Scene paramter cannot be null");
		  String suffix = fseq.getFilePattern().getSuffix();
		  if(!fseq.isSingle() || (suffix == null) || 
		        !(suffix.equals("ma") || suffix.equals("mb"))) 
		     throw new PipelineException
		     		 ("The SyflexCacheAction Action requires that the Maya Scene" + 
		 		    "must be a single Maya scene file."); 

		  sourceNodePath = new Path(PackageInfo.sProdPath, 
		        sNodeID.getWorkingParent() + "/" + fseq.getPath(0));
      }
      
      TreeSet<String> cacheSources = new TreeSet<String>();
      for (String sourceName : agenda.getSourceNames())
      {
		  ActionInfo info = agenda.getSourceActionInfo(sourceName);
		  if (info != null)
		  {
            		  String name = info.getName();
            		  if (name.equals("SyflexCache") || name.equals("SyflexCacheCopy"))
            		  {
            		     cacheSources.add(sourceName);
            		  }
		  }
      }
      
      /* the generated Maya scene filename */ 
      {
		 FileSeq fseq = agenda.getPrimaryTarget();
		 String suffix = fseq.getFilePattern().getSuffix();
		 if(!fseq.isSingle() || 
		    (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
		   throw new PipelineException
		     ("The MayaShaderCopy Action requires that the primary target file sequence must " + 
		      "be a single Maya scene file."); 
		 
		 isAscii = suffix.equals("ma");
		 finalScene = new Path(PackageInfo.sProdPath,
		 		 		      agenda.getNodeID().getWorkingParent() + "/" + fseq.getFile(0));
		 
      }
      File script = createTemp(agenda, 0755, "mel");
      try {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
        
        out.println("if (!`pluginInfo -q -l EnvPathConvert`)");
        out.write("     loadPlugin \"EnvPathConvert\";\n\n");
        
        /* rename the current scene as the output scene */ 
        out.write("// SCENE SETUP\n" + 
  		 		 "file -rename \"" + finalScene + "\";\n" + 
  		 		 "file -type \"" + (isAscii ? "mayaAscii" : "mayaBinary") + "\";\n\n");

        
        /* the initial MEL script */ 
        if(initialMel != null) {
  		 out.write("// INTITIAL MEL\n" + 
  		 		   "source \"" + initialMel + "\";\n\n");
        }
        
        for (String source : cacheSources)
        {
           //NodeID sNodeId = new NodeID(nodeID, source);
           FileSeq fseq = agenda.getPrimarySource(source);
           FrameRange range = fseq.getFrameRange();
           int startFrame = range.getStart();
           //int endFrame = range.getEnd();
           Path p = new Path(source);
           String cachePath = ("$WORKING" + p.getParent() + "/" + fseq.getFilePattern().getPrefix() + "." );
           ActionInfo info = agenda.getSourceActionInfo(source);
           String clothName = (String) info.getSingleParamValue("ClothObject");
           out.println("{");
           out.println("string $childs[] = `listRelatives -shapes "+ clothName +"`;");
           out.println("string $child = $childs[0];");
           out.println("setAttr ($child + \".startFrame\") "+(startFrame-1)+";");
           out.println("string $envNode = `createNode envPathConvert`;");
           out.println("setAttr -type \"string\" ($envNode + \".envPath\") \""+ cachePath+"\";");
           out.println("connectAttr -f ($envNode + \".absPath\") ($child + \".cacheName\");");
           out.println("catch(`setAttr ($child + \".active\") on`);");
           out.println("}");   
        }

        /* save the file */ 
        out.write("// SAVE\n" + 
                  "print \"Saving Scene: " + finalScene+ "\\n\";\n" + 
		           "file -save;\n");

        
        /* the final MEL script */ 
		 if(finalMel != null) {
		    out.write("// FINAL MEL\n" + 
		              "source \"" + finalMel + "\";\n\n");
		 }
        
        out.close();
        
      } 
      catch (IOException ex) {
        throw new PipelineException
  		 ("Unable to write temporary MEL script file (" + script + ") for Job " + 
  		  "(" + agenda.getJobID() + ")!\n"
  		  + ex.getMessage());
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
   
   /**
    * Get the abstract path to the MEL file specified by the given parameter.
    * 
    * @param pname
    *   The name of the single valued MEL parameter.
    * 
    * @param title
    *   The title of the parameter in exception messages.
    * 
    * @param agenda
    *   The agenda to be accomplished by the action.
    * 
    * @return 
    *   The MEL file or <CODE>null</CODE> if none was specified.
    */ 
   private Path
   getMelPath
   (
    String pname, 
    String title, 
    ActionAgenda agenda
   ) 
     throws PipelineException 
   {
     Path script = null; 
     String mname = (String) getSingleParamValue(pname); 
     if(mname != null) {
       FileSeq fseq = agenda.getPrimarySource(mname);
       if(fseq == null) 
 		 throw new PipelineException
 		   ("Somehow the " + title + " node (" + mname + ") was not one of the " + 
 		    "source nodes!");
       
       String suffix = fseq.getFilePattern().getSuffix();
       if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mel")) 
 		 throw new PipelineException
 		   ("The SyflexApplyCache Action requires that the source node specified by the " + 
 		    title + " parameter (" + mname + ") must have a single MEL file as its " + 
 		    "primary file sequence!");
       
       NodeID mnodeID = new NodeID(agenda.getNodeID(), mname);
       script = new Path(PackageInfo.sProdPath, 
 		 		 		 mnodeID.getWorkingParent() + "/" + fseq.getFile(0)); 
     }

     return script;		       
   }
   
   
   private static final long serialVersionUID = 2780775910959525379L;
   private static final String aMayaScene = "MayaScene";
   private static final String aInitialMEL = "InitialMEL";
   private static final String aCacheMEL = "CacheMEL";
   private static final String aFinalMEL = "FinalMEL";
}
