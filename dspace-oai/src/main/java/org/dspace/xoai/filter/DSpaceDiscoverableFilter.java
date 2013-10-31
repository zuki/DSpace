/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.xoai.filter;

import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.handle.HandleManager;
import org.dspace.xoai.data.DSpaceItem;

/**
 * 
 * @author Keiji Suzuki <zuki.ebetsu@gmail.com>
 */
public class DSpaceDiscoverableFilter extends DSpaceFilter
{
    private static Logger log = LogManager
            .getLogger(DSpaceDiscoverableFilter.class);

    @Override
    public DatabaseFilterResult getWhere(Context context)
    {
         if (DatabaseManager.isOracle())
         {
             return new DatabaseFilterResult("i.discoverable=1");
         }
         else
         {
             return new DatabaseFilterResult("i.discoverable=TRUE");
         }
    }

    @Override
    public boolean isShown(DSpaceItem item)
    {
        try
        {
            Context ctx = super.getContext();
            String handle = DSpaceItem.parseHandle(item.getIdentifier());
            if (handle == null) return false;
            Item dsitem = (Item) HandleManager.resolveToObject(ctx, handle);
            return dsitem.isDiscoverable();
        }
        catch (SQLException ex)
        {
            log.error(ex.getMessage());
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
        }
        return false;
    }

    @Override
    public SolrFilterResult getQuery()
    {
        return new SolrFilterResult("item.discoverable:true");
    }

}
