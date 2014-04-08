/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.lookup;

import gr.ekt.bte.core.Record;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.util.XMLUtils;
<<<<<<< HEAD
import org.dspace.core.ConfigurationManager;
=======
>>>>>>> upstream/master
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Keiji Suzuki
 */
public class CiNiiService
{
    /** log4j category */
    private static Logger log = Logger.getLogger(CiNiiService.class);

    private int timeout = 1000;

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

<<<<<<< HEAD
    public Record getByCiNiiID(String id) throws HttpException,
            IOException
    {
            return search(id);
    }

    public List<Record> searchByTerm(String title, String author, int year)
=======
    public Record getByCiNiiID(String id, String appId) throws HttpException,
            IOException
    {
            return search(id, appId);
    }

    public List<Record> searchByTerm(String title, String author, int year, 
            int maxResults, String appId)
>>>>>>> upstream/master
            throws HttpException, IOException
    {
        List<Record> records = new ArrayList<Record>();

<<<<<<< HEAD
        List<String> ids = getCiNiiIDs(title, author, year);
=======
        List<String> ids = getCiNiiIDs(title, author, year, maxResults, appId);
>>>>>>> upstream/master
        if (ids != null && ids.size() > 0)
        {
            for (String id : ids)
            {
<<<<<<< HEAD
                Record record = search(id);
=======
                Record record = search(id, appId);
>>>>>>> upstream/master
                if (record != null)
                {
                    records.add(record);
                }
            }
        }

        return records;
    }

