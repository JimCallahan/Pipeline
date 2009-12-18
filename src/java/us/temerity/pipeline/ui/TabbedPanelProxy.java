// $Id: TabbedPanelProxy.java,v 1.1 2009/12/18 08:44:26 jim Exp $

package us.temerity.pipeline.ui;

/*------------------------------------------------------------------------------------------*/
/*   T A B B E D   P A N E L   P R O X Y                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A component of the top level top-level windows of plui(1) which manages one or more 
 * child components as a set of tabbed panels.<P> 
 * 
 * This interface add no new methods as the base class {@link PanelComponentProxy} provides
 * all methods needed to access the tabs contents.  This interface exists soley to allow 
 * user code to identify panel components as being tabbed panels.
 */ 
public 
interface TabbedPanelProxy
  extends PanelComponentProxy
{}
