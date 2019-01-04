package odata.jpa;

import static org.junit.Assert.*;

import org.junit.Test;

import odata.jpa.antlr.OdataJPAHelper;

public class TestOdataJPAHelper {

	OdataJPAHelper helper = new OdataJPAHelper();

	@Test
	public void firstToLower1() {
		String str = "PaperinoEPaperina";
		String str2 = helper.firstToLower(str);
		assertEquals("paperinoEPaperina", str2);
	}

	@Test
	public void firstToLower2() {
		String str = null;
		String str2 = helper.firstToLower(str);
		assertNull(str2);
	}

	@Test
	public void firstToLower3() {
		String str = "";
		String str2 = helper.firstToLower(str);
		assertEquals("", str2);
	}

	@Test
	public void firstToLower4() {
		String str = "A";
		String str2 = helper.firstToLower(str);
		assertEquals("a", str2);
	}

	@Test
	public void parseAttribute1() {
		String str = "AbcdEfg/HilmNopq";
		String str2 = helper.parseAttribute(str);
		assertEquals("abcdEfg.hilmNopq", str2);
	}

	@Test
	public void parseAttribute2() {
		String str = "AbcdEfg";
		String str2 = helper.parseAttribute(str);
		assertEquals("abcdEfg", str2);
	}

	@Test
	public void parseAttribute3() {
		String str = null;
		String str2 = helper.parseAttribute(str);
		assertNull(str2);
	}

	@Test
	public void parseOrderByClause1() {
		String str = "AbcdEfg/HilmNopq desc,Paperino";
		String str2 = helper.parseOrderByClause(str, "x", null);
		assertNotNull(str2);
		assertEquals("x.abcdEfg.hilmNopq desc, x.paperino asc", str2.trim());
	}

	@Test
	public void parseOrderByClause2() {
		String str = "AbcdEfg/HilmNopq,Paperino asc";
		String str2 = helper.parseOrderByClause(str, "x", null);
		assertNotNull(str2);
		assertEquals("x.abcdEfg.hilmNopq asc, x.paperino asc", str2.trim());
	}

	@Test
	public void parseOrderByClause3() {
		String str = null;
		String str2 = helper.parseOrderByClause(str, "x", null);
		assertNull(str2);
	}

	@Test
	public void filtersBasics() {
		String filter = "1 eq 2";
		String jpql = helper.parseFilterClause(filter, "x", null);
		assertEquals("1 = 2", jpql);
	}

	@Test
	public void filtersBasics2() {
		String filter = "Prop le -2";
		String jpql = helper.parseFilterClause(filter, "x", null);
		assertEquals("x.prop <= -2", jpql);
	}

	@Test
	public void filtersProperty() {
		String filter = "Obj/Prop lt 57.0";
		String jpql = helper.parseFilterClause(filter, "x", null);
		assertEquals("x.obj.prop < 57.0", jpql);
	}

	@Test
	public void filtersParenthesis() {
		String filter = "((Pluto add Pippo)gt 7)and not(Pluto ne '33')";
		String jpql = helper.parseFilterClause(filter, "x", null);
		assertEquals("((x.pluto + x.pippo) > 7) AND  NOT (x.pluto != '33')", jpql);
	}

	@Test
	public void filtersTolower() {
		String filter = "tolower(Pluto)eq'x'";
		String jpql = helper.parseFilterClause(filter, "x", null);
		assertEquals("LOWER(x.pluto) = 'x'", jpql);
	}

	@Test
	public void filtersContains() {
		String filter = "contains(Pluto , 'something')";
		String jpql = helper.parseFilterClause(filter, "x", null);
		assertEquals("x.pluto LIKE '%something%'", jpql);
	}

	@Test
	public void filtersAny() {
		String filter = "Bars/any(y:y/Description eq 'Roma')";
		String jpql = helper.parseFilterClause(filter, "x", null);
		assertEquals(" EXISTS (SELECT y FROM x.bars y WHERE y.description = 'Roma')", jpql);
	}

	@Test
	public void filtersDates() {
		String filter = "BirthDay gt date'2000-01-01'";
		String jpql = helper.parseFilterClause(filter, "x", null);
		assertEquals("x.birthDay > {d '2000-01-01'}", jpql);
	}

}
