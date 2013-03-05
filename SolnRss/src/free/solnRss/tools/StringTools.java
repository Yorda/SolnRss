package free.solnRss.tools;

import java.util.HashMap;

public class StringTools {

	/**
	 * Replaces characters that may be confused by a HTML parser with their equivalent character entity references.
	 * <p>
	 * Any data that will appear as text on a web page should be be escaped. This is especially important for data that comes from untrusted sources such as Internet users. A common mistake in CGI programming is to ask a user for data and then put
	 * that data on a web page. For example:
	 * 
	 * <pre>
	 * Server: What is your name?
	 * User: &lt;b&gt;Joe&lt;b&gt;
	 * Server: Hello &lt;b&gt;Joe&lt;/b&gt;, Welcome
	 * </pre>
	 * 
	 * If the name is put on the page without checking that it doesn't contain HTML code or without sanitizing that HTML code, the user could reformat the page, insert scripts, and control the the content on your web server.
	 * <p>
	 * This method will replace HTML characters such as &gt; with their HTML entity reference (&amp;gt;) so that the html parser will be sure to interpret them as plain text rather than HTML or script.
	 * <p>
	 * This method should be used for both data to be displayed in text in the html document, and data put in form elements. For example:<br>
	 * <code>&lt;html&gt;&lt;body&gt;<i>This in not a &amp;lt;tag&amp;gt;
	 * in HTML</i>&lt;/body&gt;&lt;/html&gt;</code><br>
	 * and<br>
	 * <code>&lt;form&gt;&lt;input type="hidden" name="date" value="<i>This data could
	 * be &amp;quot;malicious&amp;quot;</i>"&gt;&lt;/form&gt;</code><br>
	 * In the second example, the form data would be properly be resubmitted to your CGI script in the URLEncoded format:<br>
	 * <code><i>This data could be %22malicious%22</i></code>
	 * 
	 * @param s
	 *            String to be escaped
	 * @return escaped String
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String escapeHTML(String s)
	{
		int length = s.length();
		int newLength = length;
		boolean someCharacterEscaped = false;
		// first check for characters that might
		// be dangerous and calculate a length
		// of the string that has escapes.
		for (int i=0; i<length; i++){
			char c = s.charAt(i);
			int cint = 0xffff & c;
			if (cint < 32){
				switch(c){
					case '\r':
					case '\n':
					case '\t':
					case '\f':{
						// Leave whitespace untouched
					} break;
					default: {
						newLength -= 1;
						someCharacterEscaped = true;
					}
				}
			} else {
				switch(c){
					case '\"':{
						newLength += 5;
						someCharacterEscaped = true;
					} break;
					case '&':
					case '\'':{
						newLength += 4;
						someCharacterEscaped = true;
					} break;
					case '<':
					case '>':{
						newLength += 3;
						someCharacterEscaped = true;
					} break;
				}
			}
		}
		if (!someCharacterEscaped){
			// nothing to escape in the string
			return s;
		}
		StringBuffer sb = new StringBuffer(newLength);
		for (int i=0; i<length; i++){
			char c = s.charAt(i);
			int cint = 0xffff & c;
			if (cint < 32){
				switch(c){
					case '\r':
					case '\n':{
						sb.append("<br/>");
						break;
					}
					case '\t':
					case '\f':{
						sb.append(c);
					} break;
					default: {
						// Remove this character
					}
				}
			} else {
				switch(c){
					case '\"':{
						sb.append("&quot;");
					} break;
					case '\'':{
						sb.append("&#39;");
					} break;
					case '&':{
						sb.append("&amp;");
					} break;
					case '<':{
						sb.append("&lt;");
					} break;
					case '>':{
						sb.append("&gt;");
					} break;
					case '€':{
						sb.append("&#128;");
					} break;
					default: {
						sb.append(c);
					}
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Turn any HTML escape entities in the string into characters and return
	 * the resulting string.
	 * 
	 * @param s
	 *            String to be un-escaped.
	 * @return un-escaped String.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String unescapeHTML(String s)
	{
		StringBuffer result = new StringBuffer(s.length());
		int ampInd = s.indexOf("&");
		int lastEnd = 0;
		while (ampInd >= 0) {
			int nextAmp = s.indexOf("&", ampInd + 1);
			int nextSemi = s.indexOf(";", ampInd + 1);
			if (nextSemi != -1 && (nextAmp == -1 || nextSemi < nextAmp)) {
				int value = -1;
				String escape = s.substring(ampInd + 1, nextSemi);
				try {
					if (escape.startsWith("#")) {
						value = Integer.parseInt(escape.substring(1), 10);
					}
					else {
						if (htmlEntities.containsKey(escape)) {
							value = htmlEntities.get(escape).intValue();
						}
					}
				}
				catch (NumberFormatException x) {
					// Could not parse the entity,
					// output it verbatim
				}
				result.append(s.substring(lastEnd, ampInd));
				lastEnd = nextSemi + 1;
				if (value >= 0 && value <= 0xffff) {
					result.append((char) value);
				}
				else {
					result.append("&").append(escape).append(";");
				}
			}
			ampInd = nextAmp;
		}
		result.append(s.substring(lastEnd));
		return result.toString();
	}
	
	/**
	 * Map with caracters entities
	 */
	private static HashMap<String, Integer> htmlEntities = new HashMap<String, Integer>();
	static {
		htmlEntities.put("n" + "b" + "s" + "p", Integer.valueOf(160));
		htmlEntities.put("i" + "e" + "x" + "c" + "l", Integer.valueOf(161));
		htmlEntities.put("cent", Integer.valueOf(162));
		htmlEntities.put("pound", Integer.valueOf(163));
		htmlEntities.put("c" + "u" + "r" + "r" + "e" + "n", Integer.valueOf(164));
		htmlEntities.put("y" + "e" + "n", Integer.valueOf(165));
		htmlEntities.put("b" + "r" + "v" + "b" + "a" + "r", Integer.valueOf(166));
		htmlEntities.put("sect", Integer.valueOf(167));
		htmlEntities.put("u" + "m" + "l", Integer.valueOf(168));
		htmlEntities.put("copy", Integer.valueOf(169));
		htmlEntities.put("o" + "r" + "d" + "f", Integer.valueOf(170));
		htmlEntities.put("l" + "a" + "quo", Integer.valueOf(171));
		htmlEntities.put("not", Integer.valueOf(172));
		htmlEntities.put("shy", Integer.valueOf(173));
		htmlEntities.put("r" + "e" + "g", Integer.valueOf(174));
		htmlEntities.put("m" + "a" + "c" + "r", Integer.valueOf(175));
		htmlEntities.put("d" + "e" + "g", Integer.valueOf(176));
		htmlEntities.put("plus" + "m" + "n", Integer.valueOf(177));
		htmlEntities.put("sup2", Integer.valueOf(178));
		htmlEntities.put("sup3", Integer.valueOf(179));
		htmlEntities.put("acute", Integer.valueOf(180));
		htmlEntities.put("m" + "i" + "c" + "r" + "o", Integer.valueOf(181));
		htmlEntities.put("par" + "a", Integer.valueOf(182));
		htmlEntities.put("mid" + "dot", Integer.valueOf(183));
		htmlEntities.put("c" + "e" + "d" + "i" + "l", Integer.valueOf(184));
		htmlEntities.put("sup1", Integer.valueOf(185));
		htmlEntities.put("o" + "r" + "d" + "m", Integer.valueOf(186));
		htmlEntities.put("r" + "a" + "quo", Integer.valueOf(187));
		htmlEntities.put("frac14", Integer.valueOf(188));
		htmlEntities.put("frac12", Integer.valueOf(189));
		htmlEntities.put("frac34", Integer.valueOf(190));
		htmlEntities.put("i" + "quest", Integer.valueOf(191));
		htmlEntities.put("A" + "grave", Integer.valueOf(192));
		htmlEntities.put("A" + "a" + "cute", Integer.valueOf(193));
		htmlEntities.put("A" + "c" + "i" + "r" + "c", Integer.valueOf(194));
		htmlEntities.put("A" + "tilde", Integer.valueOf(195));
		htmlEntities.put("A" + "u" + "m" + "l", Integer.valueOf(196));
		htmlEntities.put("A" + "ring", Integer.valueOf(197));
		htmlEntities.put("A" + "E" + "l" + "i" + "g", Integer.valueOf(198));
		htmlEntities.put("C" + "c" + "e" + "d" + "i" + "l", Integer.valueOf(199));
		htmlEntities.put("E" + "grave", Integer.valueOf(200));
		htmlEntities.put("E" + "a" + "cute", Integer.valueOf(201));
		htmlEntities.put("E" + "c" + "i" + "r" + "c", Integer.valueOf(202));
		htmlEntities.put("E" + "u" + "m" + "l", Integer.valueOf(203));
		htmlEntities.put("I" + "grave", Integer.valueOf(204));
		htmlEntities.put("I" + "a" + "cute", Integer.valueOf(205));
		htmlEntities.put("I" + "c" + "i" + "r" + "c", Integer.valueOf(206));
		htmlEntities.put("I" + "u" + "m" + "l", Integer.valueOf(207));
		htmlEntities.put("ETH", Integer.valueOf(208));
		htmlEntities.put("N" + "tilde", Integer.valueOf(209));
		htmlEntities.put("O" + "grave", Integer.valueOf(210));
		htmlEntities.put("O" + "a" + "cute", Integer.valueOf(211));
		htmlEntities.put("O" + "c" + "i" + "r" + "c", Integer.valueOf(212));
		htmlEntities.put("O" + "tilde", Integer.valueOf(213));
		htmlEntities.put("O" + "u" + "" + "m" + "l", Integer.valueOf(214));
		htmlEntities.put("times", Integer.valueOf(215));
		htmlEntities.put("O" + "slash", Integer.valueOf(216));
		htmlEntities.put("U" + "grave", Integer.valueOf(217));
		htmlEntities.put("U" + "a" + "cute", Integer.valueOf(218));
		htmlEntities.put("U" + "c" + "i" + "r" + "c", Integer.valueOf(219));
		htmlEntities.put("U" + "u" + "m" + "l", Integer.valueOf(220));
		htmlEntities.put("Y" + "a" + "cute", Integer.valueOf(221));
		htmlEntities.put("THORN", Integer.valueOf(222));
		htmlEntities.put("s" + "z" + "l" + "i" + "g", Integer.valueOf(223));
		htmlEntities.put("a" + "grave", Integer.valueOf(224));
		htmlEntities.put("a" + "a" + "cute", Integer.valueOf(225));
		htmlEntities.put("a" + "c" + "i" + "r" + "c", Integer.valueOf(226));
		htmlEntities.put("a" + "tilde", Integer.valueOf(227));
		htmlEntities.put("a" + "u" + "m" + "l", Integer.valueOf(228));
		htmlEntities.put("a" + "ring", Integer.valueOf(229));
		htmlEntities.put("a" + "e" + "l" + "i" + "g", Integer.valueOf(230));
		htmlEntities.put("c" + "c" + "e" + "d" + "i" + "l", Integer.valueOf(231));
		htmlEntities.put("e" + "grave", Integer.valueOf(232));
		htmlEntities.put("e" + "a" + "cute", Integer.valueOf(233));
		htmlEntities.put("e" + "c" + "i" + "r" + "c", Integer.valueOf(234));
		htmlEntities.put("e" + "u" + "m" + "l", Integer.valueOf(235));
		htmlEntities.put("i" + "grave", Integer.valueOf(236));
		htmlEntities.put("i" + "a" + "cute", Integer.valueOf(237));
		htmlEntities.put("i" + "c" + "i" + "r" + "c", Integer.valueOf(238));
		htmlEntities.put("i" + "u" + "" + "m" + "l", Integer.valueOf(239));
		htmlEntities.put("e" + "t" + "h", Integer.valueOf(240));
		htmlEntities.put("n" + "tilde", Integer.valueOf(241));
		htmlEntities.put("o" + "grave", Integer.valueOf(242));
		htmlEntities.put("o" + "a" + "cute", Integer.valueOf(243));
		htmlEntities.put("o" + "c" + "i" + "r" + "c", Integer.valueOf(244));
		htmlEntities.put("o" + "tilde", Integer.valueOf(245));
		htmlEntities.put("o" + "u" + "m" + "l", Integer.valueOf(246));
		htmlEntities.put("divide", Integer.valueOf(247));
		htmlEntities.put("o" + "slash", Integer.valueOf(248));
		htmlEntities.put("u" + "grave", Integer.valueOf(249));
		htmlEntities.put("u" + "a" + "cute", Integer.valueOf(250));
		htmlEntities.put("u" + "c" + "i" + "r" + "c", Integer.valueOf(251));
		htmlEntities.put("u" + "u" + "m" + "l", Integer.valueOf(252));
		htmlEntities.put("y" + "a" + "cute", Integer.valueOf(253));
		htmlEntities.put("thorn", Integer.valueOf(254));
		htmlEntities.put("y" + "u" + "m" + "l", Integer.valueOf(255));
		htmlEntities.put("f" + "no" + "f", Integer.valueOf(402));
		htmlEntities.put("Alpha", Integer.valueOf(913));
		htmlEntities.put("Beta", Integer.valueOf(914));
		htmlEntities.put("Gamma", Integer.valueOf(915));
		htmlEntities.put("Delta", Integer.valueOf(916));
		htmlEntities.put("Epsilon", Integer.valueOf(917));
		htmlEntities.put("Z" + "e" + "t" + "a", Integer.valueOf(918));
		htmlEntities.put("E" + "t" + "a", Integer.valueOf(919));
		htmlEntities.put("T" + "h" + "e" + "t" + "a", Integer.valueOf(920));
		htmlEntities.put("I" + "o" + "t" + "a", Integer.valueOf(921));
		htmlEntities.put("K" + "a" + "p" + "pa", Integer.valueOf(922));
		htmlEntities.put("Lambda", Integer.valueOf(923));
		htmlEntities.put("M" + "u", Integer.valueOf(924));
		htmlEntities.put("N" + "u", Integer.valueOf(925));
		htmlEntities.put("Xi", Integer.valueOf(926));
		htmlEntities.put("O" + "m" + "i" + "c" + "r" + "on", Integer.valueOf(927));
		htmlEntities.put("Pi", Integer.valueOf(928));
		htmlEntities.put("R" + "h" + "o", Integer.valueOf(929));
		htmlEntities.put("S" + "i" + "g" + "m" + "a", Integer.valueOf(931));
		htmlEntities.put("Tau", Integer.valueOf(932));
		htmlEntities.put("Up" + "s" + "i" + "l" + "on", Integer.valueOf(933));
		htmlEntities.put("P" + "h" + "i", Integer.valueOf(934));
		htmlEntities.put("C" + "h" + "i", Integer.valueOf(935));
		htmlEntities.put("P" + "s" + "i", Integer.valueOf(936));
		htmlEntities.put("O" + "m" + "e" + "g" + "a", Integer.valueOf(937));
		htmlEntities.put("alpha", Integer.valueOf(945));
		htmlEntities.put("beta", Integer.valueOf(946));
		htmlEntities.put("gamma", Integer.valueOf(947));
		htmlEntities.put("delta", Integer.valueOf(948));
		htmlEntities.put("epsilon", Integer.valueOf(949));
		htmlEntities.put("z" + "e" + "t" + "a", Integer.valueOf(950));
		htmlEntities.put("e" + "t" + "a", Integer.valueOf(951));
		htmlEntities.put("the" + "t" + "a", Integer.valueOf(952));
		htmlEntities.put("i" + "o" + "t" + "a", Integer.valueOf(953));
		htmlEntities.put("k" + "a" + "p" + "pa", Integer.valueOf(954));
		htmlEntities.put("lambda", Integer.valueOf(955));
		htmlEntities.put("m" + "u", Integer.valueOf(956));
		htmlEntities.put("n" + "u", Integer.valueOf(957));
		htmlEntities.put("xi", Integer.valueOf(958));
		htmlEntities.put("o" + "m" + "i" + "" + "c" + "r" + "on", Integer.valueOf(959));
		htmlEntities.put("pi", Integer.valueOf(960));
		htmlEntities.put("r" + "h" + "o", Integer.valueOf(961));
		htmlEntities.put("s" + "i" + "g" + "m" + "a" + "f", Integer.valueOf(962));
		htmlEntities.put("s" + "i" + "g" + "m" + "a", Integer.valueOf(963));
		htmlEntities.put("tau", Integer.valueOf(964));
		htmlEntities.put("up" + "s" + "i" + "l" + "on", Integer.valueOf(965));
		htmlEntities.put("p" + "h" + "i", Integer.valueOf(966));
		htmlEntities.put("c" + "h" + "i", Integer.valueOf(967));
		htmlEntities.put("p" + "s" + "i", Integer.valueOf(968));
		htmlEntities.put("o" + "m" + "e" + "g" + "a", Integer.valueOf(969));
		htmlEntities.put("the" + "t" + "a" + "s" + "y" + "m", Integer.valueOf(977));
		htmlEntities.put("up" + "s" + "i" + "h", Integer.valueOf(978));
		htmlEntities.put("pi" + "v", Integer.valueOf(982));
		htmlEntities.put("bull", Integer.valueOf(8226));
		htmlEntities.put("hell" + "i" + "p", Integer.valueOf(8230));
		htmlEntities.put("prime", Integer.valueOf(8242));
		htmlEntities.put("Prime", Integer.valueOf(8243));
		htmlEntities.put("o" + "line", Integer.valueOf(8254));
		htmlEntities.put("f" + "r" + "" + "a" + "s" + "l", Integer.valueOf(8260));
		htmlEntities.put("we" + "i" + "e" + "r" + "p", Integer.valueOf(8472));
		htmlEntities.put("image", Integer.valueOf(8465));
		htmlEntities.put("real", Integer.valueOf(8476));
		htmlEntities.put("trade", Integer.valueOf(8482));
		htmlEntities.put("ale" + "f" + "s" + "y" + "m", Integer.valueOf(8501));
		htmlEntities.put("l" + "a" + "r" + "r", Integer.valueOf(8592));
		htmlEntities.put("u" + "a" + "r" + "r", Integer.valueOf(8593));
		htmlEntities.put("r" + "a" + "r" + "r", Integer.valueOf(8594));
		htmlEntities.put("d" + "a" + "r" + "r", Integer.valueOf(8595));
		htmlEntities.put("ha" + "r" + "r", Integer.valueOf(8596));
		htmlEntities.put("c" + "r" + "" + "a" + "r" + "r", Integer.valueOf(8629));
		htmlEntities.put("lArr", Integer.valueOf(8656));
		htmlEntities.put("uArr", Integer.valueOf(8657));
		htmlEntities.put("rArr", Integer.valueOf(8658));
		htmlEntities.put("dArr", Integer.valueOf(8659));
		htmlEntities.put("hArr", Integer.valueOf(8660));
		htmlEntities.put("for" + "all", Integer.valueOf(8704));
		htmlEntities.put("part", Integer.valueOf(8706));
		htmlEntities.put("exist", Integer.valueOf(8707));
		htmlEntities.put("empty", Integer.valueOf(8709));
		htmlEntities.put("n" + "a" + "b" + "l" + "a", Integer.valueOf(8711));
		htmlEntities.put("is" + "in", Integer.valueOf(8712));
		htmlEntities.put("not" + "in", Integer.valueOf(8713));
		htmlEntities.put("n" + "i", Integer.valueOf(8715));
		htmlEntities.put("p" + "rod", Integer.valueOf(8719));
		htmlEntities.put("sum", Integer.valueOf(8721));
		htmlEntities.put("minus", Integer.valueOf(8722));
		htmlEntities.put("low" + "as" + "t", Integer.valueOf(8727));
		htmlEntities.put("r" + "a" + "d" + "i" + "c", Integer.valueOf(8730));
		htmlEntities.put("prop", Integer.valueOf(8733));
		htmlEntities.put("in" + "fin", Integer.valueOf(8734));
		htmlEntities.put("an" + "g", Integer.valueOf(8736));
		htmlEntities.put("and", Integer.valueOf(8743));
		htmlEntities.put("or", Integer.valueOf(8744));
		htmlEntities.put("cap", Integer.valueOf(8745));
		htmlEntities.put("cup", Integer.valueOf(8746));
		htmlEntities.put("int", Integer.valueOf(8747));
		htmlEntities.put("there4", Integer.valueOf(8756));
		htmlEntities.put("s" + "i" + "m", Integer.valueOf(8764));
		htmlEntities.put("c" + "on" + "g", Integer.valueOf(8773));
		htmlEntities.put("a" + "s" + "y" + "m" + "p", Integer.valueOf(8776));
		htmlEntities.put("n" + "e", Integer.valueOf(8800));
		htmlEntities.put("e" + "q" + "u" + "i" + "v", Integer.valueOf(8801));
		htmlEntities.put("l" + "e", Integer.valueOf(8804));
		htmlEntities.put("g" + "e", Integer.valueOf(8805));
		htmlEntities.put("sub", Integer.valueOf(8834));
		htmlEntities.put("sup", Integer.valueOf(8835));
		htmlEntities.put("n" + "sub", Integer.valueOf(8836));
		htmlEntities.put("sub" + "e", Integer.valueOf(8838));
		htmlEntities.put("sup" + "e", Integer.valueOf(8839));
		htmlEntities.put("o" + "plus", Integer.valueOf(8853));
		htmlEntities.put("o" + "times", Integer.valueOf(8855));
		htmlEntities.put("per" + "p", Integer.valueOf(8869));
		htmlEntities.put("s" + "dot", Integer.valueOf(8901));
		htmlEntities.put("l" + "c" + "e" + "i" + "l", Integer.valueOf(8968));
		htmlEntities.put("r" + "c" + "e" + "i" + "l", Integer.valueOf(8969));
		htmlEntities.put("l" + "floor", Integer.valueOf(8970));
		htmlEntities.put("r" + "floor", Integer.valueOf(8971));
		htmlEntities.put("lang", Integer.valueOf(9001));
		htmlEntities.put("rang", Integer.valueOf(9002));
		htmlEntities.put("l" + "o" + "z", Integer.valueOf(9674));
		htmlEntities.put("spades", Integer.valueOf(9824));
		htmlEntities.put("clubs", Integer.valueOf(9827));
		htmlEntities.put("hearts", Integer.valueOf(9829));
		htmlEntities.put("d" + "i" + "am" + "s", Integer.valueOf(9830));
		htmlEntities.put("quot", Integer.valueOf(34));
		htmlEntities.put("amp", Integer.valueOf(38));
		htmlEntities.put("lt", Integer.valueOf(60));
		htmlEntities.put("gt", Integer.valueOf(62));
		htmlEntities.put("OElig", Integer.valueOf(338));
		htmlEntities.put("o" + "e" + "l" + "i" + "g", Integer.valueOf(339));
		htmlEntities.put("Scar" + "on", Integer.valueOf(352));
		htmlEntities.put("scar" + "on", Integer.valueOf(353));
		htmlEntities.put("Y" + "u" + "m" + "l", Integer.valueOf(376));
		htmlEntities.put("c" + "i" + "r" + "c", Integer.valueOf(710));
		htmlEntities.put("tilde", Integer.valueOf(732));
		htmlEntities.put("e" + "n" + "s" + "p", Integer.valueOf(8194));
		htmlEntities.put("e" + "m" + "s" + "p", Integer.valueOf(8195));
		htmlEntities.put("thin" + "s" + "p", Integer.valueOf(8201));
		htmlEntities.put("z" + "w" + "n" + "j", Integer.valueOf(8204));
		htmlEntities.put("z" + "w" + "j", Integer.valueOf(8205));
		htmlEntities.put("l" + "r" + "m", Integer.valueOf(8206));
		htmlEntities.put("r" + "l" + "m", Integer.valueOf(8207));
		htmlEntities.put("n" + "dash", Integer.valueOf(8211));
		htmlEntities.put("m" + "dash", Integer.valueOf(8212));
		htmlEntities.put("l" + "s" + "quo", Integer.valueOf(8216));
		htmlEntities.put("r" + "s" + "quo", Integer.valueOf(8217));
		htmlEntities.put("s" + "b" + "quo", Integer.valueOf(8218));
		htmlEntities.put("l" + "d" + "quo", Integer.valueOf(8220));
		htmlEntities.put("r" + "d" + "quo", Integer.valueOf(8221));
		htmlEntities.put("b" + "d" + "quo", Integer.valueOf(8222));
		htmlEntities.put("dagger", Integer.valueOf(8224));
		htmlEntities.put("Dagger", Integer.valueOf(8225));
		htmlEntities.put("p" + "e" + "r" + "m" + "i" + "l", Integer.valueOf(8240));
		htmlEntities.put("l" + "s" + "a" + "quo", Integer.valueOf(8249));
		htmlEntities.put("r" + "s" + "a" + "quo", Integer.valueOf(8250));
		htmlEntities.put("euro", Integer.valueOf(8364));
	}
}
