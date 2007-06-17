// $Id: ListSourcesAction.java,v 1.1 2007/06/17 15:34:42 jim Exp $

package us.temerity.pipeline.plugin.ListSourcesAction.v2_0_9;

import java.io.*;
import java.util.ArrayList;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   L I S T   S O U R C E S   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates a text file that contains a list of all primary files associated with the 
 * immediately upstream source nodes. <P>
 * 
 * Can be a useful replacement for the Touch action on texture grouping nodes to log the 
 * full list of texture files.
 */
public class 
ListSourcesAction 
  extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  ListSourcesAction()
  {
    super("List Sources", new VersionID("2.0.9"), "Temerity",
	  "Generates a text file that contains a list of all primary files associated " + 
	  "with the immediately upstream source nodes.");

    addSupport(OsType.MacOS);   
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

    Path scene;
    FileSeq targetSeq;

    {
      targetSeq = agenda.getPrimaryTarget();
      if(!targetSeq.isSingle())
	throw new PipelineException
	  ("The ListSources Action requires a single file as its target");

      scene = new Path(PackageInfo.sProdPath, 
		       nodeID.getWorkingParent() + "/" + targetSeq.getPath(0));
    }

    File document = createTemp(agenda, 0644, "txt");
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(document)));
      for (String source : agenda.getSourceNames()) {
	FileSeq sourceSeq = agenda.getPrimarySource(source);
	Path spath = new Path(source);
	for(Path p : sourceSeq.getPaths()) {
	  Path path = new Path(spath.getParentPath(), p);
	  out.println(path.toOsString());
	}
      }

      out.close();

    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary file (" + document + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" + ex.getMessage());
    }

    {
      ArrayList<String> args = new ArrayList<String>();
      args.add(document.getPath());
      args.add(scene.toOsString());

      try {
	return new SubProcessHeavy
	  (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	   "cp", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	   outFile, errFile);
      } 
      catch (Exception ex) {
	throw new PipelineException
	  ("Unable to generate the SubProcess to perform this Action!\n"
	   + ex.getMessage());
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3550351686639451935L;

}
