import java.util.ArrayList;
import java.util.Collection;

public class Report extends ReferencedRow {

	private int volunteerID;
	private ArrayList<Record> records;
	
	public Report(int id, String date, int volunteerID) {
		super(id, date);
		this.volunteerID = volunteerID;
		records = new ArrayList<>();
	}
	
	public Report(String date, int volunteerID) {
		super(date);
		this.volunteerID = volunteerID;
		records = new ArrayList<>();
	}
	
	public Report(int id, String name, int volunteerID, ArrayList<Record> records) {
		super(id, name);
		this.volunteerID = volunteerID;
		this.records = records;
	}

	public String getDate() {
		String[] parts = super.getName().split("-");
		String date = parts[1] + "/" + parts[2] + "/" + parts[0];
		return date;
	}
	
	public Report setDate(String date) {
		return new Report(super.getId(), date, this.volunteerID);
	}
	
	public int getVolunteerID() {
		return volunteerID;
	}

	public Report setVolunteerID(int volunteerID) {
		return new Report(super.getId(), super.getName(), volunteerID);
	}

	public ArrayList<Record> getRecords() {
		return records;
	}

	public void setRecords(ArrayList<Record> records) {
		this.records = records;
	}
	
	public boolean addRecord(Record record) {
		return records.add(record);
	}
	
	public boolean addRecords(Collection<Record> records) {
		return this.records.addAll(records);
	}
	
	public boolean removeRecord(Record record) {
		return records.remove(record);
	}
	
	public Record removeRecord(int index) {
		return records.remove(index);
	}
	
	public boolean removeRecords(Collection<Record> records) {
		return this.records.removeAll(records);
	}
	
	public Record setRecord(int index, Record record) {
		return records.set(index, record);
	}
	
	@Override
	public String toString() {
		return getDate();
	}
	
	public String toString(boolean expanded) {
		String output = "\t" + super.getName() + " -> ";
		for (Record record : records) output += record.toString() + " - ";
		output = output.substring(0, output.length()-3);
		return output;
	}
	
}