package moe.hilaryoi.filemanager;

import moe.hilaryoi.filemanager.gui.ItemEditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.*;
import java.util.ArrayList;

public class Database {

	private Connection c;
	private String stringDir;
	private int nameCountDir;

	public Database (String stringDir) throws SQLException {

		Path pathDir = Paths.get (stringDir);

		this.stringDir = pathDir.toString ();
		this.nameCountDir = pathDir.getNameCount ();

		c = DriverManager.getConnection ("jdbc:sqlite:" + Paths.get (stringDir, "filemanager.db"));

	}

	//TODO: actually implement
	private String[] cols = { "title", "path", "tags" };

	public String[] getColumns () {

		return cols;

	}

	public String[][] getTable () throws SQLException {

		Statement s = c.createStatement ();

		// is this bad because can't close the ResultSet?
		// This is bad because if it's a query with less than max it is very inefficient
		int rows = s.executeQuery ("select count (*) from files").getInt (1);

		String[][] table = new String[rows][3];

		int row = 0;

		ResultSet rs = s.executeQuery ("select * from files order by datecreated desc");

		while (rs.next ()) {

			String path = rs.getString ("path");

			StringBuilder tags = new StringBuilder ();

			for (int tag : getTags (path)) {

				tags.append (" "); tags.append (tag); tags.append ("t");

			}

			table[row++] = new String[] { rs.getString ("title"), path, tags.toString () };
		}

		rs.close (); s.close ();

		return table;

	}

	public Tag[] getTags () throws SQLException {

		ArrayList<Tag> tags = new ArrayList<> ();

		Statement s = c.createStatement ();

		ResultSet rs = s.executeQuery ("select * from tags");

		while (rs.next ()) {

			tags.add (new Tag (rs.getInt ("id"), rs.getString ("title")));

		}

		rs.close (); s.close ();

		return tags.toArray (new Tag[tags.size ()]);

	}

	public ArrayList<Integer> getTags (String path) throws SQLException {

		ArrayList<Integer> tagIds = new ArrayList<> ();

		Statement s = c.createStatement ();

		ResultSet rs = s.executeQuery (String.format ("select tag from tagged where path='%s'", path));

		while (rs.next ()) { tagIds.add (rs.getInt ("tag")); }

		rs.close ();

		s.close ();

		return tagIds;

	}

	public void refreshDatabase (String[] blacklistExtensions) throws SQLException, IOException {

		System.out.println ("Started refresh...");

		ArrayList<Path> paths = new ArrayList<> ();

		getPaths (Paths.get (stringDir), paths);

		Statement s = c.createStatement ();

		fLoop:
		for (Path path : paths) {

			for (String extension : blacklistExtensions) {

				if (path.toString ().endsWith (extension)) {

					continue fLoop;

				}

			}

			String relativePath = path.subpath (nameCountDir, path.getNameCount ()).toString ();

			ResultSet rs = s.executeQuery (String.format ("select * from files where path='%s'", relativePath));

			if (rs.next ()) continue;

			try { addItem (path, relativePath); }
			catch (IOException e) { System.err.printf ("Could not find file `%s`.", path); }

		}

		System.out.println ("Done!");


	}

	public static void getPaths (Path dir, ArrayList<Path> paths) throws IOException {

		Files.list (dir).forEach ((path) -> {

			try {

				if (Files.isDirectory (path)) getPaths (path, paths);
				else paths.add (path);

			}

			catch (IOException e) { e.printStackTrace (); }

		});

	}

	public void setItem (ItemEditor ie, String path) throws SQLException {

		Statement s = c.createStatement ();

		ResultSet rs = s.executeQuery (String.format ("select * from files where path='%s'", path));

		if (rs.next ()) ie.setItem (rs.getString ("path"), rs.getString ("title"), rs.getLong ("size"), rs.getLong ("datecreated"), getTags (path));

		rs.close (); s.close ();

	}

	// path, title, size, datecreated

	public void addItem (Path path, String relativePath) throws SQLException, IOException {

		//TODO: dunno if reusing statement is best
		executeSingle (String.format ("insert into files values ('%s', '%s', %s, %s)",
			relativePath, "Untitled", Files.size (path), creationDate (path).toMillis ()));

		System.out.println ("Added " + relativePath);

	}

	public void removeItem (String path) throws SQLException {

		Statement s = c.createStatement ();

		s.execute (String.format ("delete from files where path='%s'", path));
		s.execute (String.format ("delete from tagged where path='%s'", path));

		s.close ();

	}

	public void addTag (Tag tag, String filePath) throws SQLException { executeSingle (String.format ("insert into tagged (tag, path) values (%d, '%s')", tag.getId (), filePath));}

	public void removeTag (Tag tag, String filePath) throws SQLException { executeSingle (String.format ("delete from tagged where tag=%d and path='%s'", tag.getId (), filePath));}

	public void updateTitle (String title, String filePath) throws SQLException { executeSingle (String.format ("update files set title='%s' where path='%s'", title, filePath)); }

	private void executeSingle (String sql) throws SQLException {

		Statement s = c.createStatement ();

		s.execute (sql);

		s.close ();

	}

	FileTime creationDate (Path path) {

		try {
			BasicFileAttributes a = Files.readAttributes (path, BasicFileAttributes.class);

			return a.lastModifiedTime ();

		}

		catch (IOException e) {
			return null;

		}

	}

	public void create () throws SQLException, IOException {

		BufferedReader bf = new BufferedReader (new InputStreamReader (this.getClass ().getResourceAsStream ("/createdb.sql")));

		String line;
		StringBuilder b = new StringBuilder ();

		while ((line = bf.readLine ()) != null) {

			b.append (line);

		}

		Statement s = c.createStatement ();

		String[] statements = b.toString ().split (";");
		for (String statement : statements) s.execute (statement);

		s.close ();

		System.out.println (b.toString ());


	}

	public void close () throws SQLException { c.close (); }

	public String getPath () { return stringDir; }


}
