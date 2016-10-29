package visitor;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.Node;

public interface IVisitor {
	/**
	 * Initialize if necessary the tools for the anonymization.
	 */
	public void init();
	
	/**
	 * The name of the anonymization function
	 * @return the name of the anonymization function
	 */
	public String name();
	
	/**
	 * The anonymization function to apply at a set of data in a given column
	 * @param c  The set of data ove rwhich the anonymization function will be applied
	 * @param numCol The number of the column where this function will be applied.
	 */
	public void anonymization(Collection<Vector<String>> c, int numCol);
	
	/**
	 * The content which will be add to the policy tree after the use of the function
	 * @return A tree which will content the change to add to the tree
	 */
	public DefaultMutableTreeNode policyContent();
	
	/**
	 * Open a panel for the configuration of the algorithm
	 * @return A reference to the open panel
	 */
	public JPanel configuration();
	
	/**
	 * Return if the function is global (i.e. will be applied to all the columns) or not
	 * @return True if the function is global, false otherwise.
	 */
	public boolean isAll();
	
	/**
	 * Return the XML representation results of the anonymization policy
	 * @return string containing XML anonymization policy content
	 */
	public Node asXML();
}
