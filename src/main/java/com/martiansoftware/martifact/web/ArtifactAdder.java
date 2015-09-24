package com.martiansoftware.martifact.web;

import com.martiansoftware.boom.Boom;
import static com.martiansoftware.boom.Boom.halt;
import static com.martiansoftware.boom.Boom.q;
import static com.martiansoftware.boom.Boom.request;
import com.martiansoftware.martifact.ArtifactStore;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

/**
 *
 * @author mlamb
 */
public class ArtifactAdder {

    private final ArtifactStore _store;
    private static final int HTTP_MISSING_OR_BAD_PARAM = 422;
    
    public ArtifactAdder(ArtifactStore store) { _store = store; }
    
    private Date getFileTimeFromRequest() {
        String fileTimeS = q("filetime");
        if (fileTimeS == null) return new Date();
        try {
            return new Date(Long.parseLong(fileTimeS));
        } catch (Exception e) {
            halt(HTTP_MISSING_OR_BAD_PARAM, String.format("Unable to parse filetime '%s': " + e.getMessage(), fileTimeS));
            return null; // unreachable due to halt, included to appease compiler
        }
    }
    
    private Collection<String> getTagsFromRequest() {
        String tagString = q("tags");
        if (tagString == null) return Collections.EMPTY_LIST;
        return Arrays.asList(tagString.split("\\s+"));
    }
    
    // e.g.: curl -H "ACCEPT: text/plain" -F file=@testdisk.log -F "filetime=`date -r testdisk.log '+%s000'`" -F "tags=tag1 tag2 tag3" http://127.0.0.1:4567/add
    Object add() throws IOException, ServletException {
        MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp/boom", Long.MAX_VALUE, Long.MAX_VALUE, 0);
        request().raw().setAttribute(org.eclipse.jetty.server.Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
   
        System.err.println("MARTY: " + Boom.h("Accept"));
        Part file = request().raw().getPart("file");
        if (file == null) halt(HTTP_MISSING_OR_BAD_PARAM, "No file provided!");

        return ArtifactResponse.of(_store.create(file.getSubmittedFileName(),
                                                 file.getInputStream(),
                                                 getFileTimeFromRequest(),
                                                 getTagsFromRequest()));
    }
    
}
