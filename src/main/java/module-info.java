module dk.easv.thorsmovieplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;


    opens dk.easv.thorsmovieplayer to javafx.fxml;
    exports dk.easv.thorsmovieplayer;
    exports dk.easv.thorsmovieplayer.GUI;
    opens dk.easv.thorsmovieplayer.GUI to javafx.fxml;
    exports dk.easv.thorsmovieplayer.DAL;
    opens dk.easv.thorsmovieplayer.DAL to javafx.fxml;
}