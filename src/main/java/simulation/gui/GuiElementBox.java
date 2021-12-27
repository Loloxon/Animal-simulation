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
        if(energy > -1){
//            ToggleButton animalButton = new ToggleButton();
//            animalButton.
            colorAdjust.setBrightness(Math.max((startEnergy - energy - 15) / (startEnergy),-0.5));
        }
        view.setEffect(colorAdjust);
    }
    public void update(Image image, double energy){
        this.energy = (int) energy;
        view.setImage(image);
        ColorAdjust colorAdjust = new ColorAdjust();
        if(energy > -1){
            colorAdjust.setBrightness(Math.max((startEnergy - energy - 15) / (startEnergy),-0.5));
        }
        if(energy == -2){
            colorAdjust.setBrightness(-1);
        }
        view.setEffect(colorAdjust);
    }
    @Override
    public void start(Stage newStage){

    }
}
