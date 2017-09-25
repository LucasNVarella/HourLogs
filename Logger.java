import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public abstract class Logger {
	
	private static final long FILE_SIZE_THERSHOLD = 100000000; // 100MB
	private static final String DEFAULT_FILENAME = "log.txt";
	private static final String DEFAULT_PATH = "C:/hourlogsfiles/";
	
	private static String[] files;
	
	private static boolean initialized = false;
	private static volatile String currentFileName;
	private static volatile long currentFileSize;
	
	private static volatile Scanner in;
	private static volatile BufferedWriter out;
	private static volatile File currentFile;
	
	public static void initialize() {
		if (initialized) {
			System.out.println("Attempt to initialize Logger when it was already initialized!");
			return;
		}
		System.out.println("Initializing Logger...");
		// Check existing files
		File folder = new File(DEFAULT_PATH);
		File[] files = folder.listFiles();
		int logFiles = -1;
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().split("\\.")[0].contains(DEFAULT_FILENAME.split("\\.")[0])) logFiles++;
		}
		if (logFiles < 0) {
			currentFileName = DEFAULT_FILENAME.split("\\.")[0] + "0." + DEFAULT_FILENAME.split("\\.")[1];
			File firstFile = new File(DEFAULT_PATH + currentFileName);
			try {
				if (!firstFile.createNewFile()) throw new IOException();
			} catch (IOException e) {
				System.out.println("Unable to create a new log file!");
				e.printStackTrace();
				return;
			}
			currentFileSize = 0;
		} else {
			currentFileName = DEFAULT_FILENAME.split("\\.")[0] + logFiles + "." + DEFAULT_FILENAME.split("\\.")[1];
			currentFileSize = new File(folder, currentFileName).length();
		}
		initialized = true;
		System.out.println("Logger initialization successful");
	}

	public synchronized static void output(String out) {
		out += "\t" + (new Date(System.currentTimeMillis())).toString();
		System.out.println(out);
		if (!initialized) {
			System.out.println("Attempt to log when Logger was not initialized!");
			initialize();
		}
		String message = "Unable to log!";
		try {
			File currentFile = new File(DEFAULT_PATH + currentFileName);
			if (exceedsThreshold(currentFile, out)) {
				String[] nameParts = currentFileName.split("\\.");
				int currentFileNumber = Integer.parseInt(nameParts[0].substring(nameParts[0].length()-1, nameParts[0].length()));
				currentFileNumber++;
				currentFileName = nameParts[0].substring(0, nameParts[0].length()-1) + currentFileNumber + "." + nameParts[1];
				currentFile = new File(DEFAULT_PATH + currentFileName);
				if (!currentFile.createNewFile()) {
					message += " Creation of new log file failed!";
					throw new IOException();
				}
			}
			in = new Scanner(currentFile);
			ArrayList<String> content = new ArrayList<>();
			while (in.hasNextLine()) content.add(in.nextLine());
			content.add(out);
			Logger.out = new BufferedWriter(new FileWriter(DEFAULT_PATH + currentFileName));
			for (String line : content) {
				Logger.out.write(line);
				Logger.out.newLine();
			}
			Logger.out.close();
		} catch (IOException e) {
			System.out.println(message);
			e.printStackTrace();
		}
	}
	
	public static void output(Exception e) {
		String output = e.toString();
    	StackTraceElement[] stackTrace = e.getStackTrace();
    	for (int i = 0; i < stackTrace.length; i++) {
    		output += "\t" + "at " + stackTrace[i].getClassName() + "." + stackTrace[i].getMethodName() + "("
    				+ ((stackTrace[i].getFileName() == null) ? "Unknown Source" : stackTrace[i].getFileName() + ":" + stackTrace[i].getLineNumber()) + ")\n";
    	}
    	output(output);
	}
	
	public static void output(ArrayList<Volunteer> volunteerList) {
		String result = "Results:\n";
        for (Volunteer volunteer : volunteerList) result += volunteer.toString() + "\n";
        output(result);
	}
	
	public static void output(Volunteer[] volunteers) {
		String result = "Results:\n";
        for (Volunteer volunteer : volunteers) result += volunteer.toString() + "\n";
        output(result);
	}
	
	public static void output(SearchParam[] params) {
		String search = "";
    	int validParams = 0;
		for (int i = 0; i < params.length; i++)
			if (!params[i].getPhrase().isEmpty()) {
				search += (validParams == 0) ? (params[i].getJoinParam() + " " + params[i].toString()) :
												("\n\t" + params[i].getJoinParam() + " " + params[i].toString());
				validParams++;
			}
		output(search);
	}
	
	private static boolean exceedsThreshold(File file, String output) {
		return ((file.length() + output.getBytes().length) >= FILE_SIZE_THERSHOLD);
	}
	
}