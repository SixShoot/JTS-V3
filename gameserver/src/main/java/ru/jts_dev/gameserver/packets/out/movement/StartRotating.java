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

package ru.jts_dev.gameserver.packets.out.movement;

import ru.jts_dev.common.packets.OutgoingMessageWrapper;
import ru.jts_dev.gameserver.model.GameCharacter;

/**
 * @author Java-man
 * @since 26.01.2016
 */
public class StartRotating extends OutgoingMessageWrapper {
    private final int charId;
    private final int degree;
    private final int side;
    private final int speed;

    public StartRotating(GameCharacter character, int degree, int side, int speed) {
        charId = character.getObjectId();
        this.degree = degree;
        this.side = side;
        this.speed = speed;
    }

    @Override
    public final void write() {
        writeByte(0x7a);
        writeInt(charId);
        writeInt(degree);
        writeInt(side); // side (1 = right, -1 = left)
        writeInt(speed);
    }
}
