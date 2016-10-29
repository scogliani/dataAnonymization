package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.undo.UndoManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import visitor.IVisitor;

@SuppressWarnings("serial")
public class View extends JFrame implements Observer {		
	public View(Model model, String plugin_file) {
		super("Data Anonymization");		
	
		popup = new JPopupMenu();
		
		visitors = new HashMap<JMenuItem, IVisitor>();
		anonymization = new JMenu("Anonymisation");
		
		IVisitor v;
		JMenuItem menuItem;
		
		Document document = null;
		NodeList plugins = null;
		
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(plugin_file));
			plugins = document.getElementsByTagName("plugin");
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		for (int n = 0; n < plugins.getLength(); n++) {
			try {			
				v = (IVisitor) Class.forName(
						plugins.item(n).getAttributes().getNamedItem("class").getNodeValue(),
						true,
						new URLClassLoader(new URL[] {new File(plugins.item(n).getAttributes().getNamedItem("path").getNodeValue()).toURI().toURL()})).newInstance();
				v.init();
				
				menuItem = new JMenuItem(v.name()); 
				visitors.put(menuItem, v);
				if (!v.isAll()) {
					popup.add(menuItem);
				} else {
					anonymization.add(menuItem);
				}
				
			} catch (InstantiationException	| IllegalAccessException | ClassNotFoundException | MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		tree = new JTree(model.getTreeModel());
		
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
	    renderer.setOpenIcon(null);
	    renderer.setClosedIcon(null);
	    renderer.setLeafIcon(null);
	    tree.setCellRenderer(renderer);
		
		table                       = new JTable(model.getTableModel());
		save             			= new JButton("Save");
		
		obfuscation                 = new JMenuItem("Obfuscation");
		
		undo = new JMenuItem("Annuler");
		redo = new JMenuItem("Rétablir");
				
		open = new JMenuItem("Ouvrir un fichier...");
		saveMenu = new JMenuItem("Enregistrer sous...");
		quit = new JMenuItem("Quitter");
		anonymizationPolicy = new JCheckBoxMenuItem("Profil d'anonymisation");
		
		kanon = new JMenuItem("K-anonymisation");
		ldiv = new JMenuItem("L-Diversite");
		
		aPropos = new JMenuItem("A propos");
		
		kanonCols  = new JTextField();
		
		new ControllerAction(model, this);
		new ControllerWindow(model, this);
		new ControllerMouse(model, this);

		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		model.addObserver(this);
		
		add(addSouth(), BorderLayout.SOUTH);

		setJMenuBar(addMenuBar());
		
		jPaneTable = new JScrollPane(table);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(tree, BorderLayout.CENTER);
		policyView =  new JScrollPane(panel);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, policyView, jPaneTable);
		splitPane.remove(policyView);
		splitPane.setContinuousLayout(true);
		
		add(splitPane, BorderLayout.CENTER);
		
		pack();
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setExtendedState(MAXIMIZED_BOTH);
	}
	
	public JMenuItem getaPropos() {
		return aPropos;
	}

	public IVisitor getVisitor(JMenuItem menuItem) {
		return visitors.get(menuItem);
	}
	
	public HashMap<JMenuItem, IVisitor> getVisitors() {
		return visitors;
	}
	
	/**
	 * Return a reference to the K-anonymization menu
	 * @return a reference to the K-anonymization menu
	 */
	public JMenuItem getKanon() {
		return kanon;
	}

	/**
	 * Return a reference to the L-diversity menu
	 * @return a reference to the L-diversity menu
	 */
	public JMenuItem getLDiv() {
		return ldiv;
	}
	
	/**
	 * Return the list of the columns where the k-anonymization will be applied
	 * @return the list of the columns where the k-anonymization will be applied
	 */
	public JTextField getKanonCols() {
		return kanonCols;
	}

	/**
	 * Return a reference to the save button
	 * @return a reference to the save button
	 */
	public JButton getSave() {
		return save;
	}
	
