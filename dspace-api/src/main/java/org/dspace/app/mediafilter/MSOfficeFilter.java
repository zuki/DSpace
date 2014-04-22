/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.mediafilter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;

/*
 * 
 * to do: helpful error messages - can't find mediafilter.cfg - can't
 * instantiate filter - bitstream format doesn't exist.
 *  
 */
public class MSOfficeFilter extends MediaFilter
{

    private static Logger log = Logger.getLogger(MSOfficeFilter.class);

    public String getFilteredName(String oldFilename)
    {
        return oldFilename + ".txt";
    }

    /**
     * @return String bundle name
     *  
     */
    public String getBundleName()
    {
        return "TEXT";
    }

    /**
     * @return String bitstreamformat
     */
    public String getFormatString()
    {
        return "Text";
    }

    /**
     * @return String description
     */
    public String getDescription()
    {
        return "Extracted text";
    }

    /**
     * @param source
     *            source input stream
     * 
     * @return InputStream the resulting input stream
     */
    public InputStream getDestinationStream(InputStream source)
            throws Exception
    {
        // get input stream from bitstream
        // pass to filter, get string back
        try  
        {
            String extractedText;
            POITextExtractor extractor = ExtractorFactory.createExtractor(source);
            if (extractor instanceof PowerPointExtractor)
            {
                extractedText = ((PowerPointExtractor) extractor)
                        .getText(true, true);
            }
            else if (extractor instanceof XSLFPowerPointExtractor)
            {
                extractedText = ((XSLFPowerPointExtractor) extractor)
                        .getText(true, true);
            }
            else
            {
                extractedText = extractor.getText();
            }

            // if verbose flag is set, print out extracted text
            // to STDOUT
            if (MediaFilterManager.isVerbose)
            {
                System.out.println(extractedText);
            }

            // generate an input stream with the extracted text
            byte[] textBytes = extractedText.getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(textBytes);

            return bais; // will this work? or will the byte array be out of scope?
        } 
        catch (Exception e)
        {
            System.out.println("MSOfficeFilter Exception");
            log.error("Error detected : " + e.getMessage(), e);
            throw e;
        }
    }
}
