/*
 * ItemUtility.java
 *
 * Version: 1.0
 *
 * Date: 2010-06-21
 *
 * Copyright (c) 2010, Keiji Suzuki. All rights reserved.
 *
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package jp.zuki_ebetsu.dspace.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.DCValue;
import org.dspace.handle.HandleManager;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Utils;
import org.dspace.core.I18nUtil;
import org.dspace.core.ConfigurationManager;
import org.dspace.eperson.EPerson;
import org.apache.log4j.Logger;

/**
 * Class representing a item citation
 * 
 * @author Keiji Suzuki
 * @version
 */
public class ItemUtility 
{
    private static Logger log = Logger.getLogger(ItemUtility.class);

    /** DSpace URL */
    private static final String url = ConfigurationManager.getProperty("dspace.url");
    /** the Item */
    private Item item = null;
    /** path of the metadata mapping table */
    private static final String mdPath = ConfigurationManager.getProperty("dspace.dir")
                             + File.separator + "config" + File.separator + "crosswalks"
                             + File.separator + "metadata-mapper.properties";

    private static HashMap<String, String> bibtexTypes = null;

    private static HashMap<String, String> mdMapping = null;
    static
    {
        setMetadataMapping();
        if (mdMapping == null)
        {
            setDefaultMDMapping();
        }
    }
    /**
     * Class constructor 
     * 
     * @param item
     *            the item for citatio
     */
    public ItemUtility(Item item) 
    {
        this.item = item;
    }

    /**
     * return bibliographic citation
     *
     * @param html
     *            Make a html format
     * 
     * @return String
     * 
     */
    public String[] getCitationAsParts(boolean html) 
    {
        String[] citation = new String[3];
        citation[0] = getTitle(html);
        citation[1] = getAuthor(html, true) + " (" + getDate(html) + ")";
    
        String type = getMField("niitype");
        if (type.equals("Thesis or Dissertation"))
            citation[2] = getThesis(html);
        else if (type.equals("Conference Paper"))
            citation[2] = getMeeting(html);
        else if (type.equals("Book"))
            citation[2] = getBook(html);
        else
            citation[2] = getJournal(html);

        return citation;
    }

    /**
     * return bibliographic citation
     *
     * 
     * @return String
     * 
     */
    public String getCitation() 
    {
        return this.getCitation(false);    
    }

    /**
     * return bibliographic citation
     *
     * @param html - boolean
     *            Make a html format
     * @param hrq - HttpServletRequest
     *            request
     * 
     * @return String
     * 
     */
    public String getCitation(boolean html) 
    {
        String[] parts = getCitationAsParts(html); 
    
        return new StringBuffer().append(parts[1]).append(" ").append(parts[0]).append(". ").append(parts[2]).toString();
    }

    /**
     * return bibliographic citation
     * 
     * @return String
     * 
     */
    public String getShortCitation() 
    {
        return getShortCitation(false);
    }

    /**
     * return short bibliographic citation
     *
     * @param html
     *            Make a html format
     * 
     * @return String
     * 
     */
    public String getShortCitation(boolean html) 
    {
        String[] parts = getCitationAsParts(html); 
    
        return parts[2];
    }

