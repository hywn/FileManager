package moe.hilaryoi.filemanager;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.prefs.Preferences;

public class Launcher extends JPanel {

	//TODO: make all constants actual constants?

	private Preferences prefs;

	private JTextField fieldDbPath, fieldTrashPath;

	private static final String KEY_PATH_DB = "path.db";
	private static final String KEY_PATH_TRASH = "path.trash";

	public Launcher () {

		prefs = Preferences.userNodeForPackage (Launcher.class);


		JButton butLaunch, butRefresh, butCreateDb;

		fieldDbPath = new JTextField (prefs.get (KEY_PATH_DB, "path to db"), 20);
		fieldTrashPath = new JTextField (prefs.get (KEY_PATH_TRASH, "path to trash"), 20);

		butLaunch = new JButton ("launch");
		butLaunch.addActionListener ((event) -> launchFileManager (fieldDbPath.getText (), fieldTrashPath.getText ()));

		butRefresh = new JButton ("refresh");
		butRefresh.addActionListener ((event) -> refreshFiles (fieldDbPath.getText ()));

		butCreateDb = new JButton ("create");
		butCreateDb.addActionListener ((event) -> createDb (fieldDbPath.getText ()));

		add (fieldDbPath); add (fieldTrashPath); add (butLaunch); add (butRefresh); add (butCreateDb);

	}

	private void savePreferences () {

		prefs.put (KEY_PATH_DB, fieldDbPath.getText ());
		prefs.put (KEY_PATH_TRASH, fieldTrashPath.getText ());

	}

	// TODO: customise, LIST THEN ADD CONFIRMAXON
	final String[] extensions = { ".bmp", ".db", ".dbbackup", ".conf", ".tags" };

	private void refreshFiles (String pathDb) {

		savePreferences ();

		Database db;

		try { db = new Database (pathDb); }
		catch (SQLException e) { System.err.println ("Could not open database."); e.printStackTrace (); return; }

		try { db.refreshDatabase (extensions); }
		catch (Exception e) { System.err.println ("Could not refresh database."); e.printStackTrace (); }

	}

	private void launchFileManager (String pathDb, String pathTrash) {

		savePreferences ();

		JFrame f = new JFrame ("FileManager");

		FileManager files = new FileManager (pathDb, pathTrash);

		f.setSize (1200, 800);

		f.setLocationRelativeTo (null);

		f.setContentPane (files);

		f.setVisible (true);

		f.addWindowListener (new WindowAdapter () {

			public void windowClosing (WindowEvent e) {

				System.out.println ("Extiting");

				try { files.close (); }
				catch (SQLException e1) { e1.printStackTrace (); }

				System.exit (0);

			}

		});

	}

	private void createDb (String pathDb) {

		Database db;

		try { db = new Database (pathDb); }
		catch (SQLException e) { System.err.println ("Could not open empty database."); e.printStackTrace (); return; }

		try { db.create (); System.out.printf ("Created database at path `%s`.\n", db.getPath ()); db.close (); }
		catch (Exception e) {System.err.println ("Could not create empty database."); e.printStackTrace ();}


	}

}
