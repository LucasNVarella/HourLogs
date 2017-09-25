import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.MaskFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class MainWindow extends JFrame implements ActionListener, WindowListener {

	private static final String TITLE = "WPL Hour Logs";
	private static final String SEARCH_RESULTS = "Search Results";
	private static final String REPORT_RESULTS = "Report Results";
	private static final String TABLET_INPUT_RESULTS = "Tablet Input Results";
	private static final long DBL_CLICK_THRESHOLD = 250;
	
	public static final String[] reportParams = {"All Volunteers", "A Specific Description:", "A Specific Volunteer:"};
	
	protected static final class Status {
		static final String CONNECTION_SUCCESS = "Connected to Database";
		static final String CONNECTION_FAILED = "Database connection FAILED";
		static final String WORKING = "Working...";
		static final String SEARCH_SUCCESS = "Search Successful";
		static final String EMPTY_SEARCH = "No Results";
		static final String REPORT_SUCCESS = "Report Successful";
		static final String FILE_READ_SUCCESS = "Tablet File Imported Successfully";
		static final String PROFILE_UPDATE_SUCCESS = "Profile Update Successful";
		static final String PROFILE_CREATE_SUCCESS = "Profile Creation Successful";
		static final String ENTRY_UPDATE_SUCCESS = "Entry Update Successful";
		static final String ENTRY_DELETE_SUCCESS = "Entry Deletion Successful";
		static final String ENTRY_CREATE_SUCCESS = "Form Saved Successfully";
		static final String EXPORT_SUCCESS = "Export to Flash Drive Successful";
		static final String IMPORT_SUCCESS = "Import from Flash Drive Successful";
		static final String NO_USB_TO_EXPORT = "No USBs to Export to";
		static final String NO_USB_TO_IMPORT = "No USBs to Import from";
		static final String PRINT_TO_FILE_SUCCESS = "Print to Text File Successful";
		static final String NO_RESULTS_TO_PRINT = "No Results to Print";
	}
	
	protected static final class MenuOption {
		static final String ACTIONS = "Actions";
		static final String NEW_PROFILE = "New Profile";
		static final String SUPER_SERVICE = "SuperService Alert";
		static final String UPDATE_GRADES = "Update Grades";
		static final String UPDATE_TABLET_APP = "Update Tablet App...";
		static final String SEND_NAMES = "Send Names";
		static final String SEND_DESCRIPTIONS = "Send Descriptions";
		static final String SEND_KEYWORDS = "Send Keywords";
		static final String EDIT = "Edit...";
		static final String EDIT_PROFILE = "Profile";
		static final String EDIT_ENTRY = "Entry";
		static final String DELETE_ENTRY = "Delete Entry";
		static final String SET_INCOMING_DIR = "Set New Folder To Watch";
		static final String SET_OUTGOING_DIR = "Set New Folder To Save To";
		static final String SET_THRESHOLD = "Set SuperService Threshold";
		static final String BACKUPS = "Backups";
		static final String PRINT_TO_TEXT = "Print to Text File";
		static final String IMPORT_SHEETS = "Import from Google Sheets";
		static final String IMPORT_FLASH = "Import from Flash Drive";
		static final String EXPORT_FLASH = "Export to Flash Drive";
		static final String HELP = "Help";
		static final String WALKTHROUGH = "Walkthrough";
		static final String PHONE_NUMBER = "Phone Number";
		static final String EXIT = "Exit";
		static final String CLOSE = "Close WPL Hour Logs";
	}
	
	Runnable searchThread = new Runnable() {
		@Override
		public void run() {
			search();
		}
	};
	
	Runnable reportThread = new Runnable() {
		@Override
		public void run() {
			report();
		}
	};
	
	Runnable tabletInput = new Runnable() {
		@Override
		public void run() {
			while (true) {
				int reports = FileSystemWatcher.checkFolderForFile(frame);
				if (reports > 0) {
					setTitle(Status.FILE_READ_SUCCESS);
					updateRoot(TABLET_INPUT_RESULTS + " - " + reports + " Report(s) Imported");
					JOptionPane.showMessageDialog(frame, Status.FILE_READ_SUCCESS + " - " + reports + " Report(s) Imported");
				}
			}
		}
	};
	
	Runnable insertThread = new Runnable() {
		@Override
		public void run() {
			setTitle(Status.WORKING);
			ArrayList<Report> reports = new ArrayList<>();
			reports.add(new Report(DateFormatter.formatToSql(DateFormatter.parse(txtDate.getText())),
					DatabaseManager.getVolunteerId(cbxSelectVolunteer.getSelectedItem().toString())));
			int[] id = DatabaseManager.insertReports(reports);
			if (id == null) {
				JOptionPane.showMessageDialog(frame, "A problem occurred while attempting to save the form to the database. Please try again.");
				Logger.output("A problem occurred while attempting to save the form to the database.");
			} else {
				Record[] records = new Record[2];
				int minutes;
				if (txtHours.getText().isEmpty() || Integer.parseInt(txtHours.getText().trim()) == 0) minutes = Integer.parseInt(txtMins.getText());
				else if (txtMins.getText().equals(" 0")) minutes = Integer.parseInt(txtHours.getText().trim())*60;
				else minutes = Integer.valueOf(txtHours.getText())*60 + Integer.valueOf(txtMins.getText());
				records[0] = new Record(String.valueOf(minutes), DatabaseManager.getItems()[0].getId(), id[0]);
				records[1] = new Record(DatabaseManager.checkSpecialChars(txtDescription.getText()), DatabaseManager.getItems()[1].getId(), id[0]);
				DatabaseManager.insertRecords(records);
				JOptionPane.showMessageDialog(frame, "Form saved successfully.");
				setTitle(Status.ENTRY_CREATE_SUCCESS);
			}
		}
	};
	
	Thread tabletInputThread;
	
	private JPanel contentPane;
	private final JScrollPane scrollPane = new JScrollPane();
	private JScrollPane resultsPane;
	private JLabel lblReportTotal;
	private JComboBox<String> cbxContainsParam;
	private JComboBox<String> cbxFieldParam;
	private JComboBox<String> cbxReportOptions;
	private JTextField txtSearchPhrase;
	private JTextField txtReportPhrase;
	private JFormattedTextField txtSearchFrom;
	private JFormattedTextField txtSearchTo;
	private JFormattedTextField txtReportFrom;
	private JFormattedTextField txtReportTo;
	private JCheckBox chkSearchInactive;
	private JCheckBox chkReportInactive;
	Box verticalBox_1;
	private JTree tree;
	private TreeModel model;
	ArrayList<SearchParam[]> history;
	ArrayList<String> historyComplement;
	private int currentHistory = -1;
	private long timeDiff = 0;

	ArrayList<SearchTerm> searchTerms;
	Item[] extraParams;
	Volunteer[] results;
	
	private static MainWindow frame;
	private JButton btnSearch;
	private JFormattedTextField txtDate;
	private JFormattedTextField txtMins;
	private JComboBox<String> cbxSelectVolunteer;
	private JTextField txtHours;
	private JTextField txtDescription;
	private JCheckBox chkIncludeNonLogging;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		FileSystemWatcher.init();
		Logger.output("Starting application...");
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					Logger.output(e);
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		addWindowListener(this);
		if (DatabaseManager.connect()) setTitle(Status.CONNECTION_SUCCESS);
		else setTitle(Status.CONNECTION_FAILED);
		extraParams = DatabaseManager.getItems();
		
		tabletInputThread = new Thread(tabletInput, "tabletInput");
		tabletInputThread.start();
		
		history = new ArrayList<>();
		historyComplement = new ArrayList<>();
		 
		searchTerms = new ArrayList<SearchTerm>();
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(50, 50, 1200, 700);
		setIconImage(null);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnActions = new JMenu(MenuOption.ACTIONS);
		mnActions.addMenuListener(new MenuListener() {
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			@Override
			public void menuDeselected(MenuEvent e) {
				Logger.output("Actions menu closed");
			}
			@Override
			public void menuSelected(MenuEvent e) {
				Logger.output("Actions menu opened");
			}
		});
		menuBar.add(mnActions);

		JMenuItem mnSuperService = new JMenuItem(MenuOption.SUPER_SERVICE);
		mnSuperService.addActionListener(this);
		mnActions.add(mnSuperService);

		JMenuItem mnUpdateGrades = new JMenuItem(MenuOption.UPDATE_GRADES);
		mnUpdateGrades.addActionListener(this);
		mnActions.add(mnUpdateGrades);

		JMenu mnUpdateTabletApp = new JMenu(MenuOption.UPDATE_TABLET_APP);
		mnUpdateTabletApp.addMenuListener(new MenuListener() {
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			@Override
			public void menuDeselected(MenuEvent e) {
				Logger.output("Update Tablet App submenu closed");
			}
			@Override
			public void menuSelected(MenuEvent e) {
				Logger.output("Update Tablet App submenu opened");
			}
		});
		mnActions.add(mnUpdateTabletApp);

		JMenuItem mnSendNames = new JMenuItem(MenuOption.SEND_NAMES);
		mnSendNames.addActionListener(this);
		mnUpdateTabletApp.add(mnSendNames);

		JMenuItem mnSendDescriptions = new JMenuItem(MenuOption.SEND_DESCRIPTIONS);
		mnSendDescriptions.addActionListener(this);
		mnUpdateTabletApp.add(mnSendDescriptions);

		JMenuItem mnSendKeywords = new JMenuItem(MenuOption.SEND_KEYWORDS);
		mnSendKeywords.addActionListener(this);
		mnUpdateTabletApp.add(mnSendKeywords);

		JMenu mnEdit = new JMenu(MenuOption.EDIT);
		mnEdit.addMenuListener(new MenuListener() {
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			@Override
			public void menuDeselected(MenuEvent e) {
				Logger.output("Edit submenu closed");
			}
			@Override
			public void menuSelected(MenuEvent e) {
				Logger.output("Edit submenu opened");
			}
		});
		
		JMenuItem mnNewProfile = new JMenuItem(MenuOption.NEW_PROFILE);
		mnNewProfile.addActionListener(this);
		mnActions.add(mnNewProfile);
		mnActions.add(mnEdit);

		JMenuItem mnEditProfile = new JMenuItem(MenuOption.EDIT_PROFILE);
		mnEditProfile.addActionListener(this);
		mnEdit.add(mnEditProfile);

		JMenuItem mnEditEntry = new JMenuItem(MenuOption.EDIT_ENTRY);
		mnEditEntry.addActionListener(this);
		mnEdit.add(mnEditEntry);
		
		JMenuItem mnDeleteReport = new JMenuItem(MenuOption.DELETE_ENTRY);
		mnDeleteReport.addActionListener(this);
		mnActions.add(mnDeleteReport);
		
		JMenuItem mnSetIncomingDir = new JMenuItem(MenuOption.SET_INCOMING_DIR);
		mnSetIncomingDir.addActionListener(this);
		mnActions.add(mnSetIncomingDir);
		
		JMenuItem mnSetOutgoingDir = new JMenuItem(MenuOption.SET_OUTGOING_DIR);
		mnSetOutgoingDir.addActionListener(this);
		mnActions.add(mnSetOutgoingDir);
		
		JMenuItem mnSetThreshold = new JMenuItem(MenuOption.SET_THRESHOLD);
		mnSetThreshold.addActionListener(this);
		mnActions.add(mnSetThreshold);

		JMenu mnBackups = new JMenu(MenuOption.BACKUPS);
		mnBackups.addMenuListener(new MenuListener() {
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			@Override
			public void menuDeselected(MenuEvent e) {
				Logger.output("Backups menu closed");
			}
			@Override
			public void menuSelected(MenuEvent e) {
				Logger.output("Backups menu opened");
			}
		});
		menuBar.add(mnBackups);

		JMenuItem mnPrintToText = new JMenuItem(MenuOption.PRINT_TO_TEXT);
		mnPrintToText.addActionListener(this);
		mnBackups.add(mnPrintToText);

		JMenuItem mntmExportFlash = new JMenuItem(MenuOption.EXPORT_FLASH);
		mntmExportFlash.addActionListener(this);
		mnBackups.add(mntmExportFlash);

		JMenu mnHelp = new JMenu(MenuOption.HELP);
		mnHelp.addMenuListener(new MenuListener() {
			@Override
			public void menuCanceled(MenuEvent e) {
			}
			@Override
			public void menuDeselected(MenuEvent e) {
				Logger.output("Help menu closed");
			}
			@Override
			public void menuSelected(MenuEvent e) {
				Logger.output("Help menu opened");
			}
		});
		menuBar.add(mnHelp);

		JMenuItem mnWalkthrough = new JMenuItem(MenuOption.WALKTHROUGH);
		mnWalkthrough.addActionListener(this);
		mnHelp.add(mnWalkthrough);

		JMenuItem mntmPhoneNumber = new JMenuItem(MenuOption.PHONE_NUMBER);
		mntmPhoneNumber.addActionListener(this);
		mnHelp.add(mntmPhoneNumber);

		JMenu mnExit = new JMenu(MenuOption.EXIT);
		mnExit.addMenuListener(new MenuListener() {
			@Override
			public void menuCanceled(MenuEvent arg0) {
			}
			@Override
			public void menuDeselected(MenuEvent arg0) {
				Logger.output("Exit menu closed");
			}
			@Override
			public void menuSelected(MenuEvent arg0) {
				Logger.output("Exit menu opened");
			}
		});
		menuBar.add(mnExit);

		JMenuItem mnClose = new JMenuItem(MenuOption.CLOSE);
		mnClose.addActionListener(this);
		mnExit.add(mnClose);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		scrollPane.setDoubleBuffered(true);
		scrollPane.setFocusable(false);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(0, 0, 597, 639);
		contentPane.add(scrollPane);

		JPanel panel = new JPanel();
		panel.setMaximumSize(new Dimension(590, 32767));
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		scrollPane.setViewportView(panel);

		Box verticalBox = Box.createVerticalBox();
		verticalBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(verticalBox);

		Component verticalStrut = Box.createVerticalStrut(15);
		verticalStrut.setMaximumSize(new Dimension(580, 15));
		verticalBox.add(verticalStrut);

		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut);

		JLabel lblSearch = new JLabel("Search");
		lblSearch.setAlignmentY(Component.CENTER_ALIGNMENT);
		horizontalBox.add(lblSearch);
		lblSearch.setFont(new Font("Tahoma", Font.PLAIN, 20));

		Component horizontalStrut_1 = Box.createHorizontalStrut(335);
		horizontalStrut_1.setMinimumSize(new Dimension(500, 0));
		horizontalBox.add(horizontalStrut_1);
		
		JButton btnBack = new JButton("Back");
		btnBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (currentHistory > 0) cycleHistory(-1);
			}
		});
		btnBack.setMaximumSize(new Dimension(85, 25));
		btnBack.setMinimumSize(new Dimension(80, 25));
		btnBack.setPreferredSize(new Dimension(80, 25));
		horizontalBox.add(btnBack);
		
		JButton btnForward = new JButton("Forward");
		btnForward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (currentHistory < history.size()-1) cycleHistory(1);
			}
		});
		btnForward.setPreferredSize(new Dimension(80, 25));
		btnForward.setMinimumSize(new Dimension(80, 25));
		btnForward.setMaximumSize(new Dimension(85, 25));
		horizontalBox.add(btnForward);

		JSeparator separator = new JSeparator();
		separator.setMaximumSize(new Dimension(580, 32767));
		separator.setForeground(Color.BLACK);
		verticalBox.add(separator);
		separator.setOpaque(true);

		verticalBox_1 = Box.createVerticalBox();
		verticalBox_1.setMaximumSize(new Dimension(587, 45));
		verticalBox.add(verticalBox_1);

		Component verticalStrut_1 = Box.createVerticalStrut(10);
		verticalBox_1.add(verticalStrut_1);

		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setMaximumSize(new Dimension(590, 30));
		verticalBox_1.add(horizontalBox_1);

		Component rigidArea = Box.createRigidArea(new Dimension(65, 30));
		horizontalBox_1.add(rigidArea);

		cbxContainsParam = new JComboBox<String>();
		cbxContainsParam.setMaximumRowCount(2);
		cbxContainsParam.setModel(new DefaultComboBoxModel<String>(SearchParam.containsParams));
		cbxContainsParam.setSelectedIndex(0);
