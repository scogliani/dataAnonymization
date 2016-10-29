package gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import visitor.IVisitor;
 
public class ControllerAction extends AbstractController implements ActionListener {
	public ControllerAction(Model model, View view) {
		super(model, view);
		
		for (JMenuItem key : view.getVisitors().keySet()) {
			key.addActionListener(this);
		}
		
		this.view.getUndo().addActionListener(this);
		this.view.getRedo().addActionListener(this);
		this.view.getOpen().addActionListener(this);
		this.view.getQuit().addActionListener(this);
		this.view.getAnonymizationPolicy().addActionListener(this);
		this.view.getSaveMenu().addActionListener(this);
		this.view.getKanon().addActionListener(this);
		this.view.getLDiv().addActionListener(this);
		this.view.getaPropos().addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == view.getUndo()) {
			model.undo();
		} else if (e.getSource() == view.getaPropos()) {
			JOptionPane.showConfirmDialog(null, 
					"Version 0.2\n" +
					"Application réalisée dans le cadre d'un projet ENSICAEN intitulé :\n"+
					"\"Etude et développement d’outils pour anonymiser les d’outils pour anonymiser les données\"\n" +
					"Réalisation :\n" +
					"Simon COGLIANI\n" +
					"Nicolas MARION\n" +
					"ENSICAEN, promo 2013, 3A informatique option monétique"
					, "A propos de dataAnonymization", JOptionPane.PLAIN_MESSAGE);
		} else if (e.getSource() == view.getAnonymizationPolicy()) {
			model.displayPolicy();
		} else if (e.getSource() == view.getRedo()) {
			model.redo();
		} else if (e.getSource() == view.getOpen()) {
			model.open();
		} else if (e.getSource() == view.getSaveMenu()) {
			model.saveAs();
		} else if (e.getSource() == view.getQuit()) {
			if(view.getUndo().isEnabled()) {
				JPanel tmp = new JPanel();
				tmp.add(new JLabel("Certains changements n'ont pas été enregistrés, voulez-vous sauvegarder avant de quitter ?"));
				
				int result = JOptionPane.showConfirmDialog(null, tmp,  "Sauvegarder avant de quitter", JOptionPane.YES_NO_CANCEL_OPTION);
				
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
			}	else {
				view.dispose();
			}
		} else if (e.getSource() == view.getKanon()) {
			JPanel tmp = new JPanel(new GridLayout(1,2));
			JTextField quasId  = new JTextField();
			tmp.add(new JLabel("Quasi-identifiers"));
			tmp.add(quasId);
			
			int result = JOptionPane.showConfirmDialog(null, tmp,  view.getKanon().getText(), JOptionPane.OK_CANCEL_OPTION);
			
			if (result == JOptionPane.OK_OPTION) {
				model.kanon(quasId.getText());
			}
		} else if (e.getSource() == view.getLDiv()) {
			JPanel tmp = new JPanel(new GridLayout(2,2));
			JTextField quasId  = new JTextField();
			JTextField vsens  = new JTextField();
			tmp.add(new JLabel("Quasi-identifiers"));
			tmp.add(quasId);
			tmp.add(new JLabel("Variable sensible"));
			tmp.add(vsens);
			
			int result = JOptionPane.showConfirmDialog(null, tmp,  view.getLDiv().getText(), JOptionPane.OK_CANCEL_OPTION);
			
			if (result == JOptionPane.OK_OPTION) {
				model.ldiversity(quasId.getText(), vsens.getText());
			}
		} else {
			IVisitor v = view.getVisitor((JMenuItem)e.getSource());
			SpinnerNumberModel nbLines = new SpinnerNumberModel();
			nbLines.setValue(0);
			nbLines.setStepSize(1);
			nbLines.setMinimum(0);
			nbLines.setMaximum(model.getTableModel().getRowCount());

			final JSpinner mySpinner = new JSpinner(nbLines);
			
			JPanel panel = new JPanel(new GridLayout(1, 1));
			panel.add(mySpinner);
			
			if (v.configuration() != null) {				
				int result = JOptionPane.showConfirmDialog(null, v.configuration(), v.name(), JOptionPane.OK_CANCEL_OPTION);
				
				if (result == JOptionPane.OK_OPTION) {
					
					if (v.isAll()) {
						model.anonymization(this.getColumnNumber(), v, model.getTableModel().getRowCount());
					} else {
						panel.addComponentListener(new ComponentAdapter(){    
				            public void componentShown(ComponentEvent ce){    
				            	mySpinner.requestFocusInWindow();    
				            }    
				        });
						
						result = JOptionPane.showConfirmDialog(null, panel, "Nombre de lignes", JOptionPane.OK_CANCEL_OPTION);
					
						if (result == JOptionPane.OK_OPTION) {
							model.anonymization(this.getColumnNumber(), v, (Integer)nbLines.getValue());
						} else {
							model.anonymization(this.getColumnNumber(), v, -1);
						}
					}
				}
			} else {
				if (v.isAll()) {
					model.anonymization(this.getColumnNumber(), v, model.getTableModel().getRowCount());
				} else {
					int result = JOptionPane.showConfirmDialog(null, panel, "Nombre de lignes", JOptionPane.OK_CANCEL_OPTION);
					
					if (result == JOptionPane.OK_OPTION) {
						model.anonymization(this.getColumnNumber(), v, (Integer)nbLines.getValue());
					} else {
						model.anonymization(this.getColumnNumber(), v, -1);
					}
				}
			}
		}
	}
}
