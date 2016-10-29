package gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ControllerMouse extends AbstractController implements MouseListener {

	public ControllerMouse(Model model, View view) {
		super(model, view);
		
		this.view.getTableHeader().addMouseListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			this.setColumnNumber(view.getTableHeader().columnAtPoint(e.getPoint()));
			
		    this.view.getPopup().show(e.getComponent(), e.getX(), e.getY());
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) 	{}

	@Override
	public void mousePressed(MouseEvent e) 	{}

	@Override
	public void mouseReleased(MouseEvent e) {}

}
