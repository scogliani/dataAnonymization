package gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.UndoableEditEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.UndoManager;

import policy.PolicyTree;
import visitor.IVisitor;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import command.UndoableTable;
import command.UndoableTree;

public class Model extends Observable {	
	public Model() {
		tableModel = new DefaultTableModel();
		varianceValueModel = new SpinnerNumberModel();
		varianceValueModel.setValue(0);
		varianceValueModel.setStepSize(1);
		blurValueModel = new SpinnerNumberModel();
		blurValueModel.setValue(0);
		blurValueModel.setStepSize(1);
		blurValueModel.setMinimum(0);
		policyTreeModel = new PolicyTree<String>();
		manager = new UndoManager();
		displayPolicy = false;
	}
	
	/**
	 * The actions to be performed by the selection of the undo menu.
	 */
	public void undo() {
		manager.undo();
		manager.undo();
		
		setChanged();
		notifyObservers(manager);
	}
	
	/**
	 * The actions to be performed by the selection of the redo menu.
	 */
	public void redo() {
		manager.redo();
		manager.redo();
		
		setChanged();
		notifyObservers(manager);
	}
	
	public void displayPolicy() {
		displayPolicy = !displayPolicy;
		
		setChanged();
		notifyObservers(displayPolicy);
	}
	
	/**
	 * Get the model of the table
	 * @return The model of the table
	 */
	public TableModel getTableModel() {
		return tableModel;
	}

	/**
	 * Get the model of the tree
	 * @return The model of the tree
	 */
	public DefaultMutableTreeNode getTreeModel() {
		return policyTreeModel;
	}

	/**
	 * Initialize the table and the tree from a given file
	 * @param filename The file which will be open in the application
	 */
	public void initialize(String filename) {	
		try {
			CSVReader values = null;
			values = new CSVReader(new FileReader(filename), separator);
			
			
			String[] headers = null;
			headers = values.readNext();
			
			tableModel.setRowCount(0);
			tableModel.setColumnIdentifiers(headers);
			
			String nextLine[];
			while((nextLine = values.readNext()) != null) {
				tableModel.addRow(nextLine);
			}			
			
			policyTreeModel.removeAllChildren();
			policyTreeModel.removeHeader();
			policyTreeModel.addHeader(headers);
			
			values.close();
			
			setChanged();
			notifyObservers(manager);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Ask the user which file to open and to give the separator between the columns.
	 */
	public void open() {
			JFileChooser fc = new JFileChooser();
			File file =  null;
			
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.removeChoosableFileFilter(fc.getAcceptAllFileFilter());

			fc.addChoosableFileFilter(new FileFilter() {
		        @Override public String getDescription() { return "All types (*.*)"; }
		        @Override public boolean accept(File f) { return true; }
		    });
			
			fc.addChoosableFileFilter(new FileFilter() {
		        @Override public String getDescription() { return "Comma-separated values (*.csv)"; }
		        @Override public boolean accept(File f) { return f.getName().endsWith(".csv") || f.isDirectory(); }
		    });
			
			fc.addChoosableFileFilter(new FileFilter() {
		        @Override public String getDescription() { return "Normal text file (*.txt)"; }
		        @Override public boolean accept(File f) { return f.getName().endsWith(".txt") || f.isDirectory(); }
		    });
			
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				String sep = JOptionPane.showInputDialog(null,
						  "Quel separateur utiliser ?",
						  "Entrez le separateur",
						  JOptionPane.QUESTION_MESSAGE);
				separator = sep.charAt(0);
				
				initialize(file.getAbsolutePath());
			} else {
				
			}	
	}
	
	/**
	 * Save the current state of the table in a file chosen by the user.
	 * This action remove the possibility to undo the precedents action after the save complete.
	 */
	public void saveAs() {
		JFileChooser fc = new JFileChooser();
		File file =  null;
		
		fc.removeChoosableFileFilter(fc.getAcceptAllFileFilter());

		fc.addChoosableFileFilter(new FileFilter() {
	        @Override public String getDescription() { return "All types (*.*)"; }
	        @Override public boolean accept(File f) { return true; }
	    });
		
		fc.addChoosableFileFilter(new FileFilter() {
	        @Override public String getDescription() { return "Comma-separated values (*.csv)"; }
	        @Override public boolean accept(File f) { return f.getName().endsWith(".csv") || f.isDirectory(); }
	    });
		
		fc.addChoosableFileFilter(new FileFilter() {
	        @Override public String getDescription() { return "Normal text file (*.txt)"; }
	        @Override public boolean accept(File f) { return f.getName().endsWith(".txt") || f.isDirectory(); }
	    });
		
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		int returnVal = fc.showSaveDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			JFileChooser fc2 = new JFileChooser();
			File file2 =  null;
			
			fc2.removeChoosableFileFilter(fc.getAcceptAllFileFilter());

			fc2.addChoosableFileFilter(new FileFilter() {
		        @Override public String getDescription() { return "eXtensible Markup Language file (*.xml)"; }
		        @Override public boolean accept(File f) { return f.getName().endsWith(".xml") || f.isDirectory(); }
		    });
			
			fc2.setCurrentDirectory(new File(System.getProperty("user.dir")));
			int returnVal2 = fc2.showSaveDialog(null);
			
			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
	
				BufferedWriter out = null;
				String [] rows = new String[tableModel.getColumnCount()];
				try {
					out = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
					CSVWriter writer = new CSVWriter(out);
					
					/*Sauvegarde des headers*/
					for(int i = 0; i < tableModel.getColumnCount(); i++) {
						rows[i] = tableModel.getColumnName(i);
					}
					writer.writeNext(rows);
					
					/*Sauvegarder du contenu du tableau*/
					for(int i=0; i<tableModel.getRowCount(); i++) {
						for (int j=0; j<tableModel.getColumnCount(); j++) {
							rows[j] = tableModel.getValueAt(i, j).toString();
						}
						writer.writeNext(rows);
					}
					writer.close();
					out.close();
	
					file2 = fc2.getSelectedFile();
					
					BufferedWriter out2 = new BufferedWriter(new FileWriter(file2.getAbsoluteFile()));
					
					out2.write(policyTreeModel.asXML());
					
					out2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				status = "Save succesfully"; 
				setChanged();
				notifyObservers(status);
			}
		}

		manager.discardAllEdits();
		setChanged();
		notifyObservers(manager);
	}

