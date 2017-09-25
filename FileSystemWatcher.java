import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

public abstract class FileSystemWatcher {
	
	private static final String FORM_DELIMITER = "||";
	private static final String ITEM_DELIMITER = "|";
	private static final String ID_DELIMITER = ",";
	
	public static enum SendOption {
		NAMES, DESCRIPTIONS, KEYWORDS
	}
	
	public static enum AdminFileOrder {
		INCOMING_DIR, OUTGOING_DIR, THRESHOLD
	}
	
	private static WatchService watcher;
	private static WatchKey key;
	private static boolean processing = false;
	public static File adminFolder = new File("C:/hourlogsfiles");
	private static Path adminFile = new File("C:/hourlogsfiles/adminFile.txt").toPath();
	public static String backupfile = "backup.sql";
	private static double alertThreshold = 100*60;
	// The main folders where changes will be monitored.
	private static Path tabletIncomingDir = new File(System.getProperty("user.home"), "Desktop").toPath();
	private static Path tabletOutgoingDir = new File(System.getProperty("user.home"), "Desktop/To Tablet Folder").toPath();
	
    
    public static void init() {
		try {
			if (!adminFile.toFile().exists()) {
				if (!adminFolder.mkdirs()) throw new IOException();
				if (!adminFile.toFile().createNewFile()) throw new IOException();
				BufferedWriter writer = new BufferedWriter(new FileWriter(adminFile.toFile()));
				writer.write(makeAdminFileContent());
				writer.close();
				Logger.output("Admin file created successfully.");
			} else {
				Scanner in = new Scanner(adminFile.toFile());
				String content = in.nextLine();
				String parts[] = content.split("\\" + ITEM_DELIMITER);
				in.close();
				if (parts.length < 3) {
					if (!adminFile.toFile().delete()) throw new IOException();
					if (!adminFile.toFile().createNewFile()) throw new IOException();
					BufferedWriter writer = new BufferedWriter(new FileWriter(adminFile.toFile()));
					writer.write(makeAdminFileContent());
					writer.close();
					Logger.output("Admin file reset successfully.");
				} else {
					tabletIncomingDir = new File(parts[AdminFileOrder.INCOMING_DIR.ordinal()]).toPath();
					tabletOutgoingDir = new File(parts[AdminFileOrder.OUTGOING_DIR.ordinal()]).toPath();
					alertThreshold = Double.parseDouble(parts[AdminFileOrder.THRESHOLD.ordinal()])*60;
					Logger.output("Admin file accessed successfully.");
				}
			}
		} catch (IOException e) {
			Logger.output("Unable to load admin preferences!");
			Logger.output(e);
			e.printStackTrace();
		}
    }
    
    public static int checkFolderForFile(MainWindow instance) {
    	processing = false;
    	int numReports = 0;
        Logger.output("Checking folder for file...");
        
        // the WatchService will continue to check for File System events until
        // the program is closed.
        // Look up WatchService for more info.
		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			Logger.output(e);
			e.printStackTrace();
		}
		if (!tabletIncomingDir.toFile().exists()) tabletIncomingDir.toFile().mkdir();
		try {
			key = tabletIncomingDir.register(watcher, ENTRY_CREATE);
			key = watcher.take();
			processing = true;
			Logger.output("Found change...");
		} catch (IOException | InterruptedException x) {
			Logger.output(x);
			x.printStackTrace();
			try {
				key.cancel();
				watcher.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(instance, "A fatal error has occurred. Please restart the program and try again.\nIf the problem persists, get help!");
				Logger.output("Unable to stop watcher!");
				Logger.output(e);
				e.printStackTrace();
				System.exit(-1);
			}
		}

		Logger.output("Reading a file...");

