// Copyright (c) 2003-2013, Jodd Team (jodd.org). All Rights Reserved.

package jodd.lagarto.dom;

import jodd.io.FastCharArrayWriter;
import org.junit.Test;

import static org.junit.Assert.*;

public class DomBuilderTest {

	@Test
	public void testSimpleDomCreation() {
		String page = "<html><body><p id=\"w173\">Hello<br>Jodd</p></body></html>";

		Document root = new LagartoDOMBuilder().parse(page);

		assertNotNull(root);
		assertEquals(1, root.getChildNodesCount());

		Element html = (Element) root.getFirstChild();
		assertEquals(Node.NodeType.ELEMENT, html.getNodeType());
		assertNotNull(html);
		assertEquals("html", html.getNodeName());
		assertEquals(1, html.getChildNodesCount());
		assertEquals(0, html.getAttributesCount());
		assertEquals(0, html.getOffset());

		Element body = (Element) html.getFirstChild();
		assertEquals(Node.NodeType.ELEMENT, body.getNodeType());
		assertNotNull(body);
		assertEquals("body", body.getNodeName());
		assertEquals(1, body.getChildNodesCount());
		assertNull(body.getAttribute("id"));
		assertEquals(0, body.getAttributesCount());
		assertEquals(6, body.getOffset());

		Element p = (Element) body.getChild(0);
		assertNotNull(p);
		assertEquals("p", p.getNodeName());
		assertEquals(3, p.getChildNodesCount());
		assertEquals("w173", p.getAttribute("id"));
		assertEquals(1, p.getAttributesCount());
		assertEquals(12, p.getOffset());

		Attribute attr = p.getAttribute(0);
		assertEquals("id", attr.getName());
		assertEquals("w173", attr.getValue());
		assertTrue(p.hasAttribute("id"));

		Text t = (Text) p.getChild(0);
		assertEquals(Node.NodeType.TEXT, t.getNodeType());
		assertEquals("Hello", t.getNodeValue());
		assertEquals(25, t.getOffset());

		Element br = (Element) p.getChild(1);
		assertEquals(0, br.getChildNodesCount());
		assertEquals(0, br.getAttributesCount());
		assertEquals(30, br.getOffset());

		t = (Text) p.getChild(2);
		assertEquals("Jodd", t.getNodeValue());
		assertEquals(34, t.getOffset());

		String generated = root.getHtml();

		assertEquals(page, generated);

		assertTrue(root.check());
	}

	@Test
	public void testAllTypesOffset() {
		String page = "<!DOCTYPE html><html>text<!--comment--><xmp></xmp><style type>xx</style>";

		Document root = new LagartoDOMBuilder().parse(page);
		assertEquals(-1, root.getOffset());

		DocumentType documentType = (DocumentType) root.getChild(0);
		assertEquals(0, documentType.getOffset());

		Element html = (Element) root.getChild(1);
		assertEquals(15, html.getOffset());

		Text text = (Text) html.getChild(0);
		assertEquals(21, text.getOffset());

		Comment comment = (Comment) html.getChild(1);
		assertEquals(25, comment.getOffset());

		Element xmp = (Element) html.getChild(2);
		assertEquals(39, xmp.getOffset());

		Element style = (Element) html.getChild(3);
		assertEquals(50, style.getOffset());
	}


	@Test
	public void testClone() {
		String page = "<html><body><p id=\"w173\">Hello<br>Jodd</p></body></html>";
		Document root = new LagartoDOMBuilder().parse(page);

		String generated = root.getHtml();
		assertEquals(page, generated);

		Document rootClone = root.clone();
		String generated2 = rootClone.getHtml();
		assertEquals(page, generated2);
		assertNotSame(root, rootClone);
		assertNotSame(root.getChild(0), rootClone.getChild(0));

		assertTrue(root.check());
	}

