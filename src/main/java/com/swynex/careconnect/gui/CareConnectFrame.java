package com.swynex.careconnect.gui;

import com.swynex.careconnect.model.DashboardStats;
import com.swynex.careconnect.model.Patient;
import com.swynex.careconnect.service.PatientService;
import com.swynex.careconnect.gui.components.PatientFormPanel;
import com.swynex.careconnect.gui.components.PatientTableModel;
import com.swynex.careconnect.gui.components.SectionPanel;

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
    private static final Color NAVY_LIGHT = new Color(34, 62, 91);
    private static final Color TEAL = new Color(16, 145, 137);
    private static final Color BACKGROUND = new Color(243, 247, 250);
    private static final Color MUTED = new Color(112, 128, 144);

    private final PatientService patientService;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageContainer = new JPanel(pageLayout);
    private final Map<String, JButton> navigationButtons = new java.util.LinkedHashMap<>();
    private final JLabel pageTitle = new JLabel("Dashboard");
    private final JLabel pageSubtitle = new JLabel("A clear view of your patient activity");
    private final JLabel connectionStatus = new JLabel("Connecting to PostgreSQL...");
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

        pageContainer.setOpaque(false);
        pageContainer.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
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
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(244, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 14, 18, 14));

        JPanel top = new JPanel(new BorderLayout(0, 3));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 10, 28, 8));
        JLabel brand = new JLabel("CareConnect");
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 21f));
        JLabel descriptor = new JLabel("PATIENT SERVICES");
        descriptor.setForeground(new Color(154, 183, 204));
        descriptor.setFont(descriptor.getFont().deriveFont(Font.BOLD, 10f));
        top.add(brand, BorderLayout.NORTH);
        top.add(descriptor, BorderLayout.CENTER);

        JPanel navigation = new JPanel(new GridLayout(0, 1, 0, 7));
        navigation.setOpaque(false);
        navigation.add(createNavigationButton("dashboard", "◈", "Dashboard"));
        navigation.add(createNavigationButton("register", "＋", "Register Patient"));
        navigation.add(createNavigationButton("patients", "▤", "Patient Records"));
        navigation.add(createNavigationButton("about", "ⓘ", "About / Project Info"));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JButton exitButton = createNavigationButton("exit", "↪", "Exit");
        exitButton.addActionListener(event -> confirmExit());
        bottom.add(exitButton, BorderLayout.NORTH);
        JLabel internship = new JLabel("<html>SWYNEX<br>INTERNSHIP · TASK 3</html>");
        internship.setForeground(new Color(137, 161, 181));
        internship.setFont(internship.getFont().deriveFont(Font.BOLD, 10f));
        internship.setBorder(BorderFactory.createEmptyBorder(19, 13, 0, 0));
        bottom.add(internship, BorderLayout.SOUTH);

        sidebar.add(top, BorderLayout.NORTH);
        sidebar.add(navigation, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavigationButton(String key, String icon, String title) {
        JButton button = new JButton(icon + "    " + title);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(new Color(206, 219, 230));
        button.setBackground(NAVY);
        button.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 10));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 13f));
        if (!"exit".equals(key)) {
            navigationButtons.put(key, button);
            button.addActionListener(event -> navigate(key));
        }
        return button;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(228, 235, 240)),
                BorderFactory.createEmptyBorder(18, 30, 18, 30)));

        JPanel headings = new JPanel(new BorderLayout(0, 4));
        headings.setOpaque(false);
        pageTitle.setFont(pageTitle.getFont().deriveFont(Font.BOLD, 22f));
        pageTitle.setForeground(NAVY);
        pageSubtitle.setForeground(MUTED);
        headings.add(pageTitle, BorderLayout.NORTH);
        headings.add(pageSubtitle, BorderLayout.SOUTH);

        JPanel profile = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        profile.setOpaque(false);
        JLabel avatar = new JLabel("CC", SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(225, 243, 241));
        avatar.setForeground(TEAL);
        avatar.setFont(avatar.getFont().deriveFont(Font.BOLD, 13f));
        avatar.setPreferredSize(new Dimension(38, 38));
        JLabel administrator = new JLabel("<html><b>Administrator</b><br><span style='color:#708090'>CareConnect</span></html>");
        profile.add(avatar);
        profile.add(administrator);

        header.add(headings, BorderLayout.WEST);
        header.add(profile, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(228, 235, 240)),
                BorderFactory.createEmptyBorder(10, 30, 10, 30)));
        connectionStatus.setForeground(MUTED);
        connectionStatus.setFont(connectionStatus.getFont().deriveFont(11f));
        JLabel version = new JLabel("CARECONNECT  ·  1.0");
        version.setForeground(MUTED);
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
        formCard.setBorder(cardBorder("Patient details"));
        formCard.add(registrationForm, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        actions.setOpaque(false);
        JButton clearButton = secondaryButton("Clear form");
        clearButton.addActionListener(event -> registrationForm.clear());
        JButton saveButton = primaryButton("Register patient");
        saveButton.addActionListener(event -> registerPatient());
        actions.add(clearButton);
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
                cardBorder(null), BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(NAVY);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        JLabel detailLabel = new JLabel("<html><div style='width:850px'>"
                + detail + "</div></html>");
        detailLabel.setForeground(MUTED);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(detailLabel, BorderLayout.CENTER);
        return card;
    }

    private void navigate(String page) {
        currentPage = page;
        pageLayout.show(pageContainer, page);
        switch (page) {
            case "dashboard" -> {
                setPageHeading("Dashboard", "A clear view of your patient activity");
                refreshDashboard();
            }
            case "register" ->
                    setPageHeading("Register Patient", "Add a new patient to the records");
            case "patients" -> {
                setPageHeading("Patient Records", "Search, review and manage registered patients");
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
            button.setBackground(selected ? NAVY_LIGHT : NAVY);
            button.setForeground(selected ? Color.WHITE : new Color(206, 219, 230));
            button.setFont(button.getFont().deriveFont(
                    selected ? Font.BOLD : Font.PLAIN, 13f));
        });
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
        dialog.setMinimumSize(new Dimension(680, 650));

        PatientFormPanel form = new PatientFormPanel(patient);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBorder(BorderFactory.createEmptyBorder(0, 18, 10, 18));
        JButton cancelButton = secondaryButton("Cancel");
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = primaryButton("Save changes");
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
        actions.add(saveButton);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.pack();
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
        connectionStatus.setForeground(MUTED);
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
        connectionStatus.setForeground(new Color(181, 73, 73));
        String message = "CareConnect could not complete: " + activity.toLowerCase() + ".\n\n"
                + "Check that PostgreSQL is running, the careconnect_db database and "
                + "patients table exist, and CARECONNECT_DB_USERNAME and "
                + "CARECONNECT_DB_PASSWORD are set. Technical details were written "
                + "to the application log.";
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

    private static javax.swing.border.Border cardBorder(String title) {
        if (title == null) {
            return BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(227, 234, 239)),
                    BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(227, 234, 239)),
                        title),
                BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(TEAL);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setMargin(new Insets(10, 16, 10, 16));
        return button;
    }

    private static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setMargin(new Insets(10, 15, 10, 15));
        return button;
    }

    private static JTable createPatientTable(PatientTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(39);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(235, 239, 243));
        table.setSelectionBackground(new Color(221, 241, 239));
        table.setSelectionForeground(NAVY);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setFont(table.getTableHeader().getFont()
                .deriveFont(Font.BOLD, 12f));
        table.setFont(table.getFont().deriveFont(12.5f));
        int[] widths = {64, 170, 55, 82, 120, 190, 92, 155};
        for (int column = 0; column < widths.length; column++) {
            table.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
        }
        DefaultTableCellRenderer centeredRenderer = new DefaultTableCellRenderer();
        centeredRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int column : new int[]{0, 2, 3, 6}) {
            table.getColumnModel().getColumn(column).setCellRenderer(centeredRenderer);
        }
        table.getColumnModel().getColumn(7).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    protected void setValue(Object value) {
                        setHorizontalAlignment(SwingConstants.CENTER);
                        setText(value instanceof java.time.OffsetDateTime dateTime
                                ? PatientTableModel.formatDate(dateTime)
                                : "—");
                    }
                });
        return table;
    }

    @FunctionalInterface
    private interface SqlOperation<T> {
        T run() throws SQLException;
    }

    private final class DashboardView extends JPanel {
        private final JLabel[] values = new JLabel[4];
        private final JLabel databaseNotice = new JLabel(" ");
        private final PatientTableModel tableModel = new PatientTableModel();

        private DashboardView() {
            super(new BorderLayout(0, 18));
            setOpaque(false);

            JPanel intro = new JPanel(new BorderLayout());
            intro.setOpaque(false);
            intro.add(heading("Good day, Administrator",
                    "Here is the latest overview of your hospital's patient records."),
                    BorderLayout.WEST);
            JButton registerButton = primaryButton("＋  Register patient");
            registerButton.addActionListener(event -> navigate("register"));
            intro.add(registerButton, BorderLayout.EAST);

            JPanel metrics = new JPanel(new GridLayout(1, 4, 14, 0));
            metrics.setOpaque(false);
            String[] labels = {
                    "TOTAL PATIENTS", "MALE PATIENTS",
                    "FEMALE PATIENTS", "REGISTERED TODAY"
            };
            String[] symbols = {"◉", "♂", "♀", "＋"};
            for (int index = 0; index < labels.length; index++) {
                StatCard card = new StatCard(labels[index], symbols[index]);
                values[index] = card.valueLabel;
                metrics.add(card);
            }

            databaseNotice.setForeground(new Color(181, 73, 73));
            databaseNotice.setFont(databaseNotice.getFont().deriveFont(12f));

            JPanel recentHeader = new JPanel(new BorderLayout());
            recentHeader.setOpaque(false);
            recentHeader.add(heading("Recent registrations",
                    "The six most recently registered patients"), BorderLayout.WEST);
            JButton viewAll = secondaryButton("View all records  →");
            viewAll.addActionListener(event -> navigate("patients"));
            recentHeader.add(viewAll, BorderLayout.EAST);

            JTable table = createPatientTable(tableModel);
            table.setPreferredScrollableViewportSize(new Dimension(900, 250));
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            JPanel tableCard = new JPanel(new BorderLayout());
            tableCard.setBackground(Color.WHITE);
            tableCard.setBorder(cardBorder(null));
            tableCard.add(scrollPane, BorderLayout.CENTER);

            JPanel lower = new JPanel(new BorderLayout(0, 12));
            lower.setOpaque(false);
            lower.add(recentHeader, BorderLayout.NORTH);
            lower.add(tableCard, BorderLayout.CENTER);

            JPanel content = new JPanel(new BorderLayout(0, 17));
            content.setOpaque(false);
            content.add(intro, BorderLayout.NORTH);
            JPanel main = new JPanel(new BorderLayout(0, 12));
            main.setOpaque(false);
            main.add(metrics, BorderLayout.NORTH);
            JPanel lowerWithNotice = new JPanel(new BorderLayout(0, 10));
            lowerWithNotice.setOpaque(false);
            lowerWithNotice.add(databaseNotice, BorderLayout.NORTH);
            lowerWithNotice.add(lower, BorderLayout.CENTER);
            main.add(lowerWithNotice, BorderLayout.CENTER);
            content.add(main, BorderLayout.CENTER);
            add(content, BorderLayout.CENTER);
        }

        private void showLoading() {
            for (JLabel value : values) {
                value.setText("—");
            }
            databaseNotice.setText("Loading live data from PostgreSQL…");
            databaseNotice.setForeground(MUTED);
            tableModel.setPatients(List.of());
        }

        private void showError() {
            databaseNotice.setText(
                    "Dashboard data is unavailable. Check your PostgreSQL configuration.");
            databaseNotice.setForeground(new Color(181, 73, 73));
            tableModel.setPatients(List.of());
        }

        private void setStats(DashboardStats stats) {
            values[0].setText(Long.toString(stats.totalPatients()));
            values[1].setText(Long.toString(stats.malePatients()));
            values[2].setText(Long.toString(stats.femalePatients()));
            values[3].setText(Long.toString(stats.registeredToday()));
            databaseNotice.setText("Live statistics from PostgreSQL");
            databaseNotice.setForeground(TEAL);
            tableModel.setPatients(stats.recentPatients());
        }
    }

    private final class PatientsView extends JPanel {
        private final PatientTableModel tableModel = new PatientTableModel();
        private final JTable table = createPatientTable(tableModel);
        private final JTextField searchField = new JTextField(25);
        private final JLabel resultCount = new JLabel(" ");

        private PatientsView() {
            super(new BorderLayout(0, 18));
            setOpaque(false);
            add(heading("Patient directory",
                    "Find a patient by ID or name, then choose an action for that record."),
                    BorderLayout.NORTH);

            JPanel toolbar = new JPanel();
            toolbar.setLayout(new javax.swing.BoxLayout(toolbar, javax.swing.BoxLayout.Y_AXIS));
            toolbar.setOpaque(false);
            JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            search.setOpaque(false);
            searchField.putClientProperty("JTextField.placeholderText", "Search by patient ID or name");
            searchField.setPreferredSize(new Dimension(300, 38));
            searchField.addActionListener(event -> searchPatients(searchField.getText()));
            JButton searchButton = primaryButton("Search");
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
            refreshButton.addActionListener(event -> refreshPatients());
            JButton editButton = secondaryButton("Edit selected");
            editButton.addActionListener(event -> editSelectedPatient());
            JButton deleteButton = secondaryButton("Delete");
            deleteButton.setForeground(new Color(174, 62, 62));
            deleteButton.addActionListener(event -> deleteSelectedPatient());
            actions.add(refreshButton);
            actions.add(editButton);
            actions.add(deleteButton);
            toolbar.add(search);
            toolbar.add(javax.swing.Box.createVerticalStrut(8));
            JPanel actionRow = new JPanel(new BorderLayout());
            actionRow.setOpaque(false);
            actionRow.add(actions, BorderLayout.EAST);
            actionRow.setAlignmentX(LEFT_ALIGNMENT);
            toolbar.add(actionRow);

            JPanel tableCard = new JPanel(new BorderLayout(0, 8));
            tableCard.setBackground(Color.WHITE);
            tableCard.setBorder(BorderFactory.createCompoundBorder(
                    cardBorder(null), BorderFactory.createEmptyBorder(14, 14, 10, 14)));
            resultCount.setForeground(MUTED);
            resultCount.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
            tableCard.add(resultCount, BorderLayout.NORTH);
            tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

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
        }

        private String getSearchText() {
            return searchField.getText().trim();
        }

        private void setSearchText(String text) {
            searchField.setText(text);
        }

        private void showLoading() {
            resultCount.setText("Loading patient records...");
            tableModel.setPatients(List.of());
        }

        private void showError() {
            resultCount.setText(
                    "Patient records are unavailable. Check your PostgreSQL configuration.");
            tableModel.setPatients(List.of());
        }

        private void setPatients(List<Patient> patients) {
            tableModel.setPatients(patients);
            resultCount.setText(patients.size() + " patient"
                    + (patients.size() == 1 ? "" : "s") + " found");
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

        private StatCard(String title, String symbol) {
            super(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(180, 122));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setForeground(MUTED);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 10f));

            JLabel icon = new JLabel(symbol, SwingConstants.CENTER);
            icon.setOpaque(true);
            icon.setForeground(TEAL);
            icon.setBackground(new Color(226, 244, 241));
            icon.setFont(icon.getFont().deriveFont(Font.BOLD, 18f));
            icon.setPreferredSize(new Dimension(40, 40));

            valueLabel.setForeground(NAVY);
            valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 28f));
            JPanel text = new JPanel(new BorderLayout(0, 10));
            text.setOpaque(false);
            text.add(titleLabel, BorderLayout.NORTH);
            text.add(valueLabel, BorderLayout.CENTER);
            JPanel content = new JPanel(new BorderLayout(10, 0));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            content.add(text, BorderLayout.CENTER);
            content.add(icon, BorderLayout.EAST);
            add(content, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D copy = (java.awt.Graphics2D) graphics.create();
            copy.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(Color.WHITE);
            copy.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            copy.setColor(new Color(227, 234, 239));
            copy.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            copy.dispose();
            super.paintComponent(graphics);
        }
    }
}