    /**
     * return bibliographic citation link
     *
     * 
     * @return String
     * 
     */
    public String getCitationLink() 
    {
        String[] parts = getCitationAsParts(true); 
    
        return new StringBuffer().append(parts[1]).append(" ").append("<a href=\"").append(url).append("/handle/").append(item.getHandle()).append("\">").append(parts[0]).append("</a>").append(". ").append(parts[2]).toString();
    }

   
    /**
     * if item.submitter is a researcher, add the item to the researcher's
     * top folder.
     * 
     */
    public String getOpenURL()
    {
        StringBuilder sb = new StringBuilder();
    
        sb.append("ctx_ver=Z39.88-2004&ctx_enc=info%3Aofi%2Fenc%3AUTF-8");
        
        String host = ConfigurationManager.getProperty("dspace.hostname");
        String name = ConfigurationManager.getProperty("dspace.name");
        sb.append("&rfr_id=info:sid%2F").append(URLEncoder.encode(host)).append("%3A").append(URLEncoder.encode(name));
    
        String doi = getMField("doi");
        if (!doi.equals("")) 
            sb.append("&rft_id=info%3Adoi%2F").append(URLEncoder.encode(doi));
    
        String pmid = getMField("pmid");
        if (!pmid.equals("")) 
            sb.append("&rft_id=info%3Apmid%2F").append(URLEncoder.encode(pmid));
    
        String uri = getField("citation", "uri");
        if (!uri.equals(""))
            sb.append("&rft_id=").append(URLEncoder.encode(uri));
    
        String type = getMField("niitype");
        if (type.equals("Book"))
            sb.append("&rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Amtx%3Abook");
        else if (type.equals("Thesis of Dissertation"))
            sb.append("&rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Amtx%3Adissertation");
        else 
            sb.append("&rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Amtx%3Ajournal");
        
        String title = getField("title", null);
        if (type.equals("Thesis of Dissertation"))
            sb.append("&rft.title=").append(URLEncoder.encode(title));
        else
            sb.append("&rft.atitle=").append(URLEncoder.encode(title));

        String[] authors = getAuthors(true);
        if (authors != null && authors.length > 0)
        {    
            String[] auth = authors[0].split(",\\s+", 2);
            sb.append("&rft.aulast=").append(URLEncoder.encode(auth[0]));
            if (auth.length > 1) 
                sb.append("&rft.aufirst=").append(URLEncoder.encode(auth[1]));
        }
  
        String jtitle = getMField("journal_title");
        if (!jtitle.equals("")) 
            sb.append("&rft.jtitle=").append(URLEncoder.encode(jtitle));
    
        String date = getMField("journal_dateofissued");
        if (!date.equals("") && !type.equals("Thesis of Dissertation"))
        {
            if (date.length() > 4) 
                date = date.substring(0, 4);
            sb.append("&rft.date=").append(URLEncoder.encode(date));
        }
    
        String volume = getMField("journal_volume");
        if (!volume.equals("")) 
            sb.append("&rft.volume=").append(URLEncoder.encode(volume));
    
        String issue = getMField("journal_issue");
        if (!issue.equals("")) 
            sb.append("&rft.issue=").append(URLEncoder.encode(issue));
    
        String spage = getMField("spage");
        if (!spage.equals("")) 
            sb.append("&rft.spage=").append(URLEncoder.encode(spage));
    
        String epage = getMField("epage");
        if (!epage.equals("")) 
            sb.append("&rft.epage=").append(URLEncoder.encode(epage));
    
        String issn = getMField("issn");
        if (!issn.equals("")) 
            sb.append("&rft.issn=").append(URLEncoder.encode(issn));
    
        String btitle  = getMField("book_collection");
        if (!btitle.equals("")) 
            sb.append("&rft.btitle=").append(URLEncoder.encode(btitle));
    
        String series = getMField("book_series");
        if (!series.equals("")) 
            sb.append("&rft.series=").append(URLEncoder.encode(series));
    
        String isbn = getMField("isbn");
        if (!isbn.equals("")) 
            sb.append("&rft.isbn=").append(URLEncoder.encode(isbn));
    
        String grantor = getMField("thesis_grantor");
        if (!grantor.equals("")) 
            sb.append("&rft.inst=").append(URLEncoder.encode(grantor));
    
        String level = getMField("thesis_level");
        if (!level.equals("")) 
            sb.append("&rft.degree=").append(URLEncoder.encode(I18nUtil.getMessage("org.dspace.app.webui.util.ItemUtility."+level)));
        
        String year = getMField("thesis_year");
        if (!year.equals("") && type.equals("Thesis of Dissertation"))
            sb.append("&rft.date=").append(URLEncoder.encode(year));
    
        if (type.equals("Book"))
             sb.append("&rft.genre=bookitem");
        else if (type.equals("Journal Article") || type.equals("Departmental Bulletin Paper") || type.equals("Technical Report") || type.equals("Research Paper") || type.equals("Article"))
             sb.append("&rft.genre=article");
        else if (type.equals("Conference Paper") || type.equals("Presentation"))
             sb.append("&rft.genre=conference");
        else if (type.equals("Preprint"))
             sb.append("&rft.genre=preprint");
        else
             sb.append("&rft.genre=unknown");
     
        return Utils.addEntities(sb.toString());
    }

    /**
     * return values of the specific element.qualifier
     *
     * @param element - String
     *            element
     * @param qualifier - String
     *            qualifier
     * 
     * @return String[]
     * 
     */
    public String[] getFields(String element, String qualifier) 
    {
        if (qualifier != null && qualifier.equals("*"))
            qualifier = Item.ANY;

        DCValue[] dcvalues = item.getDC(element, qualifier, Item.ANY);
        if (dcvalues != null && dcvalues.length > 0)
        {
            String[] values = new String[dcvalues.length];
            for (int i = 0; i < dcvalues.length; i++)
            {
                values[i] = dcvalues[i].value;
            }
            return values;
        }
        else 
        {
            return new String[0];
        }
    }

