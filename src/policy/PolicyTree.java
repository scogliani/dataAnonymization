package policy;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import visitor.IVisitor;

@SuppressWarnings("serial")
public class PolicyTree<T> extends DefaultMutableTreeNode implements Cloneable {
	public PolicyTree() {
		super("Policy");
		
		header = new ArrayList<DefaultMutableTreeNode>();
	}  
	
	public PolicyTree(PolicyTree<T> policyTree) {
		this();
		try {
			TransformerFactory tfactory = TransformerFactory.newInstance();
			Transformer tx   = tfactory.newTransformer();
			DOMSource source = new DOMSource(policyTree.document);
			DOMResult result = new DOMResult();
			tx.transform(source,result);
			document = (Document)result.getNode();
			
			header.addAll(policyTree.header);
			policy = (Element) policyTree.policy.cloneNode(true);
		
			copy(this, policyTree);
		} catch (CloneNotSupportedException | TransformerException e) {
			e.printStackTrace();
		}
	}
	
	public void affectValues(PolicyTree<T> policyTree) {		
		try {
			this.removeAllChildren();
			for (int i=0; i< header.size(); i++) {
				header.set(i, new DefaultMutableTreeNode(policyTree.header.get(i)));
				add(header.get(i));
			}
	
			TransformerFactory tfactory = TransformerFactory.newInstance();
			Transformer tx   = tfactory.newTransformer();
			DOMSource source = new DOMSource(policyTree.document);
			DOMResult result = new DOMResult();
			tx.transform(source,result);
			document = (Document)result.getNode();
			
			policy = (Element) policyTree.policy.cloneNode(true);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add the headers to the policy tree
	 * @param header The list of the headers which will be add to the tree
	 */
	public void addHeader(T[] header) {
		for (int i = 0; i < header.length; i++) {
			this.header.add(new DefaultMutableTreeNode(header[i]));
			add(this.header.get(i));
		}
		
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
		
			policy = document.createElement("policy");
			
			document.appendChild(policy);
			
			for(T columnIdentifier : header) {
				Element field = document.createElement("field");
				
				field.setAttribute("name", columnIdentifier.toString().trim());
				
				policy.appendChild(field);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove all the headers of the tree
	 */
	public void removeHeader() {
		this.header.removeAll(header);
	}
	
	/**
	 * Add the notification that an anonymization function had been used over the file to the tree
	 * @param visitor 
	 */
	public void add(IVisitor visitor) {
		super.add(visitor.policyContent());
		
		policy.appendChild(this.setting(visitor));
	}
	
	/**
	 * Add to a column in the policy that a new anonymization function had been used over this column
	 * @param numCol The column over which the anonymization function had been used.
	 * @param visitor
	 */
	public void add(int numCol, IVisitor visitor) {
		header.get(numCol).add(visitor.policyContent());
		
		policy.getChildNodes().item(numCol).appendChild(this.setting(visitor));
	}
	
	/**
	 * Return the headers of a given column
	 * @param numCol The column's number of the seeked header
	 * @return The header of the given column
	 */
	public DefaultMutableTreeNode getHeader(int numCol) {
		return header.get(numCol);
	}
	
	/**
	 * Return the list of the headers
	 * @return the list of the headers
	 */
	public ArrayList<DefaultMutableTreeNode> getHeaderList() {
		return header;
	}
	
	public String asXML() {
		try   {
			StringWriter str = new StringWriter();
			
			Source source = new DOMSource(document);
			Result result = new StreamResult(str);
		     
		    Transformer transfo = TransformerFactory.newInstance().newTransformer();
		     	         
		    transfo.setOutputProperty(OutputKeys.METHOD, "xml");
		    transfo.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		    transfo.setOutputProperty(OutputKeys.ENCODING, "utf-8");       
		    transfo.setOutputProperty(OutputKeys.INDENT, "yes");
		     
		    transfo.transform(source, result);
		    
		    str.close();
		    
		    return str.toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Object copy(DefaultMutableTreeNode subRoot, DefaultMutableTreeNode sourceTree) throws CloneNotSupportedException {  
		if (sourceTree == null) {  
			return subRoot;  
		}  
	   
		for (int i = 0; i < sourceTree.getChildCount(); i++) {  
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)sourceTree.getChildAt(i);  
			DefaultMutableTreeNode clone = new DefaultMutableTreeNode(child.getUserObject());  
			subRoot.add(clone);  
			copy(clone, child);  
		}  
	    
		return subRoot;  
	}
	
	private Element setting(IVisitor visitor) {
		Element anonymization = document.createElement("anonymization");
		
		Node imp = document.importNode(visitor.asXML(), true);
		
		Class<?> enclosingClass = visitor.getClass().getEnclosingClass();
		
		String className;
		String path;
		
		if (enclosingClass != null) {
		    className = enclosingClass.getName();
		    path = enclosingClass.getProtectionDomain().getCodeSource().getLocation().getPath();
		} else {
			className =  visitor.getClass().getName();
			path = visitor.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		}
		
		((Element) imp).setAttribute("class", className);			
		((Element) imp).setAttribute("path", path);
		
		anonymization.appendChild(imp);
		
		return anonymization;
	}
	
	private Element policy;
	private Document document;
	private ArrayList<DefaultMutableTreeNode> header;
}
