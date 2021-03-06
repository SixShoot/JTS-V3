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

package ru.jts_dev.common.packets;

import io.netty.buffer.ByteBuf;
import org.springframework.integration.support.MutableMessageHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.ByteOrder;

import static io.netty.buffer.Unpooled.buffer;
import static ru.jts_dev.common.packets.IncomingMessageWrapper.EOS;

/**
 * @author Camelion
 * @since 30.11.15
 */
public abstract class OutgoingMessageWrapper implements Message<ByteBuf> {
    boolean static_;
    ByteBuf buffer;
    MessageHeaders headers;

    protected OutgoingMessageWrapper() {
        buffer = buffer().order(ByteOrder.LITTLE_ENDIAN);
        headers = new MutableMessageHeaders(null);
    }

    OutgoingMessageWrapper(final boolean static_) {
        this();
        this.static_ = static_;
    }

    @Override
    public final ByteBuf getPayload() {
        if (static_)
            throw new UnsupportedOperationException("getPayload() not supported for static message");
        return buffer;
    }

    @Override
    public final MessageHeaders getHeaders() {
        if (static_)
            throw new UnsupportedOperationException("getHeaders() not supported for static message");
        return headers;
    }

    /**
     * write data to buffer before send
     */
    public abstract void write();

    protected final void writeByte(final int i) {
        buffer.writeByte(i);
    }

    /**
     * write boolean to byte, true == 0x01, false = 0x00
     *
     * @param b - boolean value for writing
     */
    protected final void writeBoolean(final boolean b) {
        buffer.writeBoolean(b);
    }

    protected final void writeShort(final int i) {
        buffer.writeShort(i);
    }

    protected final void writeInt(final int i) {
        buffer.writeInt(i);
    }

    protected final void writeLong(final long l) {
        buffer.writeLong(l);
    }

    protected final void writeDouble(final double d) {
        buffer.writeDouble(d);
    }

    protected final void writeBytes(final byte... data) {
        buffer.writeBytes(data);
    }

    protected final void writeZero(final int length) {
        buffer.writeZero(length);
    }

    protected final void writeString(final CharSequence cs) {
        for (int i = 0; i < cs.length(); i++) {
            buffer.writeChar(cs.charAt(i));
        }
        buffer.writeChar(EOS);
    }

    public final boolean isStatic() {
        return static_;
    }
}
