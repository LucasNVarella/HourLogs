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
import javax.swing.JCheckBox;
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
public class EditProfile extends JFrame {
	
	private static final String UPDATE_SUCCESSFUL = "Profile Update Successful";
	
	private Runnable searchThread = new Runnable() {
		@Override
		public void run() {
			volunteer = DatabaseManager.getVolunteer(cbxVolunteer.getSelectedItem().toString());
			txtName.setText(volunteer.getName());
			txtGrade.setText(String.valueOf(volunteer.getGrade()));
			cbxActive.setSelected(volunteer.getStatus());
			txtAddress.setText(volunteer.getAddress());
			txtSchool.setText(volunteer.getSchool());
			txtPhoneNumber.setText(volunteer.getPhoneNumber());
			txtEmail.setText(volunteer.getEmail());
		}
	};
	
	private Runnable updateThread = new Runnable() {
		@Override
		public void run() {
			volunteer.setName(DatabaseManager.checkSpecialChars(txtName.getText()));
			volunteer.setGrade(Integer.parseInt(txtGrade.getText()));
			volunteer.setStatus(cbxActive.isSelected());
			volunteer.setAddress(DatabaseManager.checkSpecialChars(txtAddress.getText()));
			volunteer.setSchool(DatabaseManager.checkSpecialChars(txtSchool.getText()));
			volunteer.setEmail(DatabaseManager.checkSpecialChars(txtEmail.getText()));
			volunteer.setPhoneNumber(txtPhoneNumber.getText());
			if (DatabaseManager.updateVolunteer(volunteer)) {
				parent.setTitle(MainWindow.Status.PROFILE_UPDATE_SUCCESS);
				parent.updateRoot(UPDATE_SUCCESSFUL);
				parent.reloadManualEntryVolunteers();
				JOptionPane.showMessageDialog(parent, "Profile Update Successful.");
			} else JOptionPane.showMessageDialog(parent, "Profile Update was UNSUCCESFUL.\nChanges may or may not have been saved.");
		}
	};
	
	private Volunteer volunteer;
	
	private MainWindow parent;
	
	private JComboBox<String> cbxVolunteer;
	private JTextField txtName;
	private JTextField txtGrade;
	private JTextField txtAddress;
	private JFormattedTextField txtPhoneNumber;
	private JTextField txtSchool;
	private JTextField txtEmail;
	private JCheckBox cbxActive;
	
	public EditProfile(MainWindow parent) {
		setIconImage(null);
		setTitle("Edit Profile");
		this.parent = parent;
		setResizable(false);
		setMinimumSize(new Dimension(400, 490));
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
		
		JLabel lblSelectAVolunteer = new JLabel("Select a Volunteer:");
		lblSelectAVolunteer.setAlignmentX(Component.CENTER_ALIGNMENT);
		horizontalBox.add(lblSelectAVolunteer);
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(30, 30));
		rigidArea_1.setMaximumSize(new Dimension(20, 30));
		horizontalBox.add(rigidArea_1);
		
		Volunteer[] volunteers = DatabaseManager.getVolunteers();
		String[] names = new String[volunteers.length];
		for (int i = 0; i < volunteers.length; i++) names[i] = volunteers[i].getName();
		
