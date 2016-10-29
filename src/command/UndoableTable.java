package command;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;
import javax.swing.undo.AbstractUndoableEdit;

@SuppressWarnings("serial")
public class UndoableTable extends AbstractUndoableEdit {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public UndoableTable(DefaultTableModel oldTable, DefaultTableModel currentTable) {
		this.oldTable     = oldTable;
		this.currentTable = currentTable;
		this.newTable     = new DefaultTableModel();
		
		for (int i = 0; i < currentTable.getColumnCount(); i++) {
			Vector v = new Vector();
			
			for (int j = 0; j < currentTable.getRowCount(); j++) {
				v.add(currentTable.getValueAt(j, i));	
			}
			
			newTable.addColumn(currentTable.getColumnName(i), v);
		}
	}
	
	/**
	 * Undo the previous action on the table
	 */
	public void undo() {
		super.undo();
		
		for (int i = 0; i < oldTable.getRowCount(); i++) {
			for (int j = 0; j < oldTable.getColumnCount(); j++) {
				currentTable.setValueAt(oldTable.getValueAt(i, j), i, j);
			}
		}
	}

	/**
	 * Redo the last canceled action on the table
	 */
	public void redo() {
		super.redo();
		
		for (int i = 0; i < newTable.getRowCount(); i++) {
			for (int j = 0; j < newTable.getColumnCount(); j++) {
				currentTable.setValueAt(newTable.getValueAt(i, j), i, j);
			}
		}
	}
	
	private DefaultTableModel oldTable;
	private DefaultTableModel currentTable;
	private DefaultTableModel newTable;
}
