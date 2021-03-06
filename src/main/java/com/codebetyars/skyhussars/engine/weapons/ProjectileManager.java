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
package com.codebetyars.skyhussars.engine.weapons;

import com.codebetyars.skyhussars.engine.DataManager;
import com.codebetyars.skyhussars.engine.plane.Plane;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ProjectileManager {

    private final static Logger logger = LoggerFactory.getLogger(ProjectileManager.class);

    @Autowired
    private Node rootNode;

    @Autowired
    private DataManager dataManager;

    private List<Projectile> projectiles = new ArrayList<>();

    private List<Geometry> projectileGeometries = new LinkedList<>();

    public void addProjectile(Bullet projectile) {
        Geometry newGeometry = dataManager.getBullet();
        projectileGeometries.add(newGeometry);
        rootNode.attachChild(newGeometry);
        newGeometry.move(projectile.getLocation());
        projectiles.add(projectile);
    }

    public void update(float tpf) {
        logger.info("Current projectiles: {}", projectileGeometries.size());
        Iterator<Geometry > geomIterator = projectileGeometries.iterator();
        projectiles.parallelStream().forEach(projectile -> projectile.update(tpf));
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile projectile = it.next();
            if (!projectile.isLive()) {
                it.remove();
                if (geomIterator.hasNext()) {
                    Geometry geom = geomIterator.next();
                    rootNode.detachChild(geom);
                    geomIterator.remove();
                }
            } else {
                if (geomIterator.hasNext()) {
                    Geometry geom = geomIterator.next();
                    geom.setLocalTranslation(projectile.getLocation());
                    Vector3f direction = projectile.getVelocity().normalize();
                    geom.lookAt(direction, Vector3f.UNIT_Y);
                }
            }
        }
    }

    public void checkCollision(Plane plane) {
        Node planeNode = plane.planeGeometry().modelNode();
        projectileGeometries.forEach(projectile -> {
            CollisionResults collisionResults = new CollisionResults();
            if (projectile.collideWith(planeNode.getWorldBound(), collisionResults) > 0) {
                if (collisionResults.size() > 0) {
                    plane.hit();
                }
            }
        });
    }
}