	@Test
	public void testSiblingsAndNames() {
		Document document = new LagartoDOMBuilder().parse("<div id='top'><p id='id1'>one</p>text1<p id='id2'>two</p>text2<p id='id3'>three</p>text3</div>");
		Element topDiv = (Element) document.getFirstChild();
		Element p1 = (Element) topDiv.getFirstChild();
		assertEquals("id1", p1.getAttribute("id"));

		Text t1 = (Text) p1.getNextSibling();
		assertEquals("text1", t1.getNodeValue());

		Element p2 = (Element) p1.getNextSiblingElement();
		assertEquals("id2", p2.getAttribute("id"));

		Element p3 = (Element) p2.getNextSiblingElement();
		assertEquals("id3", p3.getAttribute("id"));

		assertNull(p3.getNextSiblingElement());
		assertNotNull(p3.getNextSibling());

		Text t3 = (Text) t1.getNextSiblingElement().getNextSiblingElement().getNextSibling();
		assertEquals("text3", t3.getNodeValue());

		// previous
		assertEquals(t1, t3.getPreviousSiblingElement().getPreviousSiblingElement().getPreviousSibling());
		assertEquals(p1, p2.getPreviousSiblingElement());
		assertEquals(t1, p2.getPreviousSibling());

		// names
		assertEquals(p2, p1.getNextSiblingName());
		assertEquals(p3, p2.getNextSiblingName());
		assertNull(p3.getNextSiblingName());

		assertEquals(p2, p3.getPreviousSiblingName());
		assertEquals(p1, p2.getPreviousSiblingName());
		assertNull(p1.getPreviousSiblingName());

		// childs
		assertEquals(p1, topDiv.getFirstChild());
		assertEquals(p1, topDiv.getFirstChildElement());
		assertEquals(p1, topDiv.getFirstChildElement("p"));

		assertEquals(t3, topDiv.getLastChild());
		assertEquals(p3, topDiv.getLastChildElement());
		assertEquals(p3, topDiv.getLastChildElement("p"));

		assertTrue(document.check());
	}

	@Test
	public void testNamesAndChilds() {
		Document document = new LagartoDOMBuilder().parse("<div id='top'><p id='id1'>one</p><span id='t1'>text1</span><p id='id2'>two</p><span id='t2'>text2</span><p id='id3'>three</p><span id='t3'>text3</span></div>");
		Element topDiv = (Element) document.getFirstChild();
		Element p1 = (Element) topDiv.getFirstChild();
		assertEquals("id1", p1.getAttribute("id"));

		Element s1 = (Element) p1.getNextSibling();
		assertEquals("t1", s1.getAttribute("id"));

		Element p2 = (Element) s1.getNextSiblingElement();
		assertEquals("id2", p2.getAttribute("id"));

		Element p3 = (Element) p2.getNextSiblingElement().getNextSiblingElement();
		assertEquals("id3", p3.getAttribute("id"));

		Element s3 = (Element) p3.getNextSibling();
		assertEquals("t3", s3.getAttribute("id"));

		assertEquals(p2, p1.getNextSiblingName());
		assertEquals(s3, s1.getNextSiblingName().getNextSiblingName());

		assertEquals(p2, p3.getPreviousSiblingName());
		assertEquals(s1, s3.getPreviousSiblingName().getPreviousSiblingName());

		assertEquals(p1, topDiv.getFirstChild());
		assertEquals(p1, topDiv.getFirstChildElement());
		assertEquals(p1, topDiv.getFirstChildElement("p"));
		assertEquals(s1, topDiv.getFirstChildElement("span"));

		assertEquals(s3, topDiv.getLastChild());
		assertEquals(s3, topDiv.getLastChildElement());
		assertEquals(p3, topDiv.getLastChildElement("p"));
		assertEquals(s3, topDiv.getLastChildElement("span"));

		assertTrue(document.check());
	}

	@Test
	public void testUnclosedTag() {
		Document document = new LagartoDOMBuilder().parse("<html><body><form><input>text<input>text<img></form></body></html>");
		String innerHtml = document.getHtml();
		assertEquals("<html><body><form><input>text<input>text<img></form></body></html>", innerHtml);
		assertTrue(document.check());

		LagartoDOMBuilder lagartoDomBuilder = new LagartoDOMBuilder();
		document = lagartoDomBuilder.parse("<body><b>bold</b><div>text1</span><div>as</div></body>");
		innerHtml = document.getHtml();
		assertEquals("<body><b>bold</b><div>text1<div>as</div></div></body>", innerHtml);
		assertTrue(document.check());

		document = new LagartoDOMBuilder().parse("<html><body><form><input><input><img></xxx></body></html>");
		innerHtml = document.getHtml();
		assertEquals("<html><body><form><input><input><img></form></body></html>", innerHtml);
		assertTrue(document.check());
	}

