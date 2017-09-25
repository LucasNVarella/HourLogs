import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class SearchTerm extends Box {
	
	private static final long serialVersionUID = 1L;

	private String[] extraFieldParams;
	
	JComboBox<String> cbxJoinParam;
	JComboBox<String> cbxContainsParam;
	JComboBox<String> cbxFieldParam;
	JTextField txtPhrase;

	public SearchTerm() {
		super(BoxLayout.X_AXIS);
		init();
	}
	
	public SearchTerm(String[] extraFieldParams) {
		super(BoxLayout.X_AXIS);
		this.extraFieldParams = extraFieldParams;
		init();
	}
	
	public SearchTerm(Item[] extraFieldParams) {
		super(BoxLayout.X_AXIS);
		this.extraFieldParams = new String[extraFieldParams.length];
		for (int i = 0; i < extraFieldParams.length; i++) {
			this.extraFieldParams[i] = extraFieldParams[i].getName();
		}
		init();
	}
	
	private void init() {
		Component rigidArea = Box.createRigidArea(new Dimension(5, 30));
		this.add(rigidArea);
		
		cbxJoinParam = new JComboBox<String>();
		cbxJoinParam.setModel(new DefaultComboBoxModel<String>(SearchParam.joinParams));
		this.add(cbxJoinParam);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(10);
		this.add(horizontalStrut_1);
		
		cbxContainsParam = new JComboBox<String>();
		cbxContainsParam.setModel(new DefaultComboBoxModel<String>(SearchParam.containsParams));
		cbxContainsParam.setSelectedIndex(0);
		cbxContainsParam.setMaximumRowCount(2);
//		cbxContainsParam.addActionListener(new SelectionAction());
		this.add(cbxContainsParam);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		this.add(horizontalStrut_2);
		
		txtPhrase = new JTextField();
		txtPhrase.setColumns(10);
		this.add(txtPhrase);
		
		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		this.add(horizontalStrut_3);
		
		JLabel label = new JLabel(SearchParam.label);
		this.add(label);
		
		Component horizontalStrut_4 = Box.createHorizontalStrut(20);
		this.add(horizontalStrut_4);
		
		cbxFieldParam = new JComboBox<String>();
		if (extraFieldParams == null) cbxFieldParam.setModel(new DefaultComboBoxModel<String>(SearchParam.standardFieldParams));
		else cbxFieldParam.setModel(new DefaultComboBoxModel<String>(addStandardFieldParams(extraFieldParams)));
//		cbxFieldParam.addActionListener(new SelectionAction());
		this.add(cbxFieldParam);
	}

	public String getJoinParam() {
		return cbxJoinParam.getSelectedItem().toString();
	}

	public String getContainsParam() {
		return cbxContainsParam.getSelectedItem().toString();
	}

	public String getFieldParam() {
		return cbxFieldParam.getSelectedItem().toString();
	}

	public String getPhrase() {
		return txtPhrase.getText();
	}

	public void setJoinParam(String joinParam) {
		int index;
		if (SearchParam.joinParams[0].equals(joinParam)) index = 0;
		else index = 1;
		cbxJoinParam.setSelectedIndex(index);
	}

	public void setContainsParam(String containsParam) {
		int index = -1;
		for (int i = 0; i < cbxContainsParam.getItemCount(); i++) {
			if (cbxContainsParam.getItemAt(i).equals(containsParam)) index = i;
		}
		cbxContainsParam.setSelectedIndex(index);
	}

	public void setFieldParam(String fieldParam) {
		int index = -1;
		for (int i = 0; i < cbxFieldParam.getItemCount(); i++) {
			if (cbxFieldParam.getItemAt(i).equals(fieldParam)) index = i;
		}
		cbxFieldParam.setSelectedIndex(index);
	}

	public void setPhrase(String phrase) {
		txtPhrase.setText(phrase);
	}

	public static String[] addStandardFieldParams(String[] extraFieldParams) {
		boolean done = false;
		int i = 0;
		while (!done && i < extraFieldParams.length) {
			if (extraFieldParams[i].equals("Minutes")) {
				String[] extraParams = new String[extraFieldParams.length-1];
				for (int j = 0; j < extraParams.length; j++)
					extraParams[j] = extraFieldParams[j+1];
				extraFieldParams = extraParams;
				done = true;
			}
			i++;
		}
		String[] fieldParams = new String[extraFieldParams.length + SearchParam.standardFieldParams.length];
		for (int j = 0; j < SearchParam.standardFieldParams.length; j++) {
			fieldParams[j] = SearchParam.standardFieldParams[j];
		}
		for (int j = SearchParam.standardFieldParams.length; j < SearchParam.standardFieldParams.length + extraFieldParams.length; j++) {
			fieldParams[j] = extraFieldParams[j-SearchParam.standardFieldParams.length];
		}
		return fieldParams;
	}
	
	public static String[] addStandardFieldParams(Item[] extraFieldParams) {
		if (!(extraFieldParams == null)) {
			boolean done = false;
			int i = 0;
			while (!done && i < extraFieldParams.length) {
				if (extraFieldParams[i].getName().equals("Minutes")) {
					Item[] extraParams = new Item[extraFieldParams.length-1];
					for (int j = 0; j < extraParams.length; j++)
						extraParams[j] = extraFieldParams[j+1];
					extraFieldParams = extraParams;
					done = true;
				}
				i++;
			}
			String[] fieldParams = new String[extraFieldParams.length + SearchParam.standardFieldParams.length];
			for (int j = 0; j < SearchParam.standardFieldParams.length; j++) {
				fieldParams[j] = SearchParam.standardFieldParams[j];
			}
			for (int j = SearchParam.standardFieldParams.length; j < SearchParam.standardFieldParams.length + extraFieldParams.length; j++) {
				fieldParams[j] = extraFieldParams[j-SearchParam.standardFieldParams.length].toString();
			}
			return fieldParams;
		} else return SearchParam.standardFieldParams;
	}
	
	public SearchParam toSearchParam() {
		return new SearchParam(this.getJoinParam(), this.getContainsParam(), this.getFieldParam(), DatabaseManager.checkSpecialChars(this.getPhrase()));
	}
	
	private class SelectionAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			@SuppressWarnings("unchecked") // Used only with JComboBoxes of type String
			JComboBox<String> source = (JComboBox<String>) e.getSource();
			if (source.getSelectedItem().equals("To")) {
				cbxJoinParam.setEnabled(false);
				if (source.equals(cbxContainsParam)) {
					if (!cbxFieldParam.getSelectedItem().equals("Day")
							&& !cbxFieldParam.getSelectedItem().equals("Month")
							&& !cbxFieldParam.getSelectedItem().equals("Year"))
						cbxFieldParam.setSelectedIndex(9);
				} else {
					if (!cbxContainsParam.getSelectedItem().equals("From")
							&& !cbxContainsParam.getSelectedItem().equals("To"))
						cbxContainsParam.setSelectedIndex(2);
				}
			} else {
				cbxJoinParam.setEnabled(true);
				if (source.equals(cbxContainsParam)) {
					if (cbxFieldParam.getSelectedItem().equals("Day")
							|| cbxFieldParam.getSelectedItem().equals("Month")
							|| cbxFieldParam.getSelectedItem().equals("Year"))
						cbxFieldParam.setSelectedIndex(0);
				} else {
					if (cbxContainsParam.getSelectedItem().equals("From")
							|| cbxContainsParam.getSelectedItem().equals("To"))
						cbxContainsParam.setSelectedIndex(0);
				}
			}
		}
	}

}
