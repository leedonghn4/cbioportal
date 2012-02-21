package org.mskcc.portal.mut_diagram.servlet;

import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Draw Mutations Diagram Servlet.
 */
public final class OncoVizDrawServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(OncoVizDrawServlet.class);

    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/onco_viz.jsp");
        dispatcher.forward(request, response);
    }
}
