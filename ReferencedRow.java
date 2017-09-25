
public abstract class ReferencedRow {

	private int id;
	private String name;
	// name as in row name
	
	public ReferencedRow(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public ReferencedRow(String name) {
		this.id = -1;
		this.name = name;
	}

	public int getId() {
		return id;
	}


	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object o) {
		ReferencedRow row = (ReferencedRow) o;
		if (row.getId() == id) return true;
		else return false;
	}
	
}