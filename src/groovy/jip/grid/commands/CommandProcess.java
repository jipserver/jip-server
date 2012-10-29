/*
 * Copyright (C) 2012 Thasso Griebel
 *
 * This file is part of JIP.
 *
 * JIP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JIP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JIP.  If not, see <http://www.gnu.org/licenses/>.
 */

package jip.grid.commands;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface CommandProcess {
    InputStream getInputStream();
    InputStream getErrorStream();
    OutputStream getOutputStream();
    int waitFor();
    void destroy();
}