		// This code will only be reached when the WatchService has found a
		// change in the monitored folder.
		// It will hold the main thread until it has found a change.
		for (WatchEvent<?> event : key.pollEvents()) {

			// The filename is the context of the event.
			@SuppressWarnings("unchecked")
			WatchEvent<Path> ev = (WatchEvent<Path>) event;
			Path filename = ev.context();

			// Verify that the new file is a text file.
			try {
				// Resolve the filename against the directory.
				// This will indicate whether a new file found is a text
				// file.
				// Checks for extraneous new files in the monitored folder.
				// Look up resolving file names for more info.
				Path child = tabletIncomingDir.resolve(filename);
				if (!Files.probeContentType(child).equals("text/plain")) {
					String message = String.format("New file '%s'" + " is not a plain text file.%n", filename);
					Logger.output(message);
				}
			} catch (IOException | NullPointerException x) {
				Logger.output(x);
				Logger.output(String.format("New file '%s'" + " is not a plain text file.", filename));
				numReports = -1;
				continue;
			}

			// The tablets will always send all forms as a single line, to
			// be contained in this string.
			File inputFile = new File(tabletIncomingDir.toFile(),
					filename.getFileName().toString());
			// writeToUSB(inputFile);

			String content = "";
			try {
				content = readFromFile(inputFile);
				if (!inputFile.delete()) throw new IOException();
			} catch (IOException e) {
				Logger.output(e);
				e.printStackTrace();
			}
			ArrayList<String> forms = new ArrayList<>();
	        boolean done = false;
	        while (!done) {
	            // double pipes delimit forms in the file.
	            int index = content.indexOf(FORM_DELIMITER);
	            if (index == -1) {
	                done = true;
	            	forms.add(content);
	            } else {
	                forms.add(content.substring(0, index));
	                content = content.substring(index + 2);
	            }
	        }
			// Now we will iterate through each item in each form
			ArrayList<Report> reports = new ArrayList<>();
			ArrayList<Record> records = new ArrayList<>();
			int[] ids = null;
			try {
				for (int i = 0; i < forms.size(); i++) {
					String[] items = forms.get(i).split("\\" + ITEM_DELIMITER);
					int volunteerID = DatabaseManager.getVolunteerId(items[0].trim());
					if (volunteerID == -1) {
						String info = "\nInformation:";
						Item[] dbItems = DatabaseManager.getItems();
						for (int j = 2; j < items.length; j++) {
							String itemParts[] = items[j].split("\\" + ID_DELIMITER);
							String itemName = "";
							for (Item item : dbItems)
								if (item.getId() == Integer.parseInt(itemParts[1]))
									itemName = item.getName();
							info += "\n\t" + itemName + ": " + itemParts[0];
						}
						JOptionPane.showMessageDialog(instance,
								"The file received from the tablet refers to a volunteer that is not on the database.\n"
										+ "These hours will NOT be put into the database.\nVolunteer's Name: "
										+ items[0] + ", Report Date: " + items[1] + info);
					} else
						reports.add(new Report(DateFormatter.formatToSql(DateFormatter.parse(items[1])), volunteerID));
				}
				if (reports.size() > 0) {
					ids = DatabaseManager.insertReports(reports);
					for (int i = 0; i < reports.size(); i++) {
						String[] items = forms.get(i).split("\\" + ITEM_DELIMITER);
						for (int j = 2; j < items.length; j++) {
							String[] itemParts = items[j].split("\\" + ID_DELIMITER);
							records.add(new Record(itemParts[0].trim(), Integer.parseInt(itemParts[1].trim()), ids[i]));
						}
					}
					DatabaseManager.insertRecords(records.toArray(new Record[0]));
					Logger.output("File read successfully.");
					numReports = reports.size();
				}
			} catch (Exception e) {
				String message = "A problem has occurred while attempting to import the forms from the last tablet file. "
						+ "The importing of any forms in the file previous to this error have been CANCELLED. "
						+ "This is so that you may resend the file from the tablet to try again without creating any duplicate forms.";
				Logger.output(message);
				JOptionPane.showMessageDialog(instance, message);
				deletePreviousReports(ids);
			}
		}

