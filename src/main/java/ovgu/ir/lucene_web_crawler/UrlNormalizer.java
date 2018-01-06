package ovgu.ir.lucene_web_crawler;

public class UrlNormalizer {

	public static String normalize(String url) {
		
		if ( !(url.startsWith("www") || url.startsWith("http")) || url == "") {
			//System.out.println("Invalid Url: " + url);
			return null;
		}
		
		// change to lower case
		String normalizedUrl = url.toLowerCase();
		
		// add https 
		if (normalizedUrl.startsWith("www")) {
			normalizedUrl = "https://" + normalizedUrl;
		}
		
		// check if the link contains an anchor
		// if yes, truncates the anchor. This way the page won't be
		// indexed if it is already in the indexedPages HashSet
		if (normalizedUrl.contains("#")) {
			normalizedUrl = normalizedUrl.substring(0, normalizedUrl.indexOf('#'));
		}
		
		// remove trailing slash if it exists
		if (normalizedUrl.endsWith("/")) {
		    normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
		}
			
		return normalizedUrl;
	}

}
