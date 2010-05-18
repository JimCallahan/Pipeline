package us.temerity.pipeline.stages;

import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;

/**
 * Helper stage that creates a single file node and then writes data into that file.
 * <p>
 * The {@link #setFileContents(String) setFileContents} method should be called in the
 * constructor of the stage with a string that contains the full content of the file to be 
 * written to disk.
 * <p>
 * This stage is intended for situations where a builder wants to create some script or other 
 * text document that there is not a programatic way to create.  This often includes things 
 * like MEL scripts, bash scripts, etc.
 */
public 
class FileWriterStage
  extends StandardStage
{
  /**
   * Constructor.
   * 
   * @param name
   *   The name of the stage.
   *   
   * @param desc
   *   A description of what the stage does.
   *   
   * @param info
   *   The builder information.
   *   
   * @param context
   *   The {@link UtilContext} the stage will work in.
   *   
   * @param client
   *   The instance of {@link MasterMgrClient} used to make the node.
   *   
   * @param nodeName
   *   The name of the node.
   *   
   * @param suffix
   *   The suffix for the node.
   *   
   * @param stageFunction
   *   The stage function that can be used to set the editor for the node.
   */
  protected 
  FileWriterStage
  (
    String name,
    String desc,
    StageInformation info,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String suffix,
    String stageFunction
  ) 
    throws PipelineException
  {
    super(name, 
          desc, 
          info, 
          context, 
          client, 
          nodeName, 
          suffix, 
          null, 
          null,
          stageFunction);
    pFileContents = null;
  }

  /**
   * Set the string that will be written into the file.
   * <p>
   * This should be called in the constructor of the stage.
   */
  protected void
  setFileContents
  (
    String contents  
  )
  {
    pFileContents = contents;
  }
  
  @Override
  public boolean build()
    throws PipelineException
  {
    if (super.build())
    {
      File toMake = getWorkingNodeFilePath().toFile();
      File parentDir = toMake.getParentFile();
      if (!parentDir.exists())
	parentDir.mkdirs();
      try {
	FileWriter out = new FileWriter(toMake);
	out.write(pFileContents);
	out.close();
      }
      catch (IOException ex) {
	throw new PipelineException
	("The FileWriter stage was unable to write the file (" + toMake.getPath() + ").  " + 
	  ex.getMessage());
      }
      return true;
    }
    return false;
  }
  
  /**
   * Get the abstract working area file system path to the file created by this node.
   */
  protected final Path
  getWorkingNodeFilePath() 
  {
    NodeID nodeID = 
      new NodeID(pUtilContext.getAuthor(), pUtilContext.getView(), getNodeName());
    FileSeq seq = pRegisteredNodeMod.getPrimarySequence();
    return new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/" + seq.getFile(0));     
  }
  
  private String pFileContents;
  
  private static final long serialVersionUID = 4013108921342960191L;
}
