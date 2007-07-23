package us.temerity.pipeline.stages;

import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;


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
    String suffix
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
          getDefaultEditor(client, suffix), 
          null);
    pFileContents = null;
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
    super.build();
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
  
  /**
   * Get the abstract working area file system path to the file created by this node.
   */
  protected final Path
  getWorkingNodeFilePath() 
  {
    NodeID nodeID = new NodeID(pUtilContext.getAuthor(), pUtilContext.getView(), pRegisteredNodeName);
    FileSeq seq = pRegisteredNodeMod.getPrimarySequence();
    return new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/" + seq.getFile(0));     
  }
  
  private String pFileContents;
  
  private static final long serialVersionUID = 4013108921342960191L;
  
}
