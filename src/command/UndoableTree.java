package command;

import javax.swing.undo.AbstractUndoableEdit;

import policy.PolicyTree;

@SuppressWarnings("serial")
public class UndoableTree extends AbstractUndoableEdit {
	public UndoableTree(PolicyTree<String> oldTree, PolicyTree<String> currentTree) {
		this.oldTree = oldTree;
		this.currentTree = currentTree;
		this.newTree = new PolicyTree<String>(currentTree);
	}
	
	/**
	 * Undo the previous action on the policy tree
	 */
	public void undo() {
		super.undo();
		
		currentTree.affectValues(oldTree);
	}

	/**
	 * Redo the last canceled action on the policy tree
	 */
	public void redo() {
		super.redo();
		
		currentTree.affectValues(newTree);
	}
	
	private PolicyTree<String> oldTree;
	private PolicyTree<String> currentTree;
	private PolicyTree<String> newTree;
}
