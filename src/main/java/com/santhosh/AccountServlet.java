package com.santhosh;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Logger;


/**
 * Servlet implementation class AccountServlet
 */
@WebServlet("/account")
public class AccountServlet extends HttpServlet {
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AccountServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    private static final long serialVersionUID = 1L;

    /** User ID key. */
    public static final String USER_ID = "userId";

    /** Class logging facility. */
    public static final Logger LOG = Logger.getLogger(AccountServlet.class);


    /** Database connection */
    private transient Connection dbConnection = null;

    /** A cache to store user details in order to avoid hitting the database all the time. */
    private static final Map<Integer, Map<String, Object>> USER_DETAILS_CACHE = new HashMap<Integer, Map<String, Object>>();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
                                                                                              IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
                                                                                               IOException {
        final String action = request.getParameter("action");
        if (action.equals("register")) {
            doRegister(request, response);
        } else if (action.equals("login")) {
            doLogin(request, response);
        } else if (action.equals("logout")) {
            doLogout(request, response);
        } else {
            doViewAccountDetails(request, response);
        }
    }

    /**
     * Perform the 'register' action
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void doRegister(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
                                                                                                IOException {
        initConnection();

        final Map<String, Object> userData = new HashMap<String, Object>();

        if (!request.getParameter("password").equals(request.getParameter("password2"))) {
            sendError(request, response, "Passwords don't match");
            return;
        }

        userData.put("username", request.getParameter("username"));
        userData.put("password", request.getParameter("password"));
        userData.put("firstName", request.getParameter("first_name"));
        userData.put("lastName", request.getParameter("last_name"));
        userData.put("phone", request.getParameter("phone"));
        userData.put("salary", Double.valueOf(request.getParameter("salary")));

        saveUserData(userData);

        // Load data back from database to include assigned user id
        loadUserData(userData);

        final String path = new StringBuffer().append("account?action=view&userId=")
                                              .append(userData.get(USER_ID))
                                              .toString();

        redirect(request, response, path, false);

        closeConnection();
    }

    /**
     * Perform the 'login' action
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doLogin(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
                                                                                             IOException {
        initConnection();

        final String username = request.getParameter("username");
        final String password = request.getParameter("password");

        final String sql = new StringBuffer().append("select userid from users where username = '")
                                             .append(username)
                                             .append("' and password = '")
                                             .append(password)
                                             .append("'")
                                             .toString();

        try {
            final Statement stmt = dbConnection.createStatement();
            final ResultSet rst = stmt.executeQuery(sql);

            if (rst.next()) {
                // Retrieve user id that corresponds to username and password
                final int userId = rst.getInt("userid");
                final HttpSession session = request.getSession();
                // Store the user id in order to log the user in
                session.setAttribute(USER_ID, Integer.valueOf(userId));

                redirect(request, response, request.getParameter("successUrl"), false);

            } else {
                sendError(request, response, "Username not found");
            }

            rst.close();
            stmt.close();

        } catch (final SQLException ex) {
            sendError(request, response, ex);
        }

        closeConnection();
    }

    /**
     * Perform the 'logout' action.
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void doLogout(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
                                                                                              IOException {
        initConnection();

        // Clear the user id from the session, if it exists
        final HttpSession session = request.getSession();
        if (session.getAttribute(USER_ID) != null) {
            session.removeAttribute(USER_ID);
        }

        redirect(request, response, "index.jsp", false);

        closeConnection();
    }

    /**
     * Perform the 'view' action
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doViewAccountDetails(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
                                                                                                          IOException {
        initConnection();

        final Integer userId = (Integer) getCurrentUserId(request);

        if (userId == null) {
            sendError(request, response, "No userId found");
        } else {
            final Map<String, Object> userData = new HashMap<String, Object>();
            userData.put(USER_ID, userId);
            loadUserData(userData);
            request.setAttribute("userDetails", userData);
            redirect(request, response, "/account.jsp", true);
        }
        closeConnection();
    }

    /**
     * Retrieve the current logged in user.
     *
     * @param request
     * @return
     */
    public Object getCurrentUserId(final HttpServletRequest request) {
        final HttpSession session = request.getSession();

        Object userId = request.getParameter(USER_ID);
        if (userId != null) {
            session.setAttribute(USER_ID, userId);
        }
        userId = session.getAttribute(USER_ID);

        return Integer.parseInt(userId.toString());
    }

