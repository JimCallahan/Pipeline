package us.temerity.pipeline.plugin.v2_2_1;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The Motion Graphics and Compositing program from Adobe. <P> 
 */
public 
class AfterFXEditor
  extends BaseEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public
  AfterFXEditor()
  {
    super("AfterFX", new VersionID("2.2.1"), "Temerity",
	  "The Adobe After Effects image editor.", 
	  "AfterFX");

    removeSupport(OsType.Unix);
    //addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
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
    ArrayList<String> args = new ArrayList<String>();
    args.add(fseq.getPath(0).toOsString());

    return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
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
   *   The controlling <CODE>SubProcess</CODE> instance. 
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
      ("This launch() method should never be called since the prep() method returns " + 
       "a non-null SubProcessLight instance!");
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6459199921736152795L;
}
