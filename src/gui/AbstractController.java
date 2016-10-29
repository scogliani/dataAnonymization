package gui;

public abstract class AbstractController {
	public AbstractController(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	/**
	 * Return the column number
	 * @return the column number
	 */
	public int getColumnNumber() {
		return columnNumber;
	}
	/**
	 * Set the column number
	 * @param columnNumber The number of the column to be set
	 */
	public void setColumnNumber(int columnNumber) {
		AbstractController.columnNumber = columnNumber;
	}

	protected Model model;
	protected View view;
	protected static int columnNumber;
}