		cbxVolunteer = new JComboBox<>();
		cbxVolunteer.setModel(new DefaultComboBoxModel<String>(names));
		cbxVolunteer.setMaximumRowCount(16);
		cbxVolunteer.setEditable(true);
		cbxVolunteer.setDoubleBuffered(true);
		cbxVolunteer.setMaximumSize(new Dimension(220, 30));
		AutoCompletion.enable(cbxVolunteer);
		cbxVolunteer.setSelectedIndex(-1);
		cbxVolunteer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(searchThread).start();
			}
		});
		horizontalBox.add(cbxVolunteer);
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(400, 15));
		verticalBox.add(rigidArea_2);
		
		JSeparator separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		separator.setOpaque(true);
		separator.setMaximumSize(new Dimension(360, 1));
		separator.setBackground(Color.BLACK);
		verticalBox.add(separator);
		
		Component rigidArea_3 = Box.createRigidArea(new Dimension(400, 15));
		verticalBox.add(rigidArea_3);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setMaximumSize(new Dimension(400, 300));
		verticalBox.add(horizontalBox_1);
		
		Box verticalBox_1 = Box.createVerticalBox();
		verticalBox_1.setMinimumSize(new Dimension(250, 400));
		verticalBox_1.setMaximumSize(new Dimension(170, 400));
		horizontalBox_1.add(verticalBox_1);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_2);
		
		Component rigidArea_4 = Box.createRigidArea(new Dimension(20, 20));
		rigidArea_4.setPreferredSize(new Dimension(50, 20));
		rigidArea_4.setMinimumSize(new Dimension(50, 20));
		rigidArea_4.setMaximumSize(new Dimension(50, 400));
		horizontalBox_2.add(rigidArea_4);
		
		Box verticalBox_3 = Box.createVerticalBox();
		verticalBox_3.setMaximumSize(new Dimension(120, 400));
		horizontalBox_2.add(verticalBox_3);
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_3);
		
		JLabel lblName = new JLabel("Name:");
		horizontalBox_3.add(lblName);
		
		Component rigidArea_5 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_5.setMaximumSize(new Dimension(400, 30));
		horizontalBox_3.add(rigidArea_5);
		
		Component rigidArea_6 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_6.setMaximumSize(new Dimension(120, 15));
		rigidArea_6.setMinimumSize(new Dimension(120, 10));
		verticalBox_3.add(rigidArea_6);
		
		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_4);
		
		JLabel lblGrade = new JLabel("Grade:");
		horizontalBox_4.add(lblGrade);
		
		Component rigidArea_7 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_7.setMaximumSize(new Dimension(400, 30));
		horizontalBox_4.add(rigidArea_7);
		
		Component rigidArea_8 = Box.createRigidArea(new Dimension(150, 15));
		verticalBox_3.add(rigidArea_8);
		rigidArea_8.setMinimumSize(new Dimension(120, 10));
		rigidArea_8.setMaximumSize(new Dimension(120, 15));
		
		Box horizontalBox_5 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_5);
		
		JLabel lblStatus = new JLabel("Status:");
		horizontalBox_5.add(lblStatus);
		
		Component rigidArea_9 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_9.setMaximumSize(new Dimension(400, 30));
		horizontalBox_5.add(rigidArea_9);
		
		Component rigidArea_10 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_10.setMinimumSize(new Dimension(120, 10));
		rigidArea_10.setMaximumSize(new Dimension(120, 15));
		verticalBox_3.add(rigidArea_10);
		
		Box horizontalBox_6 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_6);
		
		JLabel lblAddress = new JLabel("Address:");
		horizontalBox_6.add(lblAddress);
		
		Component rigidArea_11 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_11.setMaximumSize(new Dimension(400, 30));
		horizontalBox_6.add(rigidArea_11);
		
		Component rigidArea_12 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_12.setMinimumSize(new Dimension(120, 10));
		rigidArea_12.setMaximumSize(new Dimension(120, 15));
		verticalBox_3.add(rigidArea_12);
		
		Box horizontalBox_7 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_7);
		
		JLabel lblPhoneNumber = new JLabel("Phone Number:");
		horizontalBox_7.add(lblPhoneNumber);
		
		Component rigidArea_13 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_13.setMaximumSize(new Dimension(400, 30));
		horizontalBox_7.add(rigidArea_13);
		
		Component rigidArea_14 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_14.setMinimumSize(new Dimension(120, 10));
		rigidArea_14.setMaximumSize(new Dimension(120, 15));
		verticalBox_3.add(rigidArea_14);
		
		Box horizontalBox_8 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_8);
		
		JLabel lblSchool = new JLabel("School System:");
		horizontalBox_8.add(lblSchool);
		
		Component rigidArea_15 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_15.setMaximumSize(new Dimension(400, 30));
		horizontalBox_8.add(rigidArea_15);
		
		Component rigidArea_16 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_16.setMinimumSize(new Dimension(120, 10));
		rigidArea_16.setMaximumSize(new Dimension(120, 15));
		verticalBox_3.add(rigidArea_16);
		
		Box horizontalBox_9 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_9);
		
		JLabel lblEmailAddress = new JLabel("Email Address:");
		horizontalBox_9.add(lblEmailAddress);
		
		Component rigidArea_17 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_17.setMaximumSize(new Dimension(400, 30));
		horizontalBox_9.add(rigidArea_17);
		
		Box verticalBox_2 = Box.createVerticalBox();
		verticalBox_2.setMinimumSize(new Dimension(130, 400));
		verticalBox_2.setMaximumSize(new Dimension(200, 400));
		horizontalBox_1.add(verticalBox_2);
		
		txtName = new JTextField();
		verticalBox_2.add(txtName);
		txtName.setMaximumSize(new Dimension(200, 30));
		txtName.setColumns(10);
		
		Component rigidArea_18 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_18.setMinimumSize(new Dimension(120, 10));
		rigidArea_18.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_18);
		
		txtGrade = new JTextField();
		txtGrade.setMaximumSize(new Dimension(200, 30));
		txtGrade.setColumns(2);
		verticalBox_2.add(txtGrade);
		
		Component rigidArea_19 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_19.setMinimumSize(new Dimension(120, 10));
		rigidArea_19.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_19);
		
		cbxActive = new JCheckBox("Active");
		cbxActive.setMaximumSize(new Dimension(200, 30));
		cbxActive.setAlignmentX(Component.CENTER_ALIGNMENT);
		verticalBox_2.add(cbxActive);
		
		Component rigidArea_20 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_20.setMinimumSize(new Dimension(120, 10));
		rigidArea_20.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_20);
		
		txtAddress = new JTextField();
		txtAddress.setMaximumSize(new Dimension(200, 30));
		txtAddress.setColumns(10);
		verticalBox_2.add(txtAddress);
		
		Component rigidArea_21 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_21.setMinimumSize(new Dimension(120, 10));
		rigidArea_21.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_21);
		
		txtPhoneNumber = new JFormattedTextField(DateFormatter.createFormatter("(AAA) AAA-AAAA"));
		((MaskFormatter) txtPhoneNumber.getFormatter()).setValidCharacters("0123456789 ");
		txtPhoneNumber.setMaximumSize(new Dimension(200, 30));
		txtPhoneNumber.setColumns(10);
		verticalBox_2.add(txtPhoneNumber);
		
		Component rigidArea_22 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_22.setMinimumSize(new Dimension(120, 10));
		rigidArea_22.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_22);
		
		txtSchool = new JTextField();
		txtSchool.setMaximumSize(new Dimension(200, 30));
		txtSchool.setColumns(10);
		verticalBox_2.add(txtSchool);
		
		Component rigidArea_23 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_23.setMinimumSize(new Dimension(120, 10));
		rigidArea_23.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_23);
		
		txtEmail = new JTextField();
		txtEmail.setMaximumSize(new Dimension(200, 30));
		txtEmail.setColumns(10);
		verticalBox_2.add(txtEmail);
		
		Component rigidArea_24 = Box.createRigidArea(new Dimension(400, 15));
		verticalBox.add(rigidArea_24);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOpaque(true);
		separator_1.setMaximumSize(new Dimension(360, 1));
		separator_1.setForeground(Color.BLACK);
		separator_1.setBackground(Color.BLACK);
		verticalBox.add(separator_1);
		
		Component rigidArea_28 = Box.createRigidArea(new Dimension(400, 15));
		verticalBox.add(rigidArea_28);
		
		Box horizontalBox_10 = Box.createHorizontalBox();
		horizontalBox_10.setMaximumSize(new Dimension(380, 30));
		verticalBox.add(horizontalBox_10);
		
		Component rigidArea_25 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_10.add(rigidArea_25);
		
		JButton btnCancel = new JButton("CANCEL");
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				dispose();
			}
		});
		btnCancel.setMaximumSize(new Dimension(80, 30));
		horizontalBox_10.add(btnCancel);
		
		Component rigidArea_26 = Box.createRigidArea(new Dimension(170, 20));
		rigidArea_26.setMaximumSize(new Dimension(180, 20));
		horizontalBox_10.add(rigidArea_26);
		
		JButton btnUpdate = new JButton("UPDATE");
		btnUpdate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (cbxVolunteer.getSelectedIndex() < 0) return;
				if (checkInput()) {
					int result = JOptionPane.showConfirmDialog(parent,
							"Are you sure you wish to update this profile?\nThis cannot be undone.");
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
		
		Component rigidArea_27 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_10.add(rigidArea_27);
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	private boolean checkInput() {
		if (txtName.getText().isEmpty()) {
			JOptionPane.showMessageDialog(parent, "This profile must have a name.");
			return false;
		}
		if (txtGrade.getText().isEmpty()) {
			JOptionPane.showMessageDialog(parent, "This profile must have a grade.");
			return false;
		}
		if (!DateFormatter.isNumeric(txtGrade.getText())) {
			JOptionPane.showMessageDialog(parent, txtGrade.getText() + " is not a valid grade value (must be numeric)");
			return false;
		}
		if (!txtName.getText().equals(cbxVolunteer.getSelectedItem().toString()) && DatabaseManager.checkDuplicateNames(txtName.getText())) {
			JOptionPane.showMessageDialog(parent, txtName.getText() + " is a name that already exists!");
			return false;
		}
		return true;
	}
	
}