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

import static java.lang.String.format;

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

		ArrayList<String[]> looseTable = new ArrayList<> ();

		handleQuery (rs -> {

			String path = rs.getString ("path");

			StringBuilder tags = new StringBuilder ();

			for (int tag : getTags (path)) {

				tags.append (" "); tags.append (tag); tags.append ("t");

			}

			looseTable.add (new String[] { rs.getString ("title"), path, tags.toString () });

		}, "select * from files order by datecreated desc");

		return looseTable.toArray (new String[looseTable.size ()][looseTable.get (0).length]);

	}

	public Tag[] getTags () throws SQLException {

		ArrayList<Tag> tags = new ArrayList<> ();


		handleQuery (rs -> tags.add (new Tag (rs.getInt ("id"), rs.getString ("title"))), "select * from tags");

		return tags.toArray (new Tag[tags.size ()]);

	}

	public ArrayList<Integer> getTags (String path) throws SQLException {

		ArrayList<Integer> tagIds = new ArrayList<> ();

		handleQuery (rs -> tagIds.add (rs.getInt ("tag")), format ("select tag from tagged where path='%s'", getFilteredData (path)));

		return tagIds;

	}

	public void refreshDatabase (String[] blacklistExtensions) throws SQLException, IOException {

		System.out.println ("Starting refresh...");

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

			String sqlPath = getFilteredData (path.subpath (nameCountDir, path.getNameCount ()).toString ());

			ResultSet rs = s.executeQuery (format ("select * from files where path='%s'", sqlPath));

			if (rs.next ()) continue;

			try { addItem (path, sqlPath); }
			catch (IOException e) { System.err.printf ("Could not find file `%s`.%n", path); }

		}

		System.out.println ("Done!");


	}

	public String getFilteredData (String data) { return data.replaceAll ("'", "''"); }

	public static void getPaths (Path dir, ArrayList<Path> paths) throws IOException {

		Files.list (dir).forEach ((path) -> {

			try {

				if (Files.isDirectory (path)) getPaths (path, paths);
				else paths.add (path);

			}

			catch (IOException e) { e.printStackTrace (); }

		});

	}

	//TODO: always filter strings

	public void setItem (ItemEditor ie, String path) throws SQLException {

		// will mess up if there's duplicate items I think but there shouldn't since sql prevents it
		handleQuery (rs ->
				ie.setItem (getFilteredData (rs.getString ("path")),
					getFilteredData (rs.getString ("title")),
					rs.getLong ("size"),
					rs.getLong ("datecreated"),
					getTags (path))
			, format ("select * from files where path='%s'", getFilteredData (path)));

	}

	public void addItem (Path path, String sqlPath) throws SQLException, IOException {

		//TODO: dunno if reusing statement is best
		execute (format ("insert into files values ('%s', '%s', %s, %s)",
			sqlPath, "Untitled", Files.size (path), creationDate (path).toMillis ()));

		System.out.println ("Added " + sqlPath);

	}

	public void removeItem (String path) throws SQLException {

		String sqlPath = getFilteredData (path);

		execute (format ("delete from files where path='%s'", sqlPath),
			format ("delete from tagged where path='%s'", sqlPath));

		System.out.println ("Removed " + sqlPath + " and its tags");

	}

	public void addTag (Tag tag, String filePath) throws SQLException { execute (format ("insert into tagged (tag, path) values (%d, '%s')", tag.getId (), getFilteredData (filePath)));}

	public void removeTag (Tag tag, String filePath) throws SQLException { execute (format ("delete from tagged where tag=%d and path='%s'", tag.getId (), getFilteredData (filePath)));}

	public void updateTitle (String title, String filePath) throws SQLException { execute (format ("update files set title='%s' where path='%s'", getFilteredData (title), getFilteredData (filePath))); }

	private void execute (String... sqls) throws SQLException {

		Statement s = c.createStatement ();

		for (String sql : sqls) s.execute (sql);

		s.close ();

	}

	FileTime creationDate (Path path) {

		try {
			BasicFileAttributes a = Files.readAttributes (path, BasicFileAttributes.class);

			return a.lastModifiedTime ();
		}

		catch (IOException e) { return null; }

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


	}

	public void close () throws SQLException { c.close (); }

	public String getPath () { return stringDir; }

	interface ResultSetHandler {
		void handle (ResultSet rs) throws SQLException;

	}

	public void handleQuery (ResultSetHandler handler, String query, String... params) throws SQLException {

		Statement s = c.createStatement ();

		ResultSet rs = s.executeQuery (format (query, params));

		while (rs.next ()) handler.handle (rs);

		rs.close (); s.close ();

	}

}
