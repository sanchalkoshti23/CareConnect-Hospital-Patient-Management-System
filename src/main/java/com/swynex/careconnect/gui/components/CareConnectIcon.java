package com.swynex.careconnect.gui.components;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

public final class CareConnectIcon implements Icon {
    public enum Type {
        HOSPITAL,
        DASHBOARD,
        ANALYTICS,
        REGISTER,
        RECORDS,
        REPORT,
        SEARCH,
        ABOUT,
        EXIT,
        TOTAL,
        MALE,
        FEMALE,
        TODAY,
        REFRESH,
        EDIT,
        DELETE,
        PERSON
    }

    private final Type type;
    private final Color color;
    private final int size;

    public CareConnectIcon(Type type, Color color, int size) {
        this.type = type;
        this.color = color;
        this.size = size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        g.scale(size / 24.0, size / 24.0);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case HOSPITAL -> drawHospital(g);
            case DASHBOARD -> drawDashboard(g);
            case ANALYTICS -> drawAnalytics(g);
            case REGISTER -> drawRegister(g);
            case RECORDS -> drawRecords(g);
            case REPORT -> drawReport(g);
            case SEARCH -> drawSearch(g);
            case ABOUT -> drawAbout(g);
            case EXIT -> drawExit(g);
            case TOTAL -> drawTotal(g);
            case MALE, FEMALE, PERSON -> drawPerson(g);
            case TODAY -> drawCalendar(g);
            case REFRESH -> drawRefresh(g);
            case EDIT -> drawEdit(g);
            case DELETE -> drawDelete(g);
        }
        g.dispose();
    }

    private static void drawHospital(Graphics2D g) {
        g.draw(new RoundRectangle2D.Double(4, 3, 16, 18, 3, 3));
        g.draw(new Line2D.Double(9, 21, 9, 16));
        g.draw(new Line2D.Double(15, 21, 15, 16));
        g.draw(new Line2D.Double(9, 8, 15, 8));
        g.draw(new Line2D.Double(12, 5, 12, 11));
        g.draw(new Line2D.Double(9, 19, 15, 19));
    }

    private static void drawDashboard(Graphics2D g) {
        g.draw(new RoundRectangle2D.Double(3, 3, 8, 8, 2, 2));
        g.draw(new RoundRectangle2D.Double(13, 3, 8, 5, 2, 2));
        g.draw(new RoundRectangle2D.Double(13, 10, 8, 11, 2, 2));
        g.draw(new RoundRectangle2D.Double(3, 13, 8, 8, 2, 2));
    }

    private static void drawAnalytics(Graphics2D g) {
        g.draw(new Line2D.Double(4, 20, 4, 4));
        g.draw(new Line2D.Double(4, 20, 21, 20));
        g.draw(new Line2D.Double(7, 16, 11, 12));
        g.draw(new Line2D.Double(11, 12, 15, 14));
        g.draw(new Line2D.Double(15, 14, 20, 7));
        g.draw(new Ellipse2D.Double(5.8, 14.8, 2.4, 2.4));
        g.draw(new Ellipse2D.Double(9.8, 10.8, 2.4, 2.4));
        g.draw(new Ellipse2D.Double(13.8, 12.8, 2.4, 2.4));
        g.draw(new Ellipse2D.Double(18.8, 5.8, 2.4, 2.4));
    }

    private static void drawRegister(Graphics2D g) {
        drawPerson(g);
        g.draw(new Line2D.Double(18, 13, 18, 21));
        g.draw(new Line2D.Double(14, 17, 22, 17));
    }

    private static void drawRecords(Graphics2D g) {
        g.draw(new RoundRectangle2D.Double(4, 3, 16, 18, 2, 2));
        g.draw(new Line2D.Double(8, 8, 16, 8));
        g.draw(new Line2D.Double(8, 12, 16, 12));
        g.draw(new Line2D.Double(8, 16, 14, 16));
    }

    private static void drawReport(Graphics2D g) {
        g.draw(new RoundRectangle2D.Double(5, 2, 14, 20, 2, 2));
        g.draw(new Line2D.Double(8, 8, 16, 8));
        g.draw(new Line2D.Double(8, 12, 16, 12));
        g.draw(new Line2D.Double(8, 16, 14, 16));
    }

    private static void drawSearch(Graphics2D g) {
        g.draw(new Ellipse2D.Double(3, 3, 13, 13));
        g.draw(new Line2D.Double(13, 13, 21, 21));
    }

    private static void drawAbout(Graphics2D g) {
        g.draw(new Ellipse2D.Double(3, 3, 18, 18));
        g.draw(new Line2D.Double(12, 11, 12, 17));
        g.fill(new Ellipse2D.Double(11.15, 7, 1.7, 1.7));
    }

    private static void drawExit(Graphics2D g) {
        g.draw(new RoundRectangle2D.Double(3, 3, 11, 18, 2, 2));
        g.draw(new Line2D.Double(10, 12, 21, 12));
        g.draw(new Line2D.Double(17, 8, 21, 12));
        g.draw(new Line2D.Double(17, 16, 21, 12));
    }

    private static void drawTotal(Graphics2D g) {
        g.draw(new Ellipse2D.Double(3, 3, 18, 18));
        g.draw(new Ellipse2D.Double(7, 7, 10, 10));
        g.draw(new Ellipse2D.Double(10, 10, 4, 4));
    }

    private static void drawPerson(Graphics2D g) {
        g.draw(new Ellipse2D.Double(8, 3, 8, 8));
        g.draw(new Arc2D.Double(4, 12, 16, 11, 0, 180, Arc2D.OPEN));
    }

    private static void drawCalendar(Graphics2D g) {
        g.draw(new RoundRectangle2D.Double(3, 5, 18, 16, 2, 2));
        g.draw(new Line2D.Double(3, 10, 21, 10));
        g.draw(new Line2D.Double(8, 3, 8, 7));
        g.draw(new Line2D.Double(16, 3, 16, 7));
        g.draw(new Line2D.Double(8, 14, 8, 14));
        g.draw(new Line2D.Double(12, 14, 12, 14));
        g.draw(new Line2D.Double(16, 14, 16, 14));
    }

    private static void drawRefresh(Graphics2D g) {
        g.draw(new Arc2D.Double(4, 4, 16, 16, 45, 270, Arc2D.OPEN));
        g.draw(new Line2D.Double(15, 4, 20, 4));
        g.draw(new Line2D.Double(20, 4, 20, 9));
        g.draw(new Line2D.Double(9, 20, 4, 20));
        g.draw(new Line2D.Double(4, 20, 4, 15));
    }

    private static void drawEdit(Graphics2D g) {
        g.draw(new Line2D.Double(5, 17, 16, 6));
        g.draw(new Line2D.Double(14, 5, 18, 9));
        g.draw(new Line2D.Double(4, 20, 9, 19));
        g.draw(new Line2D.Double(16, 6, 18, 4));
    }

    private static void drawDelete(Graphics2D g) {
        g.draw(new Line2D.Double(4, 7, 20, 7));
        g.draw(new Line2D.Double(9, 4, 15, 4));
        g.draw(new RoundRectangle2D.Double(6, 8, 12, 13, 2, 2));
        g.draw(new Line2D.Double(10, 11, 10, 18));
        g.draw(new Line2D.Double(14, 11, 14, 18));
    }
}
