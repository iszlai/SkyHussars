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

import com.codebetyars.skyhussars.engine.TerrainManager;
import com.codebetyars.skyhussars.engine.World;
import com.codebetyars.skyhussars.engine.ai.AIPilot;
import com.codebetyars.skyhussars.engine.physics.environment.AtmosphereImpl;
import com.codebetyars.skyhussars.engine.physics.environment.Environment;
import com.codebetyars.skyhussars.engine.plane.Plane;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldThread extends TimerTask {

    private final static Logger logger = LoggerFactory.getLogger(WorldThread.class);

    private final List<Plane> planes;
    private final float tpf;

    private final Environment environment = new Environment(10, new AtmosphereImpl());
    private final List<AIPilot> aiPilots = new LinkedList<>();
    private final World world;

    public WorldThread(List<Plane> planes, int ticks, TerrainManager terrainManager) {
        this.planes = planes;
        planes.stream().forEach((plane) -> {
            if (!plane.planeMissionDescriptor().player()) {
                aiPilots.add(new AIPilot(plane));
            }
        });

        world = new World(planes, terrainManager);
        tpf = (float) 1 / (float) ticks;
    }

    private final AtomicLong cycle = new AtomicLong(0);

    public long cycle() {
        return cycle.get();
    }

    @Override
    public void run() {
        synchronized (this) {
            planes.parallelStream().forEach(plane -> {
                plane.updatePlanePhysics(tpf, environment);
            });
        }
        aiPilots.parallelStream().forEach(aiPilot -> {
            aiPilot.update(world);
        });
        cycle.incrementAndGet();
    }

    public synchronized void updatePlaneLocations() {
        planes.stream().forEach(plane -> {
            plane.update(tpf);
        });
    }

}
