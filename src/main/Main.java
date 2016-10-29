package main;

import gui.Model;
import gui.View;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	public static void main(String[] args) {
		if (args.length != 1) {
			StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
		    StackTraceElement main = stack[stack.length - 1];
		    String mainClass = main.getClassName ();
		    
			System.out.println("usage: " + mainClass  + " plugins_file");
			System.exit(0);
		}
		
		plugin_file = args[0];
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				
				new View(new Model(), plugin_file);
			}
		});
	}
	
	private static String plugin_file;
}
