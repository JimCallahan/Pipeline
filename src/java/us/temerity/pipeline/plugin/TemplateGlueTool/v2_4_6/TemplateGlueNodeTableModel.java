// $Id: TemplateGlueNodeTableModel.java,v 1.1 2009/06/11 05:35:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import us.temerity.pipeline.ui.*;

@SuppressWarnings("unchecked")
public 
class TemplateGlueNodeTableModel
  extends AbstractSortableTableModel
{
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
      
      
      pColumnWidths = new int[pNumColumns];
      {
        for (int i = 0; i < nameSize; i++) {
          int width = headers.get(i).length() * 12;
          for (String name : names.get(i)) {
            int w = name.length() * 8;
            if (w > width)
              width = w;
          }
          pColumnWidths[i] = width;
        }

        for (int i = nameSize ; i < nameSize + shortSize; i++) {
          int width = headers.get(i).length() * 12;
          pColumnWidths[i] = width;
        }
      }
        
      
      {
        pRenderers = new TableCellRenderer[pNumColumns];
        for (int i = 0; i < nameSize; i++) 
          pRenderers[i] = new JSimpleTableCellRenderer(SwingConstants.LEFT);  
        
        for (int i = nameSize ; i < nameSize + shortSize; i++) 
          pRenderers[i] = new JSimpleTableCellRenderer(SwingConstants.CENTER);
      }
      
      pEditors = new TableCellEditor[pNumColumns];
    }
    
    sort();
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
    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx;
    
    
    Comparable[] sorting;
    int size = pNames.size(); // if this is 2, we have 0 and 1
    if ( (pSortColumn) < size ) { // so if we sort on column 1 we're here 
      sorting = pNames.get(pSortColumn);
    }
    else // If this is col 2, we want entry zero. 
      sorting = pPresent.get(pSortColumn - size);
    
    for(idx=0; idx < pNames.get(0).length; idx++) {
      Comparable value = sorting[idx];
      
      int wk;
      for(wk=0; wk<values.size(); wk++) {
        if(value.compareTo(values.get(wk)) > 0) 
          break;
      }
      values.add(wk, value);
      indices.add(wk, idx);
    }
    
    pRowToIndex = new int[indices.size()];
    int wk; 
    if(pSortAscending) {
      for(wk=0; wk<pRowToIndex.length; wk++) 
        pRowToIndex[wk] = indices.get(wk);
    }
    else {
      for(wk=0, idx=indices.size()-1; wk<pRowToIndex.length; wk++, idx--) 
        pRowToIndex[wk] = indices.get(idx);
    }

    fireTableDataChanged();
  }
  
  @Override
  public int 
  getRowCount()
  {
    return pNames.get(0).length;
  }

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

  private ArrayList<String[]> pNames;
  private ArrayList<Comparable[]> pPresent;
  
  private static final long serialVersionUID = 6513554323453717487L;
}
