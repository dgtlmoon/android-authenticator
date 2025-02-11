/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.android.sync.rest;

import okhttp3.ResponseBody;
import org.xwiki.android.sync.bean.ObjectSummary;
import org.xwiki.android.sync.bean.SearchResultContainer;
import org.xwiki.android.sync.bean.SerachResults.CustomObjectsSummariesContainer;
import org.xwiki.android.sync.bean.SerachResults.CustomSearchResultContainer;
import org.xwiki.android.sync.bean.XWikiGroup;
import org.xwiki.android.sync.bean.XWikiUserFull;
import retrofit2.Response;
import retrofit2.http.*;
import rx.Observable;

import static org.xwiki.android.sync.rest.ApiEndPoints.SPACES;

/**
 * Interface for interacting with XWiki services
 *
 * @see retrofit2.Retrofit#create(Class)
 * @see <a href="http://square.github.io/retrofit/">Retrofit docs</a>
 *
 * @version $Id$
 */
public interface XWikiServices {

    @POST("bin/login/XWiki/XWikiLogin")
    Observable<Response<ResponseBody>> login(@Header("Authorization") String basicAuth);

    @FormUrlEncoded
    @PUT(ApiEndPoints.REST + ApiEndPoints.WIKIS + "/{wiki}/" + SPACES + "/{space}/" + ApiEndPoints.PAGES + "/{pageName}/" + ApiEndPoints.XWIKI_OBJECTS)
    Observable<XWikiUserFull> updateUser(
            @Path("wiki") String wiki,
            @Path("space") String space,
            @Path("pageName") String pageName,
            @Field("property#first_name") String firstName,
            @Field("property#last_name") String lastName,
            @Field("property#email") String email,
            @Field("property#phone") String phone,
            @Field("property#address") String address,
            @Field("property#company") String company,
            @Field("property#comment") String comment
    );

    /**
     * @since 0.4
     */
    @GET(ApiEndPoints.REST + ApiEndPoints.WIKIS + "/query?q=object:XWiki.XWikiGroups")
    Observable<CustomSearchResultContainer<XWikiGroup>> availableGroups(
            @Query("number") Integer number
    );

    /**
     * @since 0.4
     */
    @GET(
        ApiEndPoints.REST +
            ApiEndPoints.WIKIS +
            "/xwiki/" +
            ApiEndPoints.SPACES +
            "/{space}/" +
            ApiEndPoints.PAGES +
            "/{name}/objects/XWiki.XWikiUsers/0"
    )
    Observable<XWikiUserFull> getFullUserDetails(
        @Path("space") String space,
        @Path("name") String name
    );

    /**
     * @since 0.4
     */
    @GET(
        ApiEndPoints.REST +
            ApiEndPoints.WIKIS +
            "/{wiki}/" +
            ApiEndPoints.SPACES +
            "/{space}/" +
            ApiEndPoints.PAGES +
            "/{name}/objects/XWiki.XWikiUsers/0"
    )
    Observable<XWikiUserFull> getFullUserDetails(
        @Path("wiki") String wiki,
        @Path("space") String space,
        @Path("name") String name
    );

    /**
     * @since 0.4
     */
    @GET(
        ApiEndPoints.REST +
            ApiEndPoints.WIKIS +
            "/xwiki/classes/XWiki.XWikiUsers/objects"
    )
    Observable<CustomObjectsSummariesContainer<ObjectSummary>> getAllUsersPreview();

    /**
     * @since 0.4
     */
    @GET(
        ApiEndPoints.REST +
            ApiEndPoints.WIKIS +
            "/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers" +
            "&number={count}" +
            "&start={offset}"
    )
    Observable<SearchResultContainer> getUsersPreview(
        @Query("number") Integer count,
        @Query("start") Integer offset
    );

    /**
     * @since 0.4
     */
    @GET(
            ApiEndPoints.REST +
                    ApiEndPoints.WIKIS +
                    "/{wiki}/" +
                    ApiEndPoints.SPACES +
                    "/{space}/" +
                    ApiEndPoints.PAGES +
                    "/{name}/objects/XWiki.XWikiGroups"
    )
    Observable<CustomObjectsSummariesContainer<ObjectSummary>> getGroupMembers(
            @Path("wiki") String wiki,
            @Path("space") String space,
            @Path("name") String name
    );
}
