package com.swynex.careconnect.gui;

import com.swynex.careconnect.model.DashboardStats;
import com.swynex.careconnect.model.Patient;
import com.swynex.careconnect.service.PatientService;
import com.swynex.careconnect.gui.components.PatientFormPanel;
import com.swynex.careconnect.gui.components.PatientTableModel;
import com.swynex.careconnect.gui.components.SectionPanel;
import com.swynex.careconnect.gui.components.CareConnectIcon;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class CareConnectFrame extends JFrame {
    private static final System.Logger LOGGER =
            System.getLogger(CareConnectFrame.class.getName());
    private static final Color NAVY = new Color(20, 43, 68);
    private static final Color NAVY_LIGHT = new Color(31, 57, 82);
    private static final Color TEAL = new Color(17, 139, 132);
    private static final Color BACKGROUND = new Color(243, 247, 250);
    private static final Color MUTED = new Color(112, 128, 144);
    private static final Color BORDER = new Color(225, 233, 239);
    private static final Color PALE_TEAL = new Color(229, 245, 242);
    private static final Color RED = new Color(177, 69, 72);
    private static final Color SIDEBAR_BACKGROUND = new Color(12, 18, 31);
    private static final Color SIDEBAR_SELECTED = new Color(29, 47, 72);
    private static final Color DASHBOARD_BACKGROUND = new Color(13, 20, 34);
    private static final Color DASHBOARD_PANEL = new Color(19, 29, 46);
    private static final Color DASHBOARD_CARD = new Color(25, 37, 56);
    private static final Color DASHBOARD_ROW = new Color(29, 42, 62);
    private static final Color DASHBOARD_BORDER = new Color(42, 58, 82);
    private static final Color DASHBOARD_TEXT = new Color(238, 243, 250);
    private static final Color DASHBOARD_MUTED = new Color(151, 167, 188);
    private static final Color ELECTRIC_BLUE = new Color(72, 139, 255);

    private final PatientService patientService;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageContainer = new JPanel(pageLayout);
    private final Map<String, JButton> navigationButtons = new java.util.LinkedHashMap<>();
    private final JLabel pageTitle = new JLabel("Hospital Admin Dashboard");
    private final JLabel pageSubtitle = new JLabel("CareConnect  /  Patient operations");
    private final JLabel connectionStatus = new JLabel("Loading PostgreSQL data...");
    private final JTextField headerSearchField = new JTextField(22);
    private final DashboardView dashboardView = new DashboardView();
    private final PatientsView patientsView = new PatientsView();
    private final PatientFormPanel registrationForm = new PatientFormPanel(null);
    private String currentPage = "dashboard";
    private boolean writeInProgress;

    public CareConnectFrame(PatientService patientService) {
        super("CareConnect | Hospital Patient Management");
        this.patientService = patientService;
        configureFrame();
        buildInterface();
        updateNavigationStyle("dashboard");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                confirmExit();
            }
        });
        refreshDashboard();
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1360, 860);
        setLocationRelativeTo(null);
        setIconImage(createApplicationIcon());
    }

    private static BufferedImage createApplicationIcon() {
        BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = icon.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(NAVY);
        graphics.fillRoundRect(2, 2, 60, 60, 16, 16);
        graphics.setColor(TEAL);
        graphics.fillRoundRect(15, 15, 34, 34, 8, 8);
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(29, 20, 6, 24, 3, 3);
        graphics.fillRoundRect(20, 29, 24, 6, 3, 3);
        graphics.dispose();
        return icon;
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setOpaque(false);
        workspace.add(buildHeader(), BorderLayout.NORTH);

        pageContainer.setOpaque(true);
        pageContainer.setBackground(DASHBOARD_BACKGROUND);
        pageContainer.setBorder(BorderFactory.createEmptyBorder(22, 28, 20, 28));
        pageContainer.add(dashboardView, "dashboard");
        pageContainer.add(buildRegistrationPage(), "register");
        pageContainer.add(patientsView, "patients");
        pageContainer.add(buildAboutPage(), "about");
        workspace.add(pageContainer, BorderLayout.CENTER);
        workspace.add(buildFooter(), BorderLayout.SOUTH);
        root.add(workspace, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(248, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 13, 18, 13));

        JPanel top = new JPanel(new BorderLayout(11, 0));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 9, 31, 8));
        JLabel logo = new JLabel(new CareConnectIcon(
                CareConnectIcon.Type.HOSPITAL, Color.WHITE, 25));
        logo.setOpaque(true);
        logo.setBackground(TEAL);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(42, 42));
        JLabel brand = new JLabel("<html><b>CareConnect</b><br>"
                + "<span style='font-size:9px;color:#A9BECE'>PATIENT SERVICES</span></html>");
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 17f));
        top.add(logo, BorderLayout.WEST);
        top.add(brand, BorderLayout.CENTER);

        JPanel navigation = new JPanel(new GridLayout(0, 1, 0, 6));
        navigation.setOpaque(false);
        navigation.add(createNavigationButton("dashboard",
                CareConnectIcon.Type.DASHBOARD, "Dashboard"));
        navigation.add(createNavigationButton("analytics",
                CareConnectIcon.Type.ANALYTICS, "Analytics"));
        navigation.add(createNavigationButton("register",
                CareConnectIcon.Type.REGISTER, "Register Patient"));
        navigation.add(createNavigationButton("patient-directory",
                CareConnectIcon.Type.RECORDS, "Patients"));
        navigation.add(createNavigationButton("patients",
                CareConnectIcon.Type.RECORDS, "Patient Records"));
        navigation.add(createNavigationButton("reports",
                CareConnectIcon.Type.REPORT, "Reports"));
        navigation.add(createNavigationButton("search",
                CareConnectIcon.Type.SEARCH, "Search Patients"));
        navigation.add(createNavigationButton("about",
                CareConnectIcon.Type.ABOUT, "About / Help"));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JButton exitButton = createNavigationButton(
                "exit", CareConnectIcon.Type.EXIT, "Exit");
        exitButton.addActionListener(event -> confirmExit());
        bottom.add(exitButton, BorderLayout.NORTH);
        JLabel internship = new JLabel("<html>SWYNEX INTERNSHIP<br>"
                + "<span style='font-size:9px'>TASK 3 · DESKTOP APPLICATION</span></html>");
        internship.setForeground(new Color(137, 161, 181));
        internship.setFont(internship.getFont().deriveFont(Font.BOLD, 10f));
        internship.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(54, 75, 96)),
                BorderFactory.createEmptyBorder(15, 11, 0, 0)));
        bottom.add(internship, BorderLayout.SOUTH);

        sidebar.add(top, BorderLayout.NORTH);
        sidebar.add(navigation, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavigationButton(
            String key, CareConnectIcon.Type iconType, String title) {
        JButton button = new JButton(title,
                new CareConnectIcon(iconType, new Color(179, 199, 215), 19));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(new Color(206, 219, 230));
        button.setBackground(SIDEBAR_BACKGROUND);
        button.setIconTextGap(15);
        button.setBorder(BorderFactory.createEmptyBorder(13, 14, 13, 10));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 13f));
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        button.putClientProperty("JButton.hoverBackground", SIDEBAR_SELECTED);
        button.putClientProperty("JButton.selectedBackground", SIDEBAR_SELECTED);
        if (!"exit".equals(key)) {
            navigationButtons.put(key, button);
            button.addActionListener(event -> navigate(key));
        }
        return button;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(DASHBOARD_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, DASHBOARD_BORDER),
                BorderFactory.createEmptyBorder(16, 30, 16, 30)));

        JPanel headings = new JPanel(new BorderLayout(0, 4));
        headings.setOpaque(false);
        pageTitle.setFont(pageTitle.getFont().deriveFont(Font.BOLD, 23f));
        pageTitle.setForeground(DASHBOARD_TEXT);
        pageSubtitle.setForeground(DASHBOARD_MUTED);
        headings.add(pageTitle, BorderLayout.NORTH);
        headings.add(pageSubtitle, BorderLayout.SOUTH);

        JPanel profile = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        profile.setOpaque(false);
        JLabel avatar = new JLabel(new CareConnectIcon(
                CareConnectIcon.Type.PERSON, TEAL, 21), SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(29, 54, 79));
        avatar.setForeground(new Color(93, 220, 207));
        avatar.setPreferredSize(new Dimension(40, 40));
        JLabel date = new JLabel(new CareConnectIcon(
                CareConnectIcon.Type.TODAY, DASHBOARD_MUTED, 15));
        JPanel dateBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 0));
        dateBadge.setOpaque(false);
        JLabel dateText = new JLabel(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEE, dd MMM")));
        dateText.setForeground(DASHBOARD_MUTED);
        dateBadge.add(date);
        dateBadge.add(dateText);
        JLabel administrator = new JLabel("<html><b>Administrator</b><br>"
                + "<span style='color:#97A7BC'>CareConnect</span></html>");
        administrator.setForeground(DASHBOARD_TEXT);
        profile.add(avatar);
        profile.add(administrator);
        profile.add(dateBadge);

        JPanel searchBox = new JPanel(new BorderLayout(9, 0));
        searchBox.setBackground(DASHBOARD_CARD);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DASHBOARD_BORDER),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        searchBox.setPreferredSize(new Dimension(330, 42));
        JLabel searchIcon = new JLabel(new CareConnectIcon(
                CareConnectIcon.Type.SEARCH, DASHBOARD_MUTED, 17));
        headerSearchField.putClientProperty(
                "JTextField.placeholderText", "Search patients by ID or name");
        headerSearchField.setBorder(BorderFactory.createEmptyBorder());
        headerSearchField.setOpaque(false);
        headerSearchField.setForeground(DASHBOARD_TEXT);
        headerSearchField.setCaretColor(DASHBOARD_TEXT);
        headerSearchField.addActionListener(
                event -> openPatientSearch(headerSearchField.getText()));
        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(headerSearchField, BorderLayout.CENTER);

        header.add(searchBox, BorderLayout.CENTER);
        header.add(headings, BorderLayout.WEST);
        header.add(profile, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(DASHBOARD_PANEL);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, DASHBOARD_BORDER),
                BorderFactory.createEmptyBorder(10, 30, 10, 30)));
        connectionStatus.setForeground(DASHBOARD_MUTED);
        connectionStatus.setFont(connectionStatus.getFont().deriveFont(11f));
        JLabel version = new JLabel("CARECONNECT  ·  1.0");
        version.setForeground(DASHBOARD_MUTED);
        version.setFont(version.getFont().deriveFont(Font.BOLD, 10f));
        footer.add(connectionStatus, BorderLayout.WEST);
        footer.add(version, BorderLayout.EAST);
        return footer;
    }

    private JPanel buildRegistrationPage() {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setOpaque(false);
        JPanel intro = heading("New patient registration",
                "Enter the patient's details. Registration time is assigned automatically.");
        page.add(intro, BorderLayout.NORTH);

        SectionPanel formCard = new SectionPanel("Patient details");
        formCard.add(registrationForm, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 4));
        actions.setOpaque(false);
        JButton cancelButton = secondaryButton("Cancel");
        cancelButton.addActionListener(event -> {
            registrationForm.clear();
            navigate("dashboard");
        });
        JButton resetButton = secondaryButton("Reset");
        resetButton.setIcon(new CareConnectIcon(
                CareConnectIcon.Type.REFRESH, MUTED, 16));
        resetButton.addActionListener(event -> registrationForm.reset());
        JButton saveButton = primaryButton("Register patient");
        saveButton.setIcon(new CareConnectIcon(
                CareConnectIcon.Type.REGISTER, Color.WHITE, 17));
        saveButton.addActionListener(event -> registerPatient());
        actions.add(cancelButton);
        actions.add(resetButton);
        actions.add(saveButton);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(formCard, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildAboutPage() {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setOpaque(false);
        page.add(heading("About CareConnect",
                "A patient-first desktop project built for the SWYNEX Java internship."),
                BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(0, 1, 0, 14));
        content.setOpaque(false);
        content.add(infoCard("Purpose",
                "CareConnect provides a focused workspace for registering, finding, "
                        + "updating and managing hospital patient records."));
        content.add(infoCard("Technology",
                "Java 25 · Swing · FlatLaf · JDBC · PostgreSQL · Maven"));
        content.add(infoCard("Architecture",
                "The desktop screens use PatientService for validation and business "
                        + "rules. PatientDao owns SQL access, and DBConnection reads "
                        + "connection settings from local environment variables."));
        content.add(infoCard("Privacy and data",
                "No sample patient records are created. Database credentials are not "
                        + "stored in the source code."));
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel infoCard(String title, String detail) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                cardBorder(), BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(NAVY);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        JLabel detailLabel = new JLabel("<html><div style='width:680px'>"
                + detail + "</div></html>");
        detailLabel.setForeground(MUTED);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(detailLabel, BorderLayout.CENTER);
        return card;
    }

    private void navigate(String page) {
        if ("reports".equals(page)) {
            currentPage = "dashboard";
            pageContainer.setBackground(DASHBOARD_BACKGROUND);
            pageLayout.show(pageContainer, "dashboard");
            setPageHeading("Reports", "A live summary of patient activity");
            updateNavigationStyle(page);
            dashboardView.generateReport();
            return;
        }
        String pageCard = switch (page) {
            case "search" -> "patients";
            case "patient-directory" -> "patients";
            case "analytics" -> "dashboard";
            default -> page;
        };
        currentPage = pageCard;
        pageContainer.setBackground("dashboard".equals(pageCard)
                ? DASHBOARD_BACKGROUND : BACKGROUND);
        pageLayout.show(pageContainer, pageCard);
        switch (page) {
            case "dashboard" -> {
                setPageHeading("Hospital Admin Dashboard",
                        "CareConnect  /  Patient operations");
                refreshDashboard();
            }
            case "analytics" -> {
                setPageHeading("Analytics", "Live patient insights from PostgreSQL");
                refreshDashboard();
                javax.swing.SwingUtilities.invokeLater(dashboardView::scrollToAnalytics);
            }
            case "register" ->
                    setPageHeading("Register Patient", "Add a new patient to the records");
            case "patients" -> {
                setPageHeading("Patient Records", "Search, review and manage registered patients");
                refreshPatients();
            }
            case "patient-directory" -> {
                setPageHeading("Patients", "Browse and manage patients stored in PostgreSQL");
                refreshPatients();
            }
            case "search" -> {
                setPageHeading("Search Patients",
                        "Find a patient by their record ID or name.");
                patientsView.focusSearch();
                refreshPatients();
            }
            case "about" ->
                    setPageHeading("About / Project Information",
                            "Project overview, technology and data handling");
            default -> throw new IllegalArgumentException("Unknown page: " + page);
        }
        updateNavigationStyle(page);
    }

    private void setPageHeading(String title, String subtitle) {
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
    }

    private void updateNavigationStyle(String selectedPage) {
        navigationButtons.forEach((key, button) -> {
            boolean selected = key.equals(selectedPage);
            button.setBackground(selected ? SIDEBAR_SELECTED : SIDEBAR_BACKGROUND);
            button.setForeground(selected ? Color.WHITE : new Color(206, 219, 230));
            button.setIcon(new CareConnectIcon(
                    navigationIcon(key),
                    selected ? new Color(91, 207, 190) : new Color(179, 199, 215), 19));
            button.setFont(button.getFont().deriveFont(
                    selected ? Font.BOLD : Font.PLAIN, 13f));
        });
    }

    private static CareConnectIcon.Type navigationIcon(String key) {
        return switch (key) {
            case "dashboard" -> CareConnectIcon.Type.DASHBOARD;
            case "analytics" -> CareConnectIcon.Type.ANALYTICS;
            case "reports" -> CareConnectIcon.Type.REPORT;
            case "register" -> CareConnectIcon.Type.REGISTER;
            case "patients" -> CareConnectIcon.Type.RECORDS;
            case "patient-directory" -> CareConnectIcon.Type.PERSON;
            case "search" -> CareConnectIcon.Type.SEARCH;
            case "about" -> CareConnectIcon.Type.ABOUT;
            default -> throw new IllegalArgumentException("Unknown navigation key: " + key);
        };
    }

    private void refreshDashboard() {
        dashboardView.showLoading();
        runDatabaseTask(
                "Loading dashboard",
                patientService::getDashboardStats,
                dashboardView::setStats);
    }

    private void refreshPatients() {
        String query = patientsView.getSearchText();
        patientsView.showLoading();
        runDatabaseTask(
                "Loading patient records",
                () -> query.isBlank()
                        ? patientService.getAllPatients()
                        : patientService.searchPatients(query),
                patientsView::setPatients);
    }

    private void searchPatients(String query) {
        patientsView.setSearchText(query);
        refreshPatients();
    }

    private void openPatientSearch(String query) {
        headerSearchField.setText(query);
        patientsView.setSearchText(query);
        navigate("search");
    }

    private void registerPatient() {
        Patient patient;
        try {
            patient = registrationForm.toPatient();
        } catch (IllegalArgumentException exception) {
            showValidationError(exception.getMessage());
            return;
        }

        runDatabaseTask("Registering patient",
                () -> patientService.createPatient(patient),
                savedPatient -> {
                    registrationForm.clear();
                    showMessage("Patient registered",
                            savedPatient.fullName() + " has been added to patient records.");
                    refreshDataAfterChange();
                });
    }

    private void editPatient(Patient patient) {
        JDialog dialog = new JDialog(this, "Update patient record", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setMinimumSize(new Dimension(700, 660));

        PatientFormPanel form = new PatientFormPanel(patient);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 10));
        actions.setBackground(Color.WHITE);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 18, 8, 18));
        JButton cancelButton = secondaryButton("Cancel");
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton resetButton = secondaryButton("Reset");
        resetButton.setIcon(new CareConnectIcon(
                CareConnectIcon.Type.REFRESH, MUTED, 16));
        resetButton.addActionListener(event -> form.reset());
        JButton saveButton = primaryButton("Save changes");
        saveButton.setIcon(new CareConnectIcon(
                CareConnectIcon.Type.EDIT, Color.WHITE, 16));
        saveButton.addActionListener(event -> {
            Patient updatedPatient;
            try {
                updatedPatient = form.toPatient();
            } catch (IllegalArgumentException exception) {
                showValidationError(exception.getMessage());
                return;
            }
            runDatabaseTask("Updating patient",
                    () -> patientService.updatePatient(updatedPatient),
                    updated -> {
                        if (updated) {
                            dialog.dispose();
                            showMessage("Changes saved",
                                    updatedPatient.fullName() + "'s record was updated.");
                            refreshDataAfterChange();
                        } else {
                            showMessage("Record not found",
                                    "This patient may have been removed by another user.");
                        }
                    });
        });
        actions.add(cancelButton);
        actions.add(resetButton);
        actions.add(saveButton);

        dialog.getContentPane().setBackground(BACKGROUND);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(
                Math.min(780, getWidth() - 80), Math.min(720, getHeight() - 80)));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deletePatient(Patient patient) {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete the record for " + patient.fullName() + " (ID "
                        + patient.patientId() + ")?\nThis action cannot be undone.",
                "Confirm patient deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        runDatabaseTask("Deleting patient",
                () -> patientService.deletePatient(patient.patientId()),
                deleted -> {
                    if (deleted) {
                        showMessage("Patient deleted",
                                patient.fullName() + "'s record was removed.");
                        refreshDataAfterChange();
                    } else {
                        showMessage("Record not found",
                                "This patient may have been removed by another user.");
                    }
                });
    }

    private void refreshDataAfterChange() {
        refreshDashboard();
        if ("patients".equals(currentPage)) {
            refreshPatients();
        }
    }

    private <T> void runDatabaseTask(
            String activity, SqlOperation<T> operation, Consumer<T> onSuccess) {
        boolean writeOperation = !activity.startsWith("Loading");
        if (writeOperation && writeInProgress) {
            connectionStatus.setText("Please wait for the current save operation to finish.");
            return;
        }
        if (writeOperation) {
            writeInProgress = true;
        }
        connectionStatus.setText(activity + "...");
        connectionStatus.setForeground(DASHBOARD_MUTED);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return operation.run();
            }

            @Override
            protected void done() {
                if (writeOperation) {
                    writeInProgress = false;
                }
                try {
                    T result = get();
                    connectionStatus.setText("Connected to PostgreSQL  ·  "
                            + OffsetDateTime.now().format(
                                    DateTimeFormatter.ofPattern("HH:mm")));
                    connectionStatus.setForeground(TEAL);
                    onSuccess.accept(result);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    reportDatabaseFailure(activity, exception);
                } catch (ExecutionException exception) {
                    reportDatabaseFailure(activity, exception.getCause());
                }
            }
        }.execute();
    }

    private void reportDatabaseFailure(String activity, Throwable failure) {
        LOGGER.log(System.Logger.Level.ERROR,
                "Database operation failed while " + activity.toLowerCase(), failure);
        if ("Loading dashboard".equals(activity)) {
            dashboardView.showError();
        } else if ("Loading patient records".equals(activity)) {
            patientsView.showError();
        }
        connectionStatus.setText("Database unavailable  ·  Check connection settings");
        connectionStatus.setForeground(new Color(255, 136, 146));
        if (activity.startsWith("Loading")) {
            return;
        }
        String message = "CareConnect could not complete: " + activity.toLowerCase() + ".\n\n"
                + "Check that PostgreSQL is running, the careconnect_db database and "
                + "patients table exist, and CARECONNECT_DB_USERNAME and "
                + "CARECONNECT_DB_PASSWORD are configured. Technical details were "
                + "written to the application log.";
        JOptionPane.showMessageDialog(
                this, message, "Database connection or operation error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Check patient details", JOptionPane.WARNING_MESSAGE);
    }

    private void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmExit() {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to close CareConnect?",
                "Exit CareConnect",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    private static JPanel heading(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(NAVY);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 19f));
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(MUTED);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(subtitleLabel, BorderLayout.SOUTH);
        return panel;
    }

    private static javax.swing.border.Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1,
                                new Color(232, 238, 242)),
                        BorderFactory.createLineBorder(BORDER)),
                BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    private static javax.swing.border.Border dashboardCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 2, 1, new Color(8, 13, 24)),
                        BorderFactory.createLineBorder(DASHBOARD_BORDER)),
                BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(TEAL);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JButton.hoverBackground", new Color(13, 119, 113));
        button.setMargin(new Insets(10, 16, 10, 16));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12.5f));
        return button;
    }

    private static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JButton.hoverBackground", new Color(239, 246, 248));
        button.setMargin(new Insets(10, 15, 10, 15));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        return button;
    }

    private static JTable createPatientTable(PatientTableModel model) {
        return createPatientTable(model, false);
    }

    private static JTable createPatientTable(PatientTableModel model, boolean darkTheme) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(darkTheme ? 46 : 42);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(darkTheme ? DASHBOARD_BORDER : new Color(237, 241, 244));
        table.setBackground(darkTheme ? DASHBOARD_CARD : Color.WHITE);
        table.setForeground(darkTheme ? DASHBOARD_TEXT : new Color(48, 65, 79));
        table.setSelectionBackground(
                darkTheme ? new Color(43, 82, 133) : new Color(220, 241, 238));
        table.setSelectionForeground(darkTheme ? Color.WHITE : NAVY);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, darkTheme ? 48 : 44));
        table.getTableHeader().setBackground(
                darkTheme ? DASHBOARD_PANEL : new Color(245, 248, 250));
        table.getTableHeader().setForeground(
                darkTheme ? DASHBOARD_MUTED : new Color(65, 83, 98));
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setFont(table.getTableHeader().getFont()
                .deriveFont(Font.BOLD, 11.5f));
        table.setFont(table.getFont().deriveFont(12f));
        int[] widths = {64, 170, 55, 82, 120, 190, 92, 155};
        for (int column = 0; column < widths.length; column++) {
            table.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
            table.getColumnModel().getColumn(column)
                    .setCellRenderer(new PatientCellRenderer(column, darkTheme));
        }
        table.setIntercellSpacing(new Dimension(0, 1));
        return table;
    }

    private static JScrollPane createTableScrollPane(JTable table) {
        return createTableScrollPane(table, Color.WHITE);
    }

    private static JScrollPane createTableScrollPane(JTable table, Color viewportColor) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(viewportColor);
        scrollPane.getViewport().setBackground(viewportColor);
        scrollPane.getVerticalScrollBar().setBackground(viewportColor);
        scrollPane.getHorizontalScrollBar().setBackground(viewportColor);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private static final class PatientCellRenderer extends DefaultTableCellRenderer {
        private final int column;
        private final boolean darkTheme;

        private PatientCellRenderer(int column, boolean darkTheme) {
            this.column = column;
            this.darkTheme = darkTheme;
            setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 9));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected, boolean focused,
                int row, int columnIndex) {
            Object displayValue = column == 7 && value instanceof OffsetDateTime dateTime
                    ? PatientTableModel.formatDate(dateTime)
                    : value;
            Component cell = super.getTableCellRendererComponent(
                    table, displayValue, selected, focused, row, columnIndex);
            if (!selected) {
                cell.setBackground(darkTheme
                        ? row % 2 == 0 ? DASHBOARD_CARD : DASHBOARD_ROW
                        : row % 2 == 0 ? Color.WHITE : new Color(249, 251, 252));
                cell.setForeground(darkTheme
                        ? column == 0 ? ELECTRIC_BLUE : DASHBOARD_TEXT
                        : column == 0 ? TEAL : new Color(48, 65, 79));
            }
            boolean centered = column == 0 || column == 2 || column == 3
                    || column == 6 || column == 7;
            setHorizontalAlignment(centered
                    ? SwingConstants.CENTER : SwingConstants.LEFT);
            if (column == 0) {
                setFont(table.getFont().deriveFont(Font.BOLD));
            } else {
                setFont(table.getFont());
            }
            return cell;
        }
    }

    @FunctionalInterface
    private interface SqlOperation<T> {
        T run() throws SQLException;
    }

    private final class DashboardView extends JPanel {
        private final JLabel[] values = new JLabel[4];
        private final JLabel databaseNotice = new JLabel(" ");
        private final JLabel recentEmptyState = new JLabel("", SwingConstants.CENTER);
        private final JPanel databaseNoticePanel = new JPanel(new BorderLayout(9, 0));
        private final PatientTableModel tableModel = new PatientTableModel();
        private final RegistrationTrendPanel registrationTrend =
                new RegistrationTrendPanel();
        private final GenderDistributionPanel genderDistribution =
                new GenderDistributionPanel();
        private final CardLayout recentLayout = new CardLayout();
        private final JPanel recentContent = new JPanel(recentLayout);
        private final JTextField quickSearch = new JTextField(18);
        private JPanel analyticsSection;
        private boolean reportAfterRefresh;

        private DashboardView() {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(DASHBOARD_BACKGROUND);

            JPanel content = new JPanel();
            content.setOpaque(true);
            content.setBackground(DASHBOARD_BACKGROUND);
            content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

            JPanel welcome = createWelcomeBanner();
            welcome.setAlignmentX(LEFT_ALIGNMENT);
            content.add(welcome);
            content.add(javax.swing.Box.createVerticalStrut(18));

            JPanel metrics = new JPanel(new GridLayout(1, 4, 14, 0));
            metrics.setOpaque(false);
            metrics.setAlignmentX(LEFT_ALIGNMENT);
            String[] labels = {
                    "TOTAL PATIENTS", "NEW REGISTRATIONS",
                    "MALE PATIENTS", "FEMALE PATIENTS"
            };
            String[] descriptions = {
                    "All registered records", "Added today",
                    "Across all records", "Across all records"
            };
            CareConnectIcon.Type[] icons = {
                    CareConnectIcon.Type.TOTAL,
                    CareConnectIcon.Type.TODAY,
                    CareConnectIcon.Type.MALE,
                    CareConnectIcon.Type.FEMALE
            };
            Color[] accents = {
                    new Color(32, 132, 166), TEAL,
                    new Color(76, 126, 202), new Color(190, 103, 158)
            };
            for (int index = 0; index < labels.length; index++) {
                StatCard card = new StatCard(
                        labels[index], descriptions[index], icons[index], accents[index]);
                values[index] = card.valueLabel;
                metrics.add(card);
            }
            content.add(metrics);
            content.add(javax.swing.Box.createVerticalStrut(17));

            JPanel noticePanel = databaseNoticePanel;
            noticePanel.setOpaque(true);
            noticePanel.setBackground(new Color(51, 42, 34));
            noticePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(110, 82, 49)),
                    BorderFactory.createEmptyBorder(10, 13, 10, 13)));
            JLabel noticeIcon = new JLabel(new CareConnectIcon(
                    CareConnectIcon.Type.ABOUT, new Color(255, 181, 82), 17));
            databaseNotice.setForeground(new Color(255, 211, 157));
            databaseNotice.setFont(databaseNotice.getFont().deriveFont(12f));
            noticePanel.add(noticeIcon, BorderLayout.WEST);
            noticePanel.add(databaseNotice, BorderLayout.CENTER);
            noticePanel.setVisible(false);
            content.add(noticePanel);
            content.add(javax.swing.Box.createVerticalStrut(15));

            analyticsSection = new JPanel(new BorderLayout(0, 12));
            analyticsSection.setOpaque(false);
            analyticsSection.setAlignmentX(LEFT_ALIGNMENT);
            analyticsSection.add(dashboardHeading("Patient analytics",
                    "Live registration activity and patient demographics."),
                    BorderLayout.NORTH);
            JPanel analyticsCards = new JPanel(new GridLayout(1, 2, 14, 0));
            analyticsCards.setOpaque(false);
            analyticsCards.add(createAnalyticsCard(
                    "Patient Registration Trends",
                    "Daily counts among the latest six PostgreSQL records",
                    registrationTrend));
            analyticsCards.add(createAnalyticsCard(
                    "Gender distribution",
                    "Patient counts and share of the total",
                    genderDistribution));
            analyticsSection.add(analyticsCards, BorderLayout.CENTER);
            content.add(analyticsSection);
            content.add(javax.swing.Box.createVerticalStrut(18));

            JPanel quickActionSection = new JPanel(new BorderLayout(0, 11));
            quickActionSection.setOpaque(false);
            quickActionSection.setAlignmentX(LEFT_ALIGNMENT);
            quickActionSection.add(dashboardHeading("Quick actions",
                    "Common patient-management tasks, one click away."),
                    BorderLayout.NORTH);
            JPanel quickActions = new JPanel(new GridLayout(1, 4, 12, 0));
            quickActions.setOpaque(false);
            quickActions.add(createActionCard(
                    "Register patient", "Add a new patient",
                    CareConnectIcon.Type.REGISTER, TEAL,
                    () -> navigate("register")));
            quickActions.add(createActionCard(
                    "View all patients", "Browse patient records",
                    CareConnectIcon.Type.RECORDS, new Color(55, 119, 190),
                    () -> navigate("patients")));
            quickActions.add(createActionCard(
                    "Search patient", "Find by ID or name",
                    CareConnectIcon.Type.SEARCH, new Color(127, 100, 177),
                    () -> openPatientSearch("")));
            quickActions.add(createActionCard(
                    "Generate report", "Refresh live summary",
                    CareConnectIcon.Type.REPORT, new Color(199, 137, 54),
                    this::generateReport));
            quickActionSection.add(quickActions, BorderLayout.CENTER);
            content.add(quickActionSection);
            content.add(javax.swing.Box.createVerticalStrut(18));

            JPanel recentSection = new JPanel(new BorderLayout(0, 12));
            recentSection.setOpaque(false);
            recentSection.setAlignmentX(LEFT_ALIGNMENT);
            JPanel recentHeader = new JPanel(new BorderLayout(12, 0));
            recentHeader.setOpaque(false);
            recentHeader.add(dashboardHeading("Recent patients",
                    "The latest patient records from PostgreSQL"), BorderLayout.WEST);

            JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
            controls.setOpaque(false);
            quickSearch.putClientProperty("JTextField.placeholderText", "Search ID or name");
            quickSearch.setPreferredSize(new Dimension(190, 36));
            quickSearch.setBackground(DASHBOARD_CARD);
            quickSearch.setForeground(DASHBOARD_TEXT);
            quickSearch.setCaretColor(DASHBOARD_TEXT);
            quickSearch.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DASHBOARD_BORDER),
                    BorderFactory.createEmptyBorder(7, 9, 7, 9)));
            quickSearch.addActionListener(event -> openPatientSearch(quickSearch.getText()));
            JButton searchButton = secondaryButton("Search");
            searchButton.setIcon(new CareConnectIcon(
                    CareConnectIcon.Type.SEARCH, ELECTRIC_BLUE, 15));
            styleDashboardButton(searchButton);
            searchButton.addActionListener(event -> openPatientSearch(quickSearch.getText()));
            JButton refreshButton = secondaryButton("Refresh");
            refreshButton.setIcon(new CareConnectIcon(
                    CareConnectIcon.Type.REFRESH, DASHBOARD_MUTED, 15));
            styleDashboardButton(refreshButton);
            refreshButton.addActionListener(event -> refreshDashboard());
            JButton viewAll = secondaryButton("View all");
            viewAll.setIcon(new CareConnectIcon(
                    CareConnectIcon.Type.RECORDS, DASHBOARD_MUTED, 15));
            styleDashboardButton(viewAll);
            viewAll.addActionListener(event -> navigate("patients"));
            controls.add(quickSearch);
            controls.add(searchButton);
            controls.add(refreshButton);
            controls.add(viewAll);
            recentHeader.add(controls, BorderLayout.EAST);
            recentSection.add(recentHeader, BorderLayout.NORTH);

            JTable table = createPatientTable(tableModel, true);
            table.removeColumn(table.getColumnModel().getColumn(5));
            table.getColumnModel().getColumn(1).setHeaderValue("Full name");
            table.setPreferredScrollableViewportSize(new Dimension(900, 235));
            JScrollPane scrollPane = createTableScrollPane(table, DASHBOARD_CARD);
            recentEmptyState.setForeground(DASHBOARD_MUTED);
            recentEmptyState.setFont(recentEmptyState.getFont().deriveFont(12f));
            recentContent.setOpaque(true);
            recentContent.setBackground(DASHBOARD_CARD);
            recentContent.add(scrollPane, "table");
            recentContent.add(recentEmptyState, "empty");
            JPanel tableCard = new JPanel(new BorderLayout());
            tableCard.setBackground(DASHBOARD_CARD);
            tableCard.setBorder(dashboardCardBorder());
            tableCard.add(recentContent, BorderLayout.CENTER);
            recentSection.add(tableCard, BorderLayout.CENTER);
            content.add(recentSection);

            JScrollPane pageScroll = new JScrollPane(content);
            pageScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            pageScroll.setOpaque(true);
            pageScroll.setBackground(DASHBOARD_BACKGROUND);
            pageScroll.getViewport().setOpaque(true);
            pageScroll.getViewport().setBackground(DASHBOARD_BACKGROUND);
            pageScroll.getVerticalScrollBar().setBackground(DASHBOARD_PANEL);
            pageScroll.getVerticalScrollBar().setUnitIncrement(18);
            add(pageScroll, BorderLayout.CENTER);

            tableModel.addTableModelListener(event -> updateRecentState());
        }

        private JPanel createAnalyticsCard(
                String title, String subtitle, Component chart) {
            JPanel card = new JPanel(new BorderLayout(0, 10));
            card.setBackground(DASHBOARD_PANEL);
            card.setBorder(BorderFactory.createCompoundBorder(
                    dashboardCardBorder(),
                    BorderFactory.createEmptyBorder(14, 17, 12, 17)));
            JPanel cardHeading = new JPanel(new BorderLayout(0, 4));
            cardHeading.setOpaque(false);
            JLabel titleLabel = new JLabel(title);
            titleLabel.setForeground(DASHBOARD_TEXT);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setForeground(DASHBOARD_MUTED);
            subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(11f));
            cardHeading.add(titleLabel, BorderLayout.NORTH);
            cardHeading.add(subtitleLabel, BorderLayout.CENTER);
            card.add(cardHeading, BorderLayout.NORTH);
            card.add(chart, BorderLayout.CENTER);
            return card;
        }

        private JButton createActionCard(
                String title, String description, CareConnectIcon.Type iconType,
                Color accent, Runnable action) {
            JButton button = new JButton(
                    "<html><b>" + title + "</b><br><span style='font-size:10px;color:#97A7BC'>"
                            + description + "</span></html>",
                    new CareConnectIcon(iconType, accent, 21));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setIconTextGap(12);
            button.setForeground(DASHBOARD_TEXT);
            button.setBackground(DASHBOARD_CARD);
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.putClientProperty("JButton.buttonType", "roundRect");
            button.putClientProperty("JButton.hoverBackground", new Color(37, 54, 78));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DASHBOARD_BORDER),
                    BorderFactory.createEmptyBorder(9, 12, 9, 12)));
            button.setPreferredSize(new Dimension(190, 68));
            button.addActionListener(event -> action.run());
            return button;
        }

        private JPanel dashboardHeading(String title, String subtitle) {
            JPanel panel = new JPanel(new BorderLayout(0, 5));
            panel.setOpaque(false);
            JLabel titleLabel = new JLabel(title);
            titleLabel.setForeground(DASHBOARD_TEXT);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setForeground(DASHBOARD_MUTED);
            subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(11.5f));
            panel.add(titleLabel, BorderLayout.NORTH);
            panel.add(subtitleLabel, BorderLayout.SOUTH);
            return panel;
        }

        private void styleDashboardButton(JButton button) {
            button.setForeground(DASHBOARD_TEXT);
            button.setBackground(DASHBOARD_CARD);
            button.putClientProperty("JButton.hoverBackground", new Color(37, 54, 78));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DASHBOARD_BORDER),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        }

        private void generateReport() {
            reportAfterRefresh = true;
            refreshDashboard();
        }

        private void showReport(DashboardStats stats) {
            long otherPatients = Math.max(
                    0, stats.totalPatients() - stats.malePatients() - stats.femalePatients());
            String report = "Live patient summary\n\n"
                    + "Total patients: " + stats.totalPatients() + "\n"
                    + "New registrations today: " + stats.registeredToday() + "\n"
                    + "Male patients: " + stats.malePatients() + "\n"
                    + "Female patients: " + stats.femalePatients() + "\n"
                    + "Other patients: " + otherPatients + "\n\n"
                    + "Generated from the latest PostgreSQL dashboard data.";
            showMessage("Patient summary report", report);
        }

        private void scrollToAnalytics() {
            analyticsSection.scrollRectToVisible(new java.awt.Rectangle(
                    0, 0, analyticsSection.getWidth(), analyticsSection.getHeight()));
        }

        private JPanel createWelcomeBanner() {
            JPanel banner = new JPanel(new BorderLayout(18, 0)) {
                @Override
                protected void paintComponent(java.awt.Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setPaint(new java.awt.GradientPaint(
                            0, 0, new Color(22, 42, 69),
                            getWidth(), getHeight(), new Color(22, 82, 116)));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                    g.setColor(new Color(72, 139, 255, 30));
                    int markX = Math.max(0, getWidth() - 260);
                    g.fillRoundRect(markX + 13, 9, 10, 40, 8, 8);
                    g.fillRoundRect(markX, 24, 36, 10, 8, 8);
                    g.dispose();
                    super.paintComponent(graphics);
                }
            };
            banner.setOpaque(false);
            banner.setBorder(BorderFactory.createEmptyBorder(21, 26, 21, 26));

            JLabel welcomeTitle = new JLabel(greeting() + ", Administrator");
            welcomeTitle.setForeground(Color.WHITE);
            welcomeTitle.setFont(welcomeTitle.getFont().deriveFont(Font.BOLD, 24f));
            JLabel welcomeCopy = new JLabel(
                    "Your patient care workspace is ready. Here is your latest overview.");
            welcomeCopy.setForeground(new Color(217, 234, 239));
            welcomeCopy.setFont(welcomeCopy.getFont().deriveFont(12f));
            JLabel eyebrow = new JLabel("PATIENT CARE  /  OPERATIONS");
            eyebrow.setForeground(new Color(177, 225, 219));
            eyebrow.setFont(eyebrow.getFont().deriveFont(Font.BOLD, 9.5f));
            JPanel welcomeHeading = new JPanel();
            welcomeHeading.setOpaque(false);
            welcomeHeading.setLayout(new javax.swing.BoxLayout(
                    welcomeHeading, javax.swing.BoxLayout.Y_AXIS));
            welcomeHeading.add(eyebrow);
            welcomeHeading.add(javax.swing.Box.createVerticalStrut(5));
            welcomeHeading.add(welcomeTitle);
            welcomeHeading.add(javax.swing.Box.createVerticalStrut(5));
            welcomeHeading.add(welcomeCopy);
            JPanel welcomeText = new JPanel(new BorderLayout(14, 0));
            welcomeText.setOpaque(false);
            JLabel hospitalIcon = new JLabel(new CareConnectIcon(
                    CareConnectIcon.Type.HOSPITAL, new Color(215, 246, 239), 28));
            hospitalIcon.setHorizontalAlignment(SwingConstants.CENTER);
            welcomeText.add(hospitalIcon, BorderLayout.WEST);
            welcomeText.add(welcomeHeading, BorderLayout.CENTER);

            JButton registerButton = primaryButton("Register new patient");
            registerButton.setIcon(new CareConnectIcon(
                    CareConnectIcon.Type.REGISTER, Color.WHITE, 17));
            registerButton.addActionListener(event -> navigate("register"));
            banner.add(welcomeText, BorderLayout.CENTER);
            banner.add(registerButton, BorderLayout.EAST);
            return banner;
        }

        private String greeting() {
            int hour = LocalTime.now().getHour();
            if (hour < 12) {
                return "Good morning";
            }
            return hour < 18 ? "Good afternoon" : "Good evening";
        }

        private void showLoading() {
            for (JLabel value : values) {
                value.setText("—");
            }
            registrationTrend.clear("Loading registration history...");
            genderDistribution.clear("Loading live database statistics...");
            databaseNotice.setText(
                    "Loading dashboard data from PostgreSQL. Statistics appear when the query succeeds.");
            databaseNoticePanel.setBackground(new Color(51, 42, 34));
            databaseNoticePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(110, 82, 49)),
                    BorderFactory.createEmptyBorder(10, 13, 10, 13)));
            databaseNotice.setForeground(new Color(255, 211, 157));
            databaseNoticePanel.setVisible(true);
            recentEmptyState.setText("Loading recent patient registrations...");
            recentLayout.show(recentContent, "empty");
            tableModel.setPatients(List.of());
        }

        private void showError() {
            for (JLabel value : values) {
                value.setText("—");
            }
            registrationTrend.clear("Registration trends are unavailable.");
            genderDistribution.clear("Analytics are unavailable until PostgreSQL reconnects.");
            reportAfterRefresh = false;
            databaseNotice.setText(
                    "Dashboard data is unavailable. Check PostgreSQL and the local connection settings.");
            databaseNoticePanel.setBackground(new Color(56, 35, 44));
            databaseNoticePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(116, 64, 77)),
                    BorderFactory.createEmptyBorder(10, 13, 10, 13)));
            databaseNotice.setForeground(new Color(255, 166, 175));
            databaseNoticePanel.setVisible(true);
            recentEmptyState.setText("Recent registrations could not be loaded.");
            recentLayout.show(recentContent, "empty");
            tableModel.setPatients(List.of());
        }

        private void setStats(DashboardStats stats) {
            values[0].setText(Long.toString(stats.totalPatients()));
            values[1].setText(Long.toString(stats.registeredToday()));
            values[2].setText(Long.toString(stats.malePatients()));
            values[3].setText(Long.toString(stats.femalePatients()));
            registrationTrend.setPatients(stats.recentPatients());
            genderDistribution.setStats(stats);
            databaseNoticePanel.setVisible(false);
            if (stats.totalPatients() == 0) {
                recentEmptyState.setText(
                        "No patient records yet. Register a patient to get started.");
            } else {
                recentEmptyState.setText("No recent registrations are available.");
            }
            tableModel.setPatients(stats.recentPatients());
            updateRecentState();
            if (reportAfterRefresh) {
                reportAfterRefresh = false;
                showReport(stats);
            }
        }

        private void updateRecentState() {
            if (tableModel.getRowCount() > 0) {
                recentLayout.show(recentContent, "table");
            } else {
                recentLayout.show(recentContent, "empty");
            }
        }
    }

    private static final class RegistrationTrendPanel extends JPanel {
        private java.util.NavigableMap<LocalDate, Integer> dailyCounts =
                new java.util.TreeMap<>();
        private String emptyMessage = "Waiting for live registration data...";

        private RegistrationTrendPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(420, 144));
            setMinimumSize(new Dimension(230, 120));
        }

        private void clear(String message) {
            dailyCounts = new java.util.TreeMap<>();
            emptyMessage = message;
            repaint();
        }

        private void setPatients(List<Patient> patients) {
            java.util.NavigableMap<LocalDate, Integer> counts =
                    new java.util.TreeMap<>();
            for (Patient patient : patients) {
                if (patient.createdAt() != null) {
                    counts.merge(patient.createdAt().toLocalDate(), 1, Integer::sum);
                }
            }
            dailyCounts = counts;
            emptyMessage = "No recent registrations to chart.";
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (dailyCounts.isEmpty()) {
                g.setColor(DASHBOARD_MUTED);
                g.setFont(getFont().deriveFont(12f));
                g.drawString(emptyMessage, 6, Math.max(28, getHeight() / 2));
                g.dispose();
                return;
            }

            List<LocalDate> dates = List.copyOf(dailyCounts.navigableKeySet());
            int maximum = 1;
            for (int count : dailyCounts.values()) {
                maximum = Math.max(maximum, count);
            }
            int left = 34;
            int right = 12;
            int top = 13;
            int bottom = 27;
            int plotWidth = Math.max(1, getWidth() - left - right);
            int plotHeight = Math.max(1, getHeight() - top - bottom);
            long daySpan = java.time.temporal.ChronoUnit.DAYS.between(
                    dates.get(0), dates.get(dates.size() - 1));
            g.setFont(getFont().deriveFont(9.5f));
            for (int line = 0; line < 3; line++) {
                int y = top + plotHeight * line / 2;
                int tickValue = (int) Math.round(maximum * (2 - line) / 2.0);
                String label = Integer.toString(tickValue);
                int labelWidth = g.getFontMetrics().stringWidth(label);
                g.setColor(DASHBOARD_MUTED);
                g.drawString(label, left - labelWidth - 7, y + 3);
                g.setColor(DASHBOARD_BORDER);
                g.drawLine(left, y, left + plotWidth, y);
            }

            java.awt.geom.Path2D.Double linePath = new java.awt.geom.Path2D.Double();
            java.awt.geom.Path2D.Double areaPath = new java.awt.geom.Path2D.Double();
            double[] xPoints = new double[dates.size()];
            double[] yPoints = new double[dates.size()];
            for (int index = 0; index < dates.size(); index++) {
                double x = dates.size() == 1
                        ? left + plotWidth / 2.0
                        : left + plotWidth * java.time.temporal.ChronoUnit.DAYS.between(
                                dates.get(0), dates.get(index)) / (double) daySpan;
                double y = top + plotHeight
                        - plotHeight * dailyCounts.get(dates.get(index)) / (double) maximum;
                xPoints[index] = x;
                yPoints[index] = y;
                if (index == 0) {
                    linePath.moveTo(x, y);
                    areaPath.moveTo(x, top + plotHeight);
                    areaPath.lineTo(x, y);
                } else {
                    linePath.lineTo(x, y);
                    areaPath.lineTo(x, y);
                }
            }
            areaPath.lineTo(xPoints[xPoints.length - 1], top + plotHeight);
            areaPath.closePath();
            g.setColor(new Color(
                    ELECTRIC_BLUE.getRed(), ELECTRIC_BLUE.getGreen(),
                    ELECTRIC_BLUE.getBlue(), 28));
            g.fill(areaPath);
            g.setColor(ELECTRIC_BLUE);
            g.setStroke(new java.awt.BasicStroke(
                    2.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.draw(linePath);
            for (int index = 0; index < xPoints.length; index++) {
                g.setColor(DASHBOARD_PANEL);
                g.fillOval((int) xPoints[index] - 5, (int) yPoints[index] - 5, 10, 10);
                g.setColor(ELECTRIC_BLUE);
                g.fillOval((int) xPoints[index] - 3, (int) yPoints[index] - 3, 6, 6);
            }

            g.setFont(getFont().deriveFont(9.5f));
            g.setColor(DASHBOARD_MUTED);
            DateTimeFormatter axisDate = DateTimeFormatter.ofPattern("dd MMM");
            String firstDate = axisDate.format(dates.get(0));
            String lastDate = axisDate.format(dates.get(dates.size() - 1));
            if (dates.size() == 1) {
                int dateWidth = g.getFontMetrics().stringWidth(firstDate);
                g.drawString(firstDate, (int) xPoints[0] - dateWidth / 2,
                        getHeight() - 5);
            } else {
                g.drawString(firstDate, left, getHeight() - 5);
                int lastWidth = g.getFontMetrics().stringWidth(lastDate);
                g.drawString(lastDate, left + plotWidth - lastWidth, getHeight() - 5);
            }
            g.dispose();
        }
    }

    private static final class GenderDistributionPanel extends JPanel {
        private long total;
        private long male;
        private long female;
        private long other;
        private String emptyMessage = "Waiting for live database data...";

        private GenderDistributionPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(420, 144));
            setMinimumSize(new Dimension(230, 120));
        }

        private void clear(String message) {
            total = 0;
            male = 0;
            female = 0;
            other = 0;
            emptyMessage = message;
            repaint();
        }

        private void setStats(DashboardStats stats) {
            total = stats.totalPatients();
            male = stats.malePatients();
            female = stats.femalePatients();
            other = Math.max(0, total - male - female);
            emptyMessage = "No patient records to analyse yet.";
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (total == 0) {
                g.setColor(DASHBOARD_MUTED);
                g.setFont(getFont().deriveFont(12f));
                g.drawString(emptyMessage, 6, Math.max(27, getHeight() / 2));
                g.dispose();
                return;
            }

            String[] labels = {"Male", "Female", "Other"};
            long[] counts = {male, female, other};
            Color[] colors = {
                    ELECTRIC_BLUE,
                    new Color(172, 115, 255),
                    new Color(51, 196, 151)
            };
            int labelWidth = 55;
            int valueWidth = 76;
            int barX = labelWidth + 4;
            int barWidth = Math.max(40, getWidth() - barX - valueWidth - 14);
            int y = 13;
            for (int index = 0; index < labels.length; index++) {
                int barY = y + index * 36;
                g.setColor(DASHBOARD_MUTED);
                g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
                g.drawString(labels[index], 2, barY + 9);
                g.setColor(DASHBOARD_BORDER);
                g.fillRoundRect(barX, barY, barWidth, 10, 10, 10);
                int fillWidth = Math.min(barWidth,
                        (int) Math.round(barWidth * counts[index] / (double) total));
                if (counts[index] > 0) {
                    g.setColor(colors[index]);
                    g.fillRoundRect(barX, barY, Math.max(6, fillWidth), 10, 10, 10);
                }
                g.setColor(DASHBOARD_TEXT);
                String countLabel = counts[index] + "  "
                        + Math.round(counts[index] * 100.0 / total) + "%";
                g.drawString(countLabel, barX + barWidth + 8, barY + 9);
            }
            g.dispose();
        }
    }

    private final class PatientsView extends JPanel {
        private final PatientTableModel tableModel = new PatientTableModel();
        private final JTable table = createPatientTable(tableModel);
        private final JTextField searchField = new JTextField(25);
        private final JLabel resultCount = new JLabel(" ");
        private final JLabel emptyState = new JLabel("", SwingConstants.CENTER);
        private final CardLayout recordsLayout = new CardLayout();
        private final JPanel recordsContent = new JPanel(recordsLayout);

        private PatientsView() {
            super(new BorderLayout(0, 18));
            setOpaque(false);
            add(heading("Patient directory",
                    "Search, review and manage records securely stored in PostgreSQL."),
                    BorderLayout.NORTH);

            JPanel toolbar = new JPanel(new BorderLayout(0, 7));
            toolbar.setOpaque(false);
            JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            search.setOpaque(false);
            searchField.putClientProperty("JTextField.placeholderText",
                    "Search patient ID or name");
            searchField.setPreferredSize(new Dimension(280, 38));
            searchField.addActionListener(event -> searchPatients(searchField.getText()));
            JButton searchButton = primaryButton("Search");
            searchButton.setIcon(new CareConnectIcon(
                    CareConnectIcon.Type.SEARCH, Color.WHITE, 16));
            searchButton.addActionListener(event -> searchPatients(searchField.getText()));
            JButton clearButton = secondaryButton("Clear");
            clearButton.addActionListener(event -> {
                searchField.setText("");
                searchPatients("");
            });
            search.add(searchField);
            search.add(searchButton);
            search.add(clearButton);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);
            JButton refreshButton = secondaryButton("Refresh");
            refreshButton.setIcon(new CareConnectIcon(
                    CareConnectIcon.Type.REFRESH, NAVY, 15));
            refreshButton.addActionListener(event -> refreshPatients());
            JButton clearSelectionButton = secondaryButton("Clear selection");
            clearSelectionButton.addActionListener(event -> table.clearSelection());
            JButton editButton = secondaryButton("Edit selected");
            editButton.setIcon(new CareConnectIcon(
                    CareConnectIcon.Type.EDIT, NAVY, 15));
            editButton.addActionListener(event -> editSelectedPatient());
            JButton deleteButton = secondaryButton("Delete");
            deleteButton.setIcon(new CareConnectIcon(
                    CareConnectIcon.Type.DELETE, RED, 15));
            deleteButton.setForeground(RED);
            deleteButton.addActionListener(event -> deleteSelectedPatient());
            actions.add(refreshButton);
            actions.add(clearSelectionButton);
            actions.add(editButton);
            actions.add(deleteButton);
            toolbar.add(search, BorderLayout.NORTH);
            toolbar.add(actions, BorderLayout.SOUTH);

            JPanel tableCard = new JPanel(new BorderLayout(0, 8));
            tableCard.setBackground(Color.WHITE);
            tableCard.setBorder(BorderFactory.createCompoundBorder(
                    cardBorder(), BorderFactory.createEmptyBorder(14, 14, 10, 14)));
            resultCount.setForeground(MUTED);
            resultCount.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
            tableCard.add(resultCount, BorderLayout.NORTH);
            recordsContent.setOpaque(true);
            recordsContent.setBackground(Color.WHITE);
            recordsContent.add(createTableScrollPane(table), "table");
            emptyState.setForeground(MUTED);
            emptyState.setFont(emptyState.getFont().deriveFont(13f));
            recordsContent.add(emptyState, "empty");
            tableCard.add(recordsContent, BorderLayout.CENTER);

            JPanel content = new JPanel(new BorderLayout(0, 14));
            content.setOpaque(false);
            content.add(toolbar, BorderLayout.NORTH);
            content.add(tableCard, BorderLayout.CENTER);
            add(content, BorderLayout.CENTER);

            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent event) {
                    if (event.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                        editSelectedPatient();
                    }
                }
            });
            tableModel.addTableModelListener(event -> updateRecordsState());
        }

        private String getSearchText() {
            return searchField.getText().trim();
        }

        private void setSearchText(String text) {
            searchField.setText(text);
        }

        private void showLoading() {
            resultCount.setText("Loading patient records...");
            resultCount.setForeground(MUTED);
            emptyState.setText("Loading patient records from PostgreSQL...");
            recordsLayout.show(recordsContent, "empty");
            tableModel.setPatients(List.of());
        }

        private void showError() {
            resultCount.setText(
                    "Patient records could not be loaded.");
            resultCount.setForeground(RED);
            emptyState.setText(
                    "Patient records are unavailable. Check PostgreSQL and the local connection settings.");
            recordsLayout.show(recordsContent, "empty");
            tableModel.setPatients(List.of());
        }

        private void setPatients(List<Patient> patients) {
            tableModel.setPatients(patients);
            resultCount.setForeground(MUTED);
            resultCount.setText(patients.size() + " patient"
                    + (patients.size() == 1 ? "" : "s") + " found");
            emptyState.setText(searchField.getText().isBlank()
                    ? "No patient records yet. Register a patient to get started."
                    : "No matching patients. Try another ID or name, or clear the search.");
            updateRecordsState();
        }

        private void updateRecordsState() {
            recordsLayout.show(recordsContent,
                    tableModel.getRowCount() == 0 ? "empty" : "table");
        }

        private void focusSearch() {
            javax.swing.SwingUtilities.invokeLater(searchField::requestFocusInWindow);
        }

        private Patient selectedPatient() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                showValidationError("Select a patient record first.");
                return null;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            return tableModel.getPatientAt(modelRow);
        }

        private void editSelectedPatient() {
            Patient patient = selectedPatient();
            if (patient != null) {
                editPatient(patient);
            }
        }

        private void deleteSelectedPatient() {
            Patient patient = selectedPatient();
            if (patient != null) {
                deletePatient(patient);
            }
        }
    }

    private final class StatCard extends JPanel {
        private final JLabel valueLabel = new JLabel("—");
        private final Color accent;
        private boolean hovered;

        private StatCard(
                String title, String description, CareConnectIcon.Type iconType,
                Color accent) {
            super(new BorderLayout());
            this.accent = accent;
            setOpaque(false);
            setPreferredSize(new Dimension(190, 126));
            setMinimumSize(new Dimension(150, 116));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setForeground(DASHBOARD_MUTED);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 10.5f));

            JLabel icon = new JLabel(new CareConnectIcon(iconType, accent, 21),
                    SwingConstants.CENTER);
            JPanel iconBadge = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(java.awt.Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(
                            (accent.getRed() + DASHBOARD_CARD.getRed() * 3) / 4,
                            (accent.getGreen() + DASHBOARD_CARD.getGreen() * 3) / 4,
                            (accent.getBlue() + DASHBOARD_CARD.getBlue() * 3) / 4));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    g.dispose();
                    super.paintComponent(graphics);
                }
            };
            iconBadge.setOpaque(false);
            iconBadge.setPreferredSize(new Dimension(46, 46));
            iconBadge.add(icon, BorderLayout.CENTER);

            valueLabel.setForeground(DASHBOARD_TEXT);
            valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 30f));
            JLabel descriptionLabel = new JLabel(description);
            descriptionLabel.setForeground(DASHBOARD_MUTED);
            descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(11f));
            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
            text.add(titleLabel);
            text.add(javax.swing.Box.createVerticalStrut(5));
            text.add(valueLabel);
            text.add(javax.swing.Box.createVerticalStrut(1));
            text.add(descriptionLabel);
            JPanel content = new JPanel(new BorderLayout(10, 0));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(16, 17, 13, 17));
            content.add(text, BorderLayout.CENTER);
            content.add(iconBadge, BorderLayout.EAST);
            add(content, BorderLayout.CENTER);
            installHoverTracking(this);
        }

        private void installHoverTracking(java.awt.Component component) {
            component.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent event) {
                    java.awt.Point point = javax.swing.SwingUtilities.convertPoint(
                            event.getComponent(), event.getPoint(), StatCard.this);
                    if (!contains(point)) {
                        hovered = false;
                        repaint();
                    }
                }
            });
            if (component instanceof java.awt.Container container) {
                for (java.awt.Component child : container.getComponents()) {
                    installHoverTracking(child);
                }
            }
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D copy = (java.awt.Graphics2D) graphics.create();
            copy.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(new Color(7, 12, 22));
            copy.fillRoundRect(1, hovered ? 3 : 5,
                    getWidth() - 2, getHeight() - (hovered ? 3 : 5), 18, 18);
            copy.setColor(hovered ? new Color(31, 46, 67) : DASHBOARD_CARD);
            copy.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 6, 18, 18);
            copy.setColor(hovered ? accent : DASHBOARD_BORDER);
            copy.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 5, 18, 18);
            copy.setColor(accent);
            copy.fillRoundRect(0, 19, hovered ? 5 : 4, getHeight() - 43, 5, 5);
            copy.dispose();
            super.paintComponent(graphics);
        }
    }
}
