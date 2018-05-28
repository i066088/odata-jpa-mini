package odata.jpa;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.Stateless;

@Stateless
public class OdataJPAHelper {

	/**
	 * Convert $orderby clause to JPA ORDER BY clause (without any semantical
	 * check).
	 * 
	 * @param orderby
	 * @return
	 */
	public String parseOrderByClause(String orderby) {
		if (orderby == null)
			return null;

		StringBuilder orderbyCondition = new StringBuilder();
		if (orderby != null && !orderby.trim().isEmpty()) {
			orderbyCondition.append(" order by ");
			String comma = "";
			String[] orderbyPieces = orderby.split(",");
			for (String piece : orderbyPieces) {
				String[] attrAndAsc = piece.split(" ");
				if (attrAndAsc.length > 2 || attrAndAsc.length == 0 || attrAndAsc[0] == null)
					throw new IllegalArgumentException("Syntax error in $orderby condiction");
				String attr = parseAttribute(attrAndAsc[0]);
				if (attr == null)
					throw new IllegalArgumentException(
							"Syntax error in $orderby condiction: expected attribute instead of: " + attr);
				String asc = (attrAndAsc.length == 2) ? attrAndAsc[1].toLowerCase() : "asc";
				if (!"asc".equals(asc) && !"desc".equals(asc))
					throw new IllegalArgumentException("Syntax error in $orderby condiction: expected asc or desc");
				orderbyCondition.append(comma).append(" ").append(attr).append(" ").append(asc);
				comma = ",";
			}
		}
		return orderbyCondition.toString();
	}

	/**
	 * Convert $filter clause to JPA WHERE clause (without any semantical
	 * check).
	 * 
	 * @param orderby
	 * @return
	 */
	public String parseFilterClause(String filter) {
		if (filter == null)
			return null;

		// TODO
		return filter;
	}

	private static Pattern attributeMatcher = Pattern.compile("([a-zA-Z_]\\w*)(/[a-zA-Z_]\\w*)*");

	/**
	 * Convert an attribute from OData to JPA form. For example, Address/Street
	 * becomes address.street .
	 * 
	 * @param attribute
	 * @return
	 */
	public String parseAttribute(String attribute) {
		Matcher matcher = attributeMatcher.matcher(attribute);
		if (!matcher.matches())
			return null;

		StringBuilder sb = new StringBuilder();
		sb.append(matcher.group(1));
		for (int i = 1; i < matcher.groupCount(); ++i) {
			sb.append('.').append(matcher.group(i));
		}
		return sb.toString();
	}

	/**
	 * Convert first letter into lowercase.
	 * 
	 * @param s
	 * @return
	 */
	public String firstToLower(String s) {
		if (s == null || s.length() == 0)
			return s;
		return Character.toLowerCase(s.charAt(0)) + s.substring(1);
	}
}
