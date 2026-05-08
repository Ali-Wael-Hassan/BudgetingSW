module com.duck {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.desktop;
    requires transitive javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    
    requires org.apache.pdfbox;
    
    // Open the package where Budget is located
    opens com.duck.model.records to com.fasterxml.jackson.databind;
    
    // Open the package where Account and other types are located
    opens com.duck.model.type to com.fasterxml.jackson.databind;
    
    // Open the package where LocalStorage is located
    opens com.duck.model.dataAccessors to com.fasterxml.jackson.databind;
    
    opens com.duck to javafx.fxml;
    exports com.duck;
    exports com.duck.model.type;
    exports com.duck.model.records;
    exports com.duck.model.dataAccessors;
    exports com.duck.model.authentication;
    exports com.duck.model.accountOps;
}
