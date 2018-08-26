package moe.hilaryoi.filemanager;

import moe.hilaryoi.filemanager.gui.ItemEditor;
import moe.hilaryoi.filemanager.gui.ItemList;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class FileManager extends JPanel {

	Database db;
	ItemEditor ie;
	ItemList il;

	Path trashPath;

	public FileManager (String path, String trashPath) {

		this.trashPath = Paths.get (trashPath);

		try { db = new Database (path); }
		catch (SQLException e) { System.err.println ("Could not load database."); e.printStackTrace (); }

		try { il = new ItemList (this); ie = new ItemEditor (this); }
		catch (SQLException e) { System.out.println ("Error while loading database."); e.printStackTrace (); }

		JSplitPane split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, il, ie); split.setResizeWeight (1);

		setLayout (new BorderLayout ());
		add (split, BorderLayout.CENTER);

	}

	public void displayItem (String path) {

		try { db.setItem (ie, path); }
		catch (SQLException e) { e.printStackTrace (); }

	}

	public void openFile (String path) {

		try { Desktop.getDesktop ().open (new File (Paths.get (db.getPath (), path).toString ())); }

		catch (IOException e) {

			System.err.println (String.format ("Could not open file `%s`.", path));
			e.printStackTrace ();

		}

	}

	public void deleteFile (String path) {

		if (JOptionPane.showConfirmDialog (null, "Are you sure you want to delete this file?") != JOptionPane.OK_OPTION) return;


		try {

			Path file = Paths.get (db.getPath (), path);
			Path toTrash = Paths.get (trashPath.toString (), System.currentTimeMillis () + file.getName (file.getNameCount () - 1).toString ());

			Files.move (file, toTrash);

			db.removeItem (path);

			il.updateCurrTitle ("-- deleted --");

			System.out.println ("Moved file.");

		}

		catch (IOException e) { e.printStackTrace (); }
		catch (SQLException e) { e.printStackTrace (); }

	}

	public void close () throws SQLException { if (db != null) db.close (); }

	public Database getDatabase () { return db; }

	public ItemList getItemList () { return il; }

}
