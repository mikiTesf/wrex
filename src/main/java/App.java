import com.domain.DBConnection;
import com.gui.CommonUIResources;
import com.gui.MainWindow;

class App {

    public static void main(String[] args) {

        DBConnection.initializeDBTables();
        new CommonUIResources().initializeUIResources();

        MainWindow mainWindow = new MainWindow();
        mainWindow.setupAndDrawUI();
    }
}
