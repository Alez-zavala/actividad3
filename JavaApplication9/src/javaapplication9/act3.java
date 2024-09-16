import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class act3 {

    // Variables de conexión a la base de datos
    private static final String URL = "jdbc:mysql://localhost:3306/escuela";
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; // Sin contraseña

    public static void main(String[] args) {
        // Crear base de datos y tablas si no existen
        crearBaseDeDatosYTablas();
        
        // Crear y mostrar formulario
        SwingUtilities.invokeLater(() -> new EscuelaFrame().setVisible(true));
    }

    private static void crearBaseDeDatosYTablas() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
             
            // Crear base de datos
            stmt.execute("CREATE DATABASE IF NOT EXISTS escuela");
            stmt.execute("USE escuela");

            // Crear tabla Persona
            stmt.execute("CREATE TABLE IF NOT EXISTS Persona (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "nombres VARCHAR(100), " +
                         "apellidos VARCHAR(100), " +
                         "direccion VARCHAR(255), " +
                         "telefono VARCHAR(15), " +
                         "fecha_nacimiento DATE)");

            // Crear tabla Docente
            stmt.execute("CREATE TABLE IF NOT EXISTS Docente (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "persona_id INT, " +
                         "codigo_docente VARCHAR(50) UNIQUE, " +
                         "area VARCHAR(100), " +
                         "ingreso_laboral DATE, " +
                         "FOREIGN KEY (persona_id) REFERENCES Persona(id))");

            System.out.println("Base de datos y tablas creadas.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Clase para Persona
    static class Persona {
        private String nombres;
        private String apellidos;
        private String direccion;
        private String telefono;
        private Date fechaNacimiento;

        public Persona(String nombres, String apellidos, String direccion, String telefono, Date fechaNacimiento) {
            this.nombres = nombres;
            this.apellidos = apellidos;
            this.direccion = direccion;
            this.telefono = telefono;
            this.fechaNacimiento = fechaNacimiento;
        }

        // Getters
        public String getNombres() { return nombres; }
        public String getApellidos() { return apellidos; }
        public String getDireccion() { return direccion; }
        public String getTelefono() { return telefono; }
        public Date getFechaNacimiento() { return fechaNacimiento; }
    }

    // Clase para Docente (hereda de Persona)
    static class Docente extends Persona {
        private String codigoDocente;
        private String area;
        private Date ingresoLaboral;

        public Docente(String nombres, String apellidos, String direccion, String telefono,
                       Date fechaNacimiento, String codigoDocente, String area, Date ingresoLaboral) {
            super(nombres, apellidos, direccion, telefono, fechaNacimiento);
            this.codigoDocente = codigoDocente;
            this.area = area;
            this.ingresoLaboral = ingresoLaboral;
        }

        // Getters y Setters
        public String getCodigoDocente() { return codigoDocente; }
        public void setCodigoDocente(String codigoDocente) { this.codigoDocente = codigoDocente; }
        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }
        public Date getIngresoLaboral() { return ingresoLaboral; }
        public void setIngresoLaboral(Date ingresoLaboral) { this.ingresoLaboral = ingresoLaboral; }

        // Métodos CRUD
        public void guardarDocente(Connection conn) throws SQLException {
            String sqlPersona = "INSERT INTO Persona (nombres, apellidos, direccion, telefono, fecha_nacimiento) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmtPersona = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS);
            stmtPersona.setString(1, getNombres());
            stmtPersona.setString(2, getApellidos());
            stmtPersona.setString(3, getDireccion());
            stmtPersona.setString(4, getTelefono());
            stmtPersona.setDate(5, getFechaNacimiento());

            stmtPersona.executeUpdate();

            ResultSet generatedKeys = stmtPersona.getGeneratedKeys();
            if (generatedKeys.next()) {
                int personaId = generatedKeys.getInt(1);
                String sqlDocente = "INSERT INTO Docente (persona_id, codigo_docente, area, ingreso_laboral) VALUES (?, ?, ?, ?)";
                PreparedStatement stmtDocente = conn.prepareStatement(sqlDocente);
                stmtDocente.setInt(1, personaId);
                stmtDocente.setString(2, codigoDocente);
                stmtDocente.setString(3, area);
                stmtDocente.setDate(4, ingresoLaboral);
                stmtDocente.executeUpdate();
            }
        }

        public static List<Docente> obtenerDocentes(Connection conn) throws SQLException {
            List<Docente> docentes = new ArrayList<>();
            String sql = "SELECT d.id, p.nombres, p.apellidos, p.direccion, p.telefono, " +
                         "p.fecha_nacimiento, d.codigo_docente, d.area, d.ingreso_laboral " +
                         "FROM Docente d JOIN Persona p ON d.persona_id = p.id";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Docente docente = new Docente(
                    rs.getString("nombres"),
                    rs.getString("apellidos"),
                    rs.getString("direccion"),
                    rs.getString("telefono"),
                    rs.getDate("fecha_nacimiento"),
                    rs.getString("codigo_docente"),
                    rs.getString("area"),
                    rs.getDate("ingreso_laboral")
                );
                docentes.add(docente);
            }
            return docentes;
        }

        public void actualizarDocente(Connection conn, int docenteId) throws SQLException {
            String sql = "UPDATE Docente d JOIN Persona p ON d.persona_id = p.id " +
                         "SET p.nombres = ?, p.apellidos = ?, p.direccion = ?, " +
                         "p.telefono = ?, p.fecha_nacimiento = ?, d.codigo_docente = ?, " +
                         "d.area = ?, d.ingreso_laboral = ? WHERE d.id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, getNombres());
            stmt.setString(2, getApellidos());
            stmt.setString(3, getDireccion());
            stmt.setString(4, getTelefono());
            stmt.setDate(5, getFechaNacimiento());
            stmt.setString(6, codigoDocente);
            stmt.setString(7, area);
            stmt.setDate(8, ingresoLaboral);
            stmt.setInt(9, docenteId);
            
            stmt.executeUpdate();
        }

        public static void eliminarDocente(Connection conn, int docenteId) throws SQLException {
            String sqlDocente = "DELETE FROM Docente WHERE id = ?";
            PreparedStatement stmtDocente = conn.prepareStatement(sqlDocente);
            stmtDocente.setInt(1, docenteId);
            stmtDocente.executeUpdate();
            
            String sqlPersona = "DELETE FROM Persona WHERE id = (SELECT persona_id FROM Docente WHERE id = ?)";
            PreparedStatement stmtPersona = conn.prepareStatement(sqlPersona);
            stmtPersona.setInt(1, docenteId);
            stmtPersona.executeUpdate();
        }
    }

    // Clase para el formulario
    static class EscuelaFrame extends JFrame {
        private JTextField nombresField, apellidosField, direccionField, telefonoField, codigoDocenteField, areaField;
        private JButton saveButton, updateButton, deleteButton;
        private JTable docentesTable;
        private DefaultTableModel tableModel;
        private int selectedDocenteId = -1;

        public EscuelaFrame() {
            setTitle("Registro de Docentes");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            // Inicializar campos
            nombresField = new JTextField();
            apellidosField = new JTextField();
            direccionField = new JTextField();
            telefonoField = new JTextField();
            codigoDocenteField = new JTextField();
            areaField = new JTextField();
            saveButton = new JButton("Guardar");
            updateButton = new JButton("Actualizar");
            deleteButton = new JButton("Eliminar");

            // Panel de formulario
            JPanel panel = new JPanel(new GridLayout(7, 2));
            panel.add(new JLabel("Nombres:"));
            panel.add(nombresField);
            panel.add(new JLabel("Apellidos:"));
            panel.add(apellidosField);
            panel.add(new JLabel("Dirección:"));
            panel.add(direccionField);
            panel.add(new JLabel("Teléfono:"));
            panel.add(telefonoField);
            panel.add(new JLabel("Código Docente:"));
            panel.add(codigoDocenteField);
            panel.add(new JLabel("Área:"));
            panel.add(areaField);
            panel.add(saveButton);
            panel.add(updateButton);
            panel.add(deleteButton);

            // Tabla de docentes
            tableModel = new DefaultTableModel(new Object[]{"ID", "Nombres", "Apellidos", "Código", "Área"}, 0);
            docentesTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(docentesTable);

            // Añadir al marco
            add(panel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);

            // Manejar acciones
            saveButton.addActionListener(e -> guardarDocente());
            updateButton.addActionListener(e -> actualizarDocente());
            deleteButton.addActionListener(e -> eliminarDocente());
            docentesTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cargarDatosDesdeTabla();
                }
            });

            cargarDocentes();
        }

        private void guardarDocente() {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                // Asegúrate de que los campos no están vacíos
                if (nombresField.getText().isEmpty() || apellidosField.getText().isEmpty() ||
                    codigoDocenteField.getText().isEmpty() || areaField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Por favor, completa todos los campos.");
                    return;
                }

                Docente docente = new Docente(
                        nombresField.getText(),
                        apellidosField.getText(),
                        direccionField.getText(),
                        telefonoField.getText(),
                        new Date(System.currentTimeMillis()), // Cambia por fecha real si necesario
                        codigoDocenteField.getText(),
                        areaField.getText(),
                        new Date(System.currentTimeMillis()) // Cambia por fecha real si necesario
                );
                docente.guardarDocente(conn);
                JOptionPane.showMessageDialog(this, "Docente guardado correctamente.");
                cargarDocentes();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al guardar docente: " + ex.getMessage());
            }
        }

        private void cargarDocentes() {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                List<Docente> docentes = Docente.obtenerDocentes(conn);
                tableModel.setRowCount(0); // Limpiar la tabla

                for (Docente docente : docentes) {
                    Object[] row = {
                        docente.getCodigoDocente(), // ID en la primera columna
                        docente.getNombres(),
                        docente.getApellidos(),
                        docente.getCodigoDocente(),
                        docente.getArea()
                    };
                    tableModel.addRow(row);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void cargarDatosDesdeTabla() {
            int selectedRow = docentesTable.getSelectedRow();
            if (selectedRow != -1) {
                selectedDocenteId = (int) tableModel.getValueAt(selectedRow, 0); // obtener el ID
                nombresField.setText((String) tableModel.getValueAt(selectedRow, 1));
                apellidosField.setText((String) tableModel.getValueAt(selectedRow, 2));
                codigoDocenteField.setText((String) tableModel.getValueAt(selectedRow, 3));
                areaField.setText((String) tableModel.getValueAt(selectedRow, 4));
                // Otros campos pueden incluirse según tu necesidad
            }
        }

        private void actualizarDocente() {
            if (selectedDocenteId == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un docente para actualizar.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                Docente docente = new Docente(
                        nombresField.getText(),
                        apellidosField.getText(),
                        direccionField.getText(),
                        telefonoField.getText(),
                        new Date(System.currentTimeMillis()), // Cambia por fecha real si necesario
                        codigoDocenteField.getText(),
                        areaField.getText(),
                        new Date(System.currentTimeMillis()) // Cambia por fecha real si necesario
                );
                docente.actualizarDocente(conn, selectedDocenteId);
                JOptionPane.showMessageDialog(this, "Docente actualizado correctamente.");
                cargarDocentes();
                selectedDocenteId = -1; // Reiniciar selección
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar docente: " + ex.getMessage());
            }
        }

        private void eliminarDocente() {
            if (selectedDocenteId == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un docente para eliminar.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                Docente.eliminarDocente(conn, selectedDocenteId);
                JOptionPane.showMessageDialog(this, "Docente eliminado correctamente.");
                cargarDocentes();
                selectedDocenteId = -1; // Reiniciar selección
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar docente: " + ex.getMessage());
            }
        }
    }
}
