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

}
