package ovgu.ir.lucene_web_crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import org.apache.lucene.index.IndexWriter;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawler {
	
	private int crawlDepth;
	private BufferedWriter bfWriter;
	HashSet<String> indexedPages;
	
	public void startCrawl(String seedUrl, int crawlDepth, String indexPath) {
		
		this.crawlDepth = crawlDepth;
		
		IndexFiles indexer = new IndexFiles();
		IndexWriter writer = indexer.getIndexer(indexPath);
		
		// writer will be null when an index is already present in the index path
		if (writer == null) {
			System.out.println("Using Already Available Index...");
			return;
		}
		
		// create the log file
		try {
			bfWriter = new BufferedWriter(new FileWriter(indexPath + "/pages.txt"));
		} catch (IOException e) {
			System.out.println("Exception while creaing pages.txt. " + e);
		}
		
		// initialize a HashSet to store pages that get indexed, so that
		// we can check if a page is already indexed before indexing.
		// HashSet is used because it allows for fast searching O(1).
		this.indexedPages = new HashSet<String>();
		
		// start the crawl procedure
		this.crawl(UrlNormalizer.normalize(seedUrl), 0, writer);
		
		// close index writer and bufferedwriter after crawling is done 
		try {
			writer.close();
			bfWriter.close();
		} catch (IOException e) {
			System.out.println("Exception while closing index writer or buffered writer. " + e);
		}	
	}

	private void crawl(String url, int depth, IndexWriter writer) {
		
		// if url is null after normalization
		if (url == null) {
			return;
		}
				
		// parse the document using jsoup
		org.jsoup.nodes.Document doc = null;	
		try {
			Connection con = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
					.ignoreHttpErrors(true)
					.timeout(20000);
			Connection.Response response = con.execute();
			if (response.statusCode() == 200) {
	            doc = con.get();
			}
		} catch (HttpStatusException e) {
			System.out.println("URL could not be parsed. " + e);
		}
		catch (Exception e) {
			System.out.println("Jsoup exception while connecting to url: " + url + ". " + e);
		}
		
		if (doc != null) {
			
			// index the current doc
			System.out.println("Adding " + url);
			IndexFiles.indexDoc(writer, doc);
			
			// add the url to the indexedPages HashSet
			this.indexedPages.add(url);
			
			// write the current page to the log file
			String line = url + "\t" + depth;
			try {
				bfWriter.write(line);
				bfWriter.newLine();
				bfWriter.flush();
			} catch (IOException e) {
				System.out.println("Error while writing to pages.txt. " + e);
			}	
			
			
			// check if crawl depth has been reached
			if (depth < this.crawlDepth) {
				
				// extract links from the url and recurse
				Elements links = doc.select("a[href]");
				
				for (Element link : links) {
					String normalizedUrl = UrlNormalizer.normalize(link.absUrl("href").toString());					
					// recurse on the url if page is not already indexed
					if (normalizedUrl != null && !this.indexedPages.contains(normalizedUrl)) {
						crawl(normalizedUrl, depth+1, writer);
					}
				}
			
			}
		}	
	}
}