//		cbxContainsParam.addActionListener(new SelectionAction());
		horizontalBox_1.add(cbxContainsParam);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		horizontalBox_1.add(horizontalStrut_2);

		txtSearchPhrase = new JTextField();
		horizontalBox_1.add(txtSearchPhrase);

		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		horizontalBox_1.add(horizontalStrut_3);

		JLabel lblIn = new JLabel(SearchParam.label);
		horizontalBox_1.add(lblIn);

		Component horizontalStrut_4 = Box.createHorizontalStrut(20);
		horizontalBox_1.add(horizontalStrut_4);

		cbxFieldParam = new JComboBox<String>();
		cbxFieldParam.setModel(new DefaultComboBoxModel<String>(SearchTerm.addStandardFieldParams(extraParams)));
//		cbxFieldParam.addActionListener(new SelectionAction());
		horizontalBox_1.add(cbxFieldParam);

		Component verticalStrut_2 = Box.createVerticalStrut(3);
		verticalBox_1.add(verticalStrut_2);

		Component verticalStrut_3 = Box.createVerticalStrut(3);
		verticalStrut_3.setMaximumSize(new Dimension(500, 3));
		verticalBox.add(verticalStrut_3);

		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_3);

		JButton btnAddSearchTerm = new JButton("Add Search Term...");
		btnAddSearchTerm.setMaximumSize(new Dimension(145, 30));
		btnAddSearchTerm.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				addSearchTerm();
			}
		});
		horizontalBox_3.add(btnAddSearchTerm);

		JButton btnRemoveSearchTerm = new JButton("Remove Search Term");
		btnRemoveSearchTerm.setMaximumSize(new Dimension(157, 30));
		btnRemoveSearchTerm.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (searchTerms.size() > 0) {
					removeSearchTerm();
				}
			}
		});
		
		Component rigidArea_8 = Box.createRigidArea(new Dimension(20, 30));
		horizontalBox_3.add(rigidArea_8);
		horizontalBox_3.add(btnRemoveSearchTerm);

		Component verticalStrut_4 = Box.createVerticalStrut(25);
		verticalStrut_4.setMaximumSize(new Dimension(500, 25));
		verticalBox.add(verticalStrut_4);
				
				Box horizontalBox_2 = Box.createHorizontalBox();
				horizontalBox_2.setAlignmentY(Component.CENTER_ALIGNMENT);
				verticalBox.add(horizontalBox_2);
				
				JLabel lblSearchFrom = new JLabel("From...");
				horizontalBox_2.add(lblSearchFrom);
				lblSearchFrom.setAlignmentX(Component.RIGHT_ALIGNMENT);
				
				Component rigidArea_9 = Box.createRigidArea(new Dimension(20, 30));
				horizontalBox_2.add(rigidArea_9);
				
				txtSearchFrom = new JFormattedTextField(DateFormatter.getFormat());
				txtSearchFrom.setMinimumSize(new Dimension(45, 30));
				txtSearchFrom.setMaximumSize(new Dimension(80, 30));
				txtSearchFrom.setToolTipText("mm/dd/yy");
				txtSearchFrom.setColumns(10);
				txtSearchFrom.setText("01/01/00");
				horizontalBox_2.add(txtSearchFrom);
				
				Component horizontalStrut_6 = Box.createHorizontalStrut(25);
				horizontalBox_2.add(horizontalStrut_6);
				
				JLabel lblSearchTo = new JLabel("To...");
				lblSearchTo.setAlignmentX(1.0f);
				horizontalBox_2.add(lblSearchTo);
				
				Component horizontalStrut_7 = Box.createHorizontalStrut(20);
				horizontalBox_2.add(horizontalStrut_7);
				
				txtSearchTo = new JFormattedTextField(DateFormatter.getFormat());
				txtSearchTo.setMinimumSize(new Dimension(45, 30));
				txtSearchTo.setToolTipText("mm/dd/yy");
				txtSearchTo.setMaximumSize(new Dimension(80, 30));
				txtSearchTo.setColumns(10);
				txtSearchTo.setValue(new Date());
				horizontalBox_2.add(txtSearchTo);
				
				Component horizontalStrut_8 = Box.createHorizontalStrut(10);
				horizontalBox_2.add(horizontalStrut_8);
				
				JButton btnSearchToday = new JButton("Today");
				btnSearchToday.setMaximumSize(new Dimension(67, 30));
				btnSearchToday.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						txtSearchTo.setValue(new Date());
					}
				});
				horizontalBox_2.add(btnSearchToday);
				
				Component verticalStrut_5 = Box.createVerticalStrut(15);
				verticalStrut_5.setMaximumSize(new Dimension(500, 15));
				verticalBox.add(verticalStrut_5);
				
				Box horizontalBox_5 = Box.createHorizontalBox();
				horizontalBox_5.setAlignmentY(Component.CENTER_ALIGNMENT);
				verticalBox.add(horizontalBox_5);
				
				chkIncludeNonLogging = new JCheckBox("Include Non-Logging Volunteers");
				chkIncludeNonLogging.setMaximumSize(new Dimension(210, 30));
				horizontalBox_5.add(chkIncludeNonLogging);
				
				Component horizontalStrut_9 = Box.createHorizontalStrut(10);
				horizontalBox_5.add(horizontalStrut_9);
				
				chkSearchInactive = new JCheckBox("Include Inactive Volunteers");
				chkSearchInactive.setMaximumSize(new Dimension(183, 30));
				horizontalBox_5.add(chkSearchInactive);
				
				Component horizontalStrut_12 = Box.createHorizontalStrut(20);
				horizontalBox_5.add(horizontalStrut_12);
				
				btnSearch = new JButton("SEARCH");
				btnSearch.setPreferredSize(new Dimension(80, 30));
				btnSearch.setMinimumSize(new Dimension(80, 30));
				btnSearch.setMaximumSize(new Dimension(100, 30));
				horizontalBox_5.add(btnSearch);
				btnSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
				btnSearch.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						new Thread(searchThread).start();
					}
				});
		
		Component verticalStrut_9 = Box.createVerticalStrut(10);
		verticalStrut_9.setMaximumSize(new Dimension(500, 10));
		verticalBox.add(verticalStrut_9);
		
		Box horizontalBox_6 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_6);
		
		Component horizontalStrut_13 = Box.createHorizontalStrut(20);
		horizontalBox_6.add(horizontalStrut_13);
		
		JLabel lblHourReports = new JLabel("Hour Reports");
		lblHourReports.setFont(new Font("Tahoma", Font.PLAIN, 20));
		horizontalBox_6.add(lblHourReports);
		
		Component horizontalStrut_14 = Box.createHorizontalStrut(447);
		horizontalBox_6.add(horizontalStrut_14);

		JSeparator separator_1 = new JSeparator();
		separator_1.setMaximumSize(new Dimension(580, 32767));
		separator_1.setOpaque(true);
		separator_1.setForeground(Color.BLACK);
		verticalBox.add(separator_1);
		
		Component verticalStrut_7 = Box.createVerticalStrut(10);
		verticalStrut_7.setMaximumSize(new Dimension(500, 10));
		verticalBox.add(verticalStrut_7);
		
		Box horizontalBox_7 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_7);
		
		JLabel lblSum = new JLabel("Sum all hours for...");
		lblSum.setAlignmentX(1.0f);
		horizontalBox_7.add(lblSum);
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(20, 30));
		horizontalBox_7.add(rigidArea_1);
		
		cbxReportOptions = new JComboBox<>();
		cbxReportOptions.setMaximumSize(new Dimension(170, 30));
		cbxReportOptions.setMaximumRowCount(3);
		cbxReportOptions.addItemListener(new SelectionListener());
		cbxReportOptions.setModel(new DefaultComboBoxModel<String>(reportParams));
		horizontalBox_7.add(cbxReportOptions);
		
		Component horizontalStrut_16 = Box.createHorizontalStrut(20);
		horizontalBox_7.add(horizontalStrut_16);
		
		txtReportPhrase = new JTextField();
		txtReportPhrase.setMaximumSize(new Dimension(150, 2147483647));
		txtReportPhrase.setEnabled(false);
		horizontalBox_7.add(txtReportPhrase);
		
		Component verticalStrut_6 = Box.createVerticalStrut(20);
		verticalStrut_6.setMaximumSize(new Dimension(500, 20));
		verticalBox.add(verticalStrut_6);
		
		Box horizontalBox_8 = Box.createHorizontalBox();
		horizontalBox_8.setAlignmentY(0.5f);
		verticalBox.add(horizontalBox_8);
		
		JLabel lblReportFrom = new JLabel("From...");
		lblReportFrom.setAlignmentX(1.0f);
		horizontalBox_8.add(lblReportFrom);
		
		Component rigidArea_7 = Box.createRigidArea(new Dimension(20, 30));
		horizontalBox_8.add(rigidArea_7);
		
		txtReportFrom = new JFormattedTextField(DateFormatter.getFormat());
		txtReportFrom.setToolTipText("mm/dd/yy");
		txtReportFrom.setText("01/01/00");
		txtReportFrom.setMinimumSize(new Dimension(45, 30));
		txtReportFrom.setMaximumSize(new Dimension(80, 30));
		txtReportFrom.setColumns(10);
		horizontalBox_8.add(txtReportFrom);
		
		Component horizontalStrut_17 = Box.createHorizontalStrut(25);
		horizontalBox_8.add(horizontalStrut_17);
		
		JLabel lblReportTo = new JLabel("To...");
		lblReportTo.setAlignmentX(1.0f);
		horizontalBox_8.add(lblReportTo);
		
		Component horizontalStrut_18 = Box.createHorizontalStrut(20);
		horizontalBox_8.add(horizontalStrut_18);
		
		txtReportTo = new JFormattedTextField(DateFormatter.getFormat());
		txtReportTo.setToolTipText("mm/dd/yy");
		txtReportTo.setValue(new Date());
		txtReportTo.setMinimumSize(new Dimension(45, 30));
		txtReportTo.setMaximumSize(new Dimension(80, 30));
		txtReportTo.setColumns(10);
		horizontalBox_8.add(txtReportTo);
		
		Component horizontalStrut_19 = Box.createHorizontalStrut(10);
		horizontalBox_8.add(horizontalStrut_19);
		
		JButton btnReportToday = new JButton("Today");
		btnReportToday.setMaximumSize(new Dimension(67, 30));
		btnReportToday.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				txtReportTo.setValue(new Date());
			}
		});
		horizontalBox_8.add(btnReportToday);
		
		Component verticalStrut_8 = Box.createVerticalStrut(10);
		verticalStrut_8.setMaximumSize(new Dimension(500, 10));
		verticalBox.add(verticalStrut_8);
		
		Box horizontalBox_9 = Box.createHorizontalBox();
		horizontalBox_9.setAlignmentY(0.5f);
		verticalBox.add(horizontalBox_9);
		
		chkReportInactive = new JCheckBox("Include Inactive Volunteers");
		chkReportInactive.setMaximumSize(new Dimension(183, 30));
		chkReportInactive.setSelected(true);
		horizontalBox_9.add(chkReportInactive);
		
		Component horizontalStrut_20 = Box.createHorizontalStrut(20);
		horizontalBox_9.add(horizontalStrut_20);
		
		JButton btnCalculate = new JButton("CALCULATE");
		btnCalculate.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				new Thread(reportThread).start();
			}
		});
		btnCalculate.setPreferredSize(new Dimension(80, 30));
		btnCalculate.setMinimumSize(new Dimension(80, 30));
		btnCalculate.setMaximumSize(new Dimension(120, 30));
		btnCalculate.setAlignmentX(0.5f);
		horizontalBox_9.add(btnCalculate);
		
		Component verticalStrut_10 = Box.createVerticalStrut(10);
		verticalStrut_10.setMaximumSize(new Dimension(500, 10));
		verticalBox.add(verticalStrut_10);
		
		Box horizontalBox_10 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_10);
		
		JLabel lblTotal = new JLabel("Total:");
		horizontalBox_10.add(lblTotal);
		lblTotal.setMinimumSize(new Dimension(200, 30));
		lblTotal.setHorizontalAlignment(SwingConstants.CENTER);
		lblTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblTotal.setFont(new Font("Tahoma", Font.PLAIN, 16));
		
		Component horizontalStrut_21 = Box.createHorizontalStrut(20);
		horizontalBox_10.add(horizontalStrut_21);
		
		lblReportTotal = new JLabel("");
		lblReportTotal.setMinimumSize(new Dimension(200, 30));
		lblReportTotal.setHorizontalAlignment(SwingConstants.CENTER);
		lblReportTotal.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblReportTotal.setAlignmentX(0.5f);
		horizontalBox_10.add(lblReportTotal);
		
		Component verticalStrut_11 = Box.createVerticalStrut(10);
		verticalStrut_11.setMaximumSize(new Dimension(500, 10));
		verticalBox.add(verticalStrut_11);

		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_4);

		Component horizontalStrut_10 = Box.createHorizontalStrut(20);
		horizontalBox_4.add(horizontalStrut_10);

		JLabel lblManualEntry = new JLabel("Manual Entry");
		lblManualEntry.setFont(new Font("Tahoma", Font.PLAIN, 20));
		horizontalBox_4.add(lblManualEntry);

		Component horizontalStrut_11 = Box.createHorizontalStrut(448);
		horizontalBox_4.add(horizontalStrut_11);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setMaximumSize(new Dimension(580, 32767));
		separator_2.setOpaque(true);
		separator_2.setForeground(Color.BLACK);
		verticalBox.add(separator_2);
		
		Component verticalStrut_12 = Box.createVerticalStrut(10);
		verticalStrut_12.setMaximumSize(new Dimension(500, 10));
		verticalBox.add(verticalStrut_12);
		
		Box horizontalBox_11 = Box.createHorizontalBox();
		horizontalBox_11.setAlignmentY(Component.CENTER_ALIGNMENT);
		verticalBox.add(horizontalBox_11);
		
		Box verticalBox_2 = Box.createVerticalBox();
		verticalBox_2.setPreferredSize(new Dimension(250, 80));
		verticalBox_2.setMinimumSize(new Dimension(250, 80));
		verticalBox_2.setAlignmentX(Component.CENTER_ALIGNMENT);
		verticalBox_2.setMaximumSize(new Dimension(270, 80));
		horizontalBox_11.add(verticalBox_2);
		
		Box horizontalBox_12 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_12);
		
		JLabel lblName = new JLabel("Name:");
		horizontalBox_12.add(lblName);
		
		Component rigidArea_4 = Box.createRigidArea(new Dimension(10, 30));
		horizontalBox_12.add(rigidArea_4);
		
		cbxSelectVolunteer = new JComboBox<String>();
		cbxSelectVolunteer.setSelectedIndex(-1);
		cbxSelectVolunteer.setMaximumRowCount(5);
		cbxSelectVolunteer.setEditable(true);
		cbxSelectVolunteer.setDoubleBuffered(true);
		AutoCompletion.enable(cbxSelectVolunteer);
		cbxSelectVolunteer.setMaximumSize(new Dimension(220, 30));
		horizontalBox_12.add(cbxSelectVolunteer);
		reloadManualEntryVolunteers();
		
		Component rigidArea_3 = Box.createRigidArea(new Dimension(20, 20));
		verticalBox_2.add(rigidArea_3);
		
		Box horizontalBox_13 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_13);
		
		JLabel lblDate = new JLabel("Date:");
		horizontalBox_13.add(lblDate);
		
		Component rigidArea_5 = Box.createRigidArea(new Dimension(20, 30));
		horizontalBox_13.add(rigidArea_5);
		
		txtDate = new JFormattedTextField(DateFormatter.getFormat());
		txtDate.setToolTipText("mm/dd/yy");
		txtDate.setValue(new Date());
		txtDate.setMinimumSize(new Dimension(45, 30));
		txtDate.setMaximumSize(new Dimension(80, 30));
		txtDate.setColumns(10);
		horizontalBox_13.add(txtDate);
		
		Component horizontalStrut_5 = Box.createHorizontalStrut(10);
		horizontalBox_13.add(horizontalStrut_5);
		
		JButton btnEntryToday = new JButton("Today");
		btnEntryToday.setMaximumSize(new Dimension(67, 30));
		btnEntryToday.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				txtDate.setValue(new Date());
			}
		});
		horizontalBox_13.add(btnEntryToday);
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(10, 80));
		horizontalBox_11.add(rigidArea_2);
		
		Box verticalBox_3 = Box.createVerticalBox();
		verticalBox_3.setPreferredSize(new Dimension(250, 80));
		verticalBox_3.setMinimumSize(new Dimension(250, 80));
		verticalBox_3.setMaximumSize(new Dimension(270, 80));
		verticalBox_3.setAlignmentX(Component.CENTER_ALIGNMENT);
		horizontalBox_11.add(verticalBox_3);
		
		Box horizontalBox_14 = Box.createHorizontalBox();
		horizontalBox_14.setMaximumSize(new Dimension(250, 30));
		verticalBox_3.add(horizontalBox_14);
		
		JLabel lblTime = new JLabel("Time:");
		horizontalBox_14.add(lblTime);
		
		Component rigidArea_15 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_14.add(rigidArea_15);
		
		txtHours = new JTextField();
		txtHours.setMaximumSize(new Dimension(50, 30));
		txtHours.setColumns(2);
		horizontalBox_14.add(txtHours);
		
		Component rigidArea_10 = Box.createRigidArea(new Dimension(5, 20));
		horizontalBox_14.add(rigidArea_10);
		
		JLabel lblHours = new JLabel("Hours");
		horizontalBox_14.add(lblHours);
		
		Component rigidArea_11 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_14.add(rigidArea_11);
		
		txtMins = new JFormattedTextField(DateFormatter.createFormatter("*0"));
		((MaskFormatter) txtMins.getFormatter()).setValidCharacters("36 ");
		txtMins.setMaximumSize(new Dimension(50, 30));
		txtMins.setColumns(2);
		horizontalBox_14.add(txtMins);
		
		Component rigidArea_12 = Box.createRigidArea(new Dimension(5, 20));
		horizontalBox_14.add(rigidArea_12);
		
		JLabel lblMins = new JLabel("Minutes");
		horizontalBox_14.add(lblMins);
		
		Component rigidArea_13 = Box.createRigidArea(new Dimension(20, 20));
		verticalBox_3.add(rigidArea_13);
		
		Box horizontalBox_15 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_15);
		
		JLabel lblDescription = new JLabel("Description:");
		horizontalBox_15.add(lblDescription);
		
		Component rigidArea_14 = Box.createRigidArea(new Dimension(20, 30));
		horizontalBox_15.add(rigidArea_14);
		
		txtDescription = new JTextField();
		txtDescription.setMaximumSize(new Dimension(200, 30));
		txtDescription.setColumns(55);
		horizontalBox_15.add(txtDescription);
		
		Component verticalStrut_13 = Box.createVerticalStrut(20);
		verticalStrut_13.setMaximumSize(new Dimension(500, 10));
		verticalBox.add(verticalStrut_13);
		
		JButton btnSubmit = new JButton("SUBMIT");
		btnSubmit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (checkInput()) {
					new Thread(insertThread).start();
				}
			}
		});
		btnSubmit.setPreferredSize(new Dimension(77, 30));
		btnSubmit.setAlignmentX(Component.CENTER_ALIGNMENT);
		verticalBox.add(btnSubmit);

		resultsPane = new JScrollPane();
		resultsPane.setBounds(597, 0, 597, 639);
		contentPane.add(resultsPane);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(SEARCH_RESULTS);
		root.setAllowsChildren(true);
		model = new DefaultTreeModel(root);
		tree = new JTree(model);
		resultsPane.setViewportView(tree);
		tree.setVisibleRowCount(100000);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(true);

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				timeDiff = System.currentTimeMillis() - timeDiff;
				if (timeDiff > 0 && timeDiff < DBL_CLICK_THRESHOLD) {
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
					if (pathBounds != null && pathBounds.contains(e.getX(), e.getY())) {
						if (path.getLastPathComponent().equals(root)) return;
						for (int i = 0; i < searchTerms.size(); i++) verticalBox_1.remove(searchTerms.remove(i));
						DefaultMutableTreeNode nodeClicked = (DefaultMutableTreeNode) path.getLastPathComponent();
						if (DateFormatter.parse(nodeClicked.getUserObject().toString()) == null) {
							cbxContainsParam.setSelectedIndex(0);
							if (nodeClicked.getParent().equals(root)) {
								cbxFieldParam.setSelectedItem("Name");
								txtSearchPhrase.setText(((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject().toString());
							} else {
								DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) nodeClicked.getParent();
								if (DateFormatter.parse(((DefaultMutableTreeNode) nodeClicked.getParent()).getUserObject().toString()) == null) {
									int grade = 0;
									if (DateFormatter.isNumeric(parentNode.getFirstLeaf().getUserObject().toString().substring(0,2)))
										grade = Integer.parseInt(parentNode.getFirstLeaf().getUserObject().toString().substring(0,2));
									else grade = Integer.parseInt(parentNode.getFirstLeaf().getUserObject().toString().substring(0,1));
									Volunteer volunteer = new Volunteer((String) ((DefaultMutableTreeNode) nodeClicked.getParent()).getUserObject(), grade);
									if (((DefaultMutableTreeNode) parentNode.getChildAfter(parentNode.getFirstLeaf())).getUserObject().toString().equals("Active"))
										volunteer.setStatus(false);
									else volunteer.setStatus(true);
									int curChild = 2;
									volunteer.setSchool(((DefaultMutableTreeNode) parentNode.getChildAt(curChild)).getUserObject().toString());
									curChild++;
									volunteer.setEmail(((DefaultMutableTreeNode) parentNode.getChildAt(curChild)).getUserObject().toString());
									curChild++;
									volunteer.setPhoneNumber(((DefaultMutableTreeNode) parentNode.getChildAt(curChild)).getUserObject().toString());
									curChild++;
									volunteer.setAddress(((DefaultMutableTreeNode) parentNode.getChildAt(curChild)).getUserObject().toString());
									if (nodeClicked.getUserObject().toString().contains("Grade")) {
										txtSearchPhrase.setText(String.valueOf(volunteer.getGrade()));
										cbxFieldParam.setSelectedItem("Grade");
									} else if (nodeClicked.getUserObject().toString().contains("ctive")) {
										chkSearchInactive.setSelected(volunteer.getStatus());
										cbxFieldParam.setSelectedItem("Name");
										txtSearchPhrase.setText("");
									} else if (nodeClicked.getUserObject().toString().equals(volunteer.getSchool())) {
										txtSearchPhrase.setText(path.getLastPathComponent().toString());
										cbxFieldParam.setSelectedItem("School");
									} else if (nodeClicked.getUserObject().toString().equals(volunteer.getEmail())) {
										txtSearchPhrase.setText(path.getLastPathComponent().toString());
										cbxFieldParam.setSelectedItem("Email");
									} else if (nodeClicked.getUserObject().toString().equals(volunteer.getAddress())) {
										txtSearchPhrase.setText(path.getLastPathComponent().toString());
										cbxFieldParam.setSelectedItem("Address");
									} else if (nodeClicked.getUserObject().toString().equals(volunteer.getPhoneNumber())) {
										txtSearchPhrase.setText(path.getLastPathComponent().toString());
										cbxFieldParam.setSelectedItem("Phone Number");
									} 
								} else {
									if (((Record) nodeClicked.getUserObject()).toString().contains("Description")) cbxFieldParam.setSelectedItem("Description");
									else cbxFieldParam.setSelectedItem("Minutes");
									txtSearchPhrase.setText(((Record) nodeClicked.getUserObject()).getValue());
								}
							}
						} else {
							cbxFieldParam.setSelectedIndex(0);
							txtSearchPhrase.setText("");
							txtSearchFrom.setValue(DateFormatter.parse(nodeClicked.getUserObject().toString()));
							txtSearchTo.setValue(DateFormatter.parse(nodeClicked.getUserObject().toString()));
						}
					}
					timeDiff = 0;
					new Thread(searchThread).start();
					contentPane.validate();
				} else timeDiff = System.currentTimeMillis();
			}
		});
	}
	
	private void search() {
		Date start = new Date(System.currentTimeMillis());
		setTitle(Status.WORKING);
		SearchParam[] params = getSearchParams();
		ArrayList<Volunteer> volunteers = DatabaseManager.search(this, params, DateFormatter.parse(txtSearchFrom.getText()),
				DateFormatter.parse(txtSearchTo.getText()), chkSearchInactive.isSelected(), chkIncludeNonLogging.isSelected());
		if (!isSearchEmpty()) {
			history.add(params);
			historyComplement.add(txtSearchFrom.getText() + "," + txtSearchTo.getText() + "," + chkSearchInactive.isSelected());
			currentHistory++;
		}
		makeTree(volunteers);
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		long time = System.currentTimeMillis() - start.getTime();
		updateRoot(SEARCH_RESULTS + " - " + root.getChildCount() + " Volunteer(s) Matched - "
				+ String.valueOf(time) + " ms - " + new Date(System.currentTimeMillis()).toString());
		if (volunteers == null) {
			setTitle(Status.EMPTY_SEARCH);
			JOptionPane.showMessageDialog(this, "Search returned no results.");
		} else setTitle(Status.SEARCH_SUCCESS);
	}
	
	private void report() {
		Date start = new Date(System.currentTimeMillis());
		setTitle(Status.WORKING);
		DatabaseManager.ReportOption option;
		String phrase;
		if (cbxReportOptions.getSelectedIndex() == 0) {
			option = DatabaseManager.ReportOption.ALL_VOLUNTEERS;
			phrase = "";
		} else if (cbxReportOptions.getSelectedIndex() == 1) {
			option = DatabaseManager.ReportOption.SPECIFIC_DESCRIPTION;
			phrase = DatabaseManager.checkSpecialChars(txtReportPhrase.getText());
		} else {
			option = DatabaseManager.ReportOption.SPECIFIC_VOLUNTEER;
			phrase = DatabaseManager.checkSpecialChars(txtReportPhrase.getText());
		}
		int total = DatabaseManager.reportHours(option, phrase, DateFormatter.parse(txtReportFrom.getText()),
				DateFormatter.parse(txtReportTo.getText()), chkReportInactive.isSelected(), frame);
		Record record = new Record(String.valueOf(total), extraParams[0].getId(), -1);
		lblReportTotal.setText(record.toString());
		long time = System.currentTimeMillis() - start.getTime();
		updateRoot(REPORT_RESULTS + " - "
				+ ((DefaultMutableTreeNode) model.getRoot()).getChildCount() + " Volunteer(s) Matched - Total - "
				+ String.valueOf(time) + " ms - " + new Date(System.currentTimeMillis()).toString());
		setTitle(Status.REPORT_SUCCESS);
		JOptionPane.showMessageDialog(this, "Total : " + record.toString());
	}
	
	private boolean isSearchEmpty() {
		if (txtSearchPhrase.getText().isEmpty()) {
			if (searchTerms.isEmpty()) return true;
			boolean allTermsEmpty = true;
			for (int i = 0; i < searchTerms.size(); i++) if (!searchTerms.get(i).getPhrase().isEmpty()) allTermsEmpty = false;
			return allTermsEmpty;
		} else return false;
	}

	private SearchParam[] getSearchParams() {
		SearchParam[] params = new SearchParam[searchTerms.size() + 1];
		params[0] = new SearchParam((String) cbxContainsParam.getSelectedItem(),
				(String) cbxFieldParam.getSelectedItem(), DatabaseManager.checkSpecialChars(txtSearchPhrase.getText()));
		for (int i = 0; i < searchTerms.size(); i++) {
			params[i + 1] = searchTerms.get(i).toSearchParam();
		}
		return params;
	}

	protected void makeTree(ArrayList<Volunteer> volunteers) {
		if (volunteers == null) {
			tree.collapsePath(new TreePath((model.getRoot())));
			((DefaultMutableTreeNode) model.getRoot()).removeAllChildren();
			return;
		}
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		root.removeAllChildren();
		for (int i = 0; i < volunteers.size(); i++) {
			DefaultMutableTreeNode volunteer = new DefaultMutableTreeNode(volunteers.get(i).getName());
			volunteer.add(new DefaultMutableTreeNode(volunteers.get(i).getGrade() + "th Grade"));
			if (volunteers.get(i).getStatus()) volunteer.add(new DefaultMutableTreeNode("Active"));
			else volunteer.add(new DefaultMutableTreeNode("Inactive"));
			volunteer.add(new DefaultMutableTreeNode(volunteers.get(i).getSchool()));
			volunteer.add(new DefaultMutableTreeNode(volunteers.get(i).getEmail()));
			volunteer.add(new DefaultMutableTreeNode(volunteers.get(i).getPhoneNumber()));
			volunteer.add(new DefaultMutableTreeNode(volunteers.get(i).getAddress()));
			DefaultMutableTreeNode reportNode = new DefaultMutableTreeNode("Reports");
			ArrayList<Report> reports = volunteers.get(i).getReports();
			if (reports.isEmpty()) {
				Logger.output("Volunteer probably has no reports...");
				reportNode.setUserObject("(no reports)");
			} else {
				for (int j = 0; j < reports.size(); j++) {
					try {
						if (reports.isEmpty()) throw new NullPointerException();
						DefaultMutableTreeNode report = new DefaultMutableTreeNode(reports.get(j).getDate());
						ArrayList<Record> records = reports.get(j).getRecords();
						for (int k = 0; k < records.size(); k++) report.add(new DefaultMutableTreeNode(records.get(k)));
						reportNode.add(report);
					} catch (NullPointerException e) {
						Logger.output("Volunteer probably has no reports...");
						Logger.output(e);
						reportNode.setUserObject("(no reports)");
					}
				}
			}
			volunteer.add(reportNode);
			root.add(volunteer);
		}
		tree.collapsePath(new TreePath(root));
		tree.setSelectionPath(new TreePath(root));
		((DefaultTreeModel) model).reload(root);
		results = volunteers.toArray(new Volunteer[0]);
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(TITLE + " - " + title);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		JMenuItem menuItem = (JMenuItem) event.getSource();
		if (menuItem.getText().equals(MenuOption.NEW_PROFILE)) {
			Logger.output("Request to create new profile");
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						JFrame newProf = new NewProfile(frame);
						newProf.setVisible(true);
					} catch (Exception e) {
						Logger.output(e);
						e.printStackTrace();
					}
				}
			});
		} else if (menuItem.getText().equals(MenuOption.SUPER_SERVICE)) {
			Logger.output("Request to run super service alert");
			ArrayList<Volunteer> volunteers = DatabaseManager.superServiceAlert();
			String message = "Here's a list of volunteers whose hours exceed " + FileSystemWatcher.getThreshold()/60 + ":";
			for (Volunteer volunteer : volunteers)
				message += String.format("%n%-40s%3dth Grade%40s", volunteer.getName(), volunteer.getGrade(), volunteer.getEmail());
			JOptionPane.showMessageDialog(this, message);
		} else if (menuItem.getText().equals(MenuOption.UPDATE_GRADES)) {
			Logger.output("Request to update grades");
			int result = JOptionPane.showOptionDialog(this, "Are you sure you want to update everyone's grade and status? This can only be manually undone.\n"
					+ "(Inactive students may still submit hours - their status affects only searches and hour reports)", "Update Grades",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {"yes", "no"}, "yes");
			if (result == JOptionPane.YES_OPTION) {
				if (DatabaseManager.updateGrades()) {
					while (searchTerms.size() > 0) removeSearchTerm();
					txtSearchPhrase.setText("");
					cbxContainsParam.setSelectedIndex(0);
					cbxFieldParam.setSelectedIndex(0);
					this.invalidate();
					new Thread(searchThread).start();
					JOptionPane.showMessageDialog(this, "Grades Updated Successfully.");
				} else JOptionPane.showMessageDialog(this, "Grades Update UNSUCCESSFUL. No changes were made. Please try again.");
			}
		} else if (menuItem.getText().equals(MenuOption.SEND_NAMES)) {
			Logger.output("Request to send names");
			int result = JOptionPane.showOptionDialog(this, "Would you like to auto-generate a names file from the database?", "Send Names",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {"yes", "no"}, "yes");
			if (result == JOptionPane.YES_OPTION) {
				if (!FileSystemWatcher.isProcessing()) tabletInputThread.interrupt();
				else {
					JOptionPane.showMessageDialog(this, "Please wait while we finish processing the last tablet file.");
					return;
				}
				if (!FileSystemWatcher.createNamesFile()) {
					JOptionPane.showMessageDialog(this, "An error occurred while attempting to process the generated file.\n"
							+ "The file was NOT processed. Please try again.");
					return;
				}
				try {
					Runtime.getRuntime().exec("fsquirt");
					JOptionPane.showMessageDialog(this, "A file named 'volunteerInfo.txt' was created in " + FileSystemWatcher.getOutgoingDir()
					+ "\nPlease select this file from this folder when using the bluetooth utility.");
				} catch (IOException e) {
					Logger.output(e);
					e.printStackTrace();
				}
				tabletInputThread = new Thread(tabletInput);
				tabletInputThread.start();
			} else openFileDialog(FileSystemWatcher.SendOption.NAMES);
		} else if (menuItem.getText().equals(MenuOption.SEND_DESCRIPTIONS)) {
			Logger.output("Request to send descriptions");
			openFileDialog(FileSystemWatcher.SendOption.DESCRIPTIONS);
		} else if (menuItem.getText().equals(MenuOption.SEND_KEYWORDS)) {
			Logger.output("Request to send keywords");
			openFileDialog(FileSystemWatcher.SendOption.KEYWORDS);
		} else if (menuItem.getText().equals(MenuOption.EDIT_PROFILE)) {
			Logger.output("Request to edit profile");
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						JFrame edit = new EditProfile(frame);
						edit.setVisible(true);
					} catch (Exception e) {
						Logger.output(e);
						e.printStackTrace();
					}
				}
			});
		} else if (menuItem.getText().equals(MenuOption.EDIT_ENTRY)) {
			Logger.output("Request to edit entry");
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						JFrame edit = new EditEntry(frame);
						edit.setVisible(true);
					} catch (Exception e) {
						Logger.output(e);
						e.printStackTrace();
					}
				}
			});
		} else if (menuItem.getText().equals(MenuOption.DELETE_ENTRY)) {
			Logger.output("Request to delete entry");
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						JFrame delete = new DeleteReport(frame);
						delete.setVisible(true);
					} catch (Exception e) {
						Logger.output(e);
						e.printStackTrace();
					}
				}
			});
		} else if (menuItem.getText().equals(MenuOption.SET_INCOMING_DIR)) {
			Logger.output("Request to set incoming directory");
			openFileDialog(FileSystemWatcher.AdminFileOrder.INCOMING_DIR);
		} else if (menuItem.getText().equals(MenuOption.SET_OUTGOING_DIR)) {
			Logger.output("Request to set outgoing directory");
			openFileDialog(FileSystemWatcher.AdminFileOrder.OUTGOING_DIR);
		} else if (menuItem.getText().equals(MenuOption.SET_THRESHOLD)) {
			Logger.output("Request to change threshold");
			String input = JOptionPane.showInputDialog(this, "Please input a new number of hours for the threshold.").trim();
			try {
				double threshold = Double.parseDouble(input);
				if (FileSystemWatcher.setThreshold(threshold)) {
					JOptionPane.showMessageDialog(this, "Threshold set successfully.");
					Logger.output("Threshold set successfully.");
				}
			} catch(NumberFormatException e) {
				try {
					int threshold = Integer.parseInt(input);
					if (FileSystemWatcher.setThreshold(threshold)) {
						JOptionPane.showMessageDialog(this, "Threshold set successfully.");
						Logger.output("Threshold set successfully.");
					}
				} catch(NumberFormatException e2) {
					JOptionPane.showMessageDialog(this, "Invalid number of hours (must be numeric)");
				}
			}
		} else if (menuItem.getText().equals(MenuOption.PRINT_TO_TEXT)) {
			Logger.output("Printing search results to Text File...");
			setTitle(Status.WORKING);
			if (results == null || results.length == 0) {
				setTitle(Status.NO_RESULTS_TO_PRINT);
				return;
			}
			if (!FileSystemWatcher.printToTextFile(results)) {
				JOptionPane.showMessageDialog(this, "Unable to print search results to text file. Please try again.");
				setTitle(Status.CONNECTION_SUCCESS);
			} else setTitle(Status.PRINT_TO_FILE_SUCCESS);
//		} else if (menuItem.getText().equals(MenuOption.IMPORT_FLASH)) {
//			setTitle(Status.WORKING);
//			Logger.output("Importing from Flash Drive...");
//			ArrayList<File> usbs = FileSystemWatcher.findMountedUSBs();
//			if (usbs.size() == 0) {
//				setTitle(Status.NO_USB_TO_IMPORT);
//				return;
//			}
//			Object[] options = new Object[usbs.size()];
//			for (int i = 0; i < options.length; i++) options[i] = FileSystemView.getFileSystemView().getSystemDisplayName(usbs.get(i));
//			Object inFile = JOptionPane.showInputDialog(frame, "Please select a USB device to import from:",
//					"Export to USB", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
//			if (inFile == null) {
//				setTitle(Status.CONNECTION_SUCCESS);
//				return;
//			}
//			String path = inFile.toString();
//			if (DatabaseManager.importDatabase(path.substring(path.indexOf("(")+1, path.indexOf(")")) + "/")) {
//				JOptionPane.showMessageDialog(this, "Data Import Successful.");
//				Logger.output("Data Import Successful.");
//				setTitle(Status.IMPORT_SUCCESS);
//			} else {
//				JOptionPane.showMessageDialog(this, "Data Import UNSUCCESSFUL. Please try again.");
//				Logger.output("Data Import UNSUCCESSFUL. Please try again.");
//				setTitle(Status.CONNECTION_SUCCESS);
//			}
		} else if (menuItem.getText().equals(MenuOption.EXPORT_FLASH)) {
			setTitle(Status.WORKING);
			Logger.output("Exporting to Flash Drive...");
			ArrayList<File> usbs = FileSystemWatcher.findMountedUSBs();
			if (usbs.size() == 0) {
				setTitle(Status.NO_USB_TO_EXPORT);
				return;
			}
			Object[] options = new Object[usbs.size()];
			for (int i = 0; i < options.length; i++) options[i] = FileSystemView.getFileSystemView().getSystemDisplayName(usbs.get(i));
			Object outFile = JOptionPane.showInputDialog(frame, "Please select a USB device to export to:",
					"Export to USB", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (outFile == null) {
				setTitle(Status.CONNECTION_SUCCESS);
				return;
			}
			String path = outFile.toString();
			if (DatabaseManager.dumpDatabase(path.substring(path.indexOf("(")+1, path.indexOf(")")) + "/")) {
				JOptionPane.showMessageDialog(this, "Data Export Successful.");
				Logger.output("Data Export Successful.");
				setTitle(Status.EXPORT_SUCCESS);
			} else {
				JOptionPane.showMessageDialog(this, "Data Export UNSUCCESSFUL. Please try again.");
				Logger.output("Data Export UNSUCCESSFUL. Please try again.");
				setTitle(Status.CONNECTION_SUCCESS);
			}
		} else if (menuItem.getText().equals(MenuOption.WALKTHROUGH)) {
			Logger.output("User requested walkthrough");
			try {
			     if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(FileSystemWatcher.adminFolder + "/Walkthrough.docx"));
			     else throw new IOException();
			} catch (IOException ioe) {
				Logger.output("Unable to Open Walkthrough!");
			    ioe.printStackTrace();
			    JOptionPane.showMessageDialog(this, "We were unable to open the walkthrough document.\nIt should be located under " + FileSystemWatcher.adminFolder);
			}
		} else if (menuItem.getText().equals(MenuOption.PHONE_NUMBER)) {
			Logger.output("User requested phone number");
			JOptionPane.showMessageDialog(this, "(508) 353-4270");
		} else if (menuItem.getText().equals(MenuOption.CLOSE)) {
			Logger.output("User exited from menu. Closing Application...");
			System.exit(0);
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		Logger.output("Window got focus");
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		Logger.output("Closing Application...");
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		Logger.output("Window lost focus");
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		Logger.output("Window loaded, Application started");
	}
	
	private class SelectionListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			@SuppressWarnings("unchecked") // Used only with JComboBoxes of type String
			JComboBox<String> source = (JComboBox<String>) e.getSource();
			if (source.getSelectedItem().equals(reportParams[0])) txtReportPhrase.setEnabled(false);
			else txtReportPhrase.setEnabled(true);
		}
	}

	private void addSearchTerm() {
		SearchTerm searchTerm = new SearchTerm(extraParams);
		searchTerms.add(searchTerm);
		verticalBox_1.add(searchTerm);
		contentPane.validate();
	}
	
	private void removeSearchTerm() {
		verticalBox_1.remove(searchTerms.remove(searchTerms.size() - 1));
		contentPane.validate();
	}
	
	private void cycleHistory(int increment) {
		currentHistory += increment;
		SearchParam[] params = history.get(currentHistory);
		while (params.length-1 > searchTerms.size()) addSearchTerm();
		while (params.length-1 < searchTerms.size()) removeSearchTerm();
		cbxContainsParam.setSelectedItem(params[0].getContainsParam());
		txtSearchPhrase.setText(params[0].getPhrase());
		cbxFieldParam.setSelectedItem(params[0].getFieldParam());
		assert (params.length-1 == searchTerms.size());
		for (int i = 1; i < params.length; i++) {
			searchTerms.get(i-1).setJoinParam(params[i].getJoinParam());
			searchTerms.get(i-1).setContainsParam(params[i].getContainsParam());
			searchTerms.get(i-1).setPhrase(params[i].getPhrase());
			searchTerms.get(i-1).setFieldParam(params[i].getFieldParam());
		}
		String complement = historyComplement.get(currentHistory);
		String[] complements = complement.split(",");
		txtSearchFrom.setValue(DateFormatter.parse(complements[0]));
		txtSearchTo.setValue(DateFormatter.parse(complements[1]));
		if (Boolean.parseBoolean(complements[2])) chkSearchInactive.setSelected(true);
		else chkSearchInactive.setSelected(false);
		contentPane.validate();
	}
	
	public void updateRoot(String str) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		root.setUserObject(str);
		tree.collapsePath(new TreePath(root));
		tree.setSelectionPath(new TreePath(root));
		((DefaultTreeModel) model).reload(root);
	}
	
	public boolean isExtraParam(String param) {
		for (Item extraParam : extraParams) if (extraParam.getName().equals(param)) return true;
		return false;
	}
	
	public void reloadManualEntryVolunteers() {
		Volunteer[] volunteers = DatabaseManager.queryVolunteers();
		String[] names = new String[volunteers.length];
		for (int i = 0; i < volunteers.length; i++) names[i] = volunteers[i].getName();
		cbxSelectVolunteer.setModel(new DefaultComboBoxModel<String>(names));
	}
	
	private void openFileDialog(FileSystemWatcher.SendOption option) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (!FileSystemWatcher.isProcessing()) tabletInputThread.interrupt();
			else {
				JOptionPane.showMessageDialog(this, "Please wait while we finish processing the last tablet file.");
				return;
			}
			File file = FileSystemWatcher.prepareFileToSend(chooser.getSelectedFile(), option);
			if (file == null || !file.exists()) {
				JOptionPane.showMessageDialog(this, "An error occurred while attempting to process the selected file.\n"
						+ "The file was NOT processed. Please try again.");
				openFileDialog(option);
				return;
			}
			try {
				Runtime.getRuntime().exec("fsquirt");
				JOptionPane.showMessageDialog(this, "A file named '" + file.getName() + "' was created in " + FileSystemWatcher.getOutgoingDir()
					+ "\nPlease select this file from this folder when using the bluetooth utility.");
			} catch (IOException e) {
				Logger.output(e);
				e.printStackTrace();
			}
			tabletInputThread = new Thread(tabletInput);
			tabletInputThread.start();
		}
	}
	
	private void openFileDialog(FileSystemWatcher.AdminFileOrder position) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			boolean success = false;
			switch (position) {
				case INCOMING_DIR:
					if (chooser.getSelectedFile().toString().equals(FileSystemWatcher.getOutgoingDir())) {
						JOptionPane.showMessageDialog(this,
								"This is also the folder set to Save to.\nYou cannot have the same folder to watch and to save to.");
						return;
					}
					if (FileSystemWatcher.setIncomingDir(chooser.getSelectedFile().toString())) success = true;
					break;
				case OUTGOING_DIR:
					if (chooser.getSelectedFile().toString().equals(FileSystemWatcher.getIncomingDir())) {
						JOptionPane.showMessageDialog(this,
								"This is also the folder set to watch.\nYou cannot have the same folder to watch and to save to.");
						return;
					}
					if (FileSystemWatcher.setOutgoingDir(chooser.getSelectedFile().toString())) success = true;
			}
			if (success) {
				JOptionPane.showMessageDialog(this, "Directory set successfully.");
				Logger.output("Directory set successfully.");
			} else {
				JOptionPane.showMessageDialog(this, "Directory was NOT set successfully\nChanges may or may not have been applied.");
				Logger.output("Directory was NOT set successfully\nChanges may or may not have been applied.");
			}
		}
	}
	
	private boolean checkInput() {
		if (cbxSelectVolunteer.getSelectedIndex() < 0) {
			JOptionPane.showMessageDialog(this, "Please select a volunteer.");
			return false;
		}
		if (!DateFormatter.isNumeric(txtHours.getText().trim()) && txtMins.getText().equals(" 0")) {
			JOptionPane.showMessageDialog(this, "This entry must have a valid amount of time, either minutes or hours or both.");
			return false;
		}
		if (txtDescription.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "This entry must have a description.");
			return false;
		}
		if (DateFormatter.parse(txtDate.getText().trim()).after(new Date())) {
			JOptionPane.showMessageDialog(this, "This entry referes to a date in the future.");
			return false;
		}
		return true;
	}
	
}