	@Test
	public void testUnclosedTag2() {
		Document document = new LagartoDOMBuilder().parse(
				"<DL>\n" +
						"<DT><A HREF=\"../java/awt/PageAttributes.MediaType.html#D\"><B>D</B></A> - \n" +
						"Static variable in class java.awt.\n" +
						"<A HREF=\"../java/awt/PageAttributes.MediaType.html\">PageAttributes.MediaType</A>\n" +
						"<DD>The MediaType instance for Engineering D, 22 x 34 in." +
						"</DL>");
		assertTrue(document.check());

		String innerHtml = document.getHtml();
		assertEquals(
				"<dl>\n" +
						"<dt><a href=\"../java/awt/PageAttributes.MediaType.html#D\"><b>D</b></a> - \n" +
						"Static variable in class java.awt.\n" +
						"<a href=\"../java/awt/PageAttributes.MediaType.html\">PageAttributes.MediaType</a>\n" +
						"</dt><dd>The MediaType instance for Engineering D, 22 x 34 in." +
						"</dd></dl>",
				innerHtml);
	}

	@Test
	public void testUncapital() {
		LagartoDOMBuilder lagartoDOMBuilder = new LagartoDOMBuilder();
		Document document = lagartoDOMBuilder.parse("<HTML><bOdY at='qWe'></body></html>");
		String innerHtml = document.getHtml();
		assertEquals("<html><body at=\"qWe\"></body></html>", innerHtml);
		assertTrue(document.check());

		document = lagartoDOMBuilder.parse("<HTML><bOdY at='qWe' AT='zxc'></body></html>");
		innerHtml = document.getHtml();
		assertEquals("<html><body at=\"zxc\"></body></html>", innerHtml);
		assertTrue(document.check());
	}

	@Test
	public void testEncode() {
		LagartoDOMBuilder lagartoDOMBuilder = new LagartoDOMBuilder();
		Document document = lagartoDOMBuilder.parse("<div foo=\"q&nbsp;w\">a&lt;b</div>");
		Element div = (Element) document.getFirstChild();
		String foo = div.getAttribute("foo");
		assertEquals("q\u00A0w", foo);

		div.setAttribute("foo", "q\u00A0w\u00A0e");
		assertEquals("<div foo=\"q\u00A0w\u00A0e\">a&lt;b</div>", document.getHtml());

		Text text = (Text) document.getFirstChild().getFirstChild();

		assertEquals("a&lt;b", text.getNodeValue());
		assertEquals("a<b", text.getTextContent());
		assertTrue(document.check());
	}

	@Test
	public void testXmlDec() {
		LagartoDOMBuilder lagartoDOMBuilder = new LagartoDOMBuilder();
		Document document = lagartoDOMBuilder.parse("<?html?><div?></div>");

		XmlDeclaration xml = (XmlDeclaration) document.getFirstChild();
		assertEquals(0, xml.getAttributesCount());
		assertEquals("html", xml.getNodeName());

		Element div = (Element) xml.getNextSibling();
		assertEquals(0, div.getAttributesCount());
		assertEquals("div", div.getNodeName());

		assertTrue(document.check());
	}

	@Test
	public void testOrphanAttribute() {
		LagartoDOMBuilder lagartoDOMBuilder = new LagartoDOMBuilder();
		Document document = lagartoDOMBuilder.parse("<div qwe '8989' foo zoo='123'/>");

		Element div = (Element) document.getFirstChild();
		assertEquals("div", div.getNodeName());
		assertEquals(3, div.getAttributesCount());
		assertTrue(div.hasAttribute("qwe"));
		assertTrue(div.hasAttribute("foo"));
		assertTrue(div.hasAttribute("zoo"));
		assertFalse(div.hasAttribute("'8989'"));

		assertTrue(document.check());
	}

	@Test
	public void testAppendable() {
		LagartoDOMBuilder lagartoDOMBuilder = new LagartoDOMBuilder();
		Document document = lagartoDOMBuilder.parse("<div foo=\"123\">some <b>nice</b> text</div>");
		Element div = (Element) document.getFirstChild();

		String textContent = div.getTextContent();

		StringBuilder stringBuilder = new StringBuilder();
		div.appendTextContent(stringBuilder);

		assertEquals(textContent, stringBuilder.toString());

		FastCharArrayWriter charBuffer = new FastCharArrayWriter();
		div.appendTextContent(charBuffer);

		System.out.println(charBuffer.toString());

		assertEquals(textContent, charBuffer.toString());
	}

}