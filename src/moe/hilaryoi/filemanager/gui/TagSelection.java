package moe.hilaryoi.filemanager.gui;

import moe.hilaryoi.filemanager.Database;
import moe.hilaryoi.filemanager.Tag;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class TagSelection extends JComponent {

	Tag[] tags;

	public TagSelection (Tag[] tags) {

		this.tags = tags;

		setLayout (new GridLayout (0, 1));

		for (Tag tag : tags) add (tag.getCheckBox ());

	}

	public void runUpdates (Database db, String filePath) {

		for (Tag tag : tags) {

			if (tag.isUnchanged ()) continue;

			try {

				if (tag.isSelected ()) db.addTag (tag, filePath);
				else db.removeTag (tag, filePath);

			}

			catch (SQLException e) { e.printStackTrace (); }

		}

	}

	// gives list of ids that are enabled, rest are disabled.
	public void set (ArrayList<Integer> tagIds) {

		for (Tag tag : tags) {

			tag.reset ();

			for (int i = 0; i < tagIds.size (); i++) {

				if (tag.getId () == tagIds.get (i)) {

					tag.prime (); tagIds.remove (i); break; // a tag only has a single id

				}


			}

		}

		repaint ();

	}

	public ArrayList<Integer> getSelected () {

		ArrayList<Integer> tagIds = new ArrayList<> ();

		for (Tag tag : tags) if (tag.isSelected ()) tagIds.add (tag.getId ());

		return tagIds;

	}

}
