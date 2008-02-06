package us.temerity.pipeline.stages;

import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.StageFunction;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

public 
class FileWriterStage
  extends StandardStage
{
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
          null);
    pFileContents = null;
    pStageFunction = stageFunction;
  }
  
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
      new NodeID(pUtilContext.getAuthor(), pUtilContext.getView(), pRegisteredNodeName);
    FileSeq seq = pRegisteredNodeMod.getPrimarySequence();
    return new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/" + seq.getFile(0));     
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    if (pStageFunction != null)
      return pStageFunction;
    return StageFunction.aTextFile;
  }

  
  private String pFileContents;
  
  private String pStageFunction;
  
  private static final long serialVersionUID = 4013108921342960191L;
  
}
