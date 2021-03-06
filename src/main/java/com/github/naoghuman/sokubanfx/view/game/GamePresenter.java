/*
 * Copyright (C) 2016 Naoghuman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.naoghuman.sokubanfx.view.game;

import com.github.naoghuman.lib.action.api.ActionFacade;
import com.github.naoghuman.lib.action.api.IRegisterActions;
import com.github.naoghuman.lib.action.api.TransferData;
import com.github.naoghuman.lib.logger.api.LoggerFacade;
import com.github.naoghuman.lib.preferences.api.PreferencesFacade;
import com.github.naoghuman.sokubanfx.configuration.application.IActionConfiguration;
import com.github.naoghuman.sokubanfx.configuration.view.game.IGameConfiguration;
import com.github.naoghuman.sokubanfx.configuration.map.IMapConfiguration;
import com.github.naoghuman.sokubanfx.map.geometry.Coordinates;
import com.github.naoghuman.sokubanfx.map.geometry.EDirection;
import com.github.naoghuman.sokubanfx.map.MapFacade;
import com.github.naoghuman.sokubanfx.map.animation.EAnimation;
import com.github.naoghuman.sokubanfx.map.model.MapModel;
import com.github.naoghuman.sokubanfx.map.movement.MovementResult;
import com.github.naoghuman.sokubanfx.map.movement.EMovement;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.elusive.Elusive;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * 
 * @author Naoghuman
 */
public class GamePresenter implements Initializable, IActionConfiguration, IRegisterActions {
    
    @FXML private AnchorPane apGameArea;
    @FXML private Button bMovePlayerDown;
    @FXML private Button bMovePlayerLeft;
    @FXML private Button bMovePlayerRight;
    @FXML private Button bMovePlayerUp;
    @FXML private Button bResetMap;
    @FXML private VBox vbMap;
    
    private boolean listenToKeyEvents = Boolean.TRUE;
    
    private MapModel actualMapModel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LoggerFacade.INSTANCE.info(this.getClass(), "Initialize GamePresenter"); // NOI18N
        
        assert (apGameArea != null)   : "fx:id=\"apGameArea\" was not injected: check your FXML file 'Game.fxml'."; // NOI18N
        assert (bMovePlayerDown != null)  : "fx:id=\"bMovePlayerDown\" was not injected: check your FXML file 'Game.fxml'."; // NOI18N
        assert (bMovePlayerLeft != null)  : "fx:id=\"bMovePlayerLeft\" was not injected: check your FXML file 'Game.fxml'."; // NOI18N
        assert (bMovePlayerRight != null) : "fx:id=\"bMovePlayerRight\" was not injected: check your FXML file 'Game.fxml'."; // NOI18N
        assert (bMovePlayerUp != null)    : "fx:id=\"bMovePlayerUp\" was not injected: check your FXML file 'Game.fxml'."; // NOI18N
        assert (bResetMap != null)    : "fx:id=\"bResetMap\" was not injected: check your FXML file 'Game.fxml'."; // NOI18N
        assert (vbMap != null)        : "fx:id=\"vbMap\" was not injected: check your FXML file 'Game.fxml'."; // NOI18N
        
        this.initializeButtons();
        this.initializePreferences();
        
