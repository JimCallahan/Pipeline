// $Id: DLCatRIBAction.java,v 1.3 2007/05/04 19:18:34 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D L   C A T   R I B   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * The 3Delight tool for concatenating and converting RIB files. <P> 
 * 
 * All of the RIB file (.rib) dependencies of the target image which set the Order per-source 
 * sequence parameter will be processed to produce the target RIB file(s). <P> 
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for
 * <A href="http://www.3delight.com/ZDoc/3delight_10.html"><B>renderdl</B></A>(1) for 
 * details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   OutputFormat <BR>
 *   <DIV style="margin-left: 40px;">
 *     The format of the output RIB file: <BR>
 *     <UL>
 *       <LI>ASCII - Generates plain-text RIB.
 *       <LI>Binary - Generates binary RIB. 
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     The compression method to use for the output RIB file:<BR>
 *     <UL>
 *       <LI>None - Uncompressed. 
 *       <LI>GZip - Use gzip(1) to compress the output RIB.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Evaluate Procedurals <BR> 
 *   <DIV style="margin-left: 40px;">
 *     Whether to evaluate all procedurals encountered in the input RIBs.
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node sequence which sets this parameter should contain RIB files. This 
 *     parameter determines the order in which the input RIB files are processed. If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 * 
 * By default, the "python" program is used by this action to run the "renderdl" commands.  
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.
 */
public
class DLCatRIBAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DLCatRIBAction() 
  {
    super("DLCatRIB", new VersionID("2.2.1"), "Temerity", 
	  "Concatenates and converts RIB files.");
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aEvaluateProcedurals, 
	 "Whether to evaluate all procedurals encountered in the input RIBs.",
	 false);
      addSingleParam(param);
    } 

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("ASCII");
      choices.add("Binary");

      ActionParam param = 
	new EnumActionParam
	(aOutputFormat, 
	 "The format of the output RIB file.",
	 "ASCII", choices);
      addSingleParam(param);
    } 

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("None");
      choices.add("Gzip");

      ActionParam param = 
	new EnumActionParam
	(aCompression, 
	 "The compression method to use for the output RIB file.",
	 "None", choices);
      addSingleParam(param);
    } 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aEvaluateProcedurals);
      layout.addSeparator();
      layout.addEntry(aOutputFormat);
      layout.addEntry(aCompression);   

      setSingleLayout(layout);   
    }

    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  
   */ 
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();
    
    {
      ActionParam param = 
	new IntegerActionParam
	("Order", 
	 "The order in which the input RIB files are processed.", 
	 100);
      params.put(param.getName(), param);
    }

    return params;
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
    /* get the ordered source file paths */ 
    MappedArrayList<Integer,Path> sourcePaths = new MappedArrayList<Integer,Path>();
    { 
      NodeID nodeID = agenda.getNodeID();
      int numFrames = agenda.getPrimaryTarget().numFrames();
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, aOrder);
	  addSourcePaths(nodeID, numFrames, sname, fseq, order, sourcePaths);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, aOrder);
	    addSourcePaths(nodeID, numFrames, sname, fseq, order, sourcePaths);
	  }
	}
      }

      if(sourcePaths.isEmpty()) 
	throw new PipelineException
	  ("No source RIB files where specified using the per-source Order parameter!");
    }

    /* target RIB files */ 
    ArrayList<Path> targetPaths = getPrimaryTargetPaths(agenda, "rib", "RIB file"); 
    
    /* common command-line arguments */ 
    ArrayList<String> args = new ArrayList<String>();
    {
      if(getSingleBooleanParamValue(aEvaluateProcedurals)) 
        args.add("-callprocedurals");

      switch(getSingleEnumParamIndex(aOutputFormat)) {
      case 0:
        break;
        
      case 1:
        args.add("-binary");
        break;
        
      default:
        throw new PipelineException
          ("Illegal OutputFormat value!");
      }

      switch(getSingleEnumParamIndex(aCompression)) {
      case 0:
        break;
        
      case 1:
        args.add("-gzip");
        break;
	
      default:
        throw new PipelineException
          ("Illegal Compression value!");
      }
    }

    /* create a temporary Python script to process the RIB files */ 
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);

      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 

      /* RIB processing program */ 
      String program = "renderdl"; 
      if(PackageInfo.sOsType == OsType.Windows) 
        program = "renderdl.exe";

      /* process the RIB files */ 
      int wk=0;
      for(Path tpath : targetPaths) {

        out.write("launch('" + program + "', " + 
                  "['-noinit', '-catrib', '-o', '" + pathToStr(tpath) + "'"); 

        for(String arg : args) 
          out.write(", '" + arg + "'"); 
        
        for(ArrayList<Path> spaths : sourcePaths.values()) {
          if(spaths.size() == 1)
            out.write(", '" + pathToStr(spaths.get(0)) + "'"); 
          else 
            out.write(", '" + pathToStr(spaths.get(wk)) + "'"); 
        }

        out.write("])\n"); 

        wk++;
      }
      
      out.write("print 'ALL DONE.'\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary Python script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */     
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a properly escaped OS specific Python string for the given path.
   */ 
  private String
  pathToStr
  (
   Path path 
  ) 
  {
    if(PackageInfo.sOsType == OsType.Windows) 
      return path.toOsString().replaceAll("\\\\", "\\\\\\\\");
    return path.toOsString(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * A helper method for generating source filenames.
   */ 
  private void 
  addSourcePaths
  (
   NodeID nodeID, 
   int numFrames, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   MappedArrayList<Integer,Path> sourcePaths
  )
    throws PipelineException 
  {
    String suffix = fseq.getFilePattern().getSuffix();
    if((suffix == null) || !suffix.equals("rib"))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
	 "must have contain RIB files!");

    if((fseq.numFrames() != 1) && (fseq.numFrames() != numFrames))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
	 "must have the contain the same number of RIB files as the target sequence or " + 
	 "exactly one RIB file.");
    
    NodeID snodeID = new NodeID(nodeID, sname);
    for(Path path : fseq.getPaths()) 
      sourcePaths.put(order, getWorkingNodeFilePath(snodeID, path));
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8605598715888050579L;

  public static final String aEvaluateProcedurals = "EvaluateProcedurals";
  public static final String aOutputFormat        = "OutputFormat";
  public static final String aCompression         = "Compression";
  public static final String aOrder               = "Order";

}