    /**
     * return values of the specific element.qualifier
     *
     * @param mbstring - String
     * 
     * @return String[]
     * 
     */
    public String[] getFields(String mdString) 
    {

        DCValue[] dcvalues = item.getMetadata(mdString);
        if (dcvalues != null && dcvalues.length > 0)
        {
            String[] values = new String[dcvalues.length];
            for (int i = 0; i < dcvalues.length; i++)
            {
                values[i] = dcvalues[i].value;
            }
            return values;
        }
        else 
        {
            return new String[0];
        }
    }

    /**
     * return one value of the specific mapping metadata
     *
     * @param mdString - String
     *            schema.element.qualifier
     * @return String
     * 
     */
    public String getField(String mdString) 
    {

        String dcval = null;
        try 
        {
            dcval = item.getMetadata(mdString)[0].value;
        } 
        catch (ArrayIndexOutOfBoundsException e) 
        {
            return "";
        }
        return dcval;
    }

    /**
     * return values of the specific mapping metadata
     *
     * @param mdString - mapping key
     * @return String[]
     * 
     */
    public String[] getMFields(String key) 
    {
        String mdString = mdMapping.get(key);
        if (mdString == null || "".equals(mdString))
            return new String[0];
        else
            return getFields(mdString);
    }

    /**
     * return one value of the specific element.qualifier
     *
     * @param mdString - mapping key
     * @return String
     * 
     */
    public String getMField(String key) 
    {
        String mdString = mdMapping.get(key);
        if (mdString == null || "".equals(mdString))
            return "";
        else
            return getField(mdString);
    }

    /**
     * return one value of the specific element.qualifier
     *
     * @param schema - String
     *            schema
     * @param element - String
     *            element
     * @param qualifier - String
     *            qualifier
     * 
     * @return String
     * 
     */
    public String getField(String scheme, String element, String qualifier) 
    {

        String dcval = null;
        try 
        {
            dcval = item.getMetadata(scheme, element, qualifier, Item.ANY)[0].value;
        } 
        catch (ArrayIndexOutOfBoundsException e) 
        {
            return "";
        }
        return dcval;
    }

    /**
     * return one value of the specific element.qualifier
     *
     * @param element - String
     *            element
     * @param qualifier - String
     *            qualifier
     * 
     * @return String
     * 
     */
    public String getField(String element, String qualifier) 
    {
        return getField("dc", element, qualifier);
    }

    /**
     * return authors of the specific element.qualifier
     *
     * @param element - String
     *            element
     * @param qualifier - String
     *            qualifier
     * 
     * @return String[]
     * 
     */
    public String[] getAuthors(boolean cutid) 
    {
        String[] authors = getMFields("author");
        if (cutid && authors != null && authors.length > 0)
        {
            for (int i = 0; i < authors.length; i++)
            {
                if (authors[i] == null)
                    authors[i] = "";

                authors[i] = cutId(authors[i]);
            }
        }
        return authors;
    }

     /**
     * Get all non-internal bitstream URLs in the item. 
     * 
     * @param context
     *            DSpace context object
     * @return non-internal bitstream URL.
     */
    public Bitstream[] getBitstreams(String type) 
    {
        Bundle[] orig     = null;
        Bitstream primary = null;
        try 
        {
           orig = item.getBundles(type);
           if (orig.length == 0) 
              return new Bitstream[0];

           int id = orig[0].getPrimaryBitstreamID();
           if (id != -1) 
              primary = Bitstream.find(new Context(), id);
        } 
        catch (SQLException sqle) 
        {
           return new Bitstream[0];
        }
            
        Bitstream[] bitstreams = null;
        if (primary != null && primary.getFormat().getMIMEType().equals("text/html")) 
        {
           bitstreams    = new Bitstream[1];
           bitstreams[0] = primary;
        }
        else
        {
           bitstreams    = orig[0].getBitstreams();
        }
                
        return bitstreams;
     }

    private String cutId(String author)
    {
        int pos = author.indexOf("||");
        if (pos > -1)
            author = author.substring(0, pos);
        return author.trim();
    }