		// Reset the key -- this step is critical if you want to
		// receive further watch events. If the key is no longer valid,
		// the directory is inaccessible so exit the loop.
		boolean valid = key.reset();
		if (!valid) {
			Logger.output("Directory Inaccessible!");
			System.err.format("Directory inaccessible!");
		}
		return numReports;
    }
    
    private static void deletePreviousReports(int[] reportIDs) {
    	if (reportIDs != null) for (int id : reportIDs) DatabaseManager.deleteReport(id);
    }
    
    public static File prepareFileToSend(File file, SendOption option) {
    	String message = "Something went wrong!";
    	File sendFile = null;
    	try {
			String content = readFromFile(file);
			if (!tabletOutgoingDir.toFile().exists()) tabletOutgoingDir.toFile().mkdir();
			switch (option) {
				case NAMES:
					sendFile = new File(tabletOutgoingDir.toFile(), "volunteerInfo.txt");
					break;
				case DESCRIPTIONS:
					sendFile = new File(tabletOutgoingDir.toFile(), "descriptionsAvailable.txt");
					break;
				case KEYWORDS:
					sendFile = new File(tabletOutgoingDir.toFile(), "keywords.txt");
					break;
			}
			if (!sendFile.createNewFile()) {
				message = "Unable to create send file!";
				throw new IOException();
			}
			writeToFile(sendFile, content);
		} catch (IOException e) {
			Logger.output(message);
			Logger.output(e);
			e.printStackTrace();
			if (sendFile != null && sendFile.exists()) sendFile.delete();
		}
    	return sendFile;
    }
    
    public static boolean createNamesFile() {
    	String message = "Something went wrong!";
    	File sendFile = null;
    	try {
    		if (!tabletOutgoingDir.toFile().exists()) tabletOutgoingDir.toFile().mkdir();
			sendFile = new File(tabletOutgoingDir.toFile(), "volunteerInfo.txt");
			if (!sendFile.createNewFile()) {
				message = "Unable to create send file!";
				throw new IOException();
			}
			String content = "";
			Volunteer[] volunteers = DatabaseManager.getVolunteers();
			content = volunteers[0].getName();
			for (int i = 1; i < volunteers.length; i++) content += "\n" + volunteers[i].getName();
			writeToFile(sendFile, content);
		} catch (IOException e) {
			Logger.output(message);
			Logger.output(e);
			e.printStackTrace();
			if (sendFile != null && sendFile.exists()) sendFile.delete();
			return false;
		}
    	return true;
    }
    
    public static boolean setIncomingDir(String dir) {
    	String content = makeAdminFileContent(dir, AdminFileOrder.INCOMING_DIR);
    	try {
			writeToFile(adminFile.toFile(), content);
			init();
			return true;
		} catch (IOException e) {
			Logger.output("Unable to set incoming directory!");
			Logger.output(e);
			e.printStackTrace();
			return false;
		}
    }
    
    public static boolean setOutgoingDir(String dir) {
    	String content = makeAdminFileContent(dir, AdminFileOrder.OUTGOING_DIR);
    	try {
			writeToFile(adminFile.toFile(), content);
			init();
			return true;
		} catch (IOException e) {
			Logger.output("Unable to set outgoing directory!");
			Logger.output(e);
			e.printStackTrace();
			return false;
		}
    }
    
    public static boolean setThreshold(double threshold) {
    	String content = makeAdminFileContent(threshold, AdminFileOrder.THRESHOLD);
    	try {
			writeToFile(adminFile.toFile(), content);
			init();
			return true;
		} catch (IOException e) {
			Logger.output("Unable to set threshold!");
			Logger.output(e);
			e.printStackTrace();
			return false;
		}
    }
    
    private static String makeAdminFileContent(String str, AdminFileOrder position) {
    	String content = "";
    	switch (position) {
    		case INCOMING_DIR:
    			content = str + ITEM_DELIMITER + tabletOutgoingDir.toString() + ITEM_DELIMITER + alertThreshold;
    			break;
    		case OUTGOING_DIR:
    			content = tabletIncomingDir.toString() + ITEM_DELIMITER + str + ITEM_DELIMITER + alertThreshold;
    	}
    	return content;
    }
    
    private static String makeAdminFileContent(double num, AdminFileOrder position) {
    	String content = "";
    	switch (position) {
    		case THRESHOLD:
    			content = tabletIncomingDir.toString() + ITEM_DELIMITER + tabletOutgoingDir.toString() + ITEM_DELIMITER + String.valueOf(num);
    	}
    	return content;
    }
    
    private static String makeAdminFileContent() {
    	return tabletIncomingDir.toString() + ITEM_DELIMITER
				+ tabletOutgoingDir.toString() + ITEM_DELIMITER
				+ String.valueOf(alertThreshold/60);
    }
    
    public static ArrayList<File> findMountedUSBs() {
        Logger.output("Finding mounted USBs...");
        // Finds USBs mounted
        File files[] = File.listRoots();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        ArrayList<File> usbs = new ArrayList<>();;
        try {
            for (File file : files) if (fsv.getSystemTypeDescription(file).equals("USB Drive"))
                    usbs.add(file);
        } catch (NullPointerException ex) {
        	Logger.output("Failed to Find USB(s)");
        	Logger.output(ex);
            ex.printStackTrace();
        }
        return usbs;
    }
    
    public static String readFromFile(File inputFile) throws IOException {
        Scanner in = new Scanner(inputFile);
        String content = in.nextLine();
        while (in.hasNextLine()) content += "\n" + in.nextLine();
        in.close();
        return content;
    }
    
    public static void writeToFile(File file, String str) throws IOException {
    	BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    	Scanner scanner = new Scanner(str);
    	writer.write(scanner.nextLine());
    	while (scanner.hasNextLine()) {
    		writer.newLine();
    		writer.write(scanner.nextLine());
    	}
    	scanner.close();
    	writer.close();
    }
    
    public static String getIncomingDir() {
    	return tabletIncomingDir.toString();
    }
    
    public static String getOutgoingDir() {
    	return tabletOutgoingDir.toString();
    }
    
    public static boolean isProcessing() {
    	return processing;
    }
    
    public static double getThreshold() {
    	return alertThreshold;
    }
    
    public static boolean printToTextFile(Volunteer[] results) {
    	try {
    		File file = new File(tabletOutgoingDir + "/results.txt");
    		if (file.exists()) if(!file.delete()) throw new IOException();
    		if (!file.createNewFile()) throw new IOException();
    		String content = "";
    		for (Volunteer volunteer : results) content += String.format("%-45s%-45s%-13s\n", volunteer.getName(), volunteer.getEmail(), volunteer.getPhoneNumber());
    		writeToFile(file, content);
    		if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
    		else throw new IOException();
    		return true;
    	} catch (IOException e) {
    		Logger.output("Unable to print to text file!");
    		Logger.output(e);
    		e.printStackTrace();
    		return false;
    	}
    }
    
}