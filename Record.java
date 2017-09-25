
public class Record {

	private String value;
	private int itemID;
	private int reportID;
	
	public Record(String value, int itemID, int reportID) {
		super();
		this.value = value;
		this.itemID = itemID;
		this.reportID = reportID;
	}

	public String getValue() {
		return value;
	}

	public int getItemID() {
		return itemID;
	}

	public int getReportID() {
		return reportID;
	}
	
	@Override
	public String toString() {
		Item[] items = DatabaseManager.getItems();
		for (int i = 0; i < items.length; i++) {
			if (itemID == items[i].getId()) {
				if (items[i].getName().equals("Minutes")) {
					int hours = Integer.parseInt(value)/60;
					int mins = Integer.parseInt(value)%60;
					String hourStr = " Hour";
					if (hours > 1) hourStr = hours + hourStr + "s ";
					else if (hours == 0) hourStr = "";
					else hourStr = hours + hourStr + " ";
					if (mins == 0) return hourStr;
					else return hourStr + mins + " Minutes ";
				} else return items[i].getName() + ": " + value;
			}
		}
		return "";
	}
	
}
