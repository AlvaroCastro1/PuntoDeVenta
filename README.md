para poder crear un exe debmos hacer lo siguiente
1. `mvn clean package`

2. crear el `.exe`

```bash
jpackage `
  --type exe `
  --name "DulceriaTeddy" `
  --input target `
  --main-jar "Dulceria Teddy-jar-with-dependencies.jar" `
  --main-class dulceria.app.App `
  --icon icon.ico `
  --java-options "--module-path C:\javafx-sdk-21.0.6\lib --add-modules javafx.controls,javafx.fxml" 
```
