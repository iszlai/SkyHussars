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
package com.codebetyars.skyhussars.engine.plane;

import com.codebetyars.skyhussars.engine.DataManager;
import com.codebetyars.skyhussars.engine.ModelManager;
import com.codebetyars.skyhussars.engine.plane.instruments.BarometricAltimeter;
import com.codebetyars.skyhussars.engine.plane.instruments.Instruments;
import com.codebetyars.skyhussars.engine.sound.AudioHandler;
import com.codebetyars.skyhussars.engine.sound.SoundManager;
import com.codebetyars.skyhussars.engine.weapons.ProjectileManager;
import com.jme3.audio.AudioNode;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlaneFactory {

    @Autowired
    private DataManager dataManager;
    
    @Autowired
    private ModelManager modelManager;

    @Autowired
    private SoundManager soundManager;

    @Autowired
    private ProjectileManager projectileManager;

    public Plane createPlane(String planeType) {
        PlaneDescriptor planeDescriptor = dataManager.planeRegistry().planeDescriptor(planeType);
        Spatial model = modelManager.model("p80", "p80_material").clone();
        model.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        AudioHandler engineSound = soundManager.sound("engine");
        AudioHandler gunSound = soundManager.sound("gun");
        Box box = new Box(6f, 1f, 4f);
        Instruments instruments = new Instruments(new BarometricAltimeter(0));
        Plane plane = new Plane(
                model, planeDescriptor, engineSound,
                gunSound, projectileManager,dataManager.getCockpit(),instruments);
        plane.fireEffect(dataManager.fireEffect());
        return plane;
    }
}
