/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dan Rollo
 *******************************************************************************/

package waffle.shiro.negotiate;

import org.apache.shiro.codec.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Dan Rollo
 * Date: 2/14/13
 * Time: 11:11 PM
 */
public final class NegotiateAuthenticationFilterTest {

    private NegotiateAuthenticationFilter negAuthFilter;

    private MockServletResponse response;
    private byte[] out;

    @Before
    public void setUp() {
        negAuthFilter = new NegotiateAuthenticationFilter();

        response = new MockServletResponse();
    }

    @Test
    public void testIsLoginAttempt()  {
        Assert.assertFalse(negAuthFilter.isLoginAttempt(""));
        Assert.assertTrue(negAuthFilter.isLoginAttempt("NEGOTIATe"));
        Assert.assertTrue(negAuthFilter.isLoginAttempt("ntlm"));
    }

    @Test
    public void testSendChallengeInitiateNegotiate() {

        out = new byte[1];
        out[0] = -1;

        negAuthFilter.sendChallengeInitiateNegotiate(response);

        Assert.assertEquals("Negotiate", response.headersAdded.get("WWW-Authenticate").get(0));
        Assert.assertEquals("NTLM", response.headersAdded.get("WWW-Authenticate").get(1));

        Assert.assertEquals("keep-alive", response.headers.get("Connection"));

        Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.sc);
        Assert.assertEquals(0, response.errorCode);

        Assert.assertFalse(response.isFlushed);
    }

    @Test
    public void testSendChallengeDuringNegotiate() {

        final String myProtocol = "myProtocol";

        out = new byte[1];
        out[0] = -1;

        negAuthFilter.sendChallengeDuringNegotiate(myProtocol, response, out);

        Assert.assertEquals(myProtocol + " " + Base64.encodeToString(out), response.headers.get("WWW-Authenticate"));

        Assert.assertEquals("keep-alive", response.headers.get("Connection"));

        Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.sc);
        Assert.assertEquals(0, response.errorCode);

        Assert.assertFalse(response.isFlushed);
    }

    @Test
    public void testSendChallengeOnFailure() {

        negAuthFilter.sendChallengeOnFailure(response);

        Assert.assertEquals("Negotiate", response.headersAdded.get("WWW-Authenticate").get(0));
        Assert.assertEquals("NTLM", response.headersAdded.get("WWW-Authenticate").get(1));

        Assert.assertEquals("close", response.headers.get("Connection"));

        Assert.assertEquals(0, response.sc);
        Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.errorCode);

        Assert.assertTrue(response.isFlushed);
    }

    private static class MockServletResponse implements HttpServletResponse {
        private void notImplemented() {
            throw new RuntimeException("not implemented");
        }

        @Override
        public String getCharacterEncoding() { notImplemented(); return null; }

        @Override
        public String getContentType() { notImplemented(); return null; }

        @Override
        public ServletOutputStream getOutputStream() throws IOException { notImplemented(); return null; }

        @Override
        public PrintWriter getWriter() throws IOException { notImplemented(); return null; }

        @Override
        public void setCharacterEncoding(String charset) { notImplemented(); }

        @Override
        public void setContentLength(int len) { notImplemented(); }

        @Override
        public void setContentType(String type) { notImplemented(); }

        @Override
        public void setBufferSize(int size) { notImplemented(); }

        @Override
        public int getBufferSize() { notImplemented(); return 0; }

        boolean isFlushed;
        @Override
        public void flushBuffer() throws IOException {
            isFlushed = true;
        }

        @Override
        public void resetBuffer() { notImplemented(); }

        @Override
        public boolean isCommitted() { notImplemented(); return false; }

        @Override
        public void reset() { notImplemented(); }

        @Override
        public void setLocale(Locale loc) { notImplemented(); }

        @Override
        public Locale getLocale() { notImplemented(); return null; }

        @Override
        public void addCookie(Cookie cookie) { notImplemented(); }

        @Override
        public boolean containsHeader(String name) { notImplemented(); return false; }

        @Override
        public String encodeURL(String url) { notImplemented(); return null; }

        @Override
        public String encodeRedirectURL(String url) { notImplemented(); return null; }

        @Override
        public String encodeUrl(String url) { notImplemented(); return null; }

        @Override
        public String encodeRedirectUrl(String url) { notImplemented(); return null; }

        @Override
        public void sendError(int sc, String msg) throws IOException { notImplemented(); }

        int errorCode;
        @Override
        public void sendError(int sc) throws IOException {
            errorCode = sc;
        }

        @Override
        public void sendRedirect(String location) throws IOException { notImplemented(); }

        @Override
        public void setDateHeader(String name, long date) { notImplemented(); }

        @Override
        public void addDateHeader(String name, long date) { notImplemented(); }

        final Map<String, String> headers = new HashMap<String, String>();
        @Override
        public void setHeader(String name, String value) {
            headers.put(name, value);
        }


        final Map<String, List<String>> headersAdded = new HashMap<String, List<String>>();
        @Override
        public void addHeader(String name, String value) {
            if (headersAdded.containsKey(name)) {
                headersAdded.get(name).add(value);
                return;
            }

            List<String> values = new ArrayList<String>();
            values.add(value);
            headersAdded.put(name, values);
        }

        @Override
        public void setIntHeader(String name, int value) { notImplemented(); }

        @Override
        public void addIntHeader(String name, int value) { notImplemented(); }

        int sc;
        @Override
        public void setStatus(int sc) {
            this.sc = sc;
        }

        @Override
        public void setStatus(int sc, String sm) { notImplemented(); }
    }

}
