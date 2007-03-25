// $Id: ListSourcesAction.java,v 1.3 2007/03/25 03:12:51 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

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
 * full list of texture files.  The files printed are rooted at the base working area 
 * directory and in abstract file system path format (see {@link Path}).
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
    super("ListSources", new VersionID("2.2.1"), "Temerity",
	  "Generates a text file that contains a list of all primary files associated " + 
	  "with the immediately upstream source nodes.");

    addSupport(OsType.MacOS);   
    addSupport(OsType.Windows); 
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
    /* target text file */ 
    Path target = getPrimaryTargetPath(agenda, "text file");

    /* create a temporary file which will be copied to the target */ 
    File temp = createTemp(agenda, "txt");
    try {
      FileWriter out = new FileWriter(temp);

      for(String source : agenda.getSourceNames()) {
	FileSeq sourceSeq = agenda.getPrimarySource(source);
	Path spath = new Path(source);
	for(Path p : sourceSeq.getPaths()) {
	  Path path = new Path(spath.getParentPath(), p);
	  out.write(path.toString() + "\n");
	} 
      }
 
      out.close();
    } 
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary output file (" + temp + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" + 
         ex.getMessage());
    }

    /* create the process to run the action */ 
    return createTempCopySubProcess(agenda, temp, target, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1263903659077136996L;

}
