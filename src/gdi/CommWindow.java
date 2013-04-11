/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Allows communication between NPCs and the player.
 * Nathan Wiehoff
 */
package gdi;

import celestial.Ship.Ship;
import gdi.component.AstralList;
import gdi.component.AstralWindow;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import lib.AstralMessage;

public class CommWindow extends AstralWindow {
    
    AstralList messageLog = new AstralList(this);
    AstralList replyList = new AstralList(this);
    AstralList messageDisplay = new AstralList(this);
    protected Ship ship;
    
    public CommWindow() {
        super();
        generate();
    }
    
    private void generate() {
        backColor = windowGrey;
        //size this window
        width = 300;
        height = 300;
        setVisible(true);
        //setup the message list
        messageLog.setX(0);
        messageLog.setY(height/2);
        messageLog.setWidth(width / 2 - 1);
        messageLog.setHeight((height / 2) - 1);
        messageLog.setVisible(true);
        //setup the display for the message text
        messageDisplay.setX(0);
        messageDisplay.setY(0);
        messageDisplay.setWidth(width - 1);
        messageDisplay.setHeight((height / 2) - 1);
        messageDisplay.setVisible(true);
        //setup the list of replies
        replyList.setX(width/2);
        replyList.setY(height / 2);
        replyList.setWidth((int) (width/2) - 1);
        replyList.setHeight((height / 2) - 1);
        replyList.setVisible(true);
        //pack
        addComponent(messageLog);
        addComponent(replyList);
        addComponent(messageDisplay);
    }
    
    public void update(Ship ship) {
        setShip(ship);
        if (ship != null) {
            messageLog.clearList();
            //get ship's message que
            ArrayList<AstralMessage> que = ship.getMessages();
            //add each one
            for (int a = 0; a < que.size(); a++) {
                messageLog.addToList(que.get(a));
            }
        }
    }
    
    public Ship getShip() {
        return ship;
    }
    
    public void setShip(Ship ship) {
        this.ship = ship;
    }
    
    private void fillMessageLines(String message) {
        /*
         * Fills in the item's description being aware of things like line breaking on spaces.
         */
        if (message != null) {
            messageDisplay.addToList(" ");
            messageDisplay.addToList("--Message--");
            messageDisplay.addToList(" ");
            //
            String description = message.toString();
            int lineWidth = (((messageDisplay.getWidth() - 10) / (messageDisplay.getFont().getSize())));
            int cursor = 0;
            String tmp = "";
            String[] words = description.split(" ");
            for (int a = 0; a < words.length; a++) {
                if (a < 0) {
                    a = 0;
                }
                int len = words[a].length();
                if (cursor < lineWidth && !words[a].matches("/br/")) {
                    if (cursor + len <= lineWidth) {
                        tmp += " " + words[a];
                        cursor += len;
                    } else {
                        if (lineWidth > len) {
                            messageDisplay.addToList(tmp);
                            tmp = "";
                            cursor = 0;
                            a--;
                        } else {
                            tmp += "[LEN!]";
                        }
                    }
                } else {
                    messageDisplay.addToList(tmp);
                    tmp = "";
                    cursor = 0;
                    if (!words[a].matches("/br/")) {
                        a--;
                    }
                }
            }
            messageDisplay.addToList(tmp.toString());
        }
    }
    
    @Override
    public void handleMouseClickedEvent(MouseEvent me) {
        super.handleMouseClickedEvent(me);
        if (messageLog.isFocused()) {
            //clear
            messageDisplay.clearList();
            replyList.clearList();
            //get the faction
            int index = messageLog.getIndex();
            AstralMessage selected = (AstralMessage) messageLog.getItemAtIndex(index);
            //push message
            fillMessageLines(selected.getMessage());
            //push options
            if (selected.getChoices().size() > 0 && !selected.isRepliedTo()) {
                for (int a = 0; a < selected.getChoices().size(); a++) {
                    replyList.addToList(selected.getChoices().get(a));
                }
            } else {
                selected.setRepliedTo(true);
            }
        }
        if (messageDisplay.isFocused()) {
            //todo: handle sending reply to sender and archiving message
        }
    }
}