module com.duck {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.apache.pdfbox;
    
    opens com.duck to javafx.fxml;
    exports com.duck;
}
