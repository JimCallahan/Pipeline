// $Id: MRayInstGroupAction.java,v 1.1 2006/09/05 02:58:20 jim Exp $

package us.temerity.pipeline.plugin.v2_0_10;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/**
 * Builds Instgroups from attached MI files. <P>
 * 
 * This action pulls together multiple MI files, parses them for pertinent
 * information, and builds MI files that contain instgroups filled with all the
 * instances from all the attached scenes as well as optional include statements
 * and render statements.
 * 
 * This can be used to generate basic MIs for rendering (without any of the
 * overhead that using MRayRender can entail) or to just generate instance
 * groups that can be passed into MRayRender.
 * 
 * Files made with this Action that are passed into MRayRender need to be
 * generated with the include flag set to YES, otherwise none of the files will
 * actually be passed into final render. <P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;"> 
 *   Generate Render <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     This will cause the Action to search the files for all cameras and instgroups and 
 *     construct render statements for all that it finds. There is no finer control over 
 *     how these files are parsed. If finer control is needed then the MRayRender Action 
 *     needs to be used. 
 *   </DIV>
 * <BR>
 * 
 *   Generate Includes <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     This will create an include statement for each file that is scanned for instances. 
 *     This means that only the instGroup files need to be connected to the MRayRender 
 *     Action, rather than the all of its source files needing to be connected as well. 
 *     Since MRayRender needs to parse all of its files, this prevents extra work from 
 *     being done each time MRayRender runs. 
 *   </DIV> <BR>
 * </DIV>
 * <P>
 */