    /**
     * Get the user's information.
     *
     * @param username
     * @return
     */
    public void loadUserData(final Map<String, Object> userData) {

        try {
            final StringBuffer buffer = new StringBuffer().append("SELECT * FROM users WHERE ");

            if (userData.containsKey(USER_ID)) {
                final Integer userId = Integer.parseInt(userData.get(USER_ID).toString());

                // If the user id has been specified, use that...
                buffer.append("userid = ").append(userId);

                // Check to see if the user details are in the cache
                if (USER_DETAILS_CACHE.containsKey(userId)) {
                    userData.putAll(USER_DETAILS_CACHE.get(userId));
                }

            } else {
                // ... otherwise use the username
                buffer.append("username = '").append(userData.get("username")).append('\'');
            }

            final String sql = buffer.toString();
            final Statement stmt = dbConnection.createStatement();
            final ResultSet rst = stmt.executeQuery(sql);

            Integer userId = null;

            while (rst.next()) {
                userId = Integer.valueOf(rst.getInt(1));
                userData.put(USER_ID, userId);
                userData.put("username", rst.getString(2));
                userData.put("password", rst.getString(3));
                userData.put("firstName", rst.getString(4));
                userData.put("lastName", rst.getString(5));
                userData.put("phone", rst.getString(6));
                final double salary = rst.getDouble(7);
                final double increase = (1d / 3) / 100;
                userData.put("salary", Double.valueOf(salary));
                userData.put("increase", Double.valueOf(salary + (salary * increase)));
            }

            rst.close();
            stmt.close();

            USER_DETAILS_CACHE.put(userId, userData);

        } catch (final SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }

    }

    /**
     * Store the user's information in the database.
     *
     * @param userData
     */
    public void saveUserData(final Map<String, Object> userData) {
        final StringBuffer buffer = new StringBuffer().append("INSERT INTO users(first_name, last_name, password, phone, salary, username) VALUES (");

        final SortedSet<String> keys = new TreeSet<String>(userData.keySet());
        final Iterator<String> keyItor = keys.iterator();
        boolean first = true;
        while (keyItor.hasNext()) {
            final String key = keyItor.next();
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            buffer.append('\'');
            buffer.append(userData.get(key));
            buffer.append('\'');
        }
        buffer.append(')');

        try {
            final String sql = buffer.toString();
            final Statement stmt = dbConnection.createStatement();
            stmt.execute(sql);
            stmt.close();
        } catch (final SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    /**
     * Redirect to another page
     *
     * @param request
     * @param response
     * @param path
     * @param forward
     * @throws ServletException
     * @throws IOException
     */
    public void redirect(final HttpServletRequest request,
                         final HttpServletResponse response,
                         final String path,
                         final boolean forward) throws ServletException, IOException {
        if (forward) {
            final RequestDispatcher dispatcher = request.getRequestDispatcher(path);
            dispatcher.forward(request, response);
        } else {
            response.sendRedirect(path);
        }
    }

    /**
     * Redirect the user to an error page.
     *
     * @param request
     * @param response
     * @param errorMessage
     * @throws ServletException
     * @throws IOException
     */
    public void sendError(final HttpServletRequest request,
                          final HttpServletResponse response,
                          final String errorMessage) throws ServletException, IOException {
        request.setAttribute("errorMessage", errorMessage);
        redirect(request, response, "/error.jsp", true);
    }

    /**
     * Redirect the user to an error page.
     *
     * @param request
     * @param response
     * @param ex
     * @throws ServletException
     * @throws IOException
     */
    public void sendError(final HttpServletRequest request, final HttpServletResponse response, final Exception cause) throws ServletException,
                                                                                                                      IOException {
        sendError(request, response, cause.getMessage());
    }

    /**
     * Initialize database connection
     */
    public void initConnection() {
    	 dbConnection = new AccountDatabase().getConnection();
    }

    /**
     * Close dabase connection
     */
    public void closeConnection() {
        try {
            dbConnection.close();
        } catch (final SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

}
