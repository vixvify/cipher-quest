#!/bin/sh
exec java --module-path /app/javafx --add-modules javafx.controls,javafx.fxml,javafx.media \
    --enable-native-access=javafx.graphics,javafx.media,ALL-UNNAMED \
    -cp '/app/cipherquest.jar:/app/lib/*' com.cipherquest.Main
