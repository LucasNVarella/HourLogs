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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

@SuppressWarnings("serial")
public class DeleteReport extends JFrame {
	
	private static final String DELETE_SUCCESS = "Entry Deletion Successful";
	
	private Runnable searchReportsThread = new Runnable() {
		@Override
		public void run() {
			reports = DatabaseManager.getReports(cbxSelectVolunteer.getSelectedItem().toString());
			if (reports == null) cbxSelectReport.setModel(new DefaultComboBoxModel<String>(new String[] {}));
			else {
				String[] dates = new String[reports.length];
				for (int i = 0; i < dates.length; i++) dates[i] = reports[i].getDate();
				cbxSelectReport.setModel(new DefaultComboBoxModel<String>(dates));
			}
		}
	};
	
	private Runnable deleteThread = new Runnable() {
		@Override
		public void run() {
			if (DatabaseManager.deleteReport(reports[cbxSelectReport.getSelectedIndex()].getId())) {
				parent.setTitle(MainWindow.Status.ENTRY_DELETE_SUCCESS);
				parent.updateRoot(DELETE_SUCCESS);
				JOptionPane.showMessageDialog(parent, "Entry Deletion Successful.");
			} else JOptionPane.showMessageDialog(parent, "Entry Deletion was UNSUCCESFUL.\nEntry may or may not have been deleted.");
		}
	};

	private Report[] reports;
	
	private MainWindow parent;
	private JComboBox<String> cbxSelectVolunteer;
	private JComboBox<String> cbxSelectReport;
	
	public DeleteReport(MainWindow parent) {
		setIconImage(null);
		setMaximumSize(new Dimension(400, 195));
		setTitle("Delete Entry");
		this.parent = parent;
		setResizable(false);
		setMinimumSize(new Dimension(400, 195));
		setPreferredSize(new Dimension(400, 195));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Point parentLocation = parent.getLocationOnScreen();
		this.setLocation((int)Math.round(parentLocation.getX())+650, (int)Math.round(parentLocation.getY())+100);
		
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		Component rigidArea = Box.createRigidArea(new Dimension(400, 15));
		rigidArea.setBounds(0, 0, 394, 15);
		contentPane.add(rigidArea);
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBounds(22, 15, 350, 30);
		contentPane.add(horizontalBox);
		
		JLabel lblSelectVolunteer = new JLabel("Select a Volunteer:");
		lblSelectVolunteer.setAlignmentX(0.5f);
		horizontalBox.add(lblSelectVolunteer);
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(30, 30));
		rigidArea_1.setMaximumSize(new Dimension(20, 30));
		horizontalBox.add(rigidArea_1);
		
		Volunteer[] volunteers = DatabaseManager.getVolunteers();
		String[] names = new String[volunteers.length];
		for (int i = 0; i < volunteers.length; i++) names[i] = volunteers[i].getName();
		
		cbxSelectVolunteer = new JComboBox<String>(new DefaultComboBoxModel<String>(names));
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
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(400, 15));
		rigidArea_2.setBounds(0, 45, 394, 15);
		contentPane.add(rigidArea_2);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setBounds(22, 60, 350, 30);
		contentPane.add(horizontalBox_1);
		
		JLabel lblSelectReport = new JLabel("Select a Report:");
		lblSelectReport.setAlignmentX(0.5f);
		horizontalBox_1.add(lblSelectReport);
		
		Component rigidArea_3 = Box.createRigidArea(new Dimension(37, 30));
		horizontalBox_1.add(rigidArea_3);
		
		cbxSelectReport = new JComboBox<String>();
		cbxSelectReport.setSelectedIndex(-1);
		cbxSelectReport.setMaximumSize(new Dimension(220, 30));
		cbxSelectReport.setMaximumRowCount(16);
		cbxSelectReport.setEditable(true);
		cbxSelectReport.setDoubleBuffered(true);
		AutoCompletion.enable(cbxSelectReport);
		horizontalBox_1.add(cbxSelectReport);
		
		Component rigidArea_4 = Box.createRigidArea(new Dimension(400, 15));
		rigidArea_4.setBounds(0, 90, 394, 15);
		contentPane.add(rigidArea_4);
		
		JSeparator separator = new JSeparator();
		separator.setOpaque(true);
		separator.setMaximumSize(new Dimension(360, 1));
		separator.setForeground(Color.BLACK);
		separator.setBackground(Color.BLACK);
		separator.setBounds(17, 103, 360, 1);
		contentPane.add(separator);
		
		Component rigidArea_5 = Box.createRigidArea(new Dimension(400, 15));
		rigidArea_5.setBounds(0, 104, 394, 15);
		contentPane.add(rigidArea_5);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		horizontalBox_2.setMaximumSize(new Dimension(380, 30));
		horizontalBox_2.setBounds(7, 119, 380, 30);
		contentPane.add(horizontalBox_2);
		
		Component rigidArea_6 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_2.add(rigidArea_6);
		
		JButton btnCancel = new JButton("CANCEL");
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				dispose();
			}
		});
		btnCancel.setMaximumSize(new Dimension(80, 30));
		horizontalBox_2.add(btnCancel);
		
		Component rigidArea_7 = Box.createRigidArea(new Dimension(170, 20));
		rigidArea_7.setMaximumSize(new Dimension(180, 20));
		horizontalBox_2.add(rigidArea_7);
		
		JButton btnDelete = new JButton("DELETE");
		btnDelete.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (cbxSelectVolunteer.getSelectedIndex() < 0) return;
				int result = JOptionPane.showConfirmDialog(parent,
						"Are you sure you wish to delete this entry?\nThis cannot be undone.");
				switch (result) {
				case JOptionPane.CANCEL_OPTION:
				case JOptionPane.NO_OPTION:
					break;
				case JOptionPane.YES_OPTION:
					new Thread(deleteThread).start();
					dispose();
					break;
				}
			}
		});
		btnDelete.setMaximumSize(new Dimension(80, 30));
		horizontalBox_2.add(btnDelete);
		
		Component rigidArea_8 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_2.add(rigidArea_8);
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
}