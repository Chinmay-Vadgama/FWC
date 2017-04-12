
package jsearchengine;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import java.net.URL;
import java.util.ArrayList;


public class Main {
    

    private static IndexWriter writer;		  // new index being built
    private static ArrayList indexed;
    private static String beginDomain;


    public static void main(String[] args) throws Exception{
        
        String index = "/opt/lucene/index";
        
        boolean create = true;
        
        String link = "http://www.passportindia.gov.in/AppOnlineProject/online/siteMap";
        
        beginDomain = Domain(link);
        
        System.out.println(beginDomain);
        
        writer = new IndexWriter(index, new StandardAnalyzer(),create ,new IndexWriter.MaxFieldLength(1000000));
        indexed = new ArrayList();
        int level=5;
        
        indexDocs(link,level);
        
        System.out.println("Optimizing...");
        writer.optimize();
        writer.close();
           
    }
    

    
    private static void indexDocs(String url,int depth) throws Exception {
        
        //index page
        if(depth>0)
        {
        Document doc = HTMLDocument.Document(url);
        System.out.println("adding " + doc.get("path"));
        try {
            indexed.add(doc.get("path"));
            writer.addDocument(doc);		  // add docs unconditionally
            //TODO: only add html docs
            //and create other doc types
            

            //get all links on the page then index them
            LinkParser lp = new LinkParser(url);
            URL[] links = lp.ExtractLinks();
            System.out.print("-----at depth "+depth+"------\n");
            for (URL l : links) {
                //make sure the url hasnt already been indexed
                //make sure the url contains the home domain
                //ignore urls with a querystrings by excluding "?" 
              
                if ((!indexed.contains(l.toURI().toString())) && (l.toURI().toString().contains(beginDomain)) && (!l.toURI().toString().contains("?"))) {
                    //don't index zip,pdf files
                   
                    if (!l.toURI().toString().endsWith(".zip") && (!l.toURI().toString().endsWith(".pdf")))
                    {
                    System.out.print(l.toURI().toString());
                    indexDocs(l.toURI().toString(),depth--);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        }
    }
 
 private static String Domain(String url)
 {
     int firstDot = url.indexOf(".");
     int lastDot =  url.lastIndexOf(".");
     return url.substring(firstDot+1,lastDot);
 }


}
