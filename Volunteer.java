import java.util.ArrayList;
import java.util.Collection;

public class Volunteer extends ReferencedRow {

	private int grade;
	private boolean status;
	private String address;
	private String phoneNumber;
	private String school;
	private String email;
	private ArrayList<Report> reports;
	
	public Volunteer(int id, String name, int grade, int status) {
		super(id, name);
		this.grade = grade;
		if (status == 1) this.status = true;
		else this.status = false;
		reports = new ArrayList<>();
		address = "";
		phoneNumber = "";
		school = "";
		email = "";
	}
	
	public Volunteer(int id, String name, int grade, int status, String school, String email, String phoneNumber,
			String address) {
		super(id, name);
		this.grade = grade;
		if (status == 1) this.status = true;
		else this.status = false;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.school = school;
		this.email = email;
		reports = new ArrayList<>();
	}

	public Volunteer(int id, String name, int grade, int status, String school, String email, String phoneNumber,
			String address, ArrayList<Report> reports) {
		super(id, name);
		this.grade = grade;
		if (status == 1) this.status = true;
		else this.status = false;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.school = school;
		this.email = email;
		this.reports = reports;
	}

	public Volunteer(String name, int grade) {
		super(name);
		this.grade = grade;
		status = true;
		reports = new ArrayList<>();
		address = "";
		phoneNumber = "";
		school = "";
		email = "";
	}

	public int getGrade() {
		return grade;
	}

	public boolean getStatus() {
		return status;
	}
	
	public String getAddress() {
		if (address == null || address.isEmpty()) return "(no address)";
		else return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhoneNumber() {
		if (phoneNumber == null || phoneNumber.isEmpty() || phoneNumber.equals("(   )    -    ")) return "(no phone number)";
		else return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getSchool() {
		if (school == null || school.isEmpty()) return "(no school)";
		else return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public String getEmail() {
		if (email == null || email.isEmpty() || email.equals("null")) return "(no email)";
		else return email;
	}

	public void setEmail(String email) {
		if (email.contains("@") && email.contains(".")) this.email = email;
	}

	public void setGrade(int grade) {
		if (grade > 0) this.grade = grade;
	}
	
	public void setStatus(boolean status) {
		this.status = status;
	}

	@Override
	public boolean equals(Object volunteer) {
		if (((ReferencedRow)volunteer).getName().equals(super.getName())
				&& ((ReferencedRow)volunteer).getId() == super.getId()) return true;
		else return false;
	}
	
	@Override
	public String toString() {
		String status = "Active";
		if (!this.status) status = "Inactive";
		String output = super.getName() + " - Grade: " + grade + " - " + status + " - " + school + " - " + email + " - " + phoneNumber + " - " + address;
		for (Report report : reports) output += "\n" + report.toString(true);
		return output;
	}

	public ArrayList<Report> getReports() {
		return reports;
	}

	public void setReports(ArrayList<Report> reports) {
		this.reports = reports;
	}
	
	public boolean addReport(Report report) {
		return reports.add(report);
	}
	
	public boolean addReports(Collection<Report> reports) {
		return this.reports.addAll(reports);
	}
	
	public boolean removeReport(Report report) {
		return reports.remove(report);
	}
	
	public Report removeReport(int index) {
		return reports.remove(index);
	}
	
	public boolean removeReports(Collection<Report> reports) {
		return this.reports.removeAll(reports);
	}
	
	public Report setReport(int index, Report report) {
		return reports.set(index, report);
	}
		
}