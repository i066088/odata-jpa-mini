package odata.jpa;

import odata.antlr.ODataParserBaseVisitor;
import odata.antlr.ODataParserParser;

public class ExpressionVisitor extends ODataParserBaseVisitor<String> {

	public String visitAllExpr(ODataParserParser.AllExprContext ctx) {
		return "PIPPO";
	}
}