	/**
	 * Apply the chosen anonymization function over the selected column or overall the columns
	 * @param numCol The column where the anonymization function will be applied.
	 * @param visitor 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void anonymization(int numCol, IVisitor visitor, int nbLines) {	
		DefaultTableModel t = new DefaultTableModel();
		PolicyTree<String> p = new PolicyTree<String>(policyTreeModel);
		
		/* Initialisation de l'ancienne table */
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			Vector v = new Vector();
			
			for (int j = 0; j < tableModel.getRowCount(); j++) {
				v.add(tableModel.getValueAt(j, i));	
			}
			
			t.addColumn(tableModel.getColumnName(i), v);
		}
		
		if (nbLines != -1) {
			if (nbLines > tableModel.getDataVector().size()) {
				nbLines = tableModel.getDataVector().size();
			}
			
			visitor.anonymization(tableModel.getDataVector().subList(0, nbLines), numCol);
		} else {			
			visitor.anonymization(tableModel.getDataVector().subList(0, tableModel.getDataVector().size()), numCol);
		}
		
		if (visitor.isAll()) {
			policyTreeModel.add(visitor);
		} else {
			policyTreeModel.add(numCol, visitor);
		}
		
		manager.undoableEditHappened(new UndoableEditEvent(tableModel, new UndoableTable(t, tableModel)));
		manager.undoableEditHappened(new UndoableEditEvent(policyTreeModel, new UndoableTree(p, policyTreeModel)));
		

		PrintWriter ecrivain = null;
		try {
			ecrivain =  new PrintWriter(new BufferedWriter
					   (new FileWriter("etc/policy.xml")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ecrivain.println(policyTreeModel.asXML());
		
		ecrivain.close();
		
		setChanged();
		notifyObservers(manager);
	}	

	/**
	 * Compute the k-anonymization of the database with the given columns as quasi-identifiers.
	 * @param cols The quasi-identifiers which be used to calculate the k-anonymization.
	 */
	public void kanon(String cols) {
		HashMap< String, List <String>> kanonmap;
		String result = null ;
		String key = null;
		
		int[] columns;
		int kanonLevel = Integer.MAX_VALUE;

		//int [] levels = new int[2];
		
		String[] temp = cols.split(" ");
		columns = new int[temp.length];
		
		kanonmap = new HashMap<String, List<String>>();
		
		
		for (int i=0; i< temp.length; i++) {
				columns[i] = Integer.parseInt(temp[i]);
		}
		
		/* k-anonymisation part*/
		for (int j = 0; j < tableModel.getRowCount(); j++) {
			key = tableModel.getValueAt(j, columns[0]).toString();
	    	result = new String();
			for (int k=1; k < columns.length - 1; k++) {
				result = result + " " + tableModel.getValueAt(j, columns[k]);
			}


	    	if(kanonmap.containsKey(key)) {
	    		kanonmap.get(key).add(result); 
	    	} else {
	    		List<String> eval = new ArrayList<String>();
	    		eval.add(result);
	    		kanonmap.put(key, eval);
	    	}    
		}
		
		Set<String> keyset = kanonmap.keySet();
    	Object[] keys = keyset.toArray();
    	int freq = 0;
    	for (int j=0; j< keyset.size() ; j++) {
    		List<String> list = kanonmap.get(keys[j]);
    		for (int k =0; k< list.size(); k++) {
    			freq = Collections.frequency(list, list.get(k) );
    			kanonLevel = Math.min(freq, kanonLevel);
    		}
    	}
    	
    	JOptionPane.showMessageDialog(new JFrame(), "K-anonymisation : " + kanonLevel, "Niveau de K-anonymisation de la base", JOptionPane.PLAIN_MESSAGE);

    	
		setChanged();
		notifyObservers();		
	}
	
	/**
	 * Calcule the l-diversite of the database with the given columns as quasi-identifiers and sensitive data
	 * @param quasId The quasi-identifiers which be used to calculate the k-anonymisation.
	 * @param col The sensible data
	 */
	public void ldiversity(String quasId, String col) {
		HashMap< String, Set <String> > map;
		int ldivLevel = Integer.MAX_VALUE;
		
		String[] temp = quasId.split(" ");
		int columns[] = new int[temp.length + 1];
		
		for (int i=0; i< temp.length; i++) {
			columns[i] = Integer.parseInt(temp[i]);
		}
		columns[temp.length] = Integer.parseInt(col);
		
		String key2;
    	
    	map = new HashMap<String, Set<String>>();
    	for (int j =0; j< tableModel.getRowCount() ; j++) {
    		key2 = new String();
	   	
	    	for (int k=0; k < columns.length - 1; k++) {
	    		key2 = key2 + " " + tableModel.getValueAt(j, columns[k]);
	    	}
	    	if(map.containsKey(key2)) {
	    		System.out.println("1");
	    		map.get(key2).add(tableModel.getValueAt(j, columns[columns.length-1]).toString()); 
	    	} else {
	    		Set<String> eval2 = new HashSet<String>();
	    		eval2.add(tableModel.getValueAt(j, columns[columns.length - 1]).toString());
	    		System.out.println(key2);
	    		System.out.println(tableModel.getValueAt(j, columns[columns.length - 1]).toString());
	    		map.put(key2, eval2);
	    	} 
    	}
    	
    	Set<String> keyset2 = map.keySet();
    	Object[] keys2 = keyset2.toArray();
    	int freq2 = 0;
    	for (int j=0; j< keyset2.size() ; j++) {
    		Set<String> list = map.get(keys2[j]);
    		freq2 = list.size();
   			ldivLevel = Math.min(freq2, ldivLevel);
    	}
    	JOptionPane.showMessageDialog(new JFrame(), "L-diversite :"+ ldivLevel ,  "Niveau de L-diversité de la base", JOptionPane.PLAIN_MESSAGE);
		
    	setChanged();
		notifyObservers();	
	}
		
	private DefaultTableModel tableModel;
	
	private Boolean displayPolicy;	
	
	private SpinnerNumberModel varianceValueModel;
	private SpinnerNumberModel blurValueModel;
	
	private PolicyTree<String> policyTreeModel;
	
	private UndoManager manager;
	private String status;
	
	private char separator;
}
