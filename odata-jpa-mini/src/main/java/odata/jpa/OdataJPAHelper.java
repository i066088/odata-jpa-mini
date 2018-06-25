package odata.jpa;

import javax.ejb.Stateless;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import odata.antlr.ODataParserLexer;
import odata.antlr.ODataParserParser;

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

		// TODO use ExpressionVisitor instead

		StringBuilder orderbyCondition = new StringBuilder();
		if (orderby != null && !orderby.trim().isEmpty()) {
			String comma = "";
			String[] orderbyPieces = orderby.split(",");
			for (String piece : orderbyPieces) {
				piece = piece.trim();
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
	 * Convert $filter clause to JPA WHERE clause (without any semantical check).
	 * 
	 * @param orderby
	 * @return
	 */
	public String parseFilterClause(String filter) {
		if (filter == null)
			return null;

		// Init lexer
		ODataParserLexer lexer = new ODataParserLexer(new ANTLRInputStream(filter));
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

		// Get a list of matched tokens
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		// Init parser with got tokens
		ODataParserParser parser = new ODataParserParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(ThrowingErrorListener.INSTANCE);

		// Get the context
		ParseTree tree = parser.clause();
		// here, the input has already been read and parsed

		// FIXME should throw exception if parse was not successful!

		// Run the Visitor
		ExpressionVisitor visitor = new ExpressionVisitor();
		String jpql = visitor.visit(tree);

		System.out.println("DEBUG HERE jpql=" + jpql);

		return jpql;
	}

	/**
	 * Convert an attribute from OData to JPA form. For example, Address/Street
	 * becomes address.street .
	 * 
	 * @param attribute
	 * @return
	 */
	public String parseAttribute(String attribute) {
		if (attribute == null || "".equals(attribute))
			return null;

		// TODO use ExpressionVisitor instead

		String[] pieces = attribute.split("/");
		StringBuilder sb = new StringBuilder();
		String dot = "";
		for (String piece : pieces) {
			piece = piece.trim();
			sb.append(dot).append(firstToLower(piece));
			dot = ".";
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
