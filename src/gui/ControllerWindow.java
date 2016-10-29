package gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ControllerWindow extends AbstractController implements WindowListener {
	public ControllerWindow(Model model, View view) {
		super(model, view);
		
		this.view.addWindowListener(this);
	}
	
	@Override
	public void windowActivated(WindowEvent e) {}
	
	@Override
	public void windowClosed(WindowEvent e) {}
	
	@Override
	public void windowClosing(WindowEvent e) {
		if(view.getUndo().isEnabled()) {
			JPanel tmp = new JPanel();
			tmp.add(new JLabel("Certains changements n'ont pas été enregistrés, voulez-vous sauvegarder avant de quitter ?"));
			
			int result = JOptionPane.showConfirmDialog(null, tmp,  "Sauvegarder avant de quitter", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			
			if (result == JOptionPane.OK_OPTION) {
				model.saveAs();
				view.dispose();
			}
			if (result == JOptionPane.NO_OPTION) {
				view.dispose();
			}
			if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
				view.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			}
		}
		
	}
	
	@Override
	public void windowDeactivated(WindowEvent e) {}
	
	@Override
	public void windowDeiconified(WindowEvent e) {}
	
	@Override
	public void windowIconified(WindowEvent e) {}
	
	@Override
	public void windowOpened(WindowEvent e) {
		model.open();
	}
}
