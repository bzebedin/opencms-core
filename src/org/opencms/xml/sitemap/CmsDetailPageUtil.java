/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsDetailPageUtil.java,v $
 * Date   : $Date: 2011/02/02 07:37:52 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This is a utility class which provides convenience methods for finding detail page names for resources which include
 * the URL names of the resources themselves.<p>
 * 
 * @see I_CmsDetailPageFinder
 * 
 * @author Georg Westenberger
 *
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public final class CmsDetailPageUtil {

    /**
     * The hidden default constructor.<p>
     */
    private CmsDetailPageUtil() {

        // do nothing
    }

    /**
     * Gets a list of detail page URIs for the given resource, with its URL name appended.<p>
     *  
     * @param cms the current CMS context 
     * @param res the resource for which the detail pages should be retrieved 
     * 
     * @return the list of detail page URIs 
     * 
     * @throws CmsException if something goes wrong 
     */
    public static List<String> getAllDetailPagesWithUrlName(CmsObject cms, CmsResource res) throws CmsException {

        List<String> result = new ArrayList<String>();
        Collection<String> detailPages = OpenCms.getSitemapManager().getDetailPageFinder().getAllDetailPages(cms, res);
        if (detailPages.isEmpty()) {
            return Collections.<String> emptyList();
        }
        String urlName = cms.getDetailName(res);
        for (String detailPage : detailPages) {
            String rootPath = CmsStringUtil.joinPaths(detailPage, urlName, "/");
            result.add(rootPath);
        }
        return result;
    }

    /**
     * Returns either the newest URL name for a structure id, or  the structure id as a string if there is no URL name.<p>
     * 
     * @param cms the current CMS context 
     * @param id the structure id of a resource 
     * 
     * @return the best URL name for the structure id 
     * 
     * @throws CmsException if something goes wrong 
     */
    public static String getBestUrlName(CmsObject cms, CmsUUID id) throws CmsException {

        String urlName = cms.readNewestUrlNameForId(id);
        if (urlName != null) {
            return urlName;
        }
        return id.toString();
    }

    /**
     * Gets the best detail page for a given resource and link source, with the URL name appended.<p>
     * 
     * @param cms the current CMS context 
     * @param res the resources for which the detail page name should be retrieved 
     * @param linkSource the source of the link
     *  
     * @return the detail page URI, including the URL name 
     * 
     * @throws CmsException if something goes wrong 
     */
    public static String getDetailPageWithUrlName(CmsObject cms, CmsResource res, String linkSource)
    throws CmsException {

        String detailPage = OpenCms.getSitemapManager().getDetailPageFinder().getDetailPage(cms, res, linkSource);
        if (detailPage == null) {
            return null;
        }
        String rootPath = CmsStringUtil.joinPaths(detailPage, cms.getDetailName(res), "/");
        return rootPath;
    }

    /**
     * Looks up a page by URI (which may be a detail page URI, or a normal VFS uri).<p>
     * 
     * @param cms the current CMS context
     * @param uri the detail page or VFS uri 
     * 
     * @return the resource with the given uri
     *  
     * @throws CmsException if something goes wrong 
     */
    public static CmsResource lookupPage(CmsObject cms, String uri) throws CmsException {

        try {
            CmsResource res = cms.readResource(uri);
            return res;
        } catch (CmsVfsResourceNotFoundException e) {
            String detailName = CmsResource.getName(uri).replaceAll("/$", "");
            CmsUUID detailId = cms.readIdForUrlName(detailName);
            if (detailId != null) {
                return cms.readResource(detailId);
            }
            throw new CmsVfsResourceNotFoundException(org.opencms.db.generic.Messages.get().container(
                org.opencms.db.generic.Messages.ERR_READ_RESOURCE_1,
                uri));
        }
    }

}
