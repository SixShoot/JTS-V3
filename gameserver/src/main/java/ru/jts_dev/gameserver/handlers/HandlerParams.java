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

package ru.jts_dev.gameserver.handlers;

import ru.jts_dev.gameserver.model.GameCharacter;
import ru.jts_dev.gameserver.model.GameSession;

import java.util.*;

/**
 * Parameter container for command handlers.
 * Contains info about active character, command name and additional parameters of command was sent by player.
 *
 * @author Yorie, AN3O
 */
public class HandlerParams<TCommandType> {
    protected final GameSession session;
    protected final GameCharacter character;
    protected final ArrayList<String> args = new ArrayList<>();
    protected final Map<String, String> queryArgs = new HashMap<>();
    protected TCommandType command;

    /**
     * @param character Active character.
     * @param command   Command ID.
     * @param args      Additional parameters.
     */
    public HandlerParams(GameSession session, GameCharacter character, TCommandType command, List<String> args,
                         Map<String, String> queryArgs) {
        this(session, character, command);
        this.args.addAll(args);
        this.args.trimToSize();

        if (queryArgs != null) {
            this.queryArgs.putAll(queryArgs);
        }
    }

    public HandlerParams(GameSession session, GameCharacter character, TCommandType command, String[] args,
                         Map<String, String> queryArgs) {
        this(session, character, command);
        Collections.addAll(this.args, args);

        if (queryArgs != null) {
            this.queryArgs.putAll(queryArgs);
        }
    }

    /**
     * @param character Active character.
     * @param command   Command ID.
     */
    public HandlerParams(GameSession session, GameCharacter character, TCommandType command) {
        this.session = session;
        this.character = character;
        this.command = command;
    }

    /**
     * Parses query string. The query string is web-like command.
     * For example: "dynamic_quest_accept?dquest_id=1&step=2". There is dquest & step is query arguments, 1 & 2 is arguments values.
     *
     * @param query Query string.
     * @return List of mapped parameters.
     */
    public static Map<String, String> parseQueryArguments(String query) {
        Map<String, String> args = new HashMap<>();
        if (!query.contains("?")) {
            return args;
        }

        query = query.substring(query.indexOf('?'));

        if (query.length() <= 1) {
            return args;
        }

        query = query.substring(1);

        for (String arg : query.split("&")) {
            // Non-value argument
            if (!arg.contains("=")) {
                args.put(arg, null);
            } else {
                int equationPos = arg.indexOf('=');
                args.put(arg.substring(0, equationPos), arg.substring(equationPos + 1));
            }
        }

        return args;
    }

    public static CommandWrapper parseCommand(String command) {
        int commandEndIndex;

        commandEndIndex = command.indexOf("?");
        if (commandEndIndex < 0) {
            commandEndIndex = command.indexOf(" ");

            if (commandEndIndex < 0) {
                commandEndIndex = command.length();
            }
        }

        String opcode = command.substring(0, commandEndIndex);
        List<String> args;
        Map<String, String> queryArgs = new HashMap<>();

        String rest = command.substring(commandEndIndex, command.length());

        if (rest.startsWith("?")) {
            rest = rest.substring(1);
            // Split query ?foo=foo&bar=bar into array {'foo=foo', 'bar=bar', ...}
            String[] tokens = rest.split("&");
            for (String arg : tokens) {
                // Non-value argument (example: ?foo=foo&bar - bar is non-value argument)
                if (!arg.contains("=")) {
                    queryArgs.put(arg, null);
                } else {
                    // Split each argument of tokens array into array {'foo', 'foo'} and etc.
                    String[] parts = arg.split("=");
                    String key = parts[0].trim();
                    String value = (parts.length > 1) ? parts[1] : "";
                    queryArgs.put(key, value.trim());
                }
            }
        } else {
            queryArgs = Collections.emptyMap();
        }

        args = parseArgs(rest);

        return new CommandWrapper(opcode, args, queryArgs);
    }

    /**
     * Does string parse separating string into tokens by spaces with trim and adds resulted strings to list.
     * For example, string "delete foo   bar" will be tokenized into {"delete", "foo", "bar"}
     *
     * @param params Parameters string.
     * @return List of tokenized parameters.
     */
    public static List<String> parseArgs(String params) {
        List<String> list = new ArrayList<>();

        if (params == null) {
            return list;
        }

        for (String param : params.split(" ")) {
            if (!param.trim().isEmpty()) {
                list.add(param);
            }
        }
        return list;
    }

    public GameSession getSession() {
        return session;
    }

    /**
     * @return Active player for this command.
     */
    public GameCharacter getCharacter() {
        return character;
    }

    /**
     * @return This command name.
     */
    public TCommandType getCommand() {
        return command;
    }

    /**
     * Re-sets command opcode.
     *
     * @param command Command opcode.
     */
    public void setCommand(TCommandType command) {
        this.command = command;
    }

    /**
     * Arguments is tokens that written right after command name, such arguments separated with space.
     * Example: "invite Player". There is "Player" is first argument.
     *
     * @return Additional parameters list.
     */
    public List<String> getArgs() {
        return args;
    }

    /**
     * Query arguments is web-like arguments.
     * Example: "dynamic_quest_accept?dquest_id=1&step=2". There is dquest & step is query arguments, 1 & 2 is arguments values.
     *
     * @return Additional parameters list.
     */
    public Map<String, String> getQueryArgs() {
        return queryArgs;
    }
}