        this.loadActualMap();
        this.displayMap();
    }
    
    private void initializeButtons() {
        LoggerFacade.INSTANCE.info(this.getClass(), "Initialize buttons"); // NOI18N
        
        this.initializeButton(bMovePlayerDown, Elusive.ARROW_DOWN, "Move player down"); // NOI18N
        this.initializeButton(bMovePlayerLeft, Elusive.ARROW_LEFT, "Move player left"); // NOI18N
        this.initializeButton(bMovePlayerRight, Elusive.ARROW_RIGHT, "Move player right"); // NOI18N
        this.initializeButton(bMovePlayerUp, Elusive.ARROW_UP, "Move player up"); // NOI18N
        
        this.initializeButton(bResetMap, Elusive.REFRESH, "Reset the map"); // NOI18N
    }
    
    private void initializeButton(Button btn, Ikon icon, String tooltip) {
        final FontIcon fi = new FontIcon(icon);
        fi.setIconSize(24);
        btn.setGraphic(fi);
        btn.setText(null);
        btn.setTooltip(new Tooltip(tooltip));
    }
    
    private void initializePreferences() {
        LoggerFacade.INSTANCE.info(this.getClass(), "Initialize Preferences"); // NOI18N
        
        // GameView is initialize
        PreferencesFacade.INSTANCE.putBoolean(
                IGameConfiguration.PROP__GAMEVIEW_IS_INITIALZE,
                Boolean.TRUE);
        
        // Listen in GameView on KeyEvents
        PreferencesFacade.INSTANCE.putBoolean(
                IGameConfiguration.PROP__KEY_RELEASED__FOR_GAMEVIEW,
                Boolean.TRUE);
    }
    
    private void displayMap() {
        LoggerFacade.INSTANCE.debug(this.getClass(), "Display Map"); // NOI18N
        
        vbMap.getChildren().clear();
        vbMap.getChildren().add(this.getLabel("Map: " + actualMapModel.getLevel())); // NOI18N
        vbMap.getChildren().add(this.getLabel("")); // NOI18N
        
        final ObservableList<String> mapAsStrings = MapFacade.INSTANCE.convertMapCoordinatesToStrings(actualMapModel);
        mapAsStrings.stream()
                .forEach(line -> {
                    vbMap.getChildren().add(this.getLabel(line));
                });
    }

    private void evaluateIsMapFinish(boolean isMapFinish) {
        LoggerFacade.INSTANCE.debug(this.getClass(), "Evaluate is Map finish"); // NOI18N
        
        // Keep going :)
        if (!isMapFinish) {
            return;
        }
        
        // Map is finish !!
        System.out.println("---------------------> map is finish :))");
        // no movement
        // keyevents=false
        
        // map-level += 1
        final int actualMap = PreferencesFacade.INSTANCE.getInt(
                IMapConfiguration.PROP__ACTUAL_MAP,
                IMapConfiguration.PROP__ACTUAL_MAP__DEFAULT_VALUE);
        PreferencesFacade.INSTANCE.putInt(
                IMapConfiguration.PROP__ACTUAL_MAP,
                actualMap + 1);
        
        // play animation
        
        // load next map
        this.loadActualMap();
        this.displayMap();
        
        // keyevents= true
    }
    
    private void evaluatePlayerMoveTo(MovementResult movementResult) {
        final EAnimation animation = movementResult.getAnimation();
        if (
                animation.equals(EAnimation.REALLY_GREAT)
                || animation.equals(EAnimation.WHAT_HAPPEN)
        ) {
            LoggerFacade.INSTANCE.trace(this.getClass(), "TODO play animation: " + animation); // NOI18N
            
        }
        
        final EMovement movement = movementResult.getMovement();
        if (movement.equals(EMovement.NONE)) {
            return;
        }

        // Update player
        final Coordinates player = actualMapModel.getPlayer();
        final Coordinates playerToMove = movementResult.getPlayerToMove();
        player.setX(playerToMove.getTranslateX());
        player.setY(playerToMove.getTranslateY());

        if (movement.equals(EMovement.PLAYER)) {
            this.displayMap();
            return;
        }

        // Update box
        final Coordinates boxToMove = movementResult.getBoxToMove();
        final ObservableList<Coordinates> boxes = actualMapModel.getBoxes();
        boxes.stream()
                .filter(box -> {
                    final boolean shouldBoxUpdate = 
                            box.getX() == boxToMove.getX()
                            && box.getY() == boxToMove.getY();
                    return shouldBoxUpdate;
                })
                .findFirst()
                .ifPresent(box -> {
                    box.setX(boxToMove.getTranslateX());
                    box.setY(boxToMove.getTranslateY());
                });

        this.displayMap();
    }
    
    private Label getLabel(String text) {
        final Label label = new Label(text);
        label.setFont(new Font("Monospaced Regular", 16.0d));
        
        return label;
    }
    
    private void loadActualMap() {
        LoggerFacade.INSTANCE.debug(this.getClass(), "Load actual Map"); // NOI18N
        
        final int actualMap = PreferencesFacade.INSTANCE.getInt(
                IMapConfiguration.PROP__ACTUAL_MAP,
                IMapConfiguration.PROP__ACTUAL_MAP__DEFAULT_VALUE);
        
        actualMapModel = MapFacade.INSTANCE.loadMap(actualMap);
    }

    @Override
    public void registerActions() {
        LoggerFacade.INSTANCE.debug(this.getClass(), "Register actions in GamePresenter"); // NOI18N
        
        this.registerOnActionDisplayMap();
        this.registerOnKeyReleased();
    }
    
    private void registerOnActionDisplayMap() {
        LoggerFacade.INSTANCE.debug(this.getClass(), "Register on action display Map"); // NOI18N
        
        ActionFacade.INSTANCE.register(
                ON_ACTION__DISPLAY_MAP,
                (ActionEvent event) -> {
                    this.displayMap();
                }
        );
    }

    private void registerOnKeyReleased() {
        LoggerFacade.INSTANCE.debug(this.getClass(), "Register on KeyReleased"); // NOI18N
        
        ActionFacade.INSTANCE.register(
                ON_ACTION__KEY_RELEASED__FOR_GAME,
                (ActionEvent event) -> {
                    final TransferData transferData = (TransferData) event.getSource();
                    final KeyEvent keyEvent = (KeyEvent) transferData.getObject();
                    this.onKeyRelease(keyEvent);
                }
        );
    }
    
    public void onActionButtonDown() {
        LoggerFacade.INSTANCE.debug(this.getClass(), "On action Button down"); // NOI18N
        
        final MovementResult movementResult = MapFacade.INSTANCE.playerMoveTo(EDirection.DOWN, actualMapModel);
        this.evaluatePlayerMoveTo(movementResult);
        
        final boolean checkIsMapFinished = movementResult.isMapFinish();
        if (checkIsMapFinished) {
            final boolean isMapFinish = MapFacade.INSTANCE.isMapFinish(actualMapModel);
            this.evaluateIsMapFinish(isMapFinish);
        }
    }
    
    public void onActionButtonLeft() {
        LoggerFacade.INSTANCE.debug(this.getClass(), "On action Button left"); // NOI18N
        
        final MovementResult movementResult = MapFacade.INSTANCE.playerMoveTo(EDirection.LEFT, actualMapModel);
        this.evaluatePlayerMoveTo(movementResult);
        
        final boolean checkIsMapFinished = movementResult.isMapFinish();
        if (checkIsMapFinished) {
            final boolean isMapFinish = MapFacade.INSTANCE.isMapFinish(actualMapModel);
            this.evaluateIsMapFinish(isMapFinish);
        }
    }
    
    public void onActionButtonResetMap() {
        LoggerFacade.INSTANCE.debug(this.getClass(), "On action Button reset Map"); // NOI18N
        
        listenToKeyEvents = Boolean.FALSE;
        this.loadActualMap();
        this.displayMap();
        listenToKeyEvents = Boolean.TRUE;
    }
    
    public void onActionButtonRight() {
        LoggerFacade.INSTANCE.debug(this.getClass(), "On action Button right"); // NOI18N
        
        final MovementResult movementResult = MapFacade.INSTANCE.playerMoveTo(EDirection.RIGHT, actualMapModel);
        this.evaluatePlayerMoveTo(movementResult);
        
        final boolean checkIsMapFinished = movementResult.isMapFinish();
        if (checkIsMapFinished) {
            final boolean isMapFinish = MapFacade.INSTANCE.isMapFinish(actualMapModel);
            this.evaluateIsMapFinish(isMapFinish);
        }
    }
    
    public void onActionButtonUp() {
        LoggerFacade.INSTANCE.debug(this.getClass(), "On action Button up"); // NOI18N
        
        final MovementResult movementResult = MapFacade.INSTANCE.playerMoveTo(EDirection.UP, actualMapModel);
        this.evaluatePlayerMoveTo(movementResult);
        
        final boolean checkIsMapFinished = movementResult.isMapFinish();
        if (checkIsMapFinished) {
            final boolean isMapFinish = MapFacade.INSTANCE.isMapFinish(actualMapModel);
            this.evaluateIsMapFinish(isMapFinish);
        }
    }
    
    /*
     * KeyEvents in GameView
     * W UP        -> move up
     * S DOWN      -> move down
     * A LEFT      -> move left
     * D RIGHT     -> move right
     * ENTER SPACE -> reset map
     * 
     * BACKSPACE   -> not needed - catched in ApplicationView (shows the menu)
     * ESC         -> not needed - catched in ApplicationView (close the application)
     */
    private void onKeyRelease(KeyEvent keyEvent) {
        final KeyCode keyCode = keyEvent.getCode();
        LoggerFacade.INSTANCE.debug(this.getClass(), "On KeyRelease: " + keyCode); // NOI18N
        
        if (
                keyCode.equals(KeyCode.ENTER)
                || keyCode.equals(KeyCode.SPACE)
        ) {
            this.onActionButtonResetMap();
            return;
        }
        
        if (!listenToKeyEvents) {
            return;
        }
        
        switch(keyCode) {
            case W:
            case UP:    { this.onActionButtonUp();    break; }
            case S:
            case DOWN:  { this.onActionButtonDown();  break; }
            case A:
            case LEFT:  { this.onActionButtonLeft();  break; }
            case D:
            case RIGHT: { this.onActionButtonRight(); break; }
        }
    }
    
}