    private String getTitle(boolean html)
    {
        String title = getField("title", null);
        if (!title.equals(""))
        {
            if (title.endsWith("."))
                title = title.substring(0, title.length() - 1);
            if (html)
                title = Utils.addEntities(title);
        }
            
        return title;
    }

    private String getAuthor(boolean html, boolean cutid)
    {
        String author = "";
        String[] authors = getAuthors(cutid);
        if ((authors != null) && (authors.length > 0))
        {
            author = authors[0];
            if (authors.length > 2) 
                author += "... [et al.]";
            else if (authors.length > 1) 
                 author += " and " + authors[1];
        }
    
        if (html && !author.equals(""))
            author = Utils.addEntities(author);
    
        return author;
    }

    private String getDate(boolean html)
    {
        String issued = getField("date", "issued");
        if (!issued.equals("") && (issued.length() > 4))
            issued = issued.substring(0,4);
        if (html && !issued.equals(""))
            issued = Utils.addEntities(issued);
        return issued;
    }

    private String getJournal(boolean html)
    {
        // if there is a processed citation, we use it
        String citation = getCitation("journal", html);
        if (!"".equals(citation))
        {
            return citation;
        }

        StringBuilder sb = new StringBuilder();
        boolean volno = false;
    
        String jtitle = getMField("journal_title");
        if (!jtitle.equals(""))
        {
            if (jtitle.endsWith(".")) 
                jtitle = jtitle.substring(0, jtitle.length() - 1);
            sb.append(jtitle);
        }
    
        String volume = getMField("journal_volume");
        if (!volume.equals(""))
        {
            sb.append(", ").append(volume);
            volno = true;
        }
    
        String issue = getMField("journal_issue");
        if (!issue.equals(""))
        {
            if (volno)
                sb.append("(").append(issue).append(")");
            else
                sb.append(", ").append(issue);
        }
    
        String page = getPages("spage", "epage");
        if (!page.equals(""))
            sb.append(": ").append(page);    
    
        String journal = sb.toString();
        if (html && !journal.equals(""))
            journal = "<i>" + Utils.addEntities(journal) + "</i>";
        
        return journal;
    }

    public String getThesis(boolean html)
    {
        // if there is a processed citation, we use it
        String citation = getCitation("thesis", html);
        if (!"".equals(citation))
            return citation;

        StringBuilder sb = new StringBuilder();

        String grantor = getMField("thesis_grantor");
        if (!grantor.equals("")) 
            sb.append(grantor);
   /* 
        String year = getMField("thesis_year");
        if (!year.equals(""))
        {
            if (year.length() > 4)
                year = year.substring(0, 4);
            sb.append(" ("+year+")");
        }
   */ 
        String name = getMField("thesis_name");
        if (!name.equals("")) 
            sb.append(" "+name);
        
        String id = getMField("thesis_id");
        if (!id.equals("")) 
            sb.append(" "+id);
        
        String thesis = sb.toString();
        if (html && !thesis.equals(""))
            thesis = "<i>" + Utils.addEntities(thesis) + "</i>";
        
        return thesis;
    }

    public String getMeeting(boolean html)
    {
        // if there is a processed citation, we use it
        String citation = getCitation("conference", html);
        if (!"".equals(citation))
            return citation;

        StringBuilder sb = new StringBuilder();
        boolean pflag = false;
    
        String name = getMField("conference_name");
        if (!name.equals(""))
            sb.append(name);
    
        String place = getMField("conference_place");
        if (!place.equals(""))
        {
            sb.append(" (").append(place);
            pflag = true;
        }
    
        String date = getMField("conference_date");
        if (!date.equals(""))
        {
            if (pflag)
                sb.append(", ").append(date).append(")");
            else
                sb.append(" (").append(date).append(")");
            
            pflag = false;
        }
    
        if (pflag)
            sb.append(")");
    
        String meeting = sb.toString();
        if (html && !meeting.equals(""))
            meeting = "<i>" + Utils.addEntities(meeting) + "</i>";
        
        return meeting;
    }

