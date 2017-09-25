import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class NewProfile extends JFrame {
	
	private Runnable insertThread = new Runnable() {
		@Override
		public void run() {
			Volunteer volunteer = new Volunteer(txtName.getText(), Integer.parseInt(txtGrade.getText()));
			volunteer.setStatus(cbxActive.isSelected());
			if (!txtAddress.getText().isEmpty()) volunteer.setAddress(DatabaseManager.checkSpecialChars(txtAddress.getText()));
			if (!txtPhoneNumber.getText().isEmpty()) volunteer.setPhoneNumber(txtPhoneNumber.getText());
			if (!txtEmail.getText().isEmpty()) volunteer.setEmail(DatabaseManager.checkSpecialChars(txtEmail.getText()));
			if (!txtSchool.getText().isEmpty()) volunteer.setSchool(DatabaseManager.checkSpecialChars(txtSchool.getText()));
			if (DatabaseManager.insertVolunteers(new Volunteer[] {volunteer}) == null)
					JOptionPane.showMessageDialog(parent, "Profile creation UNSUCCESSFUL.\nNew profile may not be saved in the database.");
			else {
				parent.setTitle(MainWindow.Status.PROFILE_CREATE_SUCCESS);
				parent.reloadManualEntryVolunteers();
				JOptionPane.showMessageDialog(parent, "Profile creation Successful.");
			}
		}
	};
	
	private MainWindow parent;
	
	private JTextField txtName;
	private JTextField txtGrade;
	private JCheckBox cbxActive;
	private JTextField txtAddress;
	private JFormattedTextField txtPhoneNumber;
	private JTextField txtSchool;
	private JTextField txtEmail;
	
	public NewProfile(MainWindow parent) {
		setIconImage(null);
		setTitle("New Profile");
		this.parent = parent;
		setResizable(false);
		setMinimumSize(new Dimension(400, 425));
		setPreferredSize(new Dimension(400, 500));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Point parentLocation = parent.getLocationOnScreen();
		this.setLocation((int)Math.round(parentLocation.getX())+650, (int)Math.round(parentLocation.getY())+100);
		
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(400, 15));
		rigidArea_1.setBounds(0, 0, 394, 15);
		contentPane.add(rigidArea_1);
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setMaximumSize(new Dimension(400, 300));
		horizontalBox.setBounds(0, 16, 394, 300);
		contentPane.add(horizontalBox);
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setMinimumSize(new Dimension(250, 400));
		verticalBox.setMaximumSize(new Dimension(170, 400));
		horizontalBox.add(verticalBox);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_1);
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(20, 20));
		rigidArea_2.setPreferredSize(new Dimension(50, 20));
		rigidArea_2.setMinimumSize(new Dimension(50, 20));
		rigidArea_2.setMaximumSize(new Dimension(50, 400));
		horizontalBox_1.add(rigidArea_2);
		
		Box verticalBox_1 = Box.createVerticalBox();
		verticalBox_1.setMaximumSize(new Dimension(120, 400));
		horizontalBox_1.add(verticalBox_1);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_2);
		
		JLabel lblName = new JLabel("Name:");
		horizontalBox_2.add(lblName);
		
		Component rigidArea_3 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_3.setMaximumSize(new Dimension(400, 30));
		horizontalBox_2.add(rigidArea_3);
		
		Component rigidArea_4 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_4.setMinimumSize(new Dimension(120, 10));
		rigidArea_4.setMaximumSize(new Dimension(120, 15));
		verticalBox_1.add(rigidArea_4);
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_3);
		
		JLabel lblGrade = new JLabel("Grade:");
		horizontalBox_3.add(lblGrade);
		
		Component rigidArea_5 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_5.setMaximumSize(new Dimension(400, 30));
		horizontalBox_3.add(rigidArea_5);
		
		Component rigidArea_6 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_6.setMinimumSize(new Dimension(120, 10));
		rigidArea_6.setMaximumSize(new Dimension(120, 15));
		verticalBox_1.add(rigidArea_6);
		
		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_4);
		
		JLabel lblStatus = new JLabel("Status:");
		horizontalBox_4.add(lblStatus);
		
		Component rigidArea_7 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_7.setMaximumSize(new Dimension(400, 30));
		horizontalBox_4.add(rigidArea_7);
		
		Component rigidArea_8 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_8.setMinimumSize(new Dimension(120, 10));
		rigidArea_8.setMaximumSize(new Dimension(120, 15));
		verticalBox_1.add(rigidArea_8);
		
		Box horizontalBox_5 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_5);
		
		JLabel lblAddress = new JLabel("Address:");
		horizontalBox_5.add(lblAddress);
		
		Component rigidArea_9 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_9.setMaximumSize(new Dimension(400, 30));
		horizontalBox_5.add(rigidArea_9);
		
		Component rigidArea_10 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_10.setMinimumSize(new Dimension(120, 10));
		rigidArea_10.setMaximumSize(new Dimension(120, 15));
		verticalBox_1.add(rigidArea_10);
		
		Box horizontalBox_6 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_6);
		
		JLabel lblPhoneNumber = new JLabel("Phone Number:");
		horizontalBox_6.add(lblPhoneNumber);
		
		Component rigidArea_11 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_11.setMaximumSize(new Dimension(400, 30));
		horizontalBox_6.add(rigidArea_11);
		
		Component rigidArea_12 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_12.setMinimumSize(new Dimension(120, 10));
		rigidArea_12.setMaximumSize(new Dimension(120, 15));
		verticalBox_1.add(rigidArea_12);
		
		Box horizontalBox_7 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_7);
		
		JLabel lblSchool = new JLabel("School System:");
		horizontalBox_7.add(lblSchool);
		
		Component rigidArea_13 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_13.setMaximumSize(new Dimension(400, 30));
		horizontalBox_7.add(rigidArea_13);
		
		Component rigidArea_14 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_14.setMinimumSize(new Dimension(120, 10));
		rigidArea_14.setMaximumSize(new Dimension(120, 15));
		verticalBox_1.add(rigidArea_14);
		
		Box horizontalBox_8 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_8);
		
		JLabel lblEmail = new JLabel("Email Address:");
		horizontalBox_8.add(lblEmail);
		
		Component rigidArea_15 = Box.createRigidArea(new Dimension(20, 30));
		rigidArea_15.setMaximumSize(new Dimension(400, 30));
		horizontalBox_8.add(rigidArea_15);
		
		Box verticalBox_2 = Box.createVerticalBox();
		verticalBox_2.setMinimumSize(new Dimension(130, 400));
		verticalBox_2.setMaximumSize(new Dimension(200, 400));
		horizontalBox.add(verticalBox_2);
		
		txtName = new JTextField();
		txtName.setMaximumSize(new Dimension(200, 30));
		txtName.setColumns(10);
		verticalBox_2.add(txtName);
		
		Component rigidArea_16 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_16.setMinimumSize(new Dimension(120, 10));
		rigidArea_16.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_16);
		
		txtGrade = new JTextField();
		txtGrade.setMaximumSize(new Dimension(200, 30));
		txtGrade.setColumns(2);
		verticalBox_2.add(txtGrade);
		
		Component rigidArea_17 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_17.setMinimumSize(new Dimension(120, 10));
		rigidArea_17.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_17);
		
		cbxActive = new JCheckBox("Active");
		cbxActive.setMaximumSize(new Dimension(200, 30));
		cbxActive.setAlignmentX(0.5f);
		cbxActive.setSelected(true);
		verticalBox_2.add(cbxActive);
		
		Component rigidArea_18 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_18.setMinimumSize(new Dimension(120, 10));
		rigidArea_18.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_18);
		
		txtAddress = new JTextField();
		txtAddress.setMaximumSize(new Dimension(200, 30));
		txtAddress.setColumns(10);
		verticalBox_2.add(txtAddress);
		
		Component rigidArea_19 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_19.setMinimumSize(new Dimension(120, 10));
		rigidArea_19.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_19);
		
		txtPhoneNumber = new JFormattedTextField(DateFormatter.createFormatter("(###) ###-####"));
		txtPhoneNumber.setMaximumSize(new Dimension(200, 30));
		txtPhoneNumber.setColumns(10);
		verticalBox_2.add(txtPhoneNumber);
		
		Component rigidArea_20 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_20.setMinimumSize(new Dimension(120, 10));
		rigidArea_20.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_20);
		
		txtSchool = new JTextField();
		txtSchool.setMaximumSize(new Dimension(200, 30));
		txtSchool.setColumns(10);
		verticalBox_2.add(txtSchool);
		
		Component rigidArea_21 = Box.createRigidArea(new Dimension(150, 15));
		rigidArea_21.setMinimumSize(new Dimension(120, 10));
		rigidArea_21.setMaximumSize(new Dimension(200, 15));
		verticalBox_2.add(rigidArea_21);
		
		txtEmail = new JTextField();
		txtEmail.setMaximumSize(new Dimension(200, 30));
		txtEmail.setColumns(10);
		verticalBox_2.add(txtEmail);
		
		Component rigidArea_22 = Box.createRigidArea(new Dimension(400, 15));
		rigidArea_22.setBounds(0, 317, 394, 15);
		contentPane.add(rigidArea_22);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOpaque(true);
		separator_1.setMaximumSize(new Dimension(360, 1));
		separator_1.setForeground(Color.BLACK);
		separator_1.setBackground(Color.BLACK);
		separator_1.setBounds(17, 330, 360, 1);
		contentPane.add(separator_1);
		
		Component rigidArea_23 = Box.createRigidArea(new Dimension(400, 15));
		rigidArea_23.setBounds(0, 331, 394, 15);
		contentPane.add(rigidArea_23);
		
		Box horizontalBox_9 = Box.createHorizontalBox();
		horizontalBox_9.setAlignmentX(Component.RIGHT_ALIGNMENT);
		horizontalBox_9.setMaximumSize(new Dimension(380, 30));
		horizontalBox_9.setBounds(0, 345, 394, 30);
		contentPane.add(horizontalBox_9);
		
		Component rigidArea = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_9.add(rigidArea);
		
		JButton btnCancel = new JButton("CANCEL");
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				dispose();
			}
		});
		btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnCancel.setMaximumSize(new Dimension(80, 30));
		horizontalBox_9.add(btnCancel);
		
		Component rigidArea_24 = Box.createRigidArea(new Dimension(194, 20));
		horizontalBox_9.add(rigidArea_24);
		
		JButton btnCreate = new JButton("CREATE");
		btnCreate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (checkInput()) {
					int result = JOptionPane.showConfirmDialog(parent, "Are you sure you wish to create this profile?");
					switch (result) {
					case JOptionPane.CANCEL_OPTION:
					case JOptionPane.NO_OPTION:
						break;
					case JOptionPane.YES_OPTION:
						new Thread(insertThread).start();
						dispose();
						break;
					}
				}
			}
		});
		btnCreate.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnCreate.setMaximumSize(new Dimension(80, 30));
		horizontalBox_9.add(btnCreate);
		
		Component rigidArea_25 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_9.add(rigidArea_25);
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
		if (DatabaseManager.checkDuplicateNames(txtName.getText())) {
			JOptionPane.showMessageDialog(parent, txtName.getText() + " is a name that already exists!");
			return false;
		}
		return true;
	}
	
}