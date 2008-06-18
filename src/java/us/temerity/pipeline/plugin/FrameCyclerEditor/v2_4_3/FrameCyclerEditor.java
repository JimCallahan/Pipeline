// $Id: FrameCyclerEditor.java,v 1.1 2008/06/18 23:57:11 jim Exp $

package us.temerity.pipeline.plugin.FrameCyclerEditor.v2_4_3;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F R A M E   C Y C L E R   E D I T O R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The FrameCycler image viewer.
 */
public
class FrameCyclerEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  FrameCyclerEditor()
  {
    super("FrameCycler", new VersionID("2.4.3"), "Temerity",
	  "The FrameCycler image viewer.", 
	  "framecycler");  

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the name of the editor executable.
   */
  public String
  getProgram() 
  {
    if(PackageInfo.sOsType == OsType.MacOS)  
      return "FrameCycler";
    else if(PackageInfo.sOsType == OsType.Windows) 
      return "FrameCycler.exe"; 
    return super.getProgram();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
   * 
   * The default implementation executes the editor program obtained with {@link #getProgram 
   * getProgram} method under the given environment.  Subclasses should override this method 
   * if more specialized behavior or different command line arguments are needed in order to 
   * launch the editor for the given file sequence. <P> 
   * 
   * @param author
   *   The name of the user owning the files. 
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   * 
   * @see SubProcessLight
   */  
  public SubProcessLight
  prep
  (
   String author, 
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    try {
      ArrayList<String> args = new ArrayList<String>();
      if(fseq.hasFrameNumbers()) {
        FilePattern fpat = fseq.getFilePattern();
        String pattern = (fpat.getPrefix() + ".#");
        String suffix = fpat.getSuffix();
        if(suffix != null) 
        pattern = (pattern + "." + suffix);
        args.add(pattern); 

        FrameRange range = fseq.getFrameRange();
        args.add(range.getStart() + "-" + range.getEnd());
      }
      else {
        args.add(fseq.getFile(0).toString());
      }

      return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to launch this Editor!\n" +
	 ex.getMessage());
    }       
  }

  /** 
   * This implementation always throws a PipelineException, to insure that the {@link #prep
   * prep} method is used for this Editor instead of this deprecated method.
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   */  
  @SuppressWarnings("deprecation")
  @Deprecated
  public SubProcessLight
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    throw new PipelineException
      ("This method should never be called since the prep() method does not return (null)!");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -181836244209704760L;

}


