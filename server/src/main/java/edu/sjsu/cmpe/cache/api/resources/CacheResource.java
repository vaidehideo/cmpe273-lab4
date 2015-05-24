package edu.sjsu.cmpe.cache.api.resources;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.cache.domain.Entry;
import edu.sjsu.cmpe.cache.repository.CacheInterface;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CacheResource {
    private final CacheInterface cache;

    /**
     * CacheResource constructor
     *
     * @param cache
     *            a InMemoryCache instance
     */
    public CacheResource(CacheInterface cache) {
        this.cache = cache;
    }

    @GET
    @Path("{key}")
    @Timed(name = "get-entry")
    public Entry get(@PathParam("key") LongParam key) {
        Entry e = cache.get(key.get());
        if (e!=null)
            return e;
        else return null;
    }

    @GET
    @Timed(name = "view-all-entries")
    public List<Entry> getAll() {
        return cache.getAll();
    }

    @PUT
    @Path("{key}/{value}")
    @Timed(name = "add-entry")
    public Response put(@PathParam("key") LongParam key,
                        @PathParam("value") String value) {
        Entry entry = new Entry();
        entry.setKey(key.get());
        entry.setValue(value);

        cache.save(entry);

        return Response.status(200).build();
    }


    @DELETE
    @Path("{key}")
    @Timed(name = "delete-entry")
    public Response delete(@PathParam("key") LongParam key){
        if(cache.delete(key.get())!=null)
            return Response.status(204).build();
        else
            return Response.status(400).build();
    }

}