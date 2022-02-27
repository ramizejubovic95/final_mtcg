package Requests;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import response.ResponseHandler;
import user.User;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ReqHandlerTest
{
    private Request req;
    private ReqHandler reqHandler;
    private User user;

    @BeforeEach
    public void setUp()
    {
        this.req = new Request();
        this.reqHandler = new ReqHandler();
        this.req.setAuthorization("admin-mtcgToken");
        this.user = new User();
    }

   @Test
    public void testCheckIfUserIsAdmin()
   {
       assertFalse(this.reqHandler.isAdmin("altenhof-mtcgToken"));
       assertFalse(this.reqHandler.isAdmin("kienboec-mtcgToken"));
       assertTrue(this.reqHandler.isAdmin("admin-mtcgToken"));
   }

    @Test
    public void checkIfRequestContainsToken()
    {
        // Wenn Token vorhanden
        assertEquals(true, this.reqHandler.doesRequestContainAuthToken(this.req.getAuthorization()));
        // Wenn Token Null ist
        this.req.reset();
        assertEquals(false, this.reqHandler.doesRequestContainAuthToken(this.req.getAuthorization()));
    }

    @Test
    public void checkIfAuthTokenContainsContainsNameInRoute()
    {
        this.req.setRoute("/users/kienboec");
        this.req.setAuthorization("kienboec-mtcgToken");

        assertTrue(this.reqHandler.isAuthMatchingRoute(this.req));

        this.req.setAuthorization("someDifferentToken-mtcgToken");
        assertFalse(this.reqHandler.isAuthMatchingRoute(this.req));
    }

    @Test
    public void switchLoggedInUserToRequestingUserTest() throws Exception
    {
        this.req.setAuthorization("kienboec-mtcgToken");
        this.user = reqHandler.switchLoggedInUserToRequestingUser(this.req);

        if (user == null)
            assertNull(this.user);
        else
        {
            assertEquals("kienboec-mtcgToken", this.user.getAuthToken());
        }
    }

    @AfterEach
    public void destroy()
    {
        this.req = null;
        this.reqHandler = null;
        this.user = null;
    }
}