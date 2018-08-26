package moe.hilaryoi.filemanager.gui;

import moe.hilaryoi.filemanager.FileManager;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

public class ItemList extends JPanel {

	private JTable table;
	private JTextField filterField;

	TableRowSorter sorter;

	public ItemList (FileManager fm) throws SQLException {

		table = new JTable ();

		table = new JTable (fm.getDatabase ().getTable (), fm.getDatabase ().getColumns ());

		table.setDefaultEditor (Object.class, null);
		table.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);

		table.getSelectionModel ().addListSelectionListener (e -> {
			if (e.getValueIsAdjusting () && table.getSelectedRow () != -1) fm.displayItem (getSelectedPath ());

		});

		table.addMouseListener (new MouseAdapter () {

			public void mouseClicked (MouseEvent e) { if (e.getClickCount () == 2) fm.openFile (getSelectedPath ()); }

		});

		//

		sorter = new TableRowSorter (table.getModel ());
		table.setRowSorter (sorter);

		filterField = new JTextField ();
		filterField.addActionListener (e -> sort (filterField.getText ()));

		//

		setLayout (new BorderLayout ());

		add (new JScrollPane (table), BorderLayout.CENTER);
		add (filterField, BorderLayout.NORTH);

	}

	public void sort (String regex) {

		System.out.println (regex);
		sorter.setRowFilter (RowFilter.regexFilter (regex, 0, 2));

	}

	public String getSelectedPath () { return (String) table.getValueAt (table.getSelectedRow (), 1); }

	public void updateCurrTitle (String title) { table.setValueAt (title, table.getSelectedRow (), 0);}

}
