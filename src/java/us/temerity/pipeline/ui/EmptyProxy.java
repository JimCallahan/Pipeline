// $Id: EmptyProxy.java,v 1.1 2009/12/18 19:56:44 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   E M P T Y   P R O X Y                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An interface for managing an Empty panel.<P> 
 * 
 * This interface add no new methods as the base class {@link PanelProxy} provides
 * all methods needed.  Empty panels are by definition a placeholder and have no additional
 * functionality not common to all panels.
 */ 
public 
interface EmptyProxy
  extends PanelProxy
{}

