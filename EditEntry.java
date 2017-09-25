import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

@SuppressWarnings("serial")
public class EditEntry extends JFrame {
	
	private static final String UPDATE_SUCCESSFUL = "Entry Update Successful";
	
	private Runnable searchReportsThread = new Runnable() {
		@Override
		public void run() {
			reports = DatabaseManager.getReports(cbxSelectVolunteer.getSelectedItem().toString());
			if (reports == null) {
				cbxSelectReport.setModel(new DefaultComboBoxModel<String>(new String[] {}));
				txtHours.setText("");
				txtMinutes.setText("");
				txtDescription.setText("");
			} else {
				cbxSelectReport.setModel(new DefaultComboBoxModel(reports));
				new Thread(searchRecordsThread).start();
			}
		}
	};
	
	private Runnable searchRecordsThread = new Runnable() {
		@Override
		public void run() {
			Report report = (Report) cbxSelectReport.getSelectedItem();
			txtDate.setText(report.getDate());
			records = DatabaseManager.getRecords(report.getId());
			int minutes = Integer.valueOf(records[0].getValue());
			txtHours.setText(String.valueOf(minutes/60));
			if (minutes%60 == 0) txtMinutes.setText(" 0");
			else txtMinutes.setText(String.valueOf(minutes%60));
			txtDescription.setText(records[1].getValue());
		}
	};
	
	private Runnable updateThread = new Runnable() {
		@Override
		public void run() {
			int minutes;
			if (txtHours.getText().isEmpty() || Integer.parseInt(txtHours.getText().trim()) == 0) minutes = Integer.parseInt(txtMinutes.getText());
			else if (txtMinutes.getText().equals(" 0")) minutes = Integer.parseInt(txtHours.getText().trim())*60;
			else minutes = Integer.valueOf(txtHours.getText())*60 + Integer.valueOf(txtMinutes.getText());
			if (DatabaseManager.updateReport(DateFormatter.parse(txtDate.getText()), minutes, DatabaseManager.checkSpecialChars(txtDescription.getText()),
					reports[cbxSelectReport.getSelectedIndex()].getId())) {
				parent.setTitle(MainWindow.Status.ENTRY_UPDATE_SUCCESS);
				parent.updateRoot(UPDATE_SUCCESSFUL);
				JOptionPane.showMessageDialog(parent, "Entry Update Successful.");
			} else JOptionPane.showMessageDialog(parent, "Entry Update was UNSUCCESFUL.\nChanges may or may not have been saved.");
		}
	};
	
	private Report[] reports;
	private Record[] records;
	
	private MainWindow parent;
	private JComboBox<String> cbxSelectVolunteer;
	private JComboBox<String> cbxSelectReport;
	private JTextField txtHours;
	private JFormattedTextField txtMinutes;
	private JTextField txtDescription;
	private JLabel lblDate;
	private JFormattedTextField txtDate;
	
	public EditEntry(MainWindow parent) {
		setIconImage(null);
		setTitle("Edit Profile");
		this.parent = parent;
		setResizable(false);
		setMinimumSize(new Dimension(400, 345));
		setPreferredSize(new Dimension(400, 500));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Point parentLocation = parent.getLocationOnScreen();
		this.setLocation((int)Math.round(parentLocation.getX())+650, (int)Math.round(parentLocation.getY())+100);
		
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setBounds(0, 0, 394, 450);
		contentPane.add(verticalBox);
		
		Component rigidArea = Box.createRigidArea(new Dimension(400, 15));
		verticalBox.add(rigidArea);
		
		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);
		