public class 
MRayInstGroupAction 
  extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  MRayInstGroupAction()
  {
    super("MRayInstGroup", new VersionID("2.0.10"), "Temerity",
	  "Builds an inst group from attached mi files");

    {
      ActionParam param = 
	new BooleanActionParam
	(renderStateParam,
	 "Do you want to build Render statements as well as an instgroup", 
	 false);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	(includeParam,
	 "Do you want to add include statements to the file", 
	 false);
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
    NodeID nodeID = agenda.getNodeID();

    /* sanity checks */
    FileSeq target = null;
    Path scene = null;
    {
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if ((suffix == null) || !suffix.equals("mi"))
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") must contain one or more Mental " + 
	     "Ray Input (.mi) files!");
	target = fseq;
      }
    }

    ArrayList<Path> alls = new ArrayList<Path>();
    TreeMap<FileSeq, ArrayList<Path>> onetoone = new TreeMap<FileSeq, ArrayList<Path>>();
    {
      for (String source : agenda.getSourceNames()) {
	FileSeq sourceSeq = agenda.getPrimarySource(source);
	String suffix = sourceSeq.getFilePattern().getSuffix();
	if ((suffix == null) || !suffix.equals("mi"))
	  throw new PipelineException
	    ("The file sequence (" + sourceSeq + ") associated with source node " + 
	     "(" + source + ") " + "must contain MI files!");
	
	Path spath = new Path(source);
	Path sourceParent = new Path(new Path("$WORKING"), spath.getParent());
	if (sourceSeq.isSingle()) {
	  /*
	   * Path sourcePath = new Path(PackageInfo.sProdPath, snodeID
	   * .getWorkingParent() + "/" + sourceSeq.getPath(0));
	   */
	  Path sourcePath = new Path(sourceParent, sourceSeq.getPath(0));
	  
	  alls.add(sourcePath);
	} 
	else {
	  ArrayList<Path> temp = new ArrayList<Path>();
	  for (Path each : sourceSeq.getPaths()) {
	    /*
	     * Path sourcePath = new Path(PackageInfo.sProdPath, snodeID
	     * .getWorkingParent() + "/" + each);
	     */
	    Path sourcePath = new Path(sourceParent, each);
	    temp.add(sourcePath);
	    ;
	  }
	  onetoone.put(sourceSeq, temp);
	}
      }
    }

    File script = createTemp(agenda, 0755, "bash");

    boolean render = (Boolean) getSingleParamValue(renderStateParam);
    boolean include = (Boolean) getSingleParamValue(includeParam);

    try {
      PrintWriter out = new PrintWriter(new FileWriter(script));
      
      ArrayList<Path> targetList = target.getPaths();
      
      //String prefix = target.getFilePattern().getPrefix();
      String instGroupName = nodeID.getName();
      instGroupName = instGroupName.replaceAll("/", "_").substring(1);
      
      out.println("#!/bin/bash");
      
      for (int i = 0; i < targetList.size(); i++) {
	File optionFile = createTemp(agenda, 0666, "txt");
	File cameraFile = createTemp(agenda, 0666, "txt");
	
	Path targetPath = targetList.get(i);
	Path finalPath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent()
				  + "/" + targetPath);
	
	out.println("# Making Target " + finalPath.toOsString());
	out.println("");
	
	out.println("rm -f " + finalPath.toOsString());
	
	if (include) {
	  for (Path allPath : alls) {
	    out.println("echo \"\\$include \\\"\\" + allPath.toOsString()
			+ "\\\"\" >> " + finalPath.toOsString());
	  }
	  for (ArrayList<Path> pathList : onetoone.values()) {
	    Path oneToOnePath = pathList.get(i);
	    out.println("echo \"\\$include \\\"\\" + oneToOnePath.toOsString()
			+ "\\\"\" >> " + finalPath.toOsString());
	  }
	}

	out.println("echo \"instgroup \\\"" + instGroupName + "\\\"\" >> "
		    + finalPath.toOsString());

	out.println("");
	out.println("# Doing the individual mi's");
	out.println("");
	for (Path allPath : alls) {
	  out.println("# Doing " + allPath.toOsString());
	  out.println("cat " + allPath.toOsString()
		      + " | grep \"^instance\" | awk \'{print $2}\' >> "
		      + finalPath.toOsString());
	  
	  // This block only runs if we're building render statements.
	  /*
	   * Basically, what happens in here is
	   */
	  if (render) {
	    out.println("for camera in `cat " + allPath.toOsString()
			+ " | grep \"^camera\" | awk \'{print $2}\'`");
	    out.println("do");
	    out.println("    cat " + allPath.toOsString()
			+ " | grep \"^instance.*${camera}\" | awk \'{print $2}\' >> "
			+ cameraFile.getPath());
	    out.println("done");
	    
	    out.println("cat " + allPath.toOsString()
			+ " | grep \"^options\" | awk \'{print $2}\' >> "
			+ optionFile.getPath());

	  } // End of render statement block
	  out.println("");
	}

	out.println("");
	out.println("# Doing the per-frame mi's");
	out.println("");
	
	for (ArrayList<Path> pathList : onetoone.values()) {
	  Path oneToOnePath = pathList.get(i);
	  out.println("# Doing " + oneToOnePath.toOsString());
	  out.println("cat " + oneToOnePath.toOsString()
		      + " | grep \"^instance\" | awk \'{print $2}\' >> "
		      + finalPath.toOsString());
	  
	  // This block only runs if we're building render statements.
	  /*
	   * See above comment for explanation
	   */
	  if (render) {
	    out.println("for camera in `cat " + oneToOnePath.toOsString()
			+ " | grep \"^camera\" | awk \'{print $2}\'`");
	    out.println("do");
	    out.println("echo $camera");
	    out.println("    cat " + oneToOnePath.toOsString()
			+ " | grep \"^instance.*${camera}\" | awk \'{print $2}\' >> "
			+ cameraFile.getPath());
	    out.println("done");
	    
	    out.println("cat " + oneToOnePath.toOsString()
			+ " | grep \"^options\" | awk \'{print $2}\' >> "
			+ optionFile.getPath());
	    
	  } // End of render statement block
	  out.println("");
	}

	out.println("echo \"end instgroup\" >> " + finalPath.toOsString());
	
	if (render) {
	  out.println("echo \"\" >> " + finalPath.toOsString());
	  out.println("for camList in `cat " + cameraFile.getPath() + "`");
	  out.println("do");
	  out.println("    for optList in `cat " + optionFile.getPath() + "`");
	  out.println("    do");
	  out.println("        echo \"render \\\"" + instGroupName
		      + "\\\" $camList $optList\" >> " + finalPath.toOsString());
	  out.println("    done");
	  out.println("done");
	}
	out.println("");
	out.println("");
	out.println("");
      }
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary bash file (" + scene
	 + ") for Job " + "(" + agenda.getJobID() + ")!\n" + ex.getMessage());
    }
    
    /* create the process to run the action */
    try {
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 script.getPath(), new ArrayList<String>(), 
	 agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
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

  private final String renderStateParam = "GenerateRender";
  private final String includeParam = "GenerateIncludes";

  private static final long serialVersionUID = -1743719262350206597L;

}
