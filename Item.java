
public class Item extends ReferencedRow {

	private int datatypeID;
	
	public Item(int id, String name, int datatypeID) {
		super(id, name);
		this.datatypeID = datatypeID;
	}
	
	public int getDatatypeID() {
		return datatypeID;
	}
	
	public String toString() {
		return super.getName();
	}
	
}
