package odata.jpa;

import static org.junit.Assert.*;

import org.junit.Test;

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
		String str2 = helper.parseOrderByClause(str);
		assertNotNull(str2);
		assertEquals("abcdEfg.hilmNopq desc, paperino asc", str2.trim());
	}

	@Test
	public void parseOrderByClause2() {
		String str = "AbcdEfg/HilmNopq,Paperino asc";
		String str2 = helper.parseOrderByClause(str);
		assertNotNull(str2);
		assertEquals("abcdEfg.hilmNopq asc, paperino asc", str2.trim());
	}

	@Test
	public void parseOrderByClause3() {
		String str = null;
		String str2 = helper.parseOrderByClause(str);
		assertNull(str2);
	}

	@Test
	public void filters1() {
		String filter = "1 eq 2";
		String jpql = helper.parseFilterClause(filter);
		assertEquals("1 = 2", jpql);
	}

	@Test
	public void filters2() {
		String filter = "Obj/Prop lt 57.0";
		String jpql = helper.parseFilterClause(filter);
		assertEquals("obj.prop < 57.0", jpql);
	}

	@Test
	public void filters3() {
		String filter = "((Pluto add Pippo)gt 7)and(Pluto ne '33')";
		String jpql = helper.parseFilterClause(filter);
		assertEquals("((pluto + pippo) > 7) AND (pluto != '33')", jpql);
	}

	@Test
	public void filters4() {
		String filter = "tolower(Pluto)eq'x'";
		String jpql = helper.parseFilterClause(filter);
		assertEquals(" LOWER(pluto) = 'x'", jpql);
	}

	@Test
	public void filters5() {
		String filter = "contains(Pluto,'somthg')";
		String jpql = helper.parseFilterClause(filter);
		assertEquals("pluto LIKE '%somethg%'", jpql);
	}
}