	@Override
	public void update(Observable observable, Object object) {
		if (object instanceof UndoManager) {
			UndoManager m = (UndoManager)object;
			
			undo.setEnabled(m.canUndo());
	        redo.setEnabled(m.canRedo());
		} else if (object instanceof String) {
			String statusLoad = (String)object;
			
			status.setText(statusLoad);
		} else if (object instanceof Boolean) {
			Boolean displayPolicy = (Boolean)object;
			
			if (displayPolicy) {
				splitPane.add(policyView);
			} else {
				splitPane.remove(policyView);
			}
			
			revalidate();
			repaint();
		}
		
		tree.updateUI();
		table.updateUI();
	}
	
	/**
	 * Return a reference to the obfuscation menu
	 * @return a reference to the obfuscation menu
	 */
	public JMenuItem getObfuscation() {
		return obfuscation;
	}	

	/**
	 * Get the headers of the table
	 * @return the headers of the table
	 */
	public JTableHeader getTableHeader() {
		return table.getTableHeader();
	}
	
	/**
	 * Get a reference of the table
	 * @return a reference of the table
	 */
	public JTable getTable() {
		return table;
	}
	
	/**
	 * Add the southern panel of the layout
	 * @return The panel to be add at the south of the layout
	 */
	private JPanel addSouth() {
		JPanel panelSouth = new JPanel();
		panelSouth.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		status = new JLabel(" ");
		panelSouth.add(status);
		
		return panelSouth;
	}
	

	/**
	 * Get a reference of the popup
	 * @return a reference of the popup
	 */
	public JPopupMenu getPopup() {
		return popup;
	}
	
	/**
	 * Get a reference of the open menu
	 * @return a reference of the open menu
	 */
	public JMenuItem getOpen() {
		return open;
	}
	
	/**
	 * Get a reference of the save menu
	 * @return a reference of the save menu
	 */
	public JMenuItem getSaveMenu() {
		return saveMenu;
	}
	
	/**
	 * Get a reference of the quit menu
	 * @return a reference of the quit menu
	 */
	public JMenuItem getQuit() {
		return quit;
	}

	/**
	 * Get a reference of the undo menu
	 * @return a reference of the undo menu
	 */
	public JMenuItem getUndo() {
		return undo;
	}

	/**
	 * Get a reference of the redo menu
	 * @return a reference of the redo menu
	 */
	public JMenuItem getRedo() {
		return redo;
	}
	
	public JMenuItem getAnonymizationPolicy() {
		return anonymizationPolicy;
	}

	/**
	 * Create the menu bar which will be added.
	 * @return The menu bar to be added
	 */
	public JMenuBar addMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu file = new JMenu("Fichier");
		JMenu edition = new JMenu("Edition");
		JMenu measureTools = new JMenu("Outils de mesure");
		JMenu view = new JMenu("Vue");
		JMenu questionMark = new JMenu("?");
		
		file.add(open);
		file.add(saveMenu);
		file.addSeparator();
		file.add(quit);
		
		measureTools.add(kanon);
		measureTools.add(ldiv);
		
		edition.add(undo);
		edition.add(redo);
		
		view.add(anonymizationPolicy);
		
		questionMark.add(aPropos);
		
		menuBar.add(file);
		menuBar.add(edition);
		menuBar.add(measureTools);
		menuBar.add(anonymization);
		menuBar.add(view);
		menuBar.add(questionMark);
		
		return menuBar;
	}

	private JTable table;
	
	private JSplitPane splitPane;
	
	private JScrollPane jPaneTable;
	private JScrollPane policyView;
	
	private JMenuItem open;
	private JMenuItem saveMenu;
	private JMenuItem quit;
	private JCheckBoxMenuItem anonymizationPolicy;
	private JMenu anonymization;
	
	private JMenuItem kanon;
	private JMenuItem ldiv;
	private JTextField kanonCols;

	private JButton save;

	private JTree tree;
	
	private JMenuItem obfuscation;
	
	private JPopupMenu popup;
	
	private HashMap<JMenuItem, IVisitor> visitors;
	
	private JMenuItem undo;
	
	private JMenuItem redo;
	
	private JMenuItem aPropos;
	
	private JLabel status;
}
