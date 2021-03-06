/*
 * Copyright (c) 2016, ZoltanTheHun
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.codebetyars.skyhussars.engine.gamestates;

import com.codebetyars.skyhussars.engine.sound.SoundManager;
import com.codebetyars.skyhussars.engine.*;
import com.codebetyars.skyhussars.engine.plane.Plane;
import com.codebetyars.skyhussars.engine.weapons.ProjectileManager;
import com.jme3.scene.Node;
import de.lessvoid.nifty.elements.render.TextRenderer;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissionState implements GameState {

    private Pilot player;
    private final CameraManager cameraManager;
    private final TerrainManager terrainManager;
    private final DayLightWeatherManager dayLightWeatherManager;
    private final ProjectileManager projectileManager;
    private boolean paused = false;
    private boolean ended = false;
    private final List<Plane> planes;
    private List<Pilot> pilots;
    private final SoundManager soundManager;
    private final static Logger logger = LoggerFactory.getLogger(MissionState.class);
    private final WorldThread worldThread;
    private Timer timer;
    private GameState nextState = this;
    private final Node rootNode;
    private final Sky sky;

    public MissionState(List<Plane> planes, ProjectileManager projectileManager, SoundManager soundManager,
            CameraManager cameraManager, TerrainManager terrainManager,
            DayLightWeatherManager dayLightWeatherManager,Node rootNode, Sky sky) {
        this.rootNode = rootNode;
        this.sky = sky;
        this.planes = planes;
        this.projectileManager = projectileManager;
        this.cameraManager = cameraManager;
        this.terrainManager = terrainManager;
        this.dayLightWeatherManager = dayLightWeatherManager;
        this.soundManager = soundManager;
        planes.stream().forEach((plane) -> {
            if (plane.planeMissionDescriptor().player()) {
                player = new Pilot(plane);
            }
        });
        initiliazePlayer();
        worldThread = new WorldThread(planes, ticks, terrainManager);
    }
    private final int ticks = 30;
    private int cycles = 0;

    public Pilot player() {
        return player;
    }

    /**
     * This method is used to initialize a scene
     */
    public void initializeScene() {
        initiliazePlayer();
        ended = false;
    }

    private void startWorldThread() {
        if (timer == null) {
            timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    worldThread.run();
                }
            }, 0, 1000 / ticks);  // 16 = 60 tick, 50 = 20 tick
        }
    }

    private void stopWorldThread() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void initiliazePlayer() {
        Plane plane = player.plane();
        cameraManager.moveCameraTo(plane.getLocation());
        cameraManager.followWithCamera(plane.planeGeometry());
        cameraManager.init();
    }

    private TextRenderer speedoMeterUI;

    public synchronized void speedoMeterUI(TextRenderer speedoMeterUI) {
        this.speedoMeterUI = speedoMeterUI;
    }

    @Override
    public synchronized GameState update(float tpf) {
        cycles++;
        long millis = System.currentTimeMillis();
        soundManager.update();
        if (!paused && !ended) {
            startWorldThread();
            updatePlanes(tpf);
            projectileManager.update(tpf);
            if (player.plane().crashed()) {
                ended = true;
            }
            /* take another look at it later to get rid of a chance of a null reference */
            if(speedoMeterUI != null) speedoMeterUI.setText(player.plane().getSpeedKmH() + "km/h");
        } else {
            stopWorldThread();
            soundManager.muteAllSounds();
        }
        cameraManager.update(tpf);
       // millis = System.currentTimeMillis() - millis;
     /*   logger.info("Gamestate update complete in " + millis);
         logger.info("Current cycle: {}", worldThread.cycle());
         logger.info("Current render cycle: {}", cycles);*/

        return nextState;
    }

    public synchronized void switchState(GameState state) {
        nextState = state;
    }

    private void updatePlanes(float tpf) {
        worldThread.updatePlaneLocations();
        planes.forEach(plane -> {
            //plane.update(tpf);
            if (terrainManager.checkCollisionWithGround(plane)) {
                plane.crashed(true);
            };
            plane.updateSound();
            projectileManager.checkCollision(plane);
        });
    }

    @Override
    public void close() {
        if (timer != null) {
            timer.cancel();
        }
        sky.disableSky();
        soundManager.muteAllSounds();
        soundManager.update();
        rootNode.detachAllChildren();
        rootNode.getLocalLightList().clear();
        rootNode.getWorldLightList().clear();
        /*rootNode.forceRefresh(true, true, true);*/
    }

    @Override
    public void initialize() {
        initializeScene();
        ended = false;
    }

    public void paused(boolean paused) {
        this.paused = paused;
    }

    public boolean paused() {
        return paused;
    }

    public void reinitPlayer() {
        player.plane().setLocation(0, 0);
        player.plane().setHeight(3000);
        player.plane().crashed(false);
        initiliazePlayer();
        ended = false;
    }

}
