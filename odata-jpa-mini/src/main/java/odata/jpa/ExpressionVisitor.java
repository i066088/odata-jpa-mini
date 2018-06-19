package odata.jpa;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;

import odata.antlr.ODataParserBaseVisitor;
import odata.antlr.ODataParserParser;

/**
 * ANTLR 4.7 visitor for Expressions. Its purpose is to parse expressions in
 * $filter, $orderby and similar parameters, and not the whole URL.
 * 
 * For security reasons, we do not accept functions we do not know.
 * 
 * @author Luca Vercelli 2018
 *
 */
public class ExpressionVisitor extends ODataParserBaseVisitor<String> {

	static Map<String, String> operators = new HashMap<String, String>();
	static List<String> methodsCalledWithFUNCTION;

	static {

		// Logic operators
		operators.put("NOT", "NOT");
		operators.put("AND", "AND");
		operators.put("OR", "OR");
		operators.put("EQ", "=");
		operators.put("NE", "!=");
		// Arithmetic operators
		operators.put("LT", "<");
		operators.put("LE", "<=");
		operators.put("GT", ">");
		operators.put("GE", ">=");
		operators.put("ADD", "+");
		operators.put("SUB", "-");
		operators.put("MUL", "*");
		operators.put("DIV", "/");
		operators.put("MOD", "MOD");
		// Arithmetic functions - not supported by JPQL
		operators.put("ROUND", "ROUND");
		operators.put("FLOOR", "FLOOR");
		operators.put("CEILING", "CEILING");
		// String functions
		operators.put("LENGTH", "LENGTH");
		operators.put("INDEXOF", "LOCATE");
		operators.put("SUBSTRING", "SUBSTRING");
		operators.put("TOLOWER", "LOWER");
		operators.put("TOUPPER", "UPPER");
		operators.put("TRIM", "TRIM");
		operators.put("CONCAT", "CONCAT");
		// String functions - not supported by JPQL
		operators.put("CONTAINS", "CONTAINS");
		operators.put("STARTSWITH", "STARTSWITH");
		operators.put("ENDSWITH", "ENDSWITH");
		operators.put("SUBSTRINGOF", "SUBSTRINGOF");
		operators.put("REPLACE", "REPLACE");
		// Date functions
		operators.put("DATE", "CURRENT_DATE");
		operators.put("TIME", "CURRENT_TIME");
		operators.put("NOW", "CURRENT_TIMESTAMP");
		// Date functions - not supported by JPQL
		operators.put("YEAR", "YEAR");
		operators.put("YEARS", "YEAR");
		operators.put("MONTH", "MONTH");
		operators.put("MONTHS", "MONTH");
		operators.put("DAY", "DAY");
		operators.put("DAYS", "DAY");
		operators.put("HOUR", "HOUR");
		operators.put("HOURS", "HOUR");
		operators.put("MINUTE", "MINUTE");
		operators.put("MINUTES", "MINUTE");
		operators.put("SECOND", "SECOND");
		operators.put("SECONDS", "SECOND");
		// Types - I don't even know what they mean. TODO
		operators.put("ISOF", "???");
		operators.put("CAST", "???");

		methodsCalledWithFUNCTION = Arrays.asList(new String[] { "ROUND", "FLOOR", "CEILING", "REPLACE", "YEAR",
				"MONTH", "DAY", "HOUR", "MINUTE", "SECOND" });
	}

	SimpleDateFormat edmDate = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat edmDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	SimpleDateFormat jpqlDate = new SimpleDateFormat("{'d' ''yyyy-MM-dd''}");
	SimpleDateFormat jpqlTime = new SimpleDateFormat("{'t' ''HH:mm:ss''}");
	SimpleDateFormat jpqlDateTime = new SimpleDateFormat("{'ts' ''yyyy-MM-dd HH:mm:ss''}");
	SimpleDateFormat hqlDate = new SimpleDateFormat("''yyyy-MM-dd''");
	SimpleDateFormat hqlTime = new SimpleDateFormat("''HH:mm:ss''");
	SimpleDateFormat hqlDateTime = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss''");

	/**
	 * DEBUG procedure
	 * 
	 * @param tree
	 */
	public String printContext(ParseTree tree) {
		if (tree instanceof ErrorNode) {
			ErrorNode node = (ErrorNode) tree;
			return "ErrorNode(" + node.getSymbol() + ")";
		} else {
			StringBuffer sb = new StringBuffer(tree.getClass().getSimpleName()).append("(");
			for (int i = 0; i < tree.getChildCount(); ++i) {
				ParseTree child = tree.getChild(i);
				if (i != 0)
					sb.append(", ");
				sb.append(printContext(child));
			}
			sb.append(")");
			return sb.toString();
		}
	}

	@Override
	public String visitClause(ODataParserParser.ClauseContext ctx) {
		System.out.println("0");
		System.out.println("ctx=" + printContext(ctx));
		System.out.println("gettext=" + ctx.getText());
		System.out.println("children=" + ctx.children);
		return visitChildren(ctx);
	}

	@Override
	public String visitBinaryOperatorClause(ODataParserParser.BinaryOperatorClauseContext ctx) {
		System.out.println("1");
		System.out.println("ctx=" + printContext(ctx));
		System.out.println("gettext=" + ctx.getText());
		System.out.println("children=" + ctx.children);
		return visitChildren(ctx);
	}

	@Override
	public String visitBinaryExpr(ODataParserParser.BinaryExprContext ctx) {
		System.out.println("2");
		System.out.println("ctx=" + printContext(ctx));
		System.out.println("gettext=" + ctx.getText());
		System.out.println("children=" + ctx.children);
		return visitChildren(ctx);
	}

	@Override
	public String visitEqClause(ODataParserParser.EqClauseContext ctx) {
		System.out.println("3");
		System.out.println("ctx=" + printContext(ctx));
		System.out.println("gettext=" + ctx.getText());
		System.out.println("children=" + ctx.children);
		return visitChildren(ctx);
	}

}