		JLabel lblSelectVolunteer = new JLabel("Select a Volunteer:");
		lblSelectVolunteer.setAlignmentX(0.5f);
		horizontalBox.add(lblSelectVolunteer);
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(30, 30));
		rigidArea_1.setMaximumSize(new Dimension(20, 30));
		horizontalBox.add(rigidArea_1);
		
		Volunteer[] volunteers = DatabaseManager.getVolunteers();
		String[] names = new String[volunteers.length];
		for (int i = 0; i < volunteers.length; i++) names[i] = volunteers[i].getName();
		
		cbxSelectVolunteer = new JComboBox<String>();
		cbxSelectVolunteer.setModel(new DefaultComboBoxModel<String>(names));
		cbxSelectVolunteer.setSelectedIndex(-1);
		cbxSelectVolunteer.setMaximumSize(new Dimension(220, 30));
		cbxSelectVolunteer.setMaximumRowCount(16);
		cbxSelectVolunteer.setEditable(true);
		cbxSelectVolunteer.setDoubleBuffered(true);
		AutoCompletion.enable(cbxSelectVolunteer);
		cbxSelectVolunteer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(searchReportsThread).start();
			}
		});
		horizontalBox.add(cbxSelectVolunteer);
		
		Component rigidArea_30 = Box.createRigidArea(new Dimension(400, 15));
		verticalBox.add(rigidArea_30);
		
		Box horizontalBox_11 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_11);
		
		JLabel lblSelectReport = new JLabel("Select a Report:");
		lblSelectReport.setAlignmentX(0.5f);
		horizontalBox_11.add(lblSelectReport);
		
		Component rigidArea_29 = Box.createRigidArea(new Dimension(37, 30));
		horizontalBox_11.add(rigidArea_29);
		
		cbxSelectReport = new JComboBox<String>();
		cbxSelectReport.setSelectedIndex(-1);
		cbxSelectReport.setMaximumSize(new Dimension(220, 30));
		cbxSelectReport.setMaximumRowCount(16);
		cbxSelectReport.setEditable(true);
		cbxSelectReport.setDoubleBuffered(true);
		AutoCompletion.enable(cbxSelectReport);
		cbxSelectReport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(searchRecordsThread).start();
			}
		});
		horizontalBox_11.add(cbxSelectReport);
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(400, 15));
		verticalBox.add(rigidArea_2);
		
		JSeparator separator = new JSeparator();
		separator.setOpaque(true);
		separator.setMaximumSize(new Dimension(360, 1));
		separator.setForeground(Color.BLACK);
		separator.setBackground(Color.BLACK);
		verticalBox.add(separator);
		
		Component rigidArea_3 = Box.createRigidArea(new Dimension(400, 15));
		verticalBox.add(rigidArea_3);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setAlignmentY(Component.CENTER_ALIGNMENT);
		horizontalBox_1.setMaximumSize(new Dimension(400, 115));
		verticalBox.add(horizontalBox_1);
		
		Box verticalBox_1 = Box.createVerticalBox();
		verticalBox_1.setMinimumSize(new Dimension(250, 400));
		verticalBox_1.setMaximumSize(new Dimension(120, 400));
		horizontalBox_1.add(verticalBox_1);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_2);
		
		Component rigidArea_4 = Box.createRigidArea(new Dimension(20, 20));
		rigidArea_4.setMaximumSize(new Dimension(20, 400));
		horizontalBox_2.add(rigidArea_4);
		
		Box verticalBox_2 = Box.createVerticalBox();
		verticalBox_2.setAlignmentX(Component.CENTER_ALIGNMENT);
		verticalBox_2.setMaximumSize(new Dimension(100, 400));
		horizontalBox_2.add(verticalBox_2);
		
		Box horizontalBox_6 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_6);
		
		lblDate = new JLabel("Date:");
		horizontalBox_6.add(lblDate);
		
		Component rigidArea_11 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_11.setMaximumSize(new Dimension(400, 30));
		horizontalBox_6.add(rigidArea_11);
		
		Component rigidArea_12 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_12.setMinimumSize(new Dimension(120, 10));
		rigidArea_12.setMaximumSize(new Dimension(120, 15));
		verticalBox_2.add(rigidArea_12);
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_3);
		
		JLabel lblTime = new JLabel("Time:");
		horizontalBox_3.add(lblTime);
		
		Component rigidArea_5 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_5.setMaximumSize(new Dimension(400, 30));
		horizontalBox_3.add(rigidArea_5);
		
		Component rigidArea_6 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_6.setMinimumSize(new Dimension(120, 10));
		rigidArea_6.setMaximumSize(new Dimension(120, 15));
		verticalBox_2.add(rigidArea_6);
		
		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_4);
		
		JLabel lblDescription = new JLabel("Description:");
		horizontalBox_4.add(lblDescription);
		
		Component rigidArea_7 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_7.setMaximumSize(new Dimension(400, 30));
		horizontalBox_4.add(rigidArea_7);
		
		Box verticalBox_3 = Box.createVerticalBox();
		verticalBox_3.setAlignmentX(Component.CENTER_ALIGNMENT);
		verticalBox_3.setMinimumSize(new Dimension(130, 400));
		verticalBox_3.setMaximumSize(new Dimension(250, 400));
		horizontalBox_1.add(verticalBox_3);
		
		txtDate = new JFormattedTextField(DateFormatter.getFormat());
		txtDate.setToolTipText("mm/dd/yy");
		txtDate.setMinimumSize(new Dimension(175, 30));
		txtDate.setMaximumSize(new Dimension(175, 30));
		txtDate.setColumns(10);
		verticalBox_3.add(txtDate);
		
		Component rigidArea_13 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_13.setMinimumSize(new Dimension(120, 10));
		rigidArea_13.setMaximumSize(new Dimension(200, 15));
		verticalBox_3.add(rigidArea_13);
		
		Box horizontalBox_5 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_5);
		
		txtHours = new JTextField();
		horizontalBox_5.add(txtHours);
		txtHours.setMaximumSize(new Dimension(50, 30));
		txtHours.setColumns(2);
		
		Component rigidArea_9 = Box.createRigidArea(new Dimension(5, 20));
		horizontalBox_5.add(rigidArea_9);
		
		JLabel lblHours = new JLabel("Hours");
		horizontalBox_5.add(lblHours);
		
		Component rigidArea_8 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_5.add(rigidArea_8);
		
		txtMinutes = new JFormattedTextField(DateFormatter.createFormatter("*0"));
		((MaskFormatter) txtMinutes.getFormatter()).setValidCharacters("36 ");
		txtMinutes.setMaximumSize(new Dimension(50, 30));
		txtMinutes.setColumns(2);
		horizontalBox_5.add(txtMinutes);
		
		Component rigidArea_10 = Box.createRigidArea(new Dimension(5, 20));
		horizontalBox_5.add(rigidArea_10);
		
		JLabel lblMinutes = new JLabel("Minutes");
		horizontalBox_5.add(lblMinutes);
		
		Component rigidArea_18 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_18.setMinimumSize(new Dimension(120, 10));
		rigidArea_18.setMaximumSize(new Dimension(200, 15));
		verticalBox_3.add(rigidArea_18);
		
		txtDescription = 	new JTextField();
		txtDescription.setMaximumSize(new Dimension(300, 30));
		txtDescription.setColumns(2);
		verticalBox_3.add(txtDescription);
		
		Component rigidArea_24 = Box.createRigidArea(new Dimension(400, 15));
		verticalBox.add(rigidArea_24);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOpaque(true);
		separator_1.setMaximumSize(new Dimension(360, 1));
		separator_1.setForeground(Color.BLACK);
		separator_1.setBackground(Color.BLACK);
		verticalBox.add(separator_1);
		
		Component rigidArea_25 = Box.createRigidArea(new Dimension(400, 15));
		verticalBox.add(rigidArea_25);
		
		Box horizontalBox_10 = Box.createHorizontalBox();
		horizontalBox_10.setMaximumSize(new Dimension(380, 30));
		verticalBox.add(horizontalBox_10);
		
		Component rigidArea_26 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_10.add(rigidArea_26);
		
		JButton btnCancel = new JButton("CANCEL");
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				dispose();
			}
		});
		btnCancel.setMaximumSize(new Dimension(80, 30));
		horizontalBox_10.add(btnCancel);
		
		Component rigidArea_27 = Box.createRigidArea(new Dimension(170, 20));
		rigidArea_27.setMaximumSize(new Dimension(180, 20));
		horizontalBox_10.add(rigidArea_27);
		
		JButton btnUpdate = new JButton("UPDATE");
		btnUpdate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (cbxSelectVolunteer.getSelectedIndex() < 0) return;
				if (cbxSelectReport.getSelectedIndex() < 0) return;
				if (checkInput()) {
					int result = JOptionPane.showConfirmDialog(parent,
							"Are you sure you wish to update this entry?\nThis cannot be undone.");
					switch (result) {
					case JOptionPane.CANCEL_OPTION:
					case JOptionPane.NO_OPTION:
						break;
					case JOptionPane.YES_OPTION:
						new Thread(updateThread).start();
						dispose();
						break;
					}
				}
			}
		});
		btnUpdate.setMaximumSize(new Dimension(80, 30));
		horizontalBox_10.add(btnUpdate);
		
		Component rigidArea_28 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_10.add(rigidArea_28);
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	private boolean checkInput() {
		if (!DateFormatter.isNumeric(txtHours.getText().trim()) && txtMinutes.getText().equals(" 0")) {
			JOptionPane.showMessageDialog(parent, "This entry must have a valid amount of time, either minutes or hours or both.");
			return false;
		}
		if (txtDescription.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(parent, "This entry must have a description.");
			return false;
		}
		return true;
	}
	
}