    /**
     * Get metadata by searching CiNii RDF API with CiNii NAID
     *
     */
<<<<<<< HEAD
    private Record search(String id) throws IOException, HttpException
    {
        String appid = ConfigurationManager.getProperty(SubmissionLookupService.CFG_MODULE, "cinii.appid");
        // Need CiNii Application ID to use the CiNii API
        if (appid == null)
        {
            log.warn("cinii.appid is not set!");
            return null;
        }

        if (!ConfigurationManager.getBooleanProperty(SubmissionLookupService.CFG_MODULE, "remoteservice.demo"))
        {
            GetMethod method = null;
            try
            {
                HttpClient client = new HttpClient();
                client.setTimeout(timeout);
                method = new GetMethod("http://ci.nii.ac.jp/naid/"+id+".rdf?appid="+appid);
                // Execute the method.
                int statusCode = client.executeMethod(method);

                if (statusCode != HttpStatus.SC_OK)
                {
                    if (statusCode == HttpStatus.SC_BAD_REQUEST)
                        throw new RuntimeException("CiNii RDF is not valid");
                    else
                        throw new RuntimeException("CiNii RDF Http call failed: "
                                + method.getStatusLine());
                }

                try
                {
                    DocumentBuilderFactory factory = DocumentBuilderFactory
                            .newInstance();
                    factory.setValidating(false);
                    factory.setIgnoringComments(true);
                    factory.setIgnoringElementContentWhitespace(true);

                    DocumentBuilder db = factory.newDocumentBuilder();
                    Document inDoc = db.parse(method.getResponseBodyAsStream());

                    Element xmlRoot = inDoc.getDocumentElement();

                    return CiNiiUtils.convertCiNiiDomToRecord(xmlRoot);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(
                            "CiNii RDF identifier is not valid or not exist");
                }
            }
            finally
            {
                if (method != null)
                {
                    method.releaseConnection();
                }
            }
        }
        else
        {
            InputStream stream = null;
            try
            {
                File file = new File(
                        ConfigurationManager.getProperty("dspace.dir")
                                + "/config/crosswalks/demo/cinii.xml");
                stream = new FileInputStream(file);
                try
                {
                    DocumentBuilderFactory factory = DocumentBuilderFactory
                            .newInstance();
                    factory.setValidating(false);
                    factory.setIgnoringComments(true);
                    factory.setIgnoringElementContentWhitespace(true);
                    DocumentBuilder db = factory.newDocumentBuilder();
                    Document inDoc = db.parse(stream);

                    Element xmlRoot = inDoc.getDocumentElement();

                    return CiNiiUtils.convertCiNiiDomToRecord(xmlRoot);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(
                            "CiNii RDF identifier is not valid or not exist");
                }
            }
            finally
            {
                if (stream != null)
                {
                    stream.close();
                }
=======
    private Record search(String id, String appId)
        throws IOException, HttpException
    {
        GetMethod method = null;
        try
        {
            HttpClient client = new HttpClient();
            client.setTimeout(timeout);
            method = new GetMethod("http://ci.nii.ac.jp/naid/"+id+".rdf?appid="+appId);
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK)
            {
                if (statusCode == HttpStatus.SC_BAD_REQUEST)
                    throw new RuntimeException("CiNii RDF is not valid");
                else
                    throw new RuntimeException("CiNii RDF Http call failed: "
                            + method.getStatusLine());
            }

            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                factory.setValidating(false);
                factory.setIgnoringComments(true);
                factory.setIgnoringElementContentWhitespace(true);

                DocumentBuilder db = factory.newDocumentBuilder();
                Document inDoc = db.parse(method.getResponseBodyAsStream());

                Element xmlRoot = inDoc.getDocumentElement();

                return CiNiiUtils.convertCiNiiDomToRecord(xmlRoot);
            }
            catch (Exception e)
            {
                throw new RuntimeException(
                        "CiNii RDF identifier is not valid or not exist");
            }
        }
        finally
        {
            if (method != null)
            {
                method.releaseConnection();
>>>>>>> upstream/master
            }
        }
    }

    /**
     * Get CiNii NAIDs by searching CiNii OpenURL API with title, author and year
     *
     */
<<<<<<< HEAD
    private  List<String> getCiNiiIDs(String title, String author, int year) 
        throws IOException, HttpException
    {
        String appid = ConfigurationManager.getProperty(SubmissionLookupService.CFG_MODULE, "cinii.appid");
        // Need CiNii Application ID to use the CiNii API
        if (appid == null)
        {
            log.warn("cinii.appid is not set!");
            return null;
        }

=======
    private  List<String> getCiNiiIDs(String title, String author, int year, 
        int maxResults, String appId) 
        throws IOException, HttpException
    {
>>>>>>> upstream/master
        // Need at least one query term
        if (title == null && author == null && year == -1)
        {
            return null;
        }

        GetMethod method = null;
        List<String> ids = new ArrayList<String>();
        try
        {
            HttpClient client = new HttpClient();
            client.setTimeout(timeout);
            StringBuilder query = new StringBuilder();
<<<<<<< HEAD
            query.append("format=rss&appid=").append(appid);
=======
            query.append("format=rss&appid=").append(appId)
                 .append("&count=").append(maxResults);
>>>>>>> upstream/master
            if (title != null)
            {
                query.append("&title=").append(URLEncoder.encode(title, "UTF-8"));
            }
            if (author != null)
            {
                query.append("&author=").append(URLEncoder.encode(author, "UTF-8"));
            }
            if (year != -1)
            {
                query.append("&year_from=").append(String.valueOf(year));
                query.append("&year_to=").append(String.valueOf(year));
            }
            method = new GetMethod("http://ci.nii.ac.jp/opensearch/search?"+query.toString());
            // Execute the method.
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK)
            {
                if (statusCode == HttpStatus.SC_BAD_REQUEST)
                    throw new RuntimeException("CiNii OpenSearch query is not valid");
                else
                    throw new RuntimeException("CiNii OpenSearch call failed: "
                            + method.getStatusLine());
            }

            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                factory.setValidating(false);
                factory.setIgnoringComments(true);
                factory.setIgnoringElementContentWhitespace(true);

                DocumentBuilder db = factory.newDocumentBuilder();
                Document inDoc = db.parse(method.getResponseBodyAsStream());

                Element xmlRoot = inDoc.getDocumentElement();
                List<Element> items = XMLUtils.getElementList(xmlRoot, "item");

                int url_len = "http://ci.nii.ac.jp/naid/".length();
                for (Element item : items)
                {
                    String about = item.getAttribute("rdf:about");
                    if (about.length() > url_len)
                    {
                        ids.add(about.substring(url_len));
                    }
                }

                return ids;
            }
            catch (Exception e)
            {
                throw new RuntimeException(
                              "CiNii OpenSearch results is not valid or not exist");
            }
        }
        finally
        {
            if (method != null)
            {
                method.releaseConnection();
            }
        }
    }
}
