import com.domain.DBConnection;
import com.gui.MainWindow;

class App {

    public static void main(String[] args) {

        DBConnection.initializeDBTables();

        MainWindow mainWindow = new MainWindow();
        mainWindow.setupAndDrawUI();
    }
}
