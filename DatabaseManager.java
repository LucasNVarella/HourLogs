import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public abstract class DatabaseManager {

	private static boolean connected = false;
	
	// SQL Database connection object
	private static Connection conn;
	
	private static String[] datatypeIDs;
	private static Item[] items;
	private static Volunteer[] volunteers;
	
	public static final class Aliases {
		public static final String record = "rcd";
		public static final String report = "rpt";
		public static final String volunteer = "vln";
		public static final String minutes = "mns";
		public static final String inner_volunteer = "vln2";
	}
	
	private static final String[] specialChars = {"\\x00", "\n", "\r", "\\", "'", "\"", "\\x1a"};
	
	public static enum ReportOption {
		ALL_VOLUNTEERS, SPECIFIC_DESCRIPTION, SPECIFIC_VOLUNTEER
	}
	
	public static boolean connect() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/wplhourlogs?useSSL=false", "lucas", "lucas");
		    connected = true;
		    queryDatatypes();
		    queryItems();
		    queryVolunteers();
		} catch (SQLException e) {
			if (!connected) Logger.output("Unable to connect!");
			Logger.output(e);
			e.printStackTrace();
		}
		return connected;
	} // End connect
	
	public static boolean isConnected() {
		return connected;
	}
	
	private static void queryDatatypes() {
		if (connected) {
			String sql = "SELECT datatype.id, datatype.DataType FROM wplhourlogs.datatype ORDER BY datatype.id";
			try {
				PreparedStatement stmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				stmt.executeQuery();
				ResultSet results = stmt.getResultSet();
				results.last();
				datatypeIDs = new String[results.getRow()];
				results.first();
				for (int i = 0; i < datatypeIDs.length; i++) {
					datatypeIDs[i] = results.getString(2);
					results.next();
				}
				stmt.close();
			} catch (SQLException e) {
				Logger.output("Unable to query datatypes!");
				Logger.output(e);
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	private static void queryItems() {
		if (connected){
			String sql = "SELECT item.id, item.`Name`, item.datatype_ID FROM wplhourlogs.item" 
					+ " WHERE item.`Active` = 1 ORDER BY item.id";
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sql);
		        stmt.executeQuery();
		        ResultSet results = stmt.getResultSet();
		        results.last();
		        items = new Item[results.getRow()];
		        results.first();
		        for (int i = 0; i < items.length; i++) {
		        	items[i] = new Item(results.getInt(1), results.getString(2), results.getInt(3));
		        	results.next();
		        }
		        stmt.close();
		    } catch (SQLException e) {
		    	Logger.output("Unable to query active items!");
				Logger.output(e);
		        e.printStackTrace();
		        System.exit(0);
		    }
		}
	}
	
	private static boolean checkDuplicateVolunteers(Volunteer[] volunteers) {
		for (int i = 0; i < DatabaseManager.volunteers.length-1; i++) {
			for (int j = 0; j < volunteers.length; j++) {
				if (volunteers[j].equals(DatabaseManager.volunteers[i])) return true;
			}
		}
		return false;
	}
	
	public static boolean checkDuplicateNames(String name) {
		for (int i = 0; i < volunteers.length; i++) 
			if (volunteers[i].getName().equals(name)) return true;
		return false;
	}
	
	public static String[] getDatatypeIDs() {
		return Arrays.copyOf(datatypeIDs, datatypeIDs.length);
	}

	public static Item[] getItems() {
		return Arrays.copyOf(items, items.length);
	}
	
	public static Volunteer[] queryVolunteers() {
		if (connected){
			String sql = "SELECT volunteer.id, volunteer.`Name`, volunteer.grade, volunteer.`status` FROM wplhourlogs.volunteer";
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sql);
		        stmt.execute();
		        ResultSet results = stmt.getResultSet();
		        results.last();
		        int rows = results.getRow();
		        if (rows == 0) {
		        	volunteers = new Volunteer[0];
		        	stmt.close();
		        	return null;
		        }
		        volunteers = new Volunteer[rows];
		        results.first();
		        for (int i = 0; i < rows; i++) {
		        	volunteers[i] = new Volunteer(results.getInt(1), results.getString(2), results.getInt(3), results.getInt(4));
		        	results.next();
		        }
		        stmt.close();
		        return volunteers;
		    } catch (SQLException e) {
		    	Logger.output(e);
		        e.printStackTrace();
		        System.exit(0);
		        return null;
		    }
		} else return null;
	}
	
	public static int getVolunteerId(String volunteerName) {
		for (int i = 0; i < volunteers.length; i++)
			if (volunteerName.equals(volunteers[i].getName())) return volunteers[i].getId();
		return -1;
	}
	
	public static Volunteer[] getVolunteers() {
		return volunteers;
	}
	
	public static Volunteer getVolunteer(String name) {
		if (connected){
			String sql = buildVolunteerSearch(name);
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sql);
		        stmt.execute();
		        ResultSet results = stmt.getResultSet();
		        results.first();
		        Volunteer volunteer = new Volunteer(results.getInt(1), results.getString(2), results.getInt(3), results.getInt(4),
		        		results.getString(7), results.getString(8), results.getString(6), results.getString(5));
		        stmt.close();
		        return volunteer;
		    } catch (SQLException e) {
		    	Logger.output(e);
		        e.printStackTrace();
		        System.exit(0);
		    }
		}
		return null;
	}
	
	public static boolean updateVolunteer(Volunteer volunteer) {
		if (connected){
			int status = 0;
			if (volunteer.getStatus()) status = 1;
			String sql = "UPDATE wplhourlogs.volunteer SET "
					+ "volunteer.`name` = '" + volunteer.getName() + "', "
					+ "volunteer.`grade` = '" + volunteer.getGrade() + "', "
					+ "volunteer.`status` = '" + status + "'";
			if (!volunteer.getAddress().isEmpty() && !volunteer.getAddress().equals("(no address)"))
				sql += ", volunteer.`address` = '" + volunteer.getAddress() + "'";
			if (!volunteer.getPhoneNumber().isEmpty() && !volunteer.getPhoneNumber().equals("(no phone number)"))
				sql += ", volunteer.`phone number` = '" + volunteer.getPhoneNumber() + "'";
			if (!volunteer.getSchool().isEmpty() && !volunteer.getSchool().equals("(no school)"))
				sql += ", volunteer.`school` = '" + volunteer.getSchool() + "'";
			if (!volunteer.getEmail().isEmpty() && !volunteer.getEmail().equals("(no email)"))
				sql += ", volunteer.`email address` = '" + volunteer.getEmail() + "'";
			sql += " WHERE volunteer.id = " + volunteer.getId();
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sql);
		        stmt.execute();
		        stmt.close();
		        queryVolunteers();
		        return true;
		    } catch (SQLException e) {
		    	Logger.output("Unable to update profile!");
		    	Logger.output(e);
		        e.printStackTrace();
		        return false;
		    }
		}
		return false;
	}
	
	public static boolean updateReport(Date date, int minutes, String description, int reportID) {
		if (connected){
			String sqlDate = "UPDATE wplhourlogs.report SET report.`date` = '" + DateFormatter.formatToSql(date)
					+ "' WHERE report.id = " + reportID;
			String sqlMins = "UPDATE wplhourlogs.record SET record.`value` = " + minutes
					+ " WHERE record.report_ID = " + reportID + " AND record.item_ID = " + items[0].getId();
			String sqlDesc = "UPDATE wplhourlogs.record SET record.`value` = '" + description
					+ "' WHERE record.report_ID = " + reportID + " AND record.item_ID = " + items[1].getId();
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sqlDate);
		        stmt.execute();
		        stmt.close();
		        stmt = conn.prepareStatement(sqlMins);
		        stmt.execute();
		        stmt.close();
		        stmt = conn.prepareStatement(sqlDesc);
		        stmt.execute();
		        stmt.close();
		        return true;
		    } catch (SQLException e) {
		    	Logger.output("Unable to update report!");
		    	Logger.output(e);
		        e.printStackTrace();
		        return false;
		    }
		}
		return false;
	}
	
	public static boolean deleteReport(int reportID) {
		if (connected){
			String sqlRecords = "DELETE FROM wplhourlogs.record WHERE record.report_ID = " + reportID;
			String sqlReport = "DELETE FROM wplhourlogs.report WHERE report.id = " + reportID;
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sqlRecords);
		        stmt.execute();
		        stmt.close();
		        stmt = conn.prepareStatement(sqlReport);
		        stmt.execute();
		        stmt.close();
		        return true;
		    } catch (SQLException e) {
		    	Logger.output("Unable to delete report!");
		    	Logger.output(e);
		        e.printStackTrace();
		        return false;
		    }
		}
		return false;
	}
	
	public static boolean isItem(String item) {
		for (int i = 0; i < items.length; i++) {
			if (items[i].getName().equals(item)) return true;
		}
		return false;
	}

	public static void insertRecords(Record[] records) {
		if (connected){
			String sql = "INSERT wplhourlogs.record (record.`value`, record.item_ID, record.report_ID) "
						+ "VALUES ";
			for (int i = 0; i < records.length; i++) {
				sql += "('" + records[i].getValue() + "'," + records[i].getItemID() + "," + records[i].getReportID() + ")";
				if (i < records.length -1) sql += ",";
			}
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sql);
		        stmt.execute();
		        stmt.close();
		    } catch (SQLException e) {
		    	Logger.output(e);
		        e.printStackTrace();
		        System.exit(0);
		    }
		}
	}
	
	public static int[] insertVolunteers(Volunteer[] volunteers) {
		if (connected){
			if (!checkDuplicateVolunteers(volunteers)) {
				int[] ids = new int[volunteers.length];
				
				for (int i = 0; i < volunteers.length; i++) {
					CallableStatement stmt;
					
					try {
						stmt = conn.prepareCall("call wplhourlogs.procInsertVolunteer(?,?,?,?,?,?,?,?)");
						stmt.setString(1, volunteers[i].getName());
						stmt.setInt(2, volunteers[i].getGrade());
						stmt.setBoolean(3, volunteers[i].getStatus());
						stmt.setString(4, (!volunteers[i].getAddress().equals("(no address)")) ? volunteers[i].getAddress() : "");
						stmt.setString(5, (!volunteers[i].getPhoneNumber().equals("(no phone number)")) ? volunteers[i].getPhoneNumber() : "");
						stmt.setString(7, (!volunteers[i].getSchool().equals("(no school)")) ? volunteers[i].getSchool() : "");
						stmt.setString(6, (!volunteers[i].getEmail().equals("(no email)")) ? volunteers[i].getEmail() : "");
						stmt.registerOutParameter(8, Types.INTEGER);
						stmt.executeQuery();
						ids[i] = stmt.getInt(8);
						stmt.close();
					} catch (SQLException e) {
						Logger.output("Unable to add a profile!");
						Logger.output(e);
				        e.printStackTrace();
				        return null;
				    } 
				}
				queryVolunteers();
				return ids;
				
			} else return null;
		} else return null;
	}
	
	public static int[] insertReports(ArrayList<Report> reports) {
		if (connected){
			int[] ids = new int[reports.size()];
				
			for (int i = 0; i < reports.size(); i++) {
				CallableStatement stmt;
				try {
					stmt = conn.prepareCall("call wplhourlogs.procInsertReport(?,?,?)");
					stmt.setString(1, reports.get(i).getName());
					stmt.setInt(2, reports.get(i).getVolunteerID());
					stmt.registerOutParameter(3, Types.INTEGER);
					stmt.executeQuery();
					ids[i] = stmt.getInt(3);
					stmt.close();
				} catch (SQLException e) {
					Logger.output(e);
				    e.printStackTrace();
				    System.exit(0);
			    } 
			}
				
			return ids;
				
		} else return null;
	}
	
	public static Report[] getReports(String volunteerName) {
		if (connected){
			String sql = buildReportSearch(volunteerName);
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sql);
		        stmt.execute();
		        ResultSet results = stmt.getResultSet();
		        results.last();
		        if (results.getRow() == 0) return null;
		        Report[] reports = new Report[results.getRow()];
		        results.first();
		        int i = 0;
		        int volunteerID = getVolunteerId(volunteerName);
		        while (!results.isAfterLast()) {
		        	reports[i] = new Report(results.getInt(1), results.getString(2), volunteerID);
		        	i++;
		        	results.next();
		        }
		        stmt.close();
		        return reports;
		    } catch (SQLException e) {
		    	Logger.output(e);
		        e.printStackTrace();
		    }
		}
		return null;
	}
	
	public static Record[] getRecords(int reportID) {
		if (connected){
			String sql = buildRecordSearch(reportID);
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sql);
		        stmt.execute();
		        ResultSet results = stmt.getResultSet();
		        results.last();
		        if (results.getRow() == 0) return null;
		        Record[] records = new Record[results.getRow()];
		        results.first();
		        int i = 0;
		        while (!results.isAfterLast()) {
		        	records[i] = new Record(results.getString(1), results.getInt(2), reportID);
		        	i++;
		        	results.next();
		        }
		        stmt.close();
		        return records;
		    } catch (SQLException e) {
		    	Logger.output(e);
		        e.printStackTrace();
		    }
		}
		return null;
	}
	
	public static ArrayList<Volunteer> search(MainWindow instance, SearchParam[] params, Date start, Date end, boolean includeInactive, boolean includeNonLogging) {
		ArrayList<Volunteer> volunteerList = new ArrayList<Volunteer>();
		if (connected){
			boolean queryExtraParams = false;
			for (SearchParam param : params) if (instance.isExtraParam(param.getFieldParam())) queryExtraParams = true;
			String sql = buildSearchQuery(params, start, end, includeInactive, includeNonLogging, queryExtraParams);
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sql);
		        stmt.executeQuery();
		        volunteerList = makeVolunteersFromSearch(stmt.getResultSet(), queryExtraParams);
		        stmt.close();
		    } catch (SQLException e) {
		    	Logger.output(e);
		    	e.printStackTrace();
		        System.exit(0);
		    }
		    return volunteerList;
		} else return null;
	}
	
	private static String buildSearchQuery(SearchParam[] params, Date start, Date end, boolean includeInactive, boolean includeNonLogging, boolean queryExtraParams) {
		String sql = "SELECT ";
		sql += Aliases.volunteer + ".ID, ";
		sql += Aliases.volunteer + ".`Name`, ";
		sql += Aliases.volunteer + ".Grade, ";
		sql += Aliases.volunteer + ".`Status`, ";
		sql += Aliases.volunteer + ".School, ";
		sql += Aliases.volunteer + ".`Email Address`, ";
		sql += Aliases.volunteer + ".`Phone Number`, ";
		sql += Aliases.volunteer + ".Address, ";
		sql += Aliases.report + ".ID, ";
		sql += Aliases.report + ".`Date`, ";
		sql += Aliases.record + ".ID, ";
		sql += Aliases.record + ".`Value`, ";
		sql += Aliases.record + ".item_ID";
		if (queryExtraParams) {
			sql += ", " + Aliases.minutes + ".`Value`, ";
			sql +=  Aliases.minutes + ".`item_ID`";
		}
		sql += " FROM wplhourlogs.record " + Aliases.record + " ";
		sql += "INNER JOIN wplhourlogs.report " + Aliases.report + " ON " + Aliases.report + ".id = " + Aliases.record + ".report_ID ";
		if (queryExtraParams) sql += "INNER JOIN wplhourlogs.minutes " + Aliases.minutes + " ON " + Aliases.minutes + ".reportID = " + Aliases.report + ".id ";
		sql += "RIGHT JOIN wplhourlogs.volunteer " + Aliases.volunteer + " ON " + Aliases.report + ".volunteer_ID = " + Aliases.volunteer + ".ID ";
		sql += "WHERE ";
		int validParams = 0;
		for (int i = 0; i < params.length; i++)
			if (!params[i].getPhrase().isEmpty()) {
				sql += (validParams == 0) ? params[i].toSql(true) : params[i].toSql(false);
				validParams++;
			}
		if (validParams != 0) sql += ") AND ";
		sql += "((" + Aliases.report + ".`Date` BETWEEN '"
				+ DateFormatter.formatToSql(start) +  "' AND '" + DateFormatter.formatToSql(end) + "')";
		if (includeNonLogging) sql += " OR ISNULL(" + Aliases.report + ".`date`))";
		else sql += ")";
		if (!includeInactive) sql += " AND " + Aliases.volunteer + ".`Status` = 1";
		return sql + " ORDER BY " + Aliases.volunteer + ".`name`, " + Aliases.report + ".`date` DESC, "
			+ Aliases.record + ".report_id, " + Aliases.record + ".item_id";
	}
	
	private static String buildVolunteerSearch(String name) {
		return "SELECT volunteer.id, volunteer.`name`, volunteer.grade, volunteer.`status`, volunteer.address, "
				+ "volunteer.`phone number`, volunteer.school, volunteer.`email address` FROM wplhourlogs.volunteer "
				+ "WHERE volunteer.`name` = '" + name + "'" ;
	}
	
	private static String buildReportSearch(String volunteerName) {
		return "SELECT report.id, report.`date` FROM wplhourlogs.report "
				+ "INNER JOIN wplhourlogs.volunteer ON volunteer.id = report.volunteer_id "
				+ "WHERE volunteer.`name` = '" + volunteerName + "' ORDER BY report.`date` DESC, report.id ASC" ;
	}
	
	private static String buildRecordSearch(int reportID) {
		return "SELECT record.`value`, record.item_ID FROM wplhourlogs.record "
				+ "INNER JOIN wplhourlogs.report ON report.id = record.report_ID "
				+ "WHERE report.id = " + reportID + " ORDER BY record.item_ID" ;
	}
	
	private static ArrayList<Volunteer> makeVolunteersFromSearch(ResultSet results, boolean extraParams) {
		ArrayList<Volunteer> volunteerList = new ArrayList<>();
		try {
			results.last();
	        if(results.getRow() == 0) return null;
	        results.first();
	        int lastID = -1;
	        while (!results.isAfterLast()) {
	        	int id = results.getInt(1);
	        	if (id != lastID) {
	        		volunteerList.add(new Volunteer(id, results.getString(2), results.getInt(3), results.getInt(4), results.getString(5),
	        				results.getString(6), results.getString(7), results.getString(8)));
	        		lastID = id;
	        	}
	        	results.next();
	        }
	        results.first();
	        int lastVolunteerID = -1;
	        int volunteerIndex = -1;
	        int lastReportID = -1;
	        while (!results.isAfterLast()) {
	        	int volunteerID = results.getInt(1);
	        	if (volunteerID != lastVolunteerID) {
	        		volunteerIndex++;
	        		lastVolunteerID = volunteerID;
	        	}
	        	int reportID = results.getInt(9);
	        	if (reportID != lastReportID) {
	        		volunteerList.get(volunteerIndex).addReport(new Report(reportID, results.getString(10), volunteerID));
	        		lastReportID = reportID;
	        	}
	        	results.next();
	        }
	        readRecords(results, volunteerList, extraParams);
		} catch (SQLException e) {
			Logger.output(e);
			e.printStackTrace();
			System.exit(0);
		}
        return volunteerList;
	}
	
	private static void readRecords(ResultSet results, ArrayList<Volunteer> volunteerList, boolean extraParams) throws SQLException {
		results.first();
		int lastVolunteerID = -1;
        int volunteerIndex = -1;
        int lastReportID = -1;
        int reportIndex = -1;
		if (extraParams) {
	        while (!results.isAfterLast()) {
	        	int volunteerID = results.getInt(1);
	        	if (volunteerID != lastVolunteerID) {
	        		volunteerIndex++;
	        		lastVolunteerID = volunteerID;
	        		reportIndex = -1;
	        	}
	        	int reportID = results.getInt(9);
	        	reportIndex++;
        		volunteerList.get(volunteerIndex).getReports().get(reportIndex)
    					.addRecord(new Record(results.getString(14), results.getInt(15), reportID));
        		volunteerList.get(volunteerIndex).getReports().get(reportIndex)
    					.addRecord(new Record(results.getString(12), results.getInt(13), reportID));
	        	results.next();
	        }
		} else {
	        while (!results.isAfterLast()) {
	        	int volunteerID = results.getInt(1);
	        	if (volunteerID != lastVolunteerID) {
	        		volunteerIndex++;
	        		lastVolunteerID = volunteerID;
	        		reportIndex = -1;
	        	}
	        	int reportID = results.getInt(9);
	        	if (reportID != lastReportID) {
	        		reportIndex++;
	        		lastReportID = reportID;
	        	}
	        	if (reportID == 0) reportIndex = 0;
	        	else {
	        		volunteerList.get(volunteerIndex).getReports().get(reportIndex)
	        			.addRecord(new Record(results.getString(12), results.getInt(13), reportID));
	        	}
	        	results.next();
	        }
		}
	}
	
	private static boolean matchesPreviousVolunteer(ArrayList<Volunteer> volunteers, int id) {
		if (volunteers == null) return false;
		for (Volunteer volunteer : volunteers) if (id == volunteer.getId()) return true;
		return false;
	}
	
	private static int getVolunteerIndex(ArrayList<Volunteer> volunteers, int id) {
		if (volunteers == null) return -1;
		for (int i = 0; i < volunteers.size(); i++) if (id == volunteers.get(i).getId()) return i;
		return -1;
	}
	
	public static int reportHours(ReportOption option, String phrase, Date start, Date end, boolean includeInactive, MainWindow instance) {
		ArrayList<Volunteer> volunteerList = new ArrayList<>();
		int total = -1;
		if (connected) {
			boolean isDescription = false;
			if (option == ReportOption.SPECIFIC_DESCRIPTION) isDescription = true;
			String sqlSearch = buildReportSearchQuery(option, phrase, start, end, includeInactive);
			String sqlReport = buildReportTotalQuery(option, phrase, start, end, includeInactive);
			try {
				PreparedStatement stmt = conn.prepareStatement(sqlSearch);
				stmt.executeQuery();
				volunteerList = makeVolunteersFromSearch(stmt.getResultSet(), isDescription);
				stmt.close();
				stmt = conn.prepareStatement(sqlReport);
				stmt.executeQuery();
				ResultSet result = stmt.getResultSet();
				result.first();
				total = result.getInt(1);
				stmt.close();
			} catch (SQLException e) {
				Logger.output(e);
			    e.printStackTrace();
			    System.exit(0);
		    }
		}
		instance.makeTree(volunteerList);
		return total;
	}
	
	private static String buildReportSearchQuery(ReportOption option, String phrase, Date start, Date end, boolean includeInactive) {
		String sql = "SELECT ";
		sql += Aliases.volunteer + ".ID, ";
		sql += Aliases.volunteer + ".`Name`, ";
		sql += Aliases.volunteer + ".Grade, ";
		sql += Aliases.volunteer + ".`Status`, ";
		sql += Aliases.volunteer + ".School, ";
		sql += Aliases.volunteer + ".`Email Address`, ";
		sql += Aliases.volunteer + ".`Phone Number`, ";
		sql += Aliases.volunteer + ".Address, ";
		sql += Aliases.report + ".ID, ";
		sql += Aliases.report + ".`Date`, ";
		sql += Aliases.record + ".ID, ";
		sql += Aliases.record + ".`Value`, ";
		sql += Aliases.record + ".item_ID";
		if (option == ReportOption.SPECIFIC_DESCRIPTION) {
			sql += ", " + Aliases.minutes + ".`Value`, ";
			sql +=  Aliases.minutes + ".`item_ID`";
		}
		sql += " FROM wplhourlogs.record " + Aliases.record + " ";
		sql += "INNER JOIN wplhourlogs.report " + Aliases.report + " ON " + Aliases.report + ".id = " + Aliases.record + ".report_ID ";
		if (option == ReportOption.SPECIFIC_DESCRIPTION)
				sql += "INNER JOIN wplhourlogs.minutes " + Aliases.minutes + " ON " + Aliases.minutes + ".reportID = " + Aliases.report + ".id ";
		sql += "INNER JOIN wplhourlogs.volunteer " + Aliases.volunteer + " ON " + Aliases.report + ".volunteer_ID = " + Aliases.volunteer + ".ID ";
		sql += "WHERE ";
		switch (option) {
			case ALL_VOLUNTEERS: break;
			case SPECIFIC_VOLUNTEER:
				sql += "(" + Aliases.volunteer + ".`name` LIKE '%" + phrase + "%') AND ";
				break;
			case SPECIFIC_DESCRIPTION:
				sql += "(" + Aliases.record + ".`value` LIKE '%" + phrase + "%') AND ";
		}
		sql += "((" + Aliases.report + ".`Date` BETWEEN '" + DateFormatter.formatToSql(start) +  "' AND '" + DateFormatter.formatToSql(end) + "'))";
		if (!includeInactive) sql += " AND " + Aliases.volunteer + ".`Status` = 1";
		return sql + " ORDER BY " + Aliases.volunteer + ".`name`, " + Aliases.report + ".`date` DESC, "
			+ Aliases.record + ".report_id, " + Aliases.record + ".item_id";
	}
	
	private static String buildReportTotalQuery(ReportOption option, String phrase, Date start, Date end, boolean includeInactive) {
		String sql = "SELECT SUM(";
		if (option == ReportOption.SPECIFIC_DESCRIPTION) sql += Aliases.minutes + ".`value`) FROM wplhourlogs.minutes " + Aliases.minutes + " ";
		else sql += Aliases.record + ".`value`) FROM wplhourlogs.record " + Aliases.record + " ";
		if (option == ReportOption.SPECIFIC_DESCRIPTION)
			sql += "INNER JOIN wplhourlogs.record " + Aliases.record + " ON " + Aliases.minutes + ".reportID = " + Aliases.record + ".report_id ";
		sql += "INNER JOIN wplhourlogs.report " + Aliases.report + " ON " + Aliases.report + ".id = " + Aliases.record + ".report_ID ";
		sql += "INNER JOIN wplhourlogs.volunteer " + Aliases.volunteer + " ON " + Aliases.report + ".volunteer_ID = " + Aliases.volunteer + ".ID ";
		sql += "WHERE (" + Aliases.record + ".`value` REGEXP ('[0-9]')) ";
		switch (option) {
			case ALL_VOLUNTEERS: 
				sql += "AND ";
				break;
			case SPECIFIC_VOLUNTEER:
				sql += "AND (" + Aliases.volunteer + ".`name` LIKE '%" + phrase + "%') AND ";
				break;
			case SPECIFIC_DESCRIPTION:
				sql += "AND (" + Aliases.record + ".`value` LIKE '%" + phrase + "%') AND ";
		}
		sql += "((" + Aliases.report + ".`Date` BETWEEN '" + DateFormatter.formatToSql(start) +  "' AND '" + DateFormatter.formatToSql(end) + "'))";
		if (!includeInactive) sql += " AND " + Aliases.volunteer + ".`Status` = 1";
		Logger.output(sql);
		return sql;
	}
	
	public static ArrayList<Volunteer> superServiceAlert() {
		ArrayList<Volunteer> volunteerList = new ArrayList<Volunteer>();
		if (connected){
			String sql = buildSuperServiceQuery();
		    try {
		    	PreparedStatement stmt = conn.prepareStatement(sql);
		        stmt.executeQuery();
		        ResultSet results = stmt.getResultSet();
		        results.last();
		        if (results.getRow() == 0) return volunteerList;
		        results.first();
		        while (!results.isAfterLast()) {
		        	Volunteer volunteer = new Volunteer(results.getString(1), results.getInt(2));
		        	volunteer.setEmail(results.getString(3));
		        	volunteerList.add(volunteer);
		        	results.next();
		        }
		        stmt.close();
		    } catch (SQLException e) {
		    	Logger.output(e);
		    	e.printStackTrace();
		        System.exit(0);
		    }
		    return volunteerList;
		} else return null;
	}
	
	private static String buildSuperServiceQuery() {
		String sql = "SELECT " + Aliases.volunteer + ".`name`, " +Aliases.volunteer + ".`grade`, "
								+ Aliases.volunteer + ".`email address` FROM wplhourlogs.volunteer " + Aliases.volunteer;
		sql += " WHERE (SELECT SUM(" + Aliases.record + ".`value`) FROM wplhourlogs.record " + Aliases.record;
		sql += " INNER JOIN wplhourlogs.report " + Aliases.report + " ON " + Aliases.report + ".id = " + Aliases.record + ".report_id";
		sql += " INNER JOIN wplhourlogs.volunteer " + Aliases.inner_volunteer + " ON " + Aliases.inner_volunteer + ".id = " + Aliases.report + ".volunteer_id";
		sql += " WHERE (" + Aliases.inner_volunteer + ".`name` = " + Aliases.volunteer + ".`name`) AND (" + Aliases.record + ".`value` REGEXP ('[0-9]'))";
		sql += ") >= " + FileSystemWatcher.getThreshold();
		return sql;
	}
	
	public static boolean updateGrades() {
		if (connected){
			String[] queries = buildUpdateGradesQueries();
		    try {
				for (String sql : queries) {
					PreparedStatement stmt = conn.prepareStatement(sql);
					stmt.execute();
					stmt.close();
				}
		        return true;
		    } catch (SQLException e) {
		    	Logger.output(e);
		    	e.printStackTrace();
		        return false;
		    }
		} else return false;
	}
	
	private static String[] buildUpdateGradesQueries() {
		String[] queries = new String[3];
		queries[0] = "SET SQL_SAFE_UPDATES = 0";
		queries[1] = "UPDATE wplhourlogs.volunteer SET volunteer.grade = volunteer.grade + 1 WHERE volunteer.`status` = 1";
		queries[2] = "UPDATE wplhourlogs.volunteer SET volunteer.`status` = 0 WHERE volunteer.grade > 12";
		return queries;
	}
	
	public static String checkSpecialChars(String str) {
 		for (int i = 0; i < specialChars.length; i++) {
			String currentChar = specialChars[i];
			int index = str.indexOf(currentChar);
			while (index > -1) {
				str = str.substring(0, index) + '\\' + str.substring(index);
				index = str.indexOf(currentChar, index+currentChar.length()+1);
			}
		}
		return str;
	}
	
	public static boolean dumpDatabase(String outFile) {
		if (connected) {
			try {
				Process mysqldump = Runtime.getRuntime().exec("\"C:/Program Files/MySQL/MySQL Server 5.7/bin/mysqldump\" --user=lucas "
						+ "--password=lucas -B -c --routines=TRUE wplhourlogs --result-file=" + outFile + FileSystemWatcher.backupfile);
				InputStream stream = mysqldump.getInputStream();
				mysqldump.waitFor();
				byte[] bytes = new byte[30000];
				stream.read(bytes);
				String str = "";
				for (Byte num : bytes) str += Byte.toString(num);
				Logger.output(str);
				return true;
			} catch(Exception e) {
				Logger.output("Unable to dump database!");
				Logger.output(e);
				return false;
			}
		} else return false;
	}
	
	public static boolean importDatabase(String inFile) {
		if (connected) {
			try {
				Process mysqldump = Runtime.getRuntime().exec("cmd -c '\"C:/Program Files/MySQL/MySQL Server 5.7/bin/mysql\" --user=lucas --password=lucas < "
								+ inFile + FileSystemWatcher.backupfile + "'");
				InputStream stream = mysqldump.getInputStream();
				mysqldump.waitFor();
				byte[] bytes = new byte[30000];
				stream.read(bytes);
				String str = "";
				for (Byte num : bytes) str += Byte.toString(num);
				Logger.output(str);
				return true;
			} catch(Exception e) {
				Logger.output("Unable to import database!");
				Logger.output(e);
				return false;
			}
		} else return false;
	}
	
}