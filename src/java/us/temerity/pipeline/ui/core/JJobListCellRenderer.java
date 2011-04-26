package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;

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
    LayoutManager layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
    this.setLayout(layout);
    
    {
      Box hbox = Box.createHorizontalBox();
      hbox.add(Box.createHorizontalStrut(8));
      {
        pGroupIDLabel = UIFactory.createFixedLabel("", 65, SwingConstants.LEFT);
        hbox.add(pGroupIDLabel);
        hbox.add(Box.createHorizontalStrut(4));

        pWorkingAreaLabel = UIFactory.createLabel("", 10, SwingConstants.LEFT);
        hbox.add(pWorkingAreaLabel);
        hbox.add(Box.createHorizontalGlue());
      }
      this.add(hbox);
    }

    this.add(Box.createVerticalStrut(3));
    
    {
      Box hbox = new Box(BoxLayout.LINE_AXIS);
      
      hbox.add(Box.createHorizontalStrut(4));
      
      JPanel panel = new JPanel();

      panel.setLayout(new BorderLayout());
      panel.setName("JobStatesTableCellRenderer");  
      JProportionGraph graph = new JProportionGraph();
      pGraph = graph;
      
      graph.setMinimumSize(new Dimension(25, 18));
      graph.setPreferredSize(new Dimension(220, 18));

      panel.add(graph);
      hbox.add(panel);
      hbox.add(Box.createHorizontalStrut(4));
      this.add(hbox);
    }
    this.add(Box.createVerticalStrut(2));
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
    pWorkingAreaLabel.setText(id.getAuthor() + " | " + id.getView());
    
    if (isSelected) {
      pGroupIDLabel.setForeground(Color.yellow);
      pWorkingAreaLabel.setForeground(Color.yellow);
      this.setBorder(sSelectedBorder);
    }
    else {
      pGroupIDLabel.setForeground(Color.white);
      pWorkingAreaLabel.setForeground(Color.white);
      this.setBorder(sNormalBorder);
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

      pGraph.setValues(sorted, colors, group.getRootSequence().toString());
    }
    else {
      pGraph.setValues(null, null);
    }
    
    this.setToolTipText(group.getNodeID().getName());

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
  
  private static final Border sNormalBorder = 
    BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0), 
                                       BorderFactory.createLineBorder(Color.black));
  
  private static final Border sSelectedBorder = 
    BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0), 
                                       BorderFactory.createLineBorder(Color.yellow));


  
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
  
  /**
   * The label that contains the working area information.
   */
  private JLabel pWorkingAreaLabel;
}
