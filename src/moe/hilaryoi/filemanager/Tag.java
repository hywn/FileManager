package moe.hilaryoi.filemanager;

import javax.swing.*;

public class Tag {

	private int id;
	private String title;
	private JCheckBox checkBox;
	private boolean original;

	public Tag (int id, String title) {

		this.id = id; this.title = title;

		checkBox = new JCheckBox (title);

		original = false;

	}

	public boolean isUnchanged () { return original == checkBox.isSelected (); }

	public void reset () { checkBox.setSelected (false); original = false; }

	public void prime () { checkBox.setSelected (true); original = true; }

	public JCheckBox getCheckBox () { return checkBox; }

	public boolean isSelected () { return checkBox.isSelected (); }

	public int getId () { return id; }

	public String getTitle () { return title; }

}
