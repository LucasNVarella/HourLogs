public class SearchParam {

	public static final String label = "in...";
	public static final String[] joinParams = {"And", "Or"};
	public static final String[] containsParams = {"Contains", "Does Not Contain"};
	public static final String[] standardFieldParams = {"Name", "Address", "Email", "Phone Number", "Grade", "Status", "School"};
	
	private String joinParam;
	private String containsParam;
	private String fieldParam;
	private String phrase;
	
	public SearchParam(String containsParam, String fieldParam, String phrase) {
		this.joinParam = "";
		this.containsParam = containsParam;
		this.fieldParam = fieldParam;
		this.phrase = phrase;
	}

	public SearchParam(String joinParam, String containsParam, String fieldParam, String phrase) {
		this.joinParam = joinParam;
		this.containsParam = containsParam;
		this.fieldParam = fieldParam;
		this.phrase = phrase;
	}
	
/*	public String toSql(boolean first) {
		String condition= "";
		if (!first) if (!(joinParam == null)) condition += " " + joinParam.toUpperCase() + " ";
		if (containsParam.equals("Does Not Contain")) {
			condition += "NOT(";
		} else if (containsParam.equals("Day")) {
			if (isNumeric(phrase)) condition += "DAYNAME(";
			else condition += "DAYOFMONTH(";
		} else if (containsParam.equals("Month")) {
			if (isNumeric(phrase)) condition += "MONTHNAME(";
			else condition += "MONTH(";
		} else if (containsParam.equals("Year")) {
			condition += "YEAR(";
		}
		String column;
		String alias;
		if (DatabaseManager.isItem(fieldParam)) {
			column = "Value";
			alias = DatabaseManager.Aliases.record;
		} else {
			column = fieldParam;
			if (fieldParam.equals("Day")
					|| fieldParam.equals("Month")
					|| fieldParam.equals("Year")) alias = DatabaseManager.Aliases.report;
			else alias = DatabaseManager.Aliases.volunteer;
		}
		condition += alias + ".`" + column + "`";
		if (containsParam.equals("Day")
				|| containsParam.equals("Month")
				|| containsParam.equals("Year")) condition += ") LIKE '" + phrase + "'";
		else condition += " LIKE '%" + phrase + "%'";
		if (containsParam.equals("Does Not Contain")) condition += ")";
		return condition;
	}*/
	
	public String toSql(boolean first) {
		String condition= "";
		if (!first) {
			if (!(joinParam == null)) condition += " " + joinParam.toUpperCase() + " ";
		} else condition += "(";
		if (containsParam.equals("Does Not Contain")) condition += "NOT(";
		String column;
		String alias;
		if (DatabaseManager.isItem(fieldParam)) {
			column = "Value";
			alias = DatabaseManager.Aliases.record;
		} else {
			column = fieldParam;
			alias = DatabaseManager.Aliases.volunteer;
		}
		if (column.equals("Email")) column += " Address";
		condition += alias + ".`" + column + "` ";
		condition += "LIKE '%" + DatabaseManager.checkSpecialChars(phrase) + "%'";
		if (column.equals("Minutes")) condition += " AND " + DatabaseManager.Aliases.record + "`" + column + "` REGEXP ('[0-9]')";
		if (column.equals("Description")) condition += " AND NOT(" + DatabaseManager.Aliases.record + "`" + column + "` REGEXP ('[0-9]'))";
		if (containsParam.equals("Does Not Contain")) condition += ")";
		return condition;
	}
	
	@Override
	public String toString() {
		return containsParam + " " + phrase + " " + label + " " + fieldParam;
	}

	public String getJoinParam() {
		return joinParam;
	}

	public void setJoinParam(String joinParam) {
		this.joinParam = joinParam;
	}

	public String getContainsParam() {
		return containsParam;
	}

	public void setContainsParam(String containsParam) {
		this.containsParam = containsParam;
	}

	public String getFieldParam() {
		return fieldParam;
	}

	public void setFieldParam(String fieldParam) {
		this.fieldParam = fieldParam;
	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}
	
}