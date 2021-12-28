package simulation.gui;

import javafx.application.Application;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


public class GuiElementBox extends Application {
    //    Image image;
//    VBox vbox = new VBox();
    ImageView view = new ImageView();
    int energy;
    int startEnergy;
    public GuiElementBox(int S, Image image, double energy, int startEnergy){
        this.energy = (int) energy;
        this.startEnergy = startEnergy;
        view.setImage(image);
        view.setFitHeight(S);
        view.setFitWidth(S);
        ColorAdjust colorAdjust = new ColorAdjust();
        switch ((int) energy) {
            case -1 -> colorAdjust.setBrightness(0.25);
            case -2 -> colorAdjust.setBrightness(-1);
            case -3 -> colorAdjust.setBrightness(-0.25);
            default -> colorAdjust.setBrightness(Math.max((startEnergy * (0.90) - energy) / (startEnergy), -0.7));
        }
        view.setEffect(colorAdjust);
    }
    public void update(Image image, double energy){
        this.energy = (int) energy;
        view.setImage(image);
        ColorAdjust colorAdjust = new ColorAdjust();
        switch ((int) energy) {
            case -1 -> colorAdjust.setBrightness(0.25);
            case -2 -> colorAdjust.setBrightness(-1);
            case -3 -> colorAdjust.setBrightness(-0.25);
            default -> colorAdjust.setBrightness(Math.max((startEnergy * (0.90) - energy) / (startEnergy), -0.5));
        }
        view.setEffect(colorAdjust);
    }
    @Override
    public void start(Stage newStage){

    }
}
