package moe.hilaryoi.filemanager.gui;

import moe.hilaryoi.filemanager.FileManager;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class ItemEditor extends JPanel {

	private String originalTitle;

	private JLabel path, size, dateCreated;
	private JTextField title;
	private JButton updateBut, deleteBut;
	private TagSelection tags;

	// TODO: only used once, how to not do that?
	private FileManager fm;

	//TODO: deselect

	public ItemEditor (FileManager fm) throws SQLException {

		this.fm = fm;

		tags = new TagSelection (fm.getDatabase ().getTags ());

		// tags
		JScrollPane tagScroll = new JScrollPane (tags);
		tagScroll.getVerticalScrollBar ().setUnitIncrement (12);

		// labels, field, and but
		path = new JLabel ("path"); size = new JLabel ("size"); dateCreated = new JLabel ("date created");
		title = new JTextField ("title");
		updateBut = new JButton ("update"); deleteBut = new JButton ("delete");

		// TODO: get better way of doing this like making sure that an item is always selected maybe? or better detecting
		updateBut.setEnabled (false);
		updateBut.addActionListener (e -> runUpdates ());

		deleteBut.setEnabled (false);
		deleteBut.addActionListener (e -> fm.deleteFile (path.getText ()));

		setLayout (new BoxLayout (this, BoxLayout.PAGE_AXIS));

		add (tagScroll);
		add (path); add (title); add (size); add (dateCreated);
		add (title); add (updateBut); add (deleteBut);

		setPreferredSize (new Dimension (200, Integer.MAX_VALUE));

	}

	public void setItem (String path, String title, long size, long dateCreated, ArrayList<Integer> tagIds) {

		updateBut.setEnabled (true);
		deleteBut.setEnabled (true);

		originalTitle = title;

		this.path.setText (path); this.title.setText (title);
		this.size.setText (String.valueOf (size));
		this.dateCreated.setText (String.valueOf (dateCreated));

		tags.set (tagIds);

	}

	public void runUpdates () {

		tags.runUpdates (fm.getDatabase (), path.getText ());

		try {

			if (!originalTitle.equals (title.getText ())) {

				fm.getDatabase ().updateTitle (title.getText (), path.getText ());
				originalTitle = title.getText ();

				fm.getItemList ().updateCurrTitle (originalTitle);

			}

		}

		catch (SQLException e) { e.printStackTrace (); return; }

		System.out.println ("アプデートしました。");


	}

}