    public String getBook(boolean html)
    {
        // if there is a processed citation, we use it
        String citation = getCitation("book", html);
        if (!"".equals(citation))
            return citation;

        StringBuilder sb = new StringBuilder();
    
        String title  = getMField("book_collection");
        String series = getMField("book_series");
        if (!title.equals(""))
        {
            if (title.endsWith(".")) 
                title = title.substring(0, title.length() - 1);
            sb.append("In ").append(title);
        } 
        else if (!series.equals(""))
        {
            if (series.endsWith(".")) 
                series = series.substring(0, series.length() - 1);
            sb.append(series);
        } 
    
        String edition = getMField("book_edition");
        if (!edition.equals(""))
            sb.append(". ").append(edition);
    
        String pages = getMField("pages");
        if (!pages.equals(""))
        {
            sb.append(": ").append(pages);
        }
        else
        {
            String page = getPages("spage", "epage");
            if (!page.equals(""))
                sb.append(": ").append(page);
        }

        String book = sb.toString();
        if (html && !book.equals(""))
            book = "<i>" + Utils.addEntities(book) + "</i>";
        return book;
    }

    public String getPages(String skey, String ekey)
    {
        String spage = getMField(skey);
        String epage = getMField(ekey);
    
        return spage + (!spage.equals("") && !epage.equals("") ? "-" : "") + epage;
    }

    private String getCitation(String key, boolean html)
    {
        String citation = getMField(key);
        if (html && !"".equals(citation))
            citation = "<i>" + Utils.addEntities(citation) + "</i>";
        return citation;
    }

    /**
     * Whether is an embargo set to the item's contents
     *
     * @param c - Context
     *            DSpace context
     * @param item - Item
     *            this item
     * 
     * @return boolean
     * 
     */
    public static boolean isEmbargo(Context c, Item item) 
    {
        try
        {
            Bitstream original = ((item.getBundles("ORIGINAL"))[0].getBitstreams())[0];
            return (AuthorizeManager.getPolicies(c, original).size() == 0 && !AuthorizeManager.isAdmin(c));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public String getAuthorsString(boolean html)
    {
        String[] authors = getAuthors(html);
        if (authors == null)
            return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String author : authors)
        {
            if (first)
                first = false;
            else
                sb.append(" and ");
            sb.append(author);
        }
        return sb.toString();
    }
        
    private static void setMetadataMapping() 
    {
        mdMapping = new HashMap<String, String>();
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader(mdPath));
            String line;
            while ((line = br.readLine()) != null)
            {
                line = line.trim();
                if (line.startsWith("#") || line.equals(""))
                    continue;
                String[] words = line.split("\\s*=\\s*");
                if (words.length != 2)
                    throw new IOException("invalid line in the mdMapping: "+line);
                if (mdMapping.containsKey(words[0]))
                    throw new IOException("duplicate element in the mdMapping: "+line);
                else 
                    mdMapping.put(words[0], words[1]);
            }
        }
        catch (IOException e)
        {
            mdMapping = null;
        }
        finally
        {
            try
            {
                if (br != null)
                    br.close();
            }
            catch (IOException e) 
            {
                // do nothing
            }
        }
    }

    private static void setDefaultMDMapping()
    {
        mdMapping = new HashMap<String, String>();
        mdMapping.put("journal", "dc.identifier.citation");
        mdMapping.put("book", "dc.identifier.citation");
        mdMapping.put("conference", "dc.identifier.citation");
        mdMapping.put("thesis", "dc.identifier.citation");
        mdMapping.put("author", "dc.contributor.author");
        mdMapping.put("niitype", "dc.type.nii");
        mdMapping.put("issn", "dc.identifier.issn");
        mdMapping.put("isbn", "dc.identifier.isbn");
        mdMapping.put("pmid", "dc.identifier.pmid");
        mdMapping.put("doi", "dc.identifier.doi");
        mdMapping.put("journal_title", "dc.source.jtitle");
        mdMapping.put("journal_volume", "dc.source.volume");
        mdMapping.put("journal_issue", "dc.source.issue");
        mdMapping.put("spage", "dc.source.spage");
        mdMapping.put("epage", "dc.source.epage");
        mdMapping.put("journal_dateofissued", "dc.date.issued");
        mdMapping.put("thesis_grantor", "dc.description.grantor");
        mdMapping.put("thesis_name", "dc.description.degreename");
        mdMapping.put("thesis_year", "dc.date.granted");
        mdMapping.put("thesis_id", "dc.identifier.grantid");
        mdMapping.put("book_collection", "dc.book.collectivetitle");
        mdMapping.put("book_series", "dc.book.series");
        mdMapping.put("book_edition", "dc.book.edition");
        mdMapping.put("pages", "dc.book.pages");
        mdMapping.put("conference_name", "dc.meeting.name");
        mdMapping.put("conference_place", "dc.meeting.place");
        mdMapping.put("conference_date", "dc.meeting.date");
    }

}
