// $Id: TemplateGlueNodeTableModel.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_10;

import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.math.*;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;


@SuppressWarnings("unchecked")
public 
class TemplateGlueNodeTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateGlueNodeTableModel
  (
    String[] names,
    ArrayList<Comparable[]> present,
    ArrayList<String> headers
  )
  {
    this(toList(names), present, headers);
  }     
  
  public 
  TemplateGlueNodeTableModel
  (
    ArrayList<String[]> names,
    ArrayList<Comparable[]> present,
    ArrayList<String> headers
  )
  {
    super();
    pNames = names;
    pPresent = present;

    pNumRows = pNames.get(0).length; 

    /* initialize the columns */ 
    {
      int nameSize = names.size();
      int shortSize = present.size();
      pNumColumns = nameSize + shortSize;
      {
        pColumnClasses = new Class[pNumColumns];
        for (int i = 0; i < pNumColumns; i++)
          pColumnClasses[i] = String.class;
      }
      {
        pColumnNames = new String[pNumColumns];
        for (int i = 0; i < pNumColumns; i++)
          pColumnNames[i] = headers.get(i);      
      }
      {
        pColumnDescriptions = new String[pNumColumns];
        pColumnDescriptions[0] = "The name of the object";
        for (int i = 1; i < pNumColumns; i++)
          pColumnDescriptions[i] = 
            "Whether the object was found in the (" + headers.get(i - 1) + ")";
      }
      
      pColumnWidthRanges = new Vector3i[pNumColumns];
      {
        for (int i = 0; i < nameSize; i++) {
          int width = headers.get(i).length() * 12;
          for (String name : names.get(i)) {
            int w = name.length() * 8;
            if (w > width)
              width = w;
          }
          pColumnWidthRanges[i] = new Vector3i(180, width, Integer.MAX_VALUE); 
        }

        for (int i = nameSize ; i < nameSize + shortSize; i++) {
          int width = headers.get(i).length() * 12;
          pColumnWidthRanges[i] = new Vector3i(width);
        }
      }
      
      {
        pRenderers = new TableCellRenderer[pNumColumns];
        for (int i = 0; i < nameSize; i++) 
          pRenderers[i] = new JSimpleTableCellRenderer(SwingConstants.LEFT);  
        
        for (int i = nameSize ; i < nameSize + shortSize; i++) {
          Comparable array[] = present.get(i - nameSize);
          if (array.length > 0) {
            Comparable v = array[0];
            if (v instanceof Boolean)
              pRenderers[i] = new JBooleanTableCellRenderer(SwingConstants.CENTER);
            else
              pRenderers[i] = new JSimpleTableCellRenderer(SwingConstants.CENTER);
          }
          else
            pRenderers[i] = new JSimpleTableCellRenderer(SwingConstants.CENTER);
        }
      }
      
      pEditors = new TableCellEditor[pNumColumns];
    }
    
    sort();
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  private static ArrayList<String[]>
  toList
  (
    String[] list 
  )
  {
    ArrayList<String[]> toReturn = new ArrayList<String[]>();
    toReturn.add(list);
    return toReturn;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  @SuppressWarnings({ "unchecked"})
  @Override
  public void 
  sort()
  {
    Comparable[] sorting;
    int size = pNames.size(); // if this is 2, we have 0 and 1
    if ( (pSortColumn) < size ) { // so if we sort on column 1 we're here 
      sorting = pNames.get(pSortColumn);
    }
    else // If this is col 2, we want entry zero. 
      sorting = pPresent.get(pSortColumn - size);
    
    IndexValue cells[] = new IndexValue[pNumRows]; 
    int idx;
    for(idx=0; idx<pNumRows; idx++) 
      cells[idx] = new IndexValue(idx, sorting[idx]);

    Comparator<IndexValue> comp = 
      pSortAscending ? new AscendingIndexValue() : new DescendingIndexValue(); 
    Arrays.sort(cells, comp);

    pRowToIndex = new int[pNumRows];
    int row; 
    for(row=0; row<pNumRows; row++) 
      pRowToIndex[row] = cells[row].getIndex();     

    fireTableDataChanged();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the value for the cell at columnIndex and rowIndex.
   */ 
  @Override
  public Object 
  getValueAt
  (
    int rowIndex,
    int columnIndex
  )
  {
    int srow = pRowToIndex[rowIndex];
    
    int size = pNames.size(); // if this is 2, we have 0 and 1
    if ( (columnIndex) < size ) { // so if we sort on column 1 we're here 
      return pNames.get(columnIndex)[srow];
    }
    else // If this is col 2, we want entry zero. 
      return pPresent.get(columnIndex- size)[srow];
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6513554323453717487L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private ArrayList<String[]> pNames;
  private ArrayList<Comparable[]> pPresent;
  
}
