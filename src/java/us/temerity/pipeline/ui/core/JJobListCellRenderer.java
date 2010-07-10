package us.temerity.pipeline.ui.core;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   L I S T   C E L L   R E N D E R E R                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * List renderer used in the {@link JJobMonitorDialog}.
 */
public 
class JJobListCellRenderer
  extends JPanel
  implements ListCellRenderer
{
  public 
  JJobListCellRenderer()
  {
    LayoutManager layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
    this.setLayout(layout);
    
    {
      JPanel panel = new JPanel();

      panel.setLayout(new BorderLayout());
      panel.setName("JobStatesTableCellRenderer");  
      JProportionGraph graph = new JProportionGraph();
      pGraph = graph;
      
      graph.setMinimumSize(new Dimension(80, 20));
      graph.setPreferredSize(new Dimension(80, 20));

      panel.add(graph);
      this.add(panel);
    }
    
    this.add(Box.createHorizontalStrut(8));
    
    {
      Box box = Box.createVerticalBox();
      {
        Box hbox = Box.createHorizontalBox();
        pGroupIDTitleLabel = 
          UIFactory.createFixedLabel("Job Group ID:", 90, SwingConstants.RIGHT); 
        hbox.add(pGroupIDTitleLabel);
        hbox.add(Box.createHorizontalStrut(4));
        pGroupIDLabel = UIFactory.createFixedLabel("", 175, SwingConstants.LEFT);
        hbox.add(pGroupIDLabel);
        hbox.add(Box.createHorizontalGlue());
        
        box.add(hbox);
      }
      {
        Box hbox = Box.createHorizontalBox();
        pWorkingAreaTitleLabel = 
          UIFactory.createFixedLabel("Working Area:", 90, SwingConstants.RIGHT); 
        hbox.add(pWorkingAreaTitleLabel);
        hbox.add(Box.createHorizontalStrut(4));
        pWorkingAreaLabel = UIFactory.createFixedLabel("", 175, SwingConstants.LEFT);
        hbox.add(pWorkingAreaLabel);
        hbox.add(Box.createHorizontalGlue());
        
        box.add(hbox);
      }
      {
        Box hbox = Box.createHorizontalBox();
        pFileSeqTitleLabel = 
          UIFactory.createFixedLabel("File Seq:", 90, SwingConstants.RIGHT); 
        hbox.add(pFileSeqTitleLabel);
        hbox.add(Box.createHorizontalStrut(4));
        pFileSeqLabel = UIFactory.createFixedLabel("", 175, SwingConstants.LEFT);
        hbox.add(pFileSeqLabel);
        hbox.add(Box.createHorizontalGlue());
        
        box.add(hbox);
      }
      this.add(box);
    }
    this.setBorder(sNormalBorder);
  }
  
  
  @Override
  public Component 
  getListCellRendererComponent
  (
    JList list,
    Object value,
    int index,
    boolean isSelected,
    boolean cellHasFocus
  )
  {
    JobListCellData data = (JobListCellData) value;
    QueueJobGroup group = data.getJobGroup();
    
    double[] dist = data.getDist();
    
    pGroupIDLabel.setText(String.valueOf(group.getGroupID()));
    NodeID id = group.getNodeID();
    pWorkingAreaLabel.setText(id.getAuthor() + ":" + id.getView());
    pFileSeqLabel.setText(group.getRootSequence().toString());
    
    if (isSelected) {
      pGroupIDTitleLabel.setForeground(Color.yellow);
      pWorkingAreaTitleLabel.setForeground(Color.yellow);
      pFileSeqTitleLabel.setForeground(Color.yellow);
    }
    else {
      pGroupIDTitleLabel.setForeground(Color.white);
      pWorkingAreaTitleLabel.setForeground(Color.white);
      pFileSeqTitleLabel.setForeground(Color.white);
    }

    double total = 0.0f;
    double[] sorted = new double[dist.length];
    {
      int wk = 0;
      for(JobState jstate : sDisplayOrder) {
        sorted[wk] = dist[jstate.ordinal()];
        total += sorted[wk];
        wk++;
      }
    }
    
    if(total > 0.0f) {
      UserPrefs prefs = UserPrefs.getInstance();
      Color[] colors = {
        colorCast(prefs.getFailedCoreColor()), 
        colorCast(prefs.getAbortedCoreColor()), 
        colorCast(prefs.getFinishedCoreColor()), 
        
        colorCast(prefs.getRunningCoreColor()), 
        colorCast(prefs.getLimboCoreColor()), 
        
        colorCast(prefs.getPausedCoreColor()), 
        colorCast(prefs.getPreemptedCoreColor()), 
        colorCast(prefs.getQueuedCoreColor())
      };

      pGraph.setValues(sorted, colors);
    }
    else {
      pGraph.setValues(null, null);
    }

    return this;
  }
  
  /**
   * Convert from Pipeline color to Swing color.
   */ 
  private Color
  colorCast
  (
   Color3d color
  )
  {
    return new Color((float) color.r(), (float) color.g(), (float) color.b());
  }
  
  /**
   * The data that the Renderer reads to determine what to draw.
   */
  public static 
  class JobListCellData
  {
    /**
     * Constructor. <p>
     * 
     * @param jobGroup
     *   The job group.
     * 
     * @param dist
     *   The distribution of job states.
     */
    public
    JobListCellData
    (
      QueueJobGroup jobGroup,
      double[] dist
    )
    {
      pJobGroup = jobGroup;
      pDist = dist;
    }
    
    /**
     * Get the job group. 
     */
    public QueueJobGroup 
    getJobGroup()
    {
      return pJobGroup;
    }
    
    /**
     * Get the job state distribution.
     */
    public double[]
    getDist()
    {
      return pDist;
    }
    
    
    private QueueJobGroup pJobGroup;
    private double[] pDist;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3328264312691509109L;
  
  private static final JobState[] sDisplayOrder = {
    JobState.Failed, JobState.Aborted, JobState.Finished, 
    JobState.Running, JobState.Limbo, 
    JobState.Paused, JobState.Preempted, JobState.Queued
  };
  

  private static final Border sNormalBorder = BorderFactory.createEmptyBorder(2, 0, 2, 0);;
//  private static final Border sSelectedBorder = 
//    BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0), 
//                                       BorderFactory.createLineBorder(Color.yellow));


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The horizontal graph showing state colors bars in proportion to the state counts.
   */ 
  private JProportionGraph pGraph;
  
  /**
   * The label that contains the job group ID.
   */
  private JLabel pGroupIDLabel;
  private JLabel pGroupIDTitleLabel;
  
  /**
   * The label that contains the working area information.
   */
  private JLabel pWorkingAreaLabel;
  private JLabel pWorkingAreaTitleLabel;
  
  /**
   * The label that contains the file sequence information.
   */
  private JLabel pFileSeqLabel;
  private JLabel pFileSeqTitleLabel;
}
