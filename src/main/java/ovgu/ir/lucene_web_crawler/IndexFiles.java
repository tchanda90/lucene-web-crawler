package ovgu.ir.lucene_web_crawler;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;


public class IndexFiles {
	
	public IndexWriter getIndexer(String indexPath) {
		
		IndexWriter writer = null;
		try {

			Directory dir = FSDirectory.open(Paths.get(indexPath));
			
			// Initialize a StandardAnalyzer object. This analyzer converts tokens
			// to lowercase and filters out stopwords
			Analyzer analyzer = new StandardAnalyzer();
			
			// IndexWriterConfig stores all the configuration parameters for IndexWriter
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			
			if (!DirectoryReader.indexExists(dir)) {
				// A new index will be created and any existing indexes will be removed
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// An index already exists so we use it
				return null;
			}
			
			writer = new IndexWriter(dir, iwc);			
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return writer;
	}
	
	public static void indexDoc(IndexWriter writer, org.jsoup.nodes.Document doc) {
		
		try {	
			
			String title = doc.title();
			String parsedContents = doc.body().text();
			String url = doc.location();
			
			// create a lucene document object
			org.apache.lucene.document.Document document = new Document();
			
			// add the title field
			Field titleField = new TextField("title", title, Field.Store.YES);
			document.add(titleField);
			
			
			// Call the doStemming() method and perform stemming on the contents
			String stemmedContents = doStemming(parsedContents);

			// add the contents of the file to a field named "contents"
			Field contentsField = new TextField("contents", stemmedContents, Field.Store.NO);
			document.add(contentsField);	
			
			// add the url field
			Field urlField = new TextField("url", url, Field.Store.YES);
			document.add(urlField);
		

			// index the document
			System.out.println("Adding " + UrlNormalizer.normalize(url));
			writer.addDocument(document);
			
			
			
		} catch(Exception e) {
			System.out.println("Error while indexing document " + doc.title());
			e.printStackTrace();
		}
	}

	private static String doStemming(String parsedContents) {
		String stemmedContents = "";
		
		// Create a PorterStemmer object
		PorterStemmer stemmer = new PorterStemmer();
		
		// Split the words into an array so that it can be iterated
		String[] words = parsedContents.split("\\s+");
		
		// Iterate over the words
		for (String word : words) {
	        stemmer.setCurrent(word);
	        
	        // Stem the current word
	        stemmer.stem();
	        
	        String stemmedWord = stemmer.getCurrent();
	        
	        if (stemmedContents.equalsIgnoreCase(""))
	        	stemmedContents = stemmedWord;
	        else {
	        	// Append the stemmed word after a space
	        	stemmedContents += " ";
	        	stemmedContents += stemmedWord;
	        }
		}
		// Finally, return all the stemmed words as a String
		return stemmedContents;
	}
}
