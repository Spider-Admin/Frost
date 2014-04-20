/*
  SettingsUpdater.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost;

/**
 * All classes that have settings values that have to be updated before
 * the settings are saved to disk must register themselves with SettingsClass
 * and implement this interface.
 */
public interface SettingsUpdater {

    /**
     * This is the callback method that will be called from Settings class
     * before saving its contents to disk.
     */
    void updateSettings();
}
