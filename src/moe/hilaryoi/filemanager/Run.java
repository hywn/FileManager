package moe.hilaryoi.filemanager;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

public class Run {

	public static void main (String[] args) {

		JFrame f = new JFrame ("FileManager Launcher");

		Launcher l = new Launcher ();

		f.setSize (1200, 800);

		f.setLocationRelativeTo (null);

		f.setContentPane (l);

		f.setVisible (true);

		f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);


	}

}