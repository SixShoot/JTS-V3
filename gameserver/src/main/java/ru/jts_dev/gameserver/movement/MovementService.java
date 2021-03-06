/*
 * Copyright (c) 2015, 2016, 2017 JTS-Team authors and/or its affiliates. All rights reserved.
 *
 * This file is part of JTS-V3 Project.
 *
 * JTS-V3 Project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTS-V3 Project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JTS-V3 Project.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.jts_dev.gameserver.movement;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.jts_dev.gameserver.model.GameCharacter;
import ru.jts_dev.gameserver.model.GameSession;
import ru.jts_dev.gameserver.packets.out.MoveToLocation;
import ru.jts_dev.gameserver.service.BroadcastService;
import ru.jts_dev.gameserver.util.RotationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Java-man
 * @since 13.12.2015
 */
@Service
public class MovementService {
    private static final long MOVE_TASK_INTERVAL_MILLIS = 200L;
    private static final double MOVE_SPEED_MULTIPLIER = MOVE_TASK_INTERVAL_MILLIS / 1000.0D;

    private final Logger logger = LoggerFactory.getLogger(MovementService.class);

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;
    @Autowired
    private BroadcastService broadcastService;
    @Autowired
    private RotationUtils rotationUtils;

    private final Map<Integer, ScheduledFuture<?>> tasks = new HashMap<>();

    public void moveTo(final GameSession session, final GameCharacter character, final Vector3D end) {
        final Vector3D start = character.getVector3D();

        final Line line = new Line(start, end, 1.0D);
        final double distance = start.distance(end);
        final Vector3D direction = line.getDirection();
        final double angle = Vector3D.angle(start, direction);
        final double degree = FastMath.toDegrees(angle);
        //final Rotation rotation = new Rotation(start, end);
        //final double angle = rotation.getAngle();
        logger.info("MovementService angle = {}", angle);
        logger.info("MovementService degree = {}", degree);
        character.setVector3D(start);
        final int clientHeading = rotationUtils.convertAngleToClientHeading((int) degree);
        //broadcastService.send(session, new StartRotating(character, clientHeading, 0, 200));
        character.setAngle(degree);
        character.setMoving(true);

        final Runnable moveTask = new MoveTask(session, character, start, end, direction, distance, true);
        ScheduledFuture<?> future = scheduledExecutorService.schedule(moveTask, MOVE_TASK_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS);
        tasks.put(character.getObjectId(), future);
    }

    public void stopMovement(final GameCharacter character)
    {
        ScheduledFuture<?> future = tasks.remove(character.getObjectId());
        if(future != null)
        {
            future.cancel(true);
        }
    }

    private final class MoveTask implements Runnable {
        private final GameSession session;
        private final GameCharacter character;
        private final Vector3D start;
        private final Vector3D end;
        private final Vector3D direction;
        private final double distance;
        private final boolean first;

        private MoveTask(final GameSession session, final GameCharacter character, final Vector3D start,
                         final Vector3D end, final Vector3D direction, final double distance, final boolean first) {
            this.session = session;
            this.character = character;
            this.start = start;
            this.end = end;
            this.direction = direction;
            this.distance = distance;
            this.first = first;
        }

        @Override
        public void run() {
            if (character.isMoving()) {
                final double speed = 200.0D; // TODO speed
                final Vector3D temp = character.getVector3D().add(speed * MOVE_SPEED_MULTIPLIER, direction);
                if (first)
                    broadcastService.send(session, new MoveToLocation(character, end));
                character.setVector3D(temp);

                if (start.distance(character.getVector3D()) >= distance) {
                    final int clientHeading = rotationUtils.convertAngleToClientHeading((int) character.getAngle());
                    logger.info("MovementService clientHeading = {}", clientHeading);
                    //broadcastService.send(session, new StopMove(character, clientHeading));
                    character.setVector3D(end);
                    character.setMoving(false);
                } else {
                    final Runnable moveTask = new MoveTask(session, character, start, end, direction, distance, false);
                    scheduledExecutorService.schedule(moveTask, MOVE_TASK_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
