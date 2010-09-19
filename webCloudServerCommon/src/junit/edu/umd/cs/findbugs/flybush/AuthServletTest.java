package edu.umd.cs.findbugs.flybush;

import edu.umd.cs.findbugs.cloud.appEngine.protobuf.ProtoClasses.FindIssuesResponse;
import edu.umd.cs.findbugs.cloud.appEngine.protobuf.ProtoClasses.LogIn;

import javax.jdo.Query;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"UnusedDeclaration"})
public abstract class AuthServletTest extends AbstractFlybushServletTest {
    @Override
    protected AbstractFlybushServlet createServlet() {
        return new AuthServlet();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void DISABLED_testBrowserAuthLoginRedirect() throws Exception {
        executeGet("/browser-auth/100");
        verify(mockResponse).sendRedirect(anyString());
    }

    public void testBrowserAuthWhenLoggedIn() throws Exception {
        initOpenidUserParameter();

        executeGet("/browser-auth/100");

        verify(mockResponse).setStatus(200);
    	verify(mockResponse).setContentType("text/html");
        String outputString = new String(outputCollector.toByteArray(), "UTF-8");
        assertTrue("Should contain 'now signed in': " + outputString,
                   outputString.contains("now signed in"));
		assertTrue("Should contain email address: " + outputString,
                   outputString.contains("my@email.com"));
        assertTrue("Should contain OpenID URL: " + outputString,
                   outputString.contains("http://some.website"));
    }

    public void testCheckAuthForValidId() throws Exception {
        DbUser user = persistenceHelper.createDbUser("http://some.website", "my@email.com");
        SqlCloudSession session = persistenceHelper.createSqlCloudSession(100, new Date(200),
                                                                          user.createKeyObject(), "my@email.com");
        getPersistenceManager().makePersistentAll(user, session);

        executeGet("/check-auth/100");

        checkResponse(200, "OK\n100\nmy@email.com\n");
    }

    public void testCheckAuthForNonexistentId() throws Exception {
        executeGet("/check-auth/100");

        checkResponse(418, "FAIL\n");
    }

    public void testLogOut() throws Exception {
        // authenticate
        createCloudSession(555);

        executeLogIn();
        checkResponse(200);
        FindIssuesResponse result = FindIssuesResponse.parseFrom(outputCollector.toByteArray());
        assertEquals(0, result.getFoundIssuesCount());

        initServletAndMocks();

        // log out
        executePost("/log-out/555", new byte[0]);
        checkResponse(200);

        initServletAndMocks();

        // make sure login no longer works
        executeLogIn();
        checkResponse(403, "not authenticated");
    }


    public void testLogInUnauthenticated() throws Exception {
        executeLogIn();
        checkResponse(403, "not authenticated");
    }

    public void testLogInNoneFound() throws Exception {
        createCloudSession(555);
        executeLogIn();
        checkResponse(200);
        FindIssuesResponse result = FindIssuesResponse.parseFrom(outputCollector.toByteArray());
        assertEquals(0, result.getFoundIssuesCount());
    }

    @SuppressWarnings("unchecked")
    public void testLogInStoresInvocation() throws Exception {
        createCloudSession(555);
        executeLogIn();
        checkResponse(200);
        FindIssuesResponse result = FindIssuesResponse.parseFrom(outputCollector.toByteArray());
        assertEquals(0, result.getFoundIssuesCount());
        Query query = getPersistenceManager().newQuery(
                "select from " + persistenceHelper.getDbInvocationClass().getName());
        List<DbInvocation> invocations = (List<DbInvocation>) query.execute();
        assertEquals(1, invocations.size());
        assertEquals("my@email.com", getDbUser(invocations.get(0).getWho()).getEmail());
		query.closeAll();
    }

    // ========================= end of tests ================================

    private void executeLogIn() throws IOException {
        executePost("/log-in",
                    LogIn.newBuilder()
                            .setSessionId(555).setAnalysisTimestamp(100)
                            .build().toByteArray());
    }
